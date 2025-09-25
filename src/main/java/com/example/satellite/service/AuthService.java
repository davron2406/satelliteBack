package com.example.satellite.service;

import com.example.satellite.entity.User;
import com.example.satellite.payload.ApiResponse;
import com.example.satellite.payload.ChangePasswordDTO;
import com.example.satellite.payload.RegisterDTO;
import com.example.satellite.repository.RoleRepository;
import com.example.satellite.repository.UserRepository;
import com.example.satellite.utils.AppConstants;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;
import java.util.regex.Pattern;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    JavaMailSender javaMailSender;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return userRepository.findByEmail(email).orElseThrow();
    }

    public ApiResponse registerUser(RegisterDTO registerDto) {
        if(userRepository.existsByEmail(registerDto.getEmail()))
            return new ApiResponse("Email already exist",false,null );
        User user = new User(
                registerDto.getFirstName(),
                registerDto.getLastName(),
                registerDto.getEmail(),
                registerDto.getPhoneNumber(),
                passwordEncoder.encode(registerDto.getPassword()),
                roleRepository.findByName(AppConstants.STUDENT),
                UUID.randomUUID().toString(),
                0,
                true
        );

        userRepository.save(user);
        sendEmail(user.getEmail(), user.getEmailCode());
        return new ApiResponse("User successfully registered", true,null);
    }

    @Transactional
    public void changePassword(User user, ChangePasswordDTO req) {
        // 1) Verify current password
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Current password is incorrect.");
        }

        // 2) Disallow same-as-old
        if (passwordEncoder.matches(req.getNewPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "New password must be different from the current password.");
        }

        // 3) Basic strength checks (customize as you like)
        validateStrength(req.getNewPassword());

        // 4) Update
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
    }

    private static final Pattern UPPER = Pattern.compile("[A-Z]");
    private static final Pattern LOWER = Pattern.compile("[a-z]");
    private static final Pattern DIGIT = Pattern.compile("\\d");

    private void validateStrength(String pwd) {
        if (pwd.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters.");
        }
        if (!UPPER.matcher(pwd).find() || !LOWER.matcher(pwd).find() || !DIGIT.matcher(pwd).find()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Password must contain uppercase, lowercase, and a digit.");
        }
    }


    public Boolean sendEmail(String sendingEmail, String emailCode){
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setFrom("davronsaydullayev2406@gmail.com");
            mailMessage.setTo(sendingEmail);
            mailMessage.setSubject("Validate your Email");
            mailMessage.setText("<a href='http://localhost:5173/verifyEmail?emailCode=" + emailCode + "&email=" + sendingEmail + "'>Tasdiqlang</a>");

            javaMailSender.send(mailMessage);
            return true;

        }catch (Exception e){
            return false;
        }
    }

    public ApiResponse verifyEmail(String email, String emailCode) {
        if(userRepository.existsByEmailAndEmailCode(email,emailCode)){
            User user = userRepository.findByEmail(email).get();
            user.setEmailCode(null);
            user.setEnabled(true);
            userRepository.save(user);
            return new ApiResponse("email successfully verified",true,null);
        }

        return new ApiResponse("User not Found",false,null);
    }
}
