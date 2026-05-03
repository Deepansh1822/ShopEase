package in.ds.ShopEase.controller;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import in.ds.ShopEase.model.CartItem;
import in.ds.ShopEase.model.OrderItem;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.repository.OrderRepository;
import in.ds.ShopEase.repository.UserRepository;
import in.ds.ShopEase.service.CartService;
import in.ds.ShopEase.service.EmailService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Value("${razorpay.key.id}")
    private String razorpayKey;

    @Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private in.ds.ShopEase.repository.ProductRepository productRepository;

    @Autowired
    private in.ds.ShopEase.repository.AddressRepository addressRepository;

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        System.out.println("Checkout hit by: " + (authentication != null ? authentication.getName() : "null"));
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        User user = userRepository.findByEmail(authentication.getName());
        System.out.println("User found: " + (user != null ? user.getEmail() : "null"));
        if (user != null) {
            cartService.setMember(user.isMember());
        }
        model.addAttribute("isMember", cartService.isMember());
        model.addAttribute("cart", cartService.getItems());
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("membershipDiscount", cartService.getMembershipDiscount());
        model.addAttribute("promoDiscountAmount", cartService.getPromoDiscountAmount());
        model.addAttribute("discountAmount", cartService.getDiscountAmount());
        model.addAttribute("deliveryFee", cartService.getDeliveryFee());
        model.addAttribute("appliedPromo", cartService.getAppliedPromoCode());
        model.addAttribute("razorpayKey", razorpayKey);
        model.addAttribute("savedAddresses", user != null ? addressRepository.findByUser(user) : new java.util.ArrayList<>());
        return "checkout";
    }

    @PostMapping("/create_order")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String createOrder(@RequestBody Map<String, Object> data, Authentication authentication) throws RazorpayException {
        if (authentication == null) {
            return "{\"status\":\"error\", \"message\":\"Not authenticated\"}";
        }
        
        double amount = Double.parseDouble(data.get("amount").toString());
        
        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int)(amount * 100)); // amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_" + System.currentTimeMillis());

        Order order = client.orders.create(orderRequest);

        // Save initial order details
        in.ds.ShopEase.model.Order myOrder = new in.ds.ShopEase.model.Order();
        myOrder.setOrderId(order.get("id"));
        myOrder.setAmount(amount);
        myOrder.setStatus("created");
        myOrder.setCreatedAt(LocalDateTime.now());
        
        User user = userRepository.findByEmail(authentication.getName());
        myOrder.setUser(user);

        orderRepository.save(myOrder);
        System.out.println("Order created in DB: " + myOrder.getOrderId());

        return order.toString();
    }

    @PostMapping("/update_order")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String updateOrder(@RequestBody Map<String, Object> data) {
        String orderId = data.get("order_id") != null ? data.get("order_id").toString() : null;
        String paymentId = data.get("payment_id") != null ? data.get("payment_id").toString() : "";
        String status = data.get("status") != null ? data.get("status").toString() : "failed";

        System.out.println("Updating order: " + orderId + " with status: " + status);

        in.ds.ShopEase.model.Order order = orderRepository.findByOrderId(orderId);
        if (order == null) {
            System.err.println("CRITICAL ERROR: Order not found for ID: " + orderId);
            return "{\"status\":\"error\", \"message\":\"Order not found\"}";
        }

        order.setPaymentId(paymentId);
        order.setStatus(status);

        if (data.containsKey("shipping_details")) {
            Map<String, String> shipDetails = (Map<String, String>) data.get("shipping_details");
            order.setShippingName(shipDetails.get("name"));
            order.setShippingPhone(shipDetails.get("phone"));
            order.setShippingAddress(shipDetails.get("address"));
            order.setShippingCity(shipDetails.get("city"));
            order.setShippingPin(shipDetails.get("pin"));
        }

        if ("paid".equals(status)) {
            // Populate items from cart
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : cartService.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProductName(item.getProduct().getName());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getProduct().getPrice());
                orderItem.setProduct(item.getProduct());
                orderItem.setOrder(order);
                orderItems.add(orderItem);
            }
            order.setOrderItems(orderItems);
            orderRepository.save(order);

            // Decrease stock
            for (CartItem item : cartService.getItems()) {
                in.ds.ShopEase.model.Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (product != null) {
                    int newStock = product.getStockQuantity() - item.getQuantity();
                    product.setStockQuantity(Math.max(0, newStock));
                    productRepository.save(product);
                }
            }

            // Send Email
            try {
                emailService.sendOrderConfirmationEmail(order);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Handle address saving if requested
            if (data.containsKey("save_address") && (Boolean)data.get("save_address")) {
                Map<String, String> shipDetails = (Map<String, String>) data.get("shipping_details");
                User user = order.getUser();
                
                boolean exists = user.getAddresses().stream().anyMatch(a -> 
                    a.getStreetAddress().equalsIgnoreCase(shipDetails.get("address")) && 
                    a.getPinCode().equals(shipDetails.get("pin")));
                
                if (!exists) {
                    in.ds.ShopEase.model.Address newAddr = new in.ds.ShopEase.model.Address();
                    newAddr.setFullName(shipDetails.get("name"));
                    newAddr.setPhone(shipDetails.get("phone"));
                    newAddr.setStreetAddress(shipDetails.get("address"));
                    newAddr.setCity(shipDetails.get("city"));
                    newAddr.setPinCode(shipDetails.get("pin"));
                    newAddr.setUser(user);
                    addressRepository.save(newAddr);
                }
            }

            cartService.clear();
        }

        return "{\"status\":\"updated\"}";
    }

    @PostMapping("/admin/update_status")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String updateOrderStatus(@RequestBody Map<String, String> data) {
        String orderId = data.get("order_id");
        String status = data.get("status");

        in.ds.ShopEase.model.Order order = orderRepository.findByOrderId(orderId);
        if (order != null) {
            String oldStatus = order.getStatus();
            order.setStatus(status);
            
            // Set delivery timestamp
            if ("delivered".equals(status) && !"delivered".equals(oldStatus)) {
                order.setDeliveredAt(LocalDateTime.now());
            }

            // Handle return specific updates
            if ("returned".equals(status)) {
                order.setReturnStatus("approved");
            } else if ("return_rejected".equals(status)) {
                order.setReturnStatus("rejected");
                order.setStatus("delivered"); // Revert to delivered if return rejected
            }

            orderRepository.save(order);

            // If status changed to cancelled OR returned from a previous valid state, re-add stock
            if (("cancelled".equals(status) && !"cancelled".equals(oldStatus)) || 
                ("returned".equals(status) && !"returned".equals(oldStatus))) {
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProduct() != null) {
                        in.ds.ShopEase.model.Product product = productRepository.findById(item.getProduct().getId()).orElse(null);
                        if (product != null) {
                            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                            productRepository.save(product);
                        }
                    }
                }
            }
            return "{\"status\":\"success\"}";
        }
        return "{\"status\":\"error\"}";
    }

    @PostMapping("/order/cancel")
    @ResponseBody
    public String cancelOrder(@RequestBody Map<String, String> data, java.security.Principal principal) {
        String orderId = data.get("order_id");
        in.ds.ShopEase.model.Order order = orderRepository.findByOrderId(orderId);
        
        if (order != null && order.getUser().getEmail().equals(principal.getName())) {
            // Only allow cancellation if not already delivered or cancelled
            if (!"delivered".equals(order.getStatus()) && !"cancelled".equals(order.getStatus())) {
                order.setStatus("cancelled");
                orderRepository.save(order);

                // Re-add stock
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProduct() != null) {
                        in.ds.ShopEase.model.Product product = item.getProduct();
                        product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
                        productRepository.save(product);
                    }
                }
                return "{\"status\":\"success\"}";
            }
        }
        return "{\"status\":\"error\"}";
    }

    @PostMapping("/order/return")
    @ResponseBody
    @org.springframework.transaction.annotation.Transactional
    public String requestReturn(@RequestBody Map<String, String> data, java.security.Principal principal) {
        String orderId = data.get("order_id");
        String reason = data.get("reason");
        in.ds.ShopEase.model.Order order = orderRepository.findByOrderId(orderId);
        
        if (order != null && order.getUser().getEmail().equals(principal.getName())) {
            // Only allow return if delivered
            if ("delivered".equals(order.getStatus())) {
                // Check return window (7 days standard, 14 days for members)
                if (order.getDeliveredAt() != null) {
                    long days = java.time.Duration.between(order.getDeliveredAt(), LocalDateTime.now()).toDays();
                    int limit = (order.getUser().isMember()) ? 14 : 7;
                    if (days > limit) {
                        return "{\"status\":\"error\", \"message\":\"Return period of " + limit + " days has expired.\"}";
                    }
                }
                
                order.setStatus("return_pending");
                order.setReturnReason(reason);
                order.setReturnStatus("pending");
                orderRepository.save(order);
                return "{\"status\":\"success\", \"message\":\"Return request submitted.\"}";
            }
        }
        return "{\"status\":\"error\", \"message\":\"Return not allowed.\"}";
    }

    @GetMapping("/orderSuccess/{orderId}")
    public String orderSuccess(@PathVariable String orderId, Model model, Authentication authentication) {
        if (authentication == null) return "redirect:/login";
        
        in.ds.ShopEase.model.Order order = orderRepository.findByOrderId(orderId);
        if (order == null || !order.getUser().getEmail().equals(authentication.getName())) {
            return "redirect:/";
        }
        
        model.addAttribute("order", order);
        return "orderSuccess";
    }
}
