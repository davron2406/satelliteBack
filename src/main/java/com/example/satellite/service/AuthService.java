package com.example.satellite.service;

import com.example.satellite.entity.User;
import com.example.satellite.payload.ApiResponse;
import com.example.satellite.payload.RegisterDTO;
import com.example.satellite.repository.RoleRepository;
import com.example.satellite.repository.UserRepository;
import com.example.satellite.utils.AppConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

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
                false
        );

        userRepository.save(user);
        sendEmail(user.getEmail(), user.getEmailCode());
        return new ApiResponse("User successfully registered", true,null);
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
