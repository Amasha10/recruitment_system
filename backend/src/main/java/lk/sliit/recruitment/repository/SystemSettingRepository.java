package lk.sliit.recruitment.repository;

import lk.sliit.recruitment.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemSettingRepository extends JpaRepository<SystemSetting, String> {
}
