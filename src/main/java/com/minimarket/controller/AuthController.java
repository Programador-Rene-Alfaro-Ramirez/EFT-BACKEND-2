package com.minimarket.controller;

import com.minimarket.security.model.LoginRequest;
import com.minimarket.security.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

@Autowired
private AuthenticationManager authenticationManager;

@Autowired
private JwtUtil jwtUtil;

@PostMapping("/login")
public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        
        // 1. Validamos las credenciales contra la base de datos
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 2. Extraemos el rol del usuario (ej: ROLE_CAJERO -> CAJERO)
        String rol = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst()
                .orElse("CLIENTE")
                .replace("ROLE_", "");

        // 3. Generamos el Token JWT usando tu clase utilitaria
        String jwt = jwtUtil.generateToken(authentication.getName(), rol);

        // 4. Devolvemos la respuesta en formato JSON limpio
        Map<String, String> response = new HashMap<>();
        response.put("token", jwt);
        response.put("username", authentication.getName());
        response.put("rol", rol);

        return ResponseEntity.ok(response);
}
}