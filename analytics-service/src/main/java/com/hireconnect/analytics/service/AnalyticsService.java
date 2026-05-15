package com.hireconnect.analytics.service;

import com.hireconnect.analytics.dto.AnalyticsSummary;

import java.util.Map;
import java.util.UUID;


public interface AnalyticsService {

    int getJobViewCount(Long jobId);

    int getAppCountByJob(Long jobId);

    double getViewToApplyRatio(Long jobId);

    double getTimeToHire(UUID recruiterId);

    AnalyticsSummary getPipelineStats(UUID recruiterId);

    AnalyticsSummary getPlatformStats();

    Map<String, Long> getTopJobCategories();
    
    int getAllActiveJobsCount();
    
    int getAllApplicationsCount();
    
    int getTotalJobsCount();
}