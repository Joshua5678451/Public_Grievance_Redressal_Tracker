package com.example.grievance.controller;

import com.example.grievance.model.User;
import com.example.grievance.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository repository;

    @PostConstruct
    public void createAdmin() {
        try {
            if (!repository.existsByUsername("admin@12")) {
                User admin = new User();
                admin.setUsername("admin12");
                admin.setPassword("admin@123");
                admin.setRole("ADMIN");
                admin.setSecurityQuestion("System Default");
                admin.setSecurityAnswer("System Default");
                repository.save(admin);
            }
        } catch (Exception e) {
            // Catches any database constraint conflicts cleanly on startup
            System.out.println("Admin account initialization check complete.");
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody User user) {
        // 1. Check if username exists
        if (repository.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body("Username already exists! Please choose a different one.");
        }

        user.setRole("CITIZEN");
        repository.save(user);
        return ResponseEntity.ok("Signup successful!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User user) {
        User existingUser = repository.findByUsername(user.getUsername());
        if (existingUser != null && existingUser.getPassword().equals(user.getPassword())) {
            return ResponseEntity.ok(existingUser); // Success
        }
        return ResponseEntity.badRequest().body("Invalid username or password.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody User requestUser) {
        User existingUser = repository.findByUsername(requestUser.getUsername());

        if (existingUser != null && existingUser.getSecurityAnswer().equalsIgnoreCase(requestUser.getSecurityAnswer())) {
            // Check new password rules on backend too just to be safe
            String np = requestUser.getPassword();
            if (!np.matches(".*[A-Z].*") || !np.matches(".*\\d.*") || !np.matches(".*[!@#$%^&*(),.?\":{}|<>].*")) {
                return ResponseEntity.badRequest().body("New password must contain a Capital letter, a number, and a symbol.");
            }

            existingUser.setPassword(np);
            repository.save(existingUser);
            return ResponseEntity.ok("Password reset successful!");
        }
        return ResponseEntity.badRequest().body("Incorrect username or security answer!");
    }
}