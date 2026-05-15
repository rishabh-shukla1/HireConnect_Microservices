package com.hireconnect.job.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hireconnect.job.config.JwtAuthenticationFilter;
import com.hireconnect.job.config.JwtService;
import com.hireconnect.job.dto.JobRequest;
import com.hireconnect.job.dto.JobResponse;
import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.enums.JobType;
import com.hireconnect.job.service.JobService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(JobResource.class)
@AutoConfigureMockMvc(addFilters = false)
class JobResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private JobService jobService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    private JwtService jwtService;

    @Test
    void addJob_ShouldReturnCreatedJob() throws Exception {

        UUID recruiterId = UUID.randomUUID();

        JobRequest request = new JobRequest();
        request.setTitle("Java Developer");
        request.setCategory("IT");
        request.setType(JobType.FULL_TIME);
        request.setLocation("Delhi");
        request.setMinSalary(50000.0);
        request.setMaxSalary(100000.0);
        request.setSkills(List.of("Java", "Spring Boot"));
        request.setExperienceRequired(2);
        request.setPostedBy(recruiterId);
        request.setDescription("Looking for an experienced Java developer.");

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Java Developer")
                .status(JobStatus.OPEN)
                .build();

        when(jobService.addJob(any(JobRequest.class))).thenReturn(response);

        mockMvc.perform(post("/jobs")
                        .contentType(APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void getAllJobs_ShouldReturnJobList() throws Exception {

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Java Developer")
                .status(JobStatus.OPEN)
                .build();

        when(jobService.getAllJobs()).thenReturn(List.of(response));

        mockMvc.perform(get("/jobs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Java Developer"));
    }

    @Test
    void getJobById_ShouldReturnJob() throws Exception {

        JobResponse response = JobResponse.builder()
                .jobId(1L)
                .title("Java Developer")
                .status(JobStatus.OPEN)
                .build();

        when(jobService.getJobById(1L)).thenReturn(response);

        mockMvc.perform(get("/jobs/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobId").value(1))
                .andExpect(jsonPath("$.title").value("Java Developer"));
    }

    @Test
    void deleteJob_ShouldReturnNoContent() throws Exception {

        doNothing().when(jobService).deleteJob(1L);

        mockMvc.perform(delete("/jobs/1"))
                .andExpect(status().isNoContent());
    }
}