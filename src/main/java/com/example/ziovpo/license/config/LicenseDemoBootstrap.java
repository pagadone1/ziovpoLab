package com.example.ziovpo.license.config;

import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.ziovpo.license.model.LicenseLicenseType;
import com.example.ziovpo.license.model.LicenseProduct;
import com.example.ziovpo.license.repository.LicenseProductRepository;
import com.example.ziovpo.license.repository.LicenseTypeRepository;
import com.example.ziovpo.model.Users;
import com.example.ziovpo.repository.UsersRepository;

@Component
@Profile("!test")
public class LicenseDemoBootstrap implements CommandLineRunner {

    private final UsersRepository usersRepository;
    private final LicenseProductRepository productRepository;
    private final LicenseTypeRepository licenseTypeRepository;
    private final PasswordEncoder passwordEncoder;

    public LicenseDemoBootstrap(
            UsersRepository usersRepository,
            LicenseProductRepository productRepository,
            LicenseTypeRepository licenseTypeRepository,
            PasswordEncoder passwordEncoder) {
        this.usersRepository = usersRepository;
        this.productRepository = productRepository;
        this.licenseTypeRepository = licenseTypeRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        String adminUser = env("ADMIN_USERNAME", "admin");
        String adminPass = env("ADMIN_PASSWORD", "Admin1234!");
        String adminEmail = env("ADMIN_EMAIL", "admin@carservice.local");

        ensureUser(DemoIds.ADMIN_ID, adminUser, adminPass, adminEmail, "ROLE_ADMIN");
        ensureUser(DemoIds.CLIENT_ID, "client", "Client1234!", "client@carservice.local", "ROLE_USER");
        ensureProduct();
        ensureLicenseTypes();
    }

    private void ensureUser(UUID id, String username, String password, String email, String role) {
        if (usersRepository.findByUsername(username).isPresent()) {
            return;
        }
        Users user = new Users();
        user.setId(id);
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        usersRepository.save(user);
        System.out.println("[bootstrap] user: " + username + " id=" + id);
    }

    private void ensureProduct() {
        if (productRepository.existsById(DemoIds.PRODUCT_ID)) {
            return;
        }
        LicenseProduct product = new LicenseProduct();
        product.setId(DemoIds.PRODUCT_ID);
        product.setName("Car Service Desktop");
        product.setBlocked(false);
        productRepository.save(product);
        System.out.println("[bootstrap] product: Car Service Desktop id=" + DemoIds.PRODUCT_ID);
    }

    private void ensureLicenseTypes() {
        if (!licenseTypeRepository.existsById(DemoIds.TYPE_STANDARD_ID)) {
            LicenseLicenseType standard = new LicenseLicenseType();
            standard.setId(DemoIds.TYPE_STANDARD_ID);
            standard.setName("STANDARD");
            standard.setDefaultDurationInDays(30);
            standard.setDescription("30 days");
            licenseTypeRepository.save(standard);
        }
        if (!licenseTypeRepository.existsById(DemoIds.TYPE_ANNUAL_ID)) {
            LicenseLicenseType annual = new LicenseLicenseType();
            annual.setId(DemoIds.TYPE_ANNUAL_ID);
            annual.setName("ANNUAL");
            annual.setDefaultDurationInDays(365);
            annual.setDescription("365 days");
            licenseTypeRepository.save(annual);
        }
        System.out.println("[bootstrap] license types: STANDARD, ANNUAL");
    }

    private static String env(String key, String defaultValue) {
        String value = System.getenv(key);
        return value != null && !value.isBlank() ? value : defaultValue;
    }
}
