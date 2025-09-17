package com.example.satellite.service;

import com.example.satellite.entity.User;
import com.example.satellite.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepo;
    public UserService(UserRepository userRepo){ this.userRepo = userRepo; }

    public User requireUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new IllegalStateException("No authentication");
        Object p = auth.getPrincipal();
        String username = null;
        if (p instanceof User u) return u;
        if (p instanceof UserDetails ud) username = ud.getUsername();
        if (username == null) username = auth.getName();
        return userRepo.findByEmail(username).orElseThrow(() -> new IllegalStateException("User not found for principal"));
    }
}
