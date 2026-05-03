package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.User;
import in.ds.ShopEase.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private in.ds.ShopEase.repository.PasswordResetTokenRepository tokenRepository;

    @Autowired
    private in.ds.ShopEase.service.EmailService emailService;

    @Autowired
    private org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registration")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "registration";
    }

    @PostMapping("/registration")
    public String registerUserAccount(@ModelAttribute("user") User user) {
        userService.save(user);
        return "redirect:/registration?success";
    }

    @GetMapping("/forgot_password")
    public String showForgotPasswordForm() {
        return "forgotPassword";
    }

    @PostMapping("/forgot_password")
    public String processForgotPassword(String email, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes, jakarta.servlet.http.HttpServletRequest request) {
        User user = userService.findByEmail(email);
        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMsg", "No account found with that email address.");
            return "redirect:/forgot_password";
        }

        // Generate Token
        String token = java.util.UUID.randomUUID().toString();
        in.ds.ShopEase.model.PasswordResetToken resetToken = new in.ds.ShopEase.model.PasswordResetToken();
        resetToken.setToken(token);
        resetToken.setUser(user);
        resetToken.setExpiryDate(java.time.LocalDateTime.now().plusHours(24));
        
        // Clear old tokens for this user
        tokenRepository.deleteByUser(user);
        tokenRepository.save(resetToken);

        // Send Email
        String appUrl = request.getRequestURL().toString().replace(request.getServletPath(), "");
        String resetLink = appUrl + "/reset_password?token=" + token;
        
        try {
            emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
            redirectAttributes.addFlashAttribute("successMsg", "Password reset link sent to your email!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMsg", "Error sending email. Please try again later.");
        }

        return "redirect:/login";
    }

    @GetMapping("/reset_password")
    public String showResetPasswordForm(@org.springframework.web.bind.annotation.RequestParam("token") String token, Model model, org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        java.util.Optional<in.ds.ShopEase.model.PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        
        if (resetToken.isEmpty() || resetToken.get().getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid or expired reset token.");
            return "redirect:/login";
        }
        
        model.addAttribute("token", token);
        return "resetPassword";
    }

    @PostMapping("/reset_password")
    public String processResetPassword(@org.springframework.web.bind.annotation.RequestParam("token") String token, 
                                      @org.springframework.web.bind.annotation.RequestParam("password") String password,
                                      org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        java.util.Optional<in.ds.ShopEase.model.PasswordResetToken> resetToken = tokenRepository.findByToken(token);
        
        if (resetToken.isEmpty() || resetToken.get().getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            redirectAttributes.addFlashAttribute("errorMsg", "Invalid or expired reset token.");
            return "redirect:/login";
        }

        User user = resetToken.get().getUser();
        userService.updatePassword(user, password);
        tokenRepository.delete(resetToken.get());

        redirectAttributes.addFlashAttribute("successMsg", "Password reset successful! You can now log in.");
        return "redirect:/login";
    }
}
