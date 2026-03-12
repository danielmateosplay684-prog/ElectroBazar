package com.proconsi.electrobazar.repository;

import com.proconsi.electrobazar.model.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, Long> {
    List<ActivityLog> findTop50ByOrderByTimestampDesc();

    List<ActivityLog> findTop20ByOrderByTimestampDesc();

    void deleteByTimestampBeforeAndActionIn(java.time.LocalDateTime timestamp, java.util.Collection<String> actions);

    void deleteByTimestampBeforeAndActionNotIn(java.time.LocalDateTime timestamp, java.util.Collection<String> actions);
}
