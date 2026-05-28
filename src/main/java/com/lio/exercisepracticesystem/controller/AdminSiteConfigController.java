package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.SiteBannerDto;
import com.lio.exercisepracticesystem.dto.SiteBannerUpdateRequest;
import com.lio.exercisepracticesystem.service.SiteConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/site")
public class AdminSiteConfigController {

    private final SiteConfigService siteConfigService;

    public AdminSiteConfigController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping("/banner")
    public SiteBannerDto getBanner() {
        return siteConfigService.getBanner();
    }

    @PutMapping("/banner")
    public SiteBannerDto updateBanner(@RequestBody SiteBannerUpdateRequest request) {
        return siteConfigService.update(request);
    }
}
