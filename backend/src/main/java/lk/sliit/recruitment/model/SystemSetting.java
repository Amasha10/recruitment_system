package lk.sliit.recruitment.model;

import jakarta.persistence.*;

/** Key/value store for system-level information the admin maintains. */
@Entity
@Table(name = "system_settings")
public class SystemSetting {

    @Id
    @Column(name = "setting_key", length = 80)
    private String key;

    @Column(name = "setting_value", nullable = false, length = 500)
    private String value;

    @Column(length = 255)
    private String description;

    protected SystemSetting() { }

    public SystemSetting(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getKey() { return key; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
    public String getDescription() { return description; }
}
