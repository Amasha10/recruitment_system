package lk.sliit.recruitment.config;

import lk.sliit.recruitment.model.Role;
import lk.sliit.recruitment.model.User;
import lk.sliit.recruitment.repository.UserRepository;
import lk.sliit.recruitment.service.SystemSettingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the default admin, a few demo users and the default system settings on first run. */
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final SystemSettingService settings;

    public DataSeeder(UserRepository users, PasswordEncoder encoder, SystemSettingService settings) {
        this.users = users;
        this.encoder = encoder;
        this.settings = settings;
    }

    @Override
    public void run(String... args) {
        settings.createIfMissing(SystemSettingService.COMPANY_NAME, "TalentBridge Recruitment",
                "Company name shown across the system");
        settings.createIfMissing(SystemSettingService.SUPPORT_EMAIL, "support@talentbridge.lk",
                "Contact email shown to users");
        settings.createIfMissing(SystemSettingService.PASSWORD_MIN_LENGTH, "8",
                "Minimum password length (6-32)");
        settings.createIfMissing(SystemSettingService.MAX_ACTIVE_APPLICATIONS, "10",
                "Max active applications per candidate (1-100)");
        settings.createIfMissing(SystemSettingService.ALLOW_REGISTRATION, "true",
                "Allow new candidates to self-register (true/false)");

        if (users.count() > 0) return;

        seed("System Admin", "admin@recruit.lk", "0711111111", Role.ADMIN, true);
        seed("Nimal Perera", "nimal.recruiter@recruit.lk", "0772222222", Role.RECRUITER, true);
        seed("Sanduni Silva", "sanduni.hr@abc.lk", "0763333333", Role.EMPLOYER, true);
        seed("Kavindu Fernando", "kavindu@gmail.com", "0704444444", Role.CANDIDATE, true);
        seed("Tharushi Jayasinghe", "tharushi@gmail.com", "0755555555", Role.CANDIDATE, true);
        seed("Ruwan Bandara", "ruwan@gmail.com", null, Role.CANDIDATE, false);
    }

    private void seed(String name, String email, String phone, Role role, boolean enabled) {
        User u = new User();
        u.setFullName(name);
        u.setEmail(email);
        u.setPhone(phone);
        u.setRole(role);
        u.setEnabled(enabled);
        u.setPassword(encoder.encode(role == Role.ADMIN ? "Admin@123" : "Password@123"));
        users.save(u);
    }
}
