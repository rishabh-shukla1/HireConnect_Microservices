package com.hireconnect.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyticsSummary {

    private Long totalJobs;

    private Long activeJobs;

    private Long closedJobs;

    private Long totalApplications;

    private Integer shortlistedCount;

    private Integer interviewScheduledCount;

    private Integer offeredCount;

    private Integer rejectedCount;

    private Double avgTimeToHireDays;

    private Double viewToApplyRatio;

    private Map<String, Long> topJobCategories;
}