package com.lio.exercisepracticesystem.controller;

import com.lio.exercisepracticesystem.dto.SiteBannerDto;
import com.lio.exercisepracticesystem.service.SiteConfigService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/site")
public class PublicSiteController {

    private final SiteConfigService siteConfigService;

    public PublicSiteController(SiteConfigService siteConfigService) {
        this.siteConfigService = siteConfigService;
    }

    @GetMapping("/banner")
    public SiteBannerDto banner() {
        return siteConfigService.getBanner();
    }
}
