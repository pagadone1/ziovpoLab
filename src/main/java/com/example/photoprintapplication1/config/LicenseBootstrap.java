package com.example.photoprintapplication1.config;

import com.example.photoprintapplication1.models.LicenseType;
import com.example.photoprintapplication1.models.Product;
import com.example.photoprintapplication1.models.Role;
import com.example.photoprintapplication1.models.User;
import com.example.photoprintapplication1.repository.LicenseTypeRepository;
import com.example.photoprintapplication1.repository.ProductRepository;
import com.example.photoprintapplication1.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Стартовые данные для демо лабы 2.
 */
@Component
public class LicenseBootstrap implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.username:admin}")
    private String adminUsername;

    @Value("${app.bootstrap.admin.password:Admin1234!}")
    private String adminPassword;

    @Value("${app.bootstrap.admin.email:admin@carservice.local}")
    private String adminEmail;

    public LicenseBootstrap(
            UserRepository userRepository,
            ProductRepository productRepository,
            LicenseTypeRepository licenseTypeRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername(adminUsername).isEmpty()) {
            User admin = new User();
            admin.setUsername(adminUsername);
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode(adminPassword));
            admin.setRole(Role.ADMIN);
            admin.setEnabled(true);
            userRepository.save(admin);
            System.out.println("[bootstrap] admin: " + adminUsername);
        }

        if (productRepository.count() == 0) {
            Product product = new Product();
            product.setName("Car Service Desktop");
            productRepository.save(product);
            System.out.println("[bootstrap] product: Car Service Desktop (id=1)");
        }

        if (licenseTypeRepository.count() == 0) {
            LicenseType standard = new LicenseType();
            standard.setName("STANDARD");
            standard.setDefaultDurationInDays(30);
            standard.setDescription("Стандартная лицензия, 30 дней");

            LicenseType annual = new LicenseType();
            annual.setName("ANNUAL");
            annual.setDefaultDurationInDays(365);
            annual.setDescription("Годовая лицензия");

            licenseTypeRepository.save(standard);
            licenseTypeRepository.save(annual);
            System.out.println("[bootstrap] license types: STANDARD (30d), ANNUAL (365d)");
        }
    }
}
