package com.BeeOranized.BeeOranized.Controller;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


import com.BeeOranized.BeeOranized.Dtos.JwtResponseDto;
import com.BeeOranized.BeeOranized.Dtos.LoginRequestDto;
import com.BeeOranized.BeeOranized.Dtos.MessageResponseDto;
import com.BeeOranized.BeeOranized.Dtos.SignupRequestDto;
import com.BeeOranized.BeeOranized.Entity.*;
import com.BeeOranized.BeeOranized.Repository.*;
import com.BeeOranized.BeeOranized.Securit.service.UserDetailsImpl;
import com.BeeOranized.BeeOranized.Security.jwt.JwtUtils;
import com.sun.xml.messaging.saaj.packaging.mime.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class AuthController {
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

    @Autowired
    MembreRepository membreRepository;

    @Autowired
    ChefScrumRepository chefScrumRepository;

    @Autowired
    AdminRepository adminRepository;


    @PostMapping("/signin")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequestDto loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUserEmail(), loginRequest.getUserPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toList());

        return ResponseEntity.ok(new JwtResponseDto(jwt,
                roles,
                userDetails.getId()));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> registerUser(@RequestBody SignupRequestDto signUpRequest) throws MessagingException {
        if (userRepository.existsByUserEmail(signUpRequest.getUserEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new JwtResponseDto("Error: Username is already taken!"));
        }

        Set<Role> roles = new HashSet<>();

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();

        if ("Membre_ROLE".equals(signUpRequest.getUserRole())) {
            Role userRole = roleRepository.findByName(ERole.Membre_ROLE)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            Membre membre = new Membre(signUpRequest.getUserEmail(), encoder.encode(signUpRequest.getUserPassword()), signUpRequest.getUserCity(), roles);
            membreRepository.save(membre);

        } else if ("ChefScrum_ROLE".equals(signUpRequest.getUserRole())) {
            Role userRole = roleRepository.findByName(ERole.ChefScrum_ROLE)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            ChefScrum chefScrum = new ChefScrum(signUpRequest.getUserEmail(), encoder.encode(signUpRequest.getUserPassword()), signUpRequest.getUserCity(), roles);
            chefScrumRepository.save(chefScrum);
        } else if ("ADMIN_ROLE".equals(signUpRequest.getUserRole())) {
            Role userRole = roleRepository.findByName(ERole.ADMIN_ROLE)
                    .orElseThrow(() -> new RuntimeException("Error: Role is not found."));
            roles.add(userRole);
            Admin admin = new Admin(signUpRequest.getUserEmail(), encoder.encode(signUpRequest.getUserPassword()), signUpRequest.getUserCity(), roles);
            adminRepository.save(admin);
        } else {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponseDto("Error: Invalid role!"));
        }

        return ResponseEntity.ok(new MessageResponseDto("User registered successfully!"));
    }
}