package com.hireconnect.job.config;

import com.hireconnect.job.document.JobDocument;
import com.hireconnect.job.entity.Job;
import com.hireconnect.job.enums.JobStatus;
import com.hireconnect.job.repository.JobRepository;
import com.hireconnect.job.repository.JobSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class JobIndexInitializer implements ApplicationRunner {

    private final JobRepository jobRepository;
    private final JobSearchRepository jobSearchRepository;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Initializing Elasticsearch job index...");
        
        try {
            jobSearchRepository.deleteAll();
            
            List<Job> jobs = jobRepository.findByStatus(JobStatus.OPEN);
            
            for (Job job : jobs) {
                JobDocument doc = toJobDocument(job);
                jobSearchRepository.save(doc);
            }
            
            log.info("Successfully indexed {} jobs in Elasticsearch", jobs.size());
        } catch (Exception e) {
            log.error("Failed to initialize job index: {}", e.getMessage());
        }
    }
    
    private JobDocument toJobDocument(Job job) {
        JobDocument doc = new JobDocument();
        doc.setJobId(job.getJobId());
        doc.setTitle(job.getTitle());
        doc.setCategory(job.getCategory());
        doc.setType(job.getType().name());
        doc.setLocation(job.getLocation());
        doc.setMinSalary(job.getMinSalary());
        doc.setMaxSalary(job.getMaxSalary());
        doc.setDescription(job.getDescription());
        doc.setExperienceRequired(job.getExperienceRequired());
        doc.setStatus(job.getStatus().name());
        doc.setPostedBy(job.getPostedBy());
        return doc;
    }
}