package lk.sliit.recruitment.service;

import lk.sliit.recruitment.event.AdminActionEvent;
import lk.sliit.recruitment.model.AuditAction;
import lk.sliit.recruitment.model.SystemSetting;
import lk.sliit.recruitment.repository.SystemSettingRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class SystemSettingService {

    public static final String COMPANY_NAME = "company.name";
    public static final String SUPPORT_EMAIL = "company.supportEmail";
    public static final String PASSWORD_MIN_LENGTH = "security.passwordMinLength";
    public static final String MAX_ACTIVE_APPLICATIONS = "applications.maxActivePerCandidate";
    public static final String ALLOW_REGISTRATION = "registration.candidatesOpen";

    private final SystemSettingRepository repository;
    private final ApplicationEventPublisher events;

    public SystemSettingService(SystemSettingRepository repository, ApplicationEventPublisher events) {
        this.repository = repository;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<SystemSetting> findAll() {
        return repository.findAll(Sort.by("key"));
    }

    @Transactional(readOnly = true)
    public String get(String key, String fallback) {
        return repository.findById(key).map(SystemSetting::getValue).orElse(fallback);
    }

    @Transactional(readOnly = true)
    public int getInt(String key, int fallback) {
        try {
            return Integer.parseInt(get(key, String.valueOf(fallback)));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    public void createIfMissing(String key, String value, String description) {
        if (!repository.existsById(key)) {
            repository.save(new SystemSetting(key, value, description));
        }
    }

    public int updateAll(Map<String, String> submitted, String adminEmail) {
        int changed = 0;
        for (SystemSetting s : repository.findAll()) {
            String newValue = submitted.get(s.getKey());
            if (newValue == null) continue;
            newValue = newValue.trim();
            validate(s.getKey(), newValue);
            if (!newValue.equals(s.getValue())) {
                String old = s.getValue();
                s.setValue(newValue);
                changed++;
                events.publishEvent(new AdminActionEvent(this, adminEmail, AuditAction.SETTING_UPDATED,
                        s.getKey(), "'" + old + "' -> '" + newValue + "'"));
            }
        }
        return changed;
    }

    private void validate(String key, String value) {
        if (value.isEmpty()) throw new AdminOperationException(key + " cannot be empty");
        switch (key) {
            case PASSWORD_MIN_LENGTH -> requireIntBetween(key, value, 6, 32);
            case MAX_ACTIVE_APPLICATIONS -> requireIntBetween(key, value, 1, 100);
            case ALLOW_REGISTRATION -> {
                if (!value.equals("true") && !value.equals("false"))
                    throw new AdminOperationException(key + " must be true or false");
            }
            case SUPPORT_EMAIL -> {
                if (!value.matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"))
                    throw new AdminOperationException(key + " must be a valid email");
            }
            default -> { }
        }
    }

    private void requireIntBetween(String key, String value, int min, int max) {
        try {
            int v = Integer.parseInt(value);
            if (v < min || v > max) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            throw new AdminOperationException(key + " must be a number between " + min + " and " + max);
        }
    }
}
