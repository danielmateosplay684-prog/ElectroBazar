package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.AppSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingRepository extends JpaRepository<AppSetting, String> {
    Optional<AppSetting> findByKey(String key);
}
