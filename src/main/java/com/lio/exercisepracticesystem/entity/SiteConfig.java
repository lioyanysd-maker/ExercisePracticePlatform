package com.lio.exercisepracticesystem.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "site_config")
public class SiteConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "home_banner_text", columnDefinition = "TEXT")
    private String homeBannerText;

    @Column(name = "home_banner_enabled", nullable = false)
    private boolean homeBannerEnabled = true;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
