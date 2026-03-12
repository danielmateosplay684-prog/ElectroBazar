package com.proconsi.electrobazar.service;

import com.proconsi.electrobazar.model.CompanySettings;

public interface CompanySettingsService {
    CompanySettings getSettings();
    CompanySettings save(CompanySettings settings);
}
