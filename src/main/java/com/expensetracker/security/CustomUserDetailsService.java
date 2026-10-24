package com.expensetracker.security;

import com.expensetracker.Entity.AppUser;
import com.expensetracker.repository.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;



@Service
public class CustomUserDetailsService  implements UserDetailsService {
    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException  {
        AppUser appUser = userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + username));

        return User.withUsername(
                appUser.getEmail()).
                password(appUser.getPasswordHash()).
                roles("USER").
                build();
    }
}
