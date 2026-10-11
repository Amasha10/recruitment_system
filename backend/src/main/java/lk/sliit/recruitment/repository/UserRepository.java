package lk.sliit.recruitment.repository;

import lk.sliit.recruitment.model.Role;
import lk.sliit.recruitment.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    long countByRole(Role role);

    long countByEnabled(boolean enabled);

    long countByRoleAndEnabled(Role role, boolean enabled);

    List<User> findTop5ByOrderByCreatedAtDesc();

    @Query("""
           SELECT u FROM User u
           WHERE (:keyword IS NULL
                  OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                  OR LOWER(u.email)    LIKE LOWER(CONCAT('%', :keyword, '%')))
             AND (:role IS NULL OR u.role = :role)
             AND (:enabled IS NULL OR u.enabled = :enabled)
           """)
    Page<User> search(@Param("keyword") String keyword,
                      @Param("role") Role role,
                      @Param("enabled") Boolean enabled,
                      Pageable pageable);
}
