package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.Address;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.repository.AddressRepository;
import in.ds.ShopEase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private in.ds.ShopEase.repository.PasswordResetTokenRepository tokenRepository;

    @Autowired
    private in.ds.ShopEase.service.EmailService emailService;

    @GetMapping("/edit")
    public String editProfilePage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);
        return "editProfile";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute User userDetails, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName());
        user.setFirstName(userDetails.getFirstName());
        user.setLastName(userDetails.getLastName());
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully!");
        return "redirect:/profile";
    }

    @GetMapping("/security")
    public String securityPage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        model.addAttribute("user", user);
        return "security";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("currentPassword") String currentPassword,
                                 @RequestParam("newPassword") String newPassword,
                                 Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName());
        if (passwordEncoder.matches(currentPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Password changed successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Current password does not match!");
        }
        return "redirect:/profile/security";
    }

    @GetMapping("/security/forgot-trigger")
    public String forgotPasswordTrigger(Principal principal, jakarta.servlet.http.HttpServletRequest request, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
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
            redirectAttributes.addFlashAttribute("success", "A password reset link has been sent to your email (" + user.getEmail() + ")!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error sending email. Please try again later.");
        }

        return "redirect:/profile/security";
    }

    @GetMapping("/addresses")
    public String myAddressesPage(Model model, Principal principal) {
        User user = userRepository.findByEmail(principal.getName());
        List<Address> addresses = addressRepository.findByUser(user);
        model.addAttribute("addresses", addresses);
        return "myAddresses";
    }

    @PostMapping("/addresses/add")
    public String addAddress(@ModelAttribute Address address, Principal principal, RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(principal.getName());
        address.setUser(user);
        
        // If this is the first address, make it default
        if (addressRepository.findByUser(user).isEmpty()) {
            address.setDefault(true);
        }
        
        addressRepository.save(address);
        redirectAttributes.addFlashAttribute("success", "Address added successfully!");
        return "redirect:/profile/addresses";
    }

    @PostMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Address address = addressRepository.findById(id).orElse(null);
        if (address != null && address.getUser().getEmail().equals(principal.getName())) {
            addressRepository.delete(address);
            redirectAttributes.addFlashAttribute("success", "Address deleted successfully!");
        }
        return "redirect:/profile/addresses";
    }
}
