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

    @GetMapping("/checkout")
    public String checkout(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("total", cartService.getTotal());
        model.addAttribute("subtotal", cartService.getSubtotal());
        model.addAttribute("discountAmount", cartService.getDiscountAmount());
        model.addAttribute("deliveryFee", cartService.getDeliveryFee());
        model.addAttribute("appliedPromo", cartService.getAppliedPromoCode());
        model.addAttribute("razorpayKey", razorpayKey);
        return "checkout";
    }

    @PostMapping("/create_order")
    @ResponseBody
    public String createOrder(@RequestBody Map<String, Object> data, Authentication authentication) throws RazorpayException {
        double amount = Double.parseDouble(data.get("amount").toString());
        
        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int)(amount * 100)); // amount in paise
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "txn_123456");

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

        return order.toString();
    }

    @PostMapping("/update_order")
    @ResponseBody
    public String updateOrder(@RequestBody Map<String, Object> data) {
        String orderId = data.get("order_id").toString();
        String paymentId = data.get("payment_id").toString();
        String status = data.get("status").toString();

        in.ds.ShopEase.model.Order order = orderRepository.findByOrderId(orderId);
        order.setPaymentId(paymentId);
        order.setStatus(status);

        if ("paid".equals(status)) {
            // Populate items from cart
            List<OrderItem> orderItems = new ArrayList<>();
            for (CartItem item : cartService.getItems()) {
                OrderItem orderItem = new OrderItem();
                orderItem.setProductName(item.getProduct().getName());
                orderItem.setQuantity(item.getQuantity());
                orderItem.setPrice(item.getProduct().getPrice());
                orderItem.setProduct(item.getProduct());
                orderItems.add(orderItem);
            }
            order.setOrderItems(orderItems);
            orderRepository.save(order);

            // Send Email
            try {
                emailService.sendOrderConfirmationEmail(order);
            } catch (Exception e) {
                e.printStackTrace();
            }

            cartService.clear();
        }

        return "{\"status\":\"updated\"}";
    }
}
