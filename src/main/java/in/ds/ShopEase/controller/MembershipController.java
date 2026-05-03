package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.User;
import in.ds.ShopEase.repository.UserRepository;
import in.ds.ShopEase.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class MembershipController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.id}")
    private String razorpayKey;

    @org.springframework.beans.factory.annotation.Value("${razorpay.key.secret}")
    private String razorpaySecret;

    @GetMapping("/membership")
    public String membershipPage(Model model, Authentication authentication) {
        if (authentication != null) {
            User user = userRepository.findByEmail(authentication.getName());
            model.addAttribute("isMember", user.isMember());
            model.addAttribute("user", user);
            model.addAttribute("expiryDate", user.getMembershipExpiryDate());
        } else {
            model.addAttribute("isMember", false);
        }
        model.addAttribute("razorpayKey", razorpayKey);
        return "membership";
    }

    @PostMapping("/membership/create_order")
    @org.springframework.web.bind.annotation.ResponseBody
    public String createOrder(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> data) throws com.razorpay.RazorpayException {
        int amount = Integer.parseInt(data.get("amount").toString());
        com.razorpay.RazorpayClient client = new com.razorpay.RazorpayClient(razorpayKey, razorpaySecret);
        org.json.JSONObject orderRequest = new org.json.JSONObject();
        orderRequest.put("amount", amount * 100);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "mem_txn_" + System.currentTimeMillis());
        com.razorpay.Order order = client.orders.create(orderRequest);
        return order.toString();
    }

    @PostMapping("/membership/update_status")
    @org.springframework.web.bind.annotation.ResponseBody
    public String updateStatus(@org.springframework.web.bind.annotation.RequestBody java.util.Map<String, Object> data, Authentication authentication) {
        String plan = data.get("plan").toString();
        User user = userRepository.findByEmail(authentication.getName());
        if (user != null) {
            user.setMember(true);
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            if (user.getMembershipExpiryDate() != null && now.isBefore(user.getMembershipExpiryDate())) {
                now = user.getMembershipExpiryDate(); // Stack on top of current membership
            }
            if ("month".equals(plan)) {
                user.setMembershipExpiryDate(now.plusMonths(1));
                user.setMembershipPlan("Monthly");
            } else if ("quarter".equals(plan)) {
                user.setMembershipExpiryDate(now.plusMonths(3));
                user.setMembershipPlan("Quarterly");
            } else if ("year".equals(plan)) {
                user.setMembershipExpiryDate(now.plusYears(1));
                user.setMembershipPlan("Yearly");
            }
            userRepository.save(user);
            try {
                emailService.sendMembershipConfirmationEmail(user, user.getMembershipPlan(), user.getMembershipExpiryDate());
            } catch (Exception e) {
                System.err.println("Failed to send membership email: " + e.getMessage());
            }
        }
        return "{\"status\":\"success\"}";
    }
}
