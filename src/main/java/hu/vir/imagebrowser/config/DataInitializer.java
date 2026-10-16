package hu.vir.imagebrowser.config;

import hu.vir.imagebrowser.model.User;
import hu.vir.imagebrowser.repository.UserRepository;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Path imageDirectory;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder,
                           @Value("${app.image.directory}") String imageDirectory) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.imageDirectory = Path.of(imageDirectory).toAbsolutePath().normalize();
    }

    @Override
    public void run(String... args) throws IOException {
        Files.createDirectories(imageDirectory);
        boolean directoryIsEmpty;
        try (Stream<Path> files = Files.list(imageDirectory)) {
            directoryIsEmpty = files.findAny().isEmpty();
        }
        if (directoryIsEmpty) {
            createSampleImage("minta1.jpg", "jpg", new Color(31, 111, 235));
            createSampleImage("minta2.jpeg", "jpeg", new Color(20, 150, 110));
            createSampleImage("minta3.png", "png", new Color(235, 145, 35));
            createSampleImage("minta4.gif", "gif", new Color(190, 55, 130));
        }
        createUserIfMissing("admin", "admin123", "ROLE_ADMIN", Set.of("jpg", "jpeg", "png", "gif"));
        createUserIfMissing("user_jpg", "user123", "ROLE_USER", Set.of("jpg", "jpeg"));
        createUserIfMissing("user_png_gif", "user123", "ROLE_USER", Set.of("png", "gif"));
    }

    private void createUserIfMissing(String username, String password, String role, Set<String> extensions) {
        if (!userRepository.existsByUsername(username)) {
            userRepository.save(new User(username, passwordEncoder.encode(password), role, new HashSet<>(extensions)));
        }
    }

    private void createSampleImage(String filename, String format, Color color) throws IOException {
        BufferedImage image = new BufferedImage(640, 360, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = image.createGraphics();
        try {
            graphics.setColor(color);
            graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
            graphics.setColor(Color.WHITE);
            graphics.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 42));
            graphics.drawString(filename, 180, 190);
        } finally {
            graphics.dispose();
        }
        ImageIO.write(image, format, imageDirectory.resolve(filename).toFile());
    }
}
