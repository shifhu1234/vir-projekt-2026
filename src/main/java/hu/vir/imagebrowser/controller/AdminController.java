package hu.vir.imagebrowser.controller;

import hu.vir.imagebrowser.model.User;
import hu.vir.imagebrowser.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Controller
public class AdminController {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/admin/users")
    public String users(Model model) {
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("extensions", List.of("jpg", "jpeg", "png", "gif"));
        return "admin";
    }

    @PostMapping("/admin/users/{id}/permissions")
    public String updatePermissions(@PathVariable Long id,
                                    @RequestParam(required = false) Set<String> extensions) {
        User user = findUser(id);
        Set<String> normalized = normalizeExtensions(extensions);
        user.setAllowedExtensions(normalized);
        userRepository.save(user);
        return "redirect:/admin/users?success=permissions";
    }

    @PostMapping("/admin/users/create")
    public String createUser(@RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role,
                             @RequestParam(required = false) Set<String> extensions) {
        if (username.isBlank() || password.isBlank() || userRepository.existsByUsername(username)) {
            return "redirect:/admin/users?error=invalid";
        }
        String normalizedRole = "ROLE_ADMIN".equals(role) ? "ROLE_ADMIN" : "ROLE_USER";
        User user = new User(username.trim(), passwordEncoder.encode(password), normalizedRole,
                normalizeExtensions(extensions));
        userRepository.save(user);
        return "redirect:/admin/users?success=created";
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "A felhasználó nem található"));
    }

    private Set<String> normalizeExtensions(Set<String> extensions) {
        if (extensions == null) {
            return new HashSet<>();
        }
        Set<String> normalized = new HashSet<>();
        for (String extension : extensions) {
            String value = extension.toLowerCase(Locale.ROOT).replace(".", "");
            if (SUPPORTED_EXTENSIONS.contains(value)) {
                normalized.add(value);
            }
        }
        return normalized;
    }
}
