package com.esprit.formation.user.repository;

import com.esprit.formation.common.domain.RoleName;
import com.esprit.formation.user.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
