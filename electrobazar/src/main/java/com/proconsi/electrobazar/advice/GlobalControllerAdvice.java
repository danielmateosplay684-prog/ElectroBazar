package com.proconsi.electrobazar.advice;

import com.proconsi.electrobazar.model.CompanySettings;
import com.proconsi.electrobazar.service.CompanySettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final CompanySettingsService companySettingsService;

    @ModelAttribute("companySettings")
    public CompanySettings getCompanySettings() {
        return companySettingsService.getSettings();
    }
}
