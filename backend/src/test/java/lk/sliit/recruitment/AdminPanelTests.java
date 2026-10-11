package lk.sliit.recruitment;

import lk.sliit.recruitment.model.AuditAction;
import lk.sliit.recruitment.repository.AuditLogRepository;
import lk.sliit.recruitment.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminPanelTests {

    private static final String ADMIN = "admin@recruit.lk";

    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AuditLogRepository audit;

    private Long idOf(String email) {
        return users.findByEmailIgnoreCase(email).orElseThrow().getId();
    }

    // ---------- authentication & role-based access ----------

    @Test
    void anonymousGets401() throws Exception {
        mvc.perform(get("/api/admin/dashboard")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "kavindu@gmail.com", roles = "CANDIDATE")
    void candidateGets403() throws Exception {
        mvc.perform(get("/api/admin/users")).andExpect(status().isForbidden())
           .andExpect(jsonPath("$.message", containsString("administrators")));
    }

    @Test
    void loginCreatesSessionThatWorksForAdminApi() throws Exception {
        MockHttpSession session = (MockHttpSession) mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"admin@recruit.lk\",\"password\":\"Admin@123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andReturn().getRequest().getSession(false);

        mvc.perform(get("/api/auth/me").session(session)).andExpect(jsonPath("$.email").value(ADMIN));
        mvc.perform(get("/api/admin/dashboard").session(session)).andExpect(status().isOk());
    }

    @Test
    void wrongPasswordAndDisabledAccountAreRejected() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"admin@recruit.lk\",\"password\":\"nope\"}"))
           .andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"ruwan@gmail.com\",\"password\":\"Password@123\"}"))
           .andExpect(status().isUnauthorized())
           .andExpect(jsonPath("$.message", containsString("disabled")));
    }

    // ---------- admin endpoints ----------

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void dashboardSettingsAuditAndRolesRespond() throws Exception {
        mvc.perform(get("/api/admin/dashboard"))
           .andExpect(jsonPath("$.totalUsers", greaterThanOrEqualTo(6)))
           .andExpect(jsonPath("$.usersByRole.ADMIN", greaterThanOrEqualTo(1)));
        mvc.perform(get("/api/admin/settings")).andExpect(jsonPath("$", hasSize(5)));
        mvc.perform(get("/api/admin/audit")).andExpect(status().isOk());
        mvc.perform(get("/api/admin/roles")).andExpect(jsonPath("$", hasSize(4)));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void searchAndFilterUsers() throws Exception {
        mvc.perform(get("/api/admin/users").param("role", "CANDIDATE").param("status", "disabled"))
           .andExpect(jsonPath("$.content[*].email", hasItem("ruwan@gmail.com")))
           .andExpect(jsonPath("$.content[*].role", everyItem(is("CANDIDATE"))));
        mvc.perform(get("/api/admin/users").param("keyword", "nimal"))
           .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void createUserWritesAuditEntry() throws Exception {
        long before = audit.count();
        mvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Test Recruiter","email":"test.rec@recruit.lk","role":"RECRUITER",
                 "enabled":true,"password":"Secret123"}"""))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.password").doesNotExist());
        assertThat(audit.count()).isEqualTo(before + 1);
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void validationErrorsComeBackPerField() throws Exception {
        mvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON)
                .content("{\"fullName\":\"A\",\"email\":\"bad\",\"phone\":\"123\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.fieldErrors.fullName").exists())
           .andExpect(jsonPath("$.fieldErrors.email").exists())
           .andExpect(jsonPath("$.fieldErrors.phone").exists())
           .andExpect(jsonPath("$.fieldErrors.role").exists());
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void businessRulesAreEnforced() throws Exception {
        Long adminId = idOf(ADMIN);
        mvc.perform(patch("/api/admin/users/" + adminId + "/status")).andExpect(status().isBadRequest());
        mvc.perform(delete("/api/admin/users/" + adminId)).andExpect(status().isBadRequest());
        mvc.perform(post("/api/admin/users").contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Dup User","email":"kavindu@gmail.com","role":"CANDIDATE","password":"Secret123"}"""))
           .andExpect(jsonPath("$.message", containsString("already exists")));
        mvc.perform(post("/api/admin/users/" + idOf("tharushi@gmail.com") + "/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"newPassword\":\"weak\",\"confirmPassword\":\"weak\"}"))
           .andExpect(status().isBadRequest());
        mvc.perform(get("/api/admin/users/999999")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void roleChangeIsAuditedByObserver() throws Exception {
        Long id = idOf("tharushi@gmail.com");
        mvc.perform(put("/api/admin/users/" + id).contentType(MediaType.APPLICATION_JSON).content("""
                {"fullName":"Tharushi Jayasinghe","email":"tharushi@gmail.com","role":"EMPLOYER","enabled":true}"""))
           .andExpect(jsonPath("$.role").value("EMPLOYER"));
        assertThat(audit.findAll()).anyMatch(a -> a.getAction() == AuditAction.ROLE_CHANGED
                && a.getTarget().equals("tharushi@gmail.com"));
    }

    @Test
    @WithMockUser(username = ADMIN, roles = "ADMIN")
    void settingsAreValidated() throws Exception {
        mvc.perform(put("/api/admin/settings").contentType(MediaType.APPLICATION_JSON)
                .content("{\"security.passwordMinLength\":\"200\"}"))
           .andExpect(status().isBadRequest());
        mvc.perform(put("/api/admin/settings").contentType(MediaType.APPLICATION_JSON)
                .content("{\"company.name\":\"New Name Ltd\"}"))
           .andExpect(jsonPath("$.changed").value(1));
    }
}
