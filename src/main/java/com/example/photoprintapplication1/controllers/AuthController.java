package com.example.photoprintapplication1.controllers;

import com.example.photoprintapplication1.config.JwtTokenProvider;
import com.example.photoprintapplication1.dto.AuthRequest;
import com.example.photoprintapplication1.dto.AuthResponse;
import com.example.photoprintapplication1.models.SessionStatus;
import com.example.photoprintapplication1.models.UserSession;
import com.example.photoprintapplication1.repository.UserSessionRepository;
import com.example.photoprintapplication1.service.AuthService;
import com.example.photoprintapplication1.service.CustomUserDetailsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthService authService;
    private final UserSessionRepository userSessionRepository;
    private final CustomUserDetailsService customUserDetailsService;

    public AuthController(
            AuthenticationManager authenticationManager,
            JwtTokenProvider jwtTokenProvider,
            AuthService authService,
            UserSessionRepository userSessionRepository,
            CustomUserDetailsService customUserDetailsService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authService = authService;
        this.userSessionRepository = userSessionRepository;
        this.customUserDetailsService = customUserDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String accessToken = jwtTokenProvider.createAccessToken(authentication);
        String refreshToken = jwtTokenProvider.createRefreshToken(authentication);

        UserSession session = new UserSession();
        session.setUser(authService.getUserByUsername(request.getUsername()));
        session.setRefreshToken(refreshToken);
        session.setExpiresAt(LocalDateTime.now().plusDays(7));
        session.setStatus(SessionStatus.ACTIVE);
        userSessionRepository.save(session);

        return ResponseEntity.ok(new AuthResponse(accessToken, refreshToken, "Login successful"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody Map<String, String> request) {
        String oldRefreshToken = request.get("refreshToken");

        if (oldRefreshToken == null || !jwtTokenProvider.validateToken(oldRefreshToken)) {
            return ResponseEntity.status(403).body(new AuthResponse(null, null, "Invalid refresh token"));
        }

        String username = jwtTokenProvider.getUsernameFromToken(oldRefreshToken);

        UserSession oldSession = userSessionRepository.findByRefreshToken(oldRefreshToken)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found"));

        if (oldSession.getStatus() != SessionStatus.ACTIVE) {
            return ResponseEntity.status(403).body(new AuthResponse(null, null, "Refresh token already used or revoked"));
        }

        oldSession.setStatus(SessionStatus.REVOKED);
        userSessionRepository.save(oldSession);

        UserDetails userDetails = customUserDetailsService.loadUserByUsername(username);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());

        String newAccessToken = jwtTokenProvider.createAccessToken(authentication);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(authentication);

        UserSession newSession = new UserSession();
        newSession.setUser(oldSession.getUser());
        newSession.setRefreshToken(newRefreshToken);
        newSession.setExpiresAt(LocalDateTime.now().plusDays(7));
        newSession.setStatus(SessionStatus.ACTIVE);
        userSessionRepository.save(newSession);

        return ResponseEntity.ok(new AuthResponse(newAccessToken, newRefreshToken, "Tokens refreshed"));
    }
}
