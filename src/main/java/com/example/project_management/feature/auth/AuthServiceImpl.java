package com.example.project_management.feature.auth;

import com.example.project_management.config.JwtProperties;
import com.example.project_management.exception.InvalidRequestException;
import com.example.project_management.feature.auth.dto.LoginRequest;
import com.example.project_management.feature.auth.dto.RegisterRequest;
import com.example.project_management.feature.auth.dto.RefreshTokenRequest;
import com.example.project_management.feature.auth.dto.TokenResponse;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProperties jwtProperties;

    public AuthServiceImpl(AuthenticationManager authenticationManager, JwtEncoder jwtEncoder,
                           JwtDecoder jwtDecoder, UserRepository userRepository,
                           PasswordEncoder passwordEncoder, JwtProperties jwtProperties) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );

        User user = userRepository.findByEmail(authentication.getName())
                .orElseGet(() -> userRepository.findByUsername(authentication.getName()).orElseThrow());

        String accessToken = generateToken(user, authentication, jwtProperties.accessTokenExpiration());
        String refreshToken = generateToken(user, authentication, jwtProperties.refreshTokenExpiration());

        return new TokenResponse(accessToken, refreshToken, "Bearer");
    }

    @Override
    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new com.example.project_management.exception.ConflictException("Username is already taken!");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new com.example.project_management.exception.ConflictException("Email is already taken!");
        }

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Override
    public TokenResponse refreshToken(RefreshTokenRequest request) {
        try {
            Jwt jwt = jwtDecoder.decode(request.refreshToken());
            String email = jwt.getSubject();
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new com.example.project_management.exception.ResourceNotFoundException("User", "email", email));

            List<GrantedAuthority> authorities = new ArrayList<>();
            if (user.getRole() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));
                if (user.getRole().getPermissions() != null) {
                    user.getRole().getPermissions().forEach(permission ->
                            authorities.add(new SimpleGrantedAuthority(permission.name())));
                }
            }

            Authentication authentication = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);

            String newAccessToken = generateToken(user, authentication, jwtProperties.accessTokenExpiration());
            String newRefreshToken = generateToken(user, authentication, jwtProperties.refreshTokenExpiration());

            return new TokenResponse(newAccessToken, newRefreshToken, "Bearer");
        } catch (JwtException e) {
            throw new InvalidRequestException("Refresh token is invalid or expired");
        }
    }

    private String generateToken(User user, Authentication authentication, long expirationTime) {
        Instant now = Instant.now();

        // Lấy authorities từ authentication (đã bao gồm ROLE_xxx và permissions)
        List<String> scopes = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("project-management")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expirationTime))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("scope", String.join(" ", scopes))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(MacAlgorithm.HS512).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
