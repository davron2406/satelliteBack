package com.example.satellite.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

    private static final long expiredDate = 100 *  60 * 60 * 24;
    private static final String secretKey = "Davron2406";

    public String generateToken(String email){
       return Jwts
                .builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredDate))
               .signWith(SignatureAlgorithm.HS512,secretKey)
                .compact();
    }

    public String getEmailFromToken(String token){
        try{
             return Jwts
                    .parser().setSigningKey(secretKey)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();

        }catch (Exception e){
            return null;
        }
    }
}
