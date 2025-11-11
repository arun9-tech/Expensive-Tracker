package com.example.expensetracker.controller;

import com.example.expensetracker.entity.User;
import com.example.expensetracker.payload.request.LoginRequest;
import com.example.expensetracker.payload.request.SignupRequest;
import com.example.expensetracker.payload.response.JwtResponse;
import com.example.expensetracker.payload.response.MessageResponse;
import com.example.expensetracker.repository.RoleRepository;
import com.example.expensetracker.repository.UserRepository;
import com.example.expensetracker.security.jwt.JwtUtils;
import com.example.expensetracker.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.expensetracker.entity.ERole;
import com.example.expensetracker.entity.Role;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtUtils jwtUtils;

    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        logger.debug("Attempting to authenticate user: {}", loginRequest.getUsername());
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);
        
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();        
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@Valid @RequestBody SignupRequest signUpRequest) {
        logger.debug("Attempting to register user: {}", signUpRequest.getUsername());
        if (userRepository.existsByUsername(signUpRequest.getUsername())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        // Create new user's account
        User user = new User(
                signUpRequest.getUsername(),
                signUpRequest.getEmail(),
                encoder.encode(signUpRequest.getPassword()));

        Set<String> strRoles = signUpRequest.getRole();
        Set<Role> roles = new HashSet<>();

        try {
            if (strRoles == null) {
                Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                        .orElseGet(() -> {
                            logger.warn("ROLE_USER not found, creating it now");
                            return roleRepository.save(new Role(ERole.ROLE_USER));
                        });
                roles.add(userRole);
            } else {
                for (String role : strRoles) {
                    switch (role.toLowerCase()) {
                        case "admin":
                            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_ADMIN)));
                            roles.add(adminRole);
                            break;
                        case "mod":
                        case "moderator":
                            Role modRole = roleRepository.findByName(ERole.ROLE_MODERATOR)
                                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_MODERATOR)));
                            roles.add(modRole);
                            break;
                        default:
                            Role userRole = roleRepository.findByName(ERole.ROLE_USER)
                                    .orElseGet(() -> roleRepository.save(new Role(ERole.ROLE_USER)));
                            roles.add(userRole);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error assigning roles to user", e);
            return ResponseEntity
                    .status(500)
                    .body(new MessageResponse("Error: Failed to assign roles to user. " + e.getMessage()));
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(new MessageResponse("User registered successfully!"));
    }
}
