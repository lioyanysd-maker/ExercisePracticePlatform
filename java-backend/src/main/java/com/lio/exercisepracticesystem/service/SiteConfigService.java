package com.lio.exercisepracticesystem.service;

import com.lio.exercisepracticesystem.dto.SiteBannerDto;
import com.lio.exercisepracticesystem.dto.SiteBannerUpdateRequest;
import com.lio.exercisepracticesystem.entity.SiteConfig;
import com.lio.exercisepracticesystem.repository.SiteConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class SiteConfigService {

    private final SiteConfigRepository siteConfigRepository;

    public SiteConfigService(SiteConfigRepository siteConfigRepository) {
        this.siteConfigRepository = siteConfigRepository;
    }

    public SiteBannerDto getBanner() {
        SiteConfig c = siteConfigRepository.findFirstByOrderByIdAsc().orElseGet(this::createDefault);
        return toDto(c);
    }

    @Transactional
    public SiteBannerDto update(SiteBannerUpdateRequest req) {
        SiteConfig c = siteConfigRepository.findFirstByOrderByIdAsc().orElseGet(this::createDefault);
        if (req.getHomeBannerText() != null) {
            c.setHomeBannerText(req.getHomeBannerText().trim());
        }
        if (req.getHomeBannerEnabled() != null) {
            c.setHomeBannerEnabled(req.getHomeBannerEnabled());
        }
        c.setUpdatedAt(LocalDateTime.now());
        siteConfigRepository.save(c);
        return toDto(c);
    }

    private SiteConfig createDefault() {
        SiteConfig c = new SiteConfig();
        c.setHomeBannerText("欢迎使用智能练习系统，坚持每日练习，稳步提升！");
        c.setHomeBannerEnabled(true);
        c.setUpdatedAt(LocalDateTime.now());
        return siteConfigRepository.save(c);
    }

    private static SiteBannerDto toDto(SiteConfig c) {
        return new SiteBannerDto(c.getHomeBannerText(), c.isHomeBannerEnabled(), c.getUpdatedAt());
    }
}
