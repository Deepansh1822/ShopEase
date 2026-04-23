package in.ds.ShopEase.controller;

import in.ds.ShopEase.model.Role;
import in.ds.ShopEase.model.User;
import in.ds.ShopEase.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/api/admin")
public class AdminApiController {

    // Change this secret key to something only you know
    private static final String ADMIN_SECRET_KEY = "shopease-secret-2024";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    /**
     * Create a new Admin user via Postman.
     *
     * POST http://localhost:8087/api/admin/create
     * Header: X-Admin-Secret: shopease-secret-2024
     * Body (JSON):
     * {
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "email": "john@shopease.com",
     *   "password": "Admin@123"
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<String> createAdmin(
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @RequestBody User user) {

        if (secret == null || !secret.equals(ADMIN_SECRET_KEY)) {
            return ResponseEntity.status(403).body("Forbidden: Invalid or missing Admin Secret Key.");
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return ResponseEntity.badRequest().body("Error: Email '" + user.getEmail() + "' already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Arrays.asList(new Role("ROLE_ADMIN")));
        userRepository.save(user);
        return ResponseEntity.ok("Admin '" + user.getEmail() + "' created successfully!");
    }

    /**
     * Promote an existing registered user to Admin via Postman.
     *
     * PUT http://localhost:8087/api/admin/promote/{email}
     * Header: X-Admin-Secret: shopease-secret-2024
     *
     * Example: PUT http://localhost:8087/api/admin/promote/user@example.com
     */
    @PutMapping("/promote/{email}")
    public ResponseEntity<String> promoteToAdmin(
            @RequestHeader(value = "X-Admin-Secret", required = false) String secret,
            @PathVariable String email) {

        if (secret == null || !secret.equals(ADMIN_SECRET_KEY)) {
            return ResponseEntity.status(403).body("Forbidden: Invalid or missing Admin Secret Key.");
        }
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        // Add ROLE_ADMIN while preserving existing roles
        boolean alreadyAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
        if (alreadyAdmin) {
            return ResponseEntity.ok("User '" + email + "' is already an Admin.");
        }
        user.getRoles().add(new Role("ROLE_ADMIN"));
        userRepository.save(user);
        return ResponseEntity.ok("User '" + email + "' has been promoted to Admin!");
    }
}

