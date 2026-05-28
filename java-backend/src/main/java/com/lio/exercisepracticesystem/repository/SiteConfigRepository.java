package com.lio.exercisepracticesystem.repository;

import com.lio.exercisepracticesystem.entity.SiteConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SiteConfigRepository extends JpaRepository<SiteConfig, Long> {

    Optional<SiteConfig> findFirstByOrderByIdAsc();
}
