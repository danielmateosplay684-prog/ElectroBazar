package com.proconsi.electrobazar.controller.api;

import com.proconsi.electrobazar.model.ActivityLog;
import com.proconsi.electrobazar.service.ActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activity-log")
public class ActivityLogApiRestController {

    @Autowired
    private ActivityLogService activityLogService;

    @GetMapping("/recent")
    public List<ActivityLog> getRecent() {
        return activityLogService.getMostRecentActivities();
    }

    @GetMapping
    public List<ActivityLog> getAll() {
        return activityLogService.getRecentActivities(); // Service already returns top 50, which is a good "all" for
                                                         // now
    }
}
