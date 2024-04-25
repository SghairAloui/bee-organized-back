package com.BeeOranized.BeeOranized.Repository;

import com.BeeOranized.BeeOranized.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserEmail(String userEmail);
    Boolean existsByUserEmail(String userEmail);
}
