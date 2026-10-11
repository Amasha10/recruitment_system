package lk.sliit.recruitment.controller;

import lk.sliit.recruitment.model.SystemSetting;
import lk.sliit.recruitment.service.SystemSettingService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/settings")
public class AdminSettingsController {

    private final SystemSettingService settingService;

    public AdminSettingsController(SystemSettingService settingService) {
        this.settingService = settingService;
    }

    @GetMapping
    public List<SystemSetting> list() {
        return settingService.findAll();
    }

    @PutMapping
    public Map<String, Object> update(@RequestBody Map<String, String> values, Authentication auth) {
        int changed = settingService.updateAll(values, auth.getName());
        return Map.of("changed", changed, "settings", settingService.findAll());
    }
}
