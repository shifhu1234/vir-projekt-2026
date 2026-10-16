package hu.vir.imagebrowser.controller;

import hu.vir.imagebrowser.model.User;
import hu.vir.imagebrowser.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public class GalleryController {

    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif");
    private final UserRepository userRepository;
    private final Path imageDirectory;

    public GalleryController(UserRepository userRepository,
                              @Value("${app.image.directory}") String imageDirectory) {
        this.userRepository = userRepository;
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/gallery")
    public String gallery(Authentication authentication, Model model) throws IOException {
        User user = currentUser(authentication);
        List<String> images;
        try (Stream<Path> files = Files.list(imageDirectory)) {
            images = files.filter(Files::isRegularFile)
                    .filter(path -> canAccess(user, extension(path.getFileName().toString())))
                    .map(path -> path.getFileName().toString())
                    .sorted(Comparator.naturalOrder())
                    .toList();
        }
        model.addAttribute("user", user);
        model.addAttribute("images", images);
        return "gallery";
    }

    @GetMapping("/images/{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> image(@PathVariable String filename, Authentication authentication) {
        User user = currentUser(authentication);
        Path requested = imageDirectory.resolve(filename).normalize();
        if (!requested.startsWith(imageDirectory) || !Files.isRegularFile(requested)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "A kép nem található");
        }
        String extension = extension(filename);
        if (!SUPPORTED_EXTENSIONS.contains(extension) || !canAccess(user, extension)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nincs jogosultság ehhez a képformátumhoz");
        }
        MediaType mediaType = switch (extension) {
            case "jpg", "jpeg" -> MediaType.IMAGE_JPEG;
            case "png" -> MediaType.IMAGE_PNG;
            case "gif" -> MediaType.IMAGE_GIF;
            default -> throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        };
        return ResponseEntity.ok().contentType(mediaType).body(new FileSystemResource(requested));
    }

    private User currentUser(Authentication authentication) {
        return userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "A felhasználó nem található"));
    }

    private boolean canAccess(User user, String extension) {
        return "ROLE_ADMIN".equals(user.getRole()) || user.getAllowedExtensions().contains(extension);
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1).toLowerCase(Locale.ROOT) : "";
    }
}
