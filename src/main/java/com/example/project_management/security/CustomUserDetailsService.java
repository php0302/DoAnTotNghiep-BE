package com.example.project_management.security;

import com.example.project_management.feature.role.Permission;
import com.example.project_management.feature.user.User;
import com.example.project_management.feature.user.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(usernameOrEmail)
                .orElseGet(() -> userRepository.findByUsername(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                "User not found with email or username: " + usernameOrEmail)));

        if (!user.isActive()) {
            throw new UsernameNotFoundException("User is inactive");
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Thêm ROLE_ prefix để dùng với hasRole() / hasAnyRole()
        if (user.getRole() != null) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()));

            // Thêm từng Permission riêng lẻ để dùng với hasAuthority()
            for (Permission permission : user.getRole().getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.name()));
            }
        }

        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
