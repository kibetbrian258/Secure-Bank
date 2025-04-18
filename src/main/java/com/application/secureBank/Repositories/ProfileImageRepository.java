package com.application.secureBank.Repositories;

import com.application.secureBank.models.ProfileImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {
    Optional<ProfileImage> findByCustomerId(String customerId);
    boolean existsByCustomerId(String customerId);
}
