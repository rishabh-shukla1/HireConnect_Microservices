package com.hireconnect.analytics.service;

import com.hireconnect.analytics.client.ApplicationServiceClient;
import com.hireconnect.analytics.client.JobServiceClient;
import com.hireconnect.analytics.dto.AnalyticsSummary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsServiceImp implements AnalyticsService {

    private final JobServiceClient jobServiceClient;
    private final ApplicationServiceClient applicationServiceClient;

    @Override
    public int getJobViewCount(Long jobId) {

        Integer views = jobServiceClient.getJobViewCount(jobId);

        return views != null ? views : 0;
    }

    @Override
    public int getAppCountByJob(Long jobId) {

        Integer applications = applicationServiceClient.getApplicationCountByJob(jobId);

        return applications != null ? applications : 0;
    }

    @Override
    public double getViewToApplyRatio(Long jobId) {

        int views = getJobViewCount(jobId);
        int applications = getAppCountByJob(jobId);

        if (views <= 0) {
            return 0.0;
        }

        return Math.round(((double) applications / views) * 100.0) / 100.0;
    }


    @Override
    public AnalyticsSummary getPipelineStats(UUID recruiterId) {

        log.info("Fetching analytics dashboard for recruiterId={}", recruiterId);

        Long totalJobs = defaultLong(jobServiceClient.getRecruiterJobCount(recruiterId));
        Long activeJobs = defaultLong(jobServiceClient.getActiveJobCount(recruiterId));
        Long closedJobs = defaultLong(jobServiceClient.getClosedJobCount(recruiterId));

        Long totalApplications = defaultLong(
                applicationServiceClient.getRecruiterApplicationCount(recruiterId));

        Integer shortlisted = defaultInteger(
                applicationServiceClient.getShortlistedCount(recruiterId));

        Integer interviewScheduled = defaultInteger(
                applicationServiceClient.getInterviewScheduledCount(recruiterId));

        Integer offered = defaultInteger(
                applicationServiceClient.getOfferedCount(recruiterId));

        Integer rejected = defaultInteger(
                applicationServiceClient.getRejectedCount(recruiterId));

        Double avgTime = defaultDouble(
                applicationServiceClient.getAverageTimeToHire(recruiterId));

        double viewToApplyRatio = totalJobs == 0
                ? 0.0
                : Math.round(((double) totalApplications / totalJobs) * 100.0) / 100.0;

        Map<String, Long> topCategories;

        try {
            topCategories = jobServiceClient.getTopCategories();
        } catch (Exception ex) {
            log.warn("Could not fetch top categories from job-service: {}", ex.getMessage());
            topCategories = Collections.emptyMap();
        }

        return AnalyticsSummary.builder()
                .totalJobs(totalJobs)
                .activeJobs(activeJobs)
                .closedJobs(closedJobs)
                .totalApplications(totalApplications)
                .shortlistedCount(shortlisted)
                .interviewScheduledCount(interviewScheduled)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .avgTimeToHireDays(avgTime)
                .viewToApplyRatio(viewToApplyRatio)
                .topJobCategories(topCategories)
                .build();
    }

    @Override
    public AnalyticsSummary getPlatformStats() {

        log.info("Fetching platform-wide analytics");

        Long totalJobs = 0L;

        Long totalApplications = defaultLong(
                applicationServiceClient.getPlatformApplicationCount());

        Integer shortlisted = defaultInteger(
                applicationServiceClient.getPlatformShortlistedCount());

        Integer interviewScheduled = defaultInteger(
                applicationServiceClient.getPlatformInterviewScheduledCount());

        Integer offered = defaultInteger(
                applicationServiceClient.getPlatformOfferedCount());

        Integer rejected = defaultInteger(
                applicationServiceClient.getPlatformRejectedCount());

        Double avgTime = defaultDouble(
                applicationServiceClient.getPlatformAverageTimeToHire());

        double viewToApplyRatio = totalJobs == 0
                ? 0.0
                : Math.round(((double) totalApplications / totalJobs) * 100.0) / 100.0;

        Map<String, Long> topCategories;

        try {
            topCategories = jobServiceClient.getTopCategories();
        } catch (Exception ex) {
            log.warn("Could not fetch top categories from job-service: {}", ex.getMessage());
            topCategories = Collections.emptyMap();
        }

        return AnalyticsSummary.builder()
                .totalJobs(totalJobs)
                .activeJobs(0L)
                .closedJobs(0L)
                .totalApplications(totalApplications)
                .shortlistedCount(shortlisted)
                .interviewScheduledCount(interviewScheduled)
                .offeredCount(offered)
                .rejectedCount(rejected)
                .avgTimeToHireDays(avgTime)
                .viewToApplyRatio(viewToApplyRatio)
                .topJobCategories(topCategories)
                .build();
    }

    @Override
    public Map<String, Long> getTopJobCategories() {

        return jobServiceClient.getTopCategories();
    }

    private Long defaultLong(Long value) {
        return value != null ? value : 0L;
    }

    private Integer defaultInteger(Integer value) {
        return value != null ? value : 0;
    }

    private Double defaultDouble(Double value) {
        return value != null ? value : 0.0;
    }

    @Override
    public double getTimeToHire(UUID recruiterId) {

        Double average = applicationServiceClient.getAverageTimeToHire(recruiterId);

        return average != null ? average : 0.0;
    }
    
    @Override
    public int getAllActiveJobsCount() {
        try {
            return jobServiceClient.getAllActiveJobsCount();
        } catch (Exception ex) {
            log.warn("Could not fetch active jobs count: {}", ex.getMessage());
            return 0;
        }
    }
    
    @Override
    public int getAllApplicationsCount() {
        try {
            return applicationServiceClient.getAllApplicationsCount();
        } catch (Exception ex) {
            log.warn("Could not fetch all applications count: {}", ex.getMessage());
            return 0;
        }
    }
    
    @Override
    public int getTotalJobsCount() {
        try {
            return jobServiceClient.getTotalJobsCount();
        } catch (Exception ex) {
            log.warn("Could not fetch total jobs count: {}", ex.getMessage());
            return 0;
        }
    }
}