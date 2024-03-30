package com.tamnaju.dev.utilities;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

public class CustomAuthChecker {
    public void applyAuthentication(Authentication authentication,
            Model model) throws IOException {
        model.addAttribute("authentication", authentication);
        model.addAttribute("isAdmin", this.isAdmin(authentication));
    }

    public boolean isAdmin(Authentication authentication) {
        boolean isAdmin = authentication != null && authentication.getAuthorities().stream()
                .anyMatch(role -> role.toString().equals("ROLE_ADMIN"));
        return isAdmin;
    }
}
