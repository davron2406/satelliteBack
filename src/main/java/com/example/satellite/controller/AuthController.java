package com.example.satellite.controller;

import com.example.satellite.entity.User;
import com.example.satellite.payload.ApiResponse;
import com.example.satellite.payload.ChangePasswordDTO;
import com.example.satellite.payload.LoginDTO;
import com.example.satellite.payload.RegisterDTO;
import com.example.satellite.security.JwtProvider;
import com.example.satellite.service.AuthService;
import com.example.satellite.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/api/auth")

public class AuthController {

    @Autowired
    AuthService authService;

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtProvider jwtProvider;

    @Autowired
    UserService userService;


    @PostMapping("/login")
    public HttpEntity<?> login(@RequestBody LoginDTO loginDto){
        try{
                Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword()));
             User user = (User) authenticate.getPrincipal();
            String token = jwtProvider.generateToken(user.getEmail());
            System.out.println(token);
            return ResponseEntity.ok(token);
        }catch (Exception e){
            return ResponseEntity.ok(new ApiResponse("Password or email is incorrect",false,null));
        }
    }

    @PostMapping("/register")
    public HttpEntity<?> registerUser(@RequestBody RegisterDTO registerDto){
        ApiResponse apiResponse = authService.registerUser(registerDto);
        return ResponseEntity.status(200).body(apiResponse);
    }

    @PostMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> changePassword(
                                            @Valid @RequestBody ChangePasswordDTO body) {
        // Resolve your domain User from principal

        User user = userService.requireUser(); // adjust to your setup (see principal class below)
        authService.changePassword(user, body);
        return ResponseEntity.ok().body(new Message("Password changed successfully."));
    }

    // simple response
    record Message(String message) {}


    @PostMapping("/verifyEmail")
    public  HttpEntity<?> verifyEmail(@RequestParam String emailCode, @RequestParam String email){
        ApiResponse apiResponse = authService.verifyEmail(email, emailCode);
        return ResponseEntity.status(apiResponse.isSuccess()?200:409).body(apiResponse);
    }
    
    @GetMapping("/me")
    public HttpEntity<?> authme(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User principal = (User)auth.getPrincipal();
        return ResponseEntity.ok(principal);
    }
}
