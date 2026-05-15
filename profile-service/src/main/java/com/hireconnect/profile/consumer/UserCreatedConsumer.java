package com.hireconnect.profile.consumer;

import com.hireconnect.profile.config.RabbitMQConfig;
import com.hireconnect.profile.entity.CandidateProfile;
import com.hireconnect.profile.entity.RecruiterProfile;
import com.hireconnect.profile.event.UserCreatedEvent;
import com.hireconnect.profile.repository.CandidateProfileRepository;
import com.hireconnect.profile.repository.RecruiterProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCreatedConsumer {

    private final CandidateProfileRepository candidateRepository;
    private final RecruiterProfileRepository recruiterRepository;

    @RabbitListener(queues = RabbitMQConfig.USER_CREATED_QUEUE)
    public void consume(UserCreatedEvent event) {
        log.info("Received UserCreatedEvent for userId={} role={}", event.getUserId(), event.getRole());

        if ("CANDIDATE".equalsIgnoreCase(event.getRole())) {

            if (candidateRepository.existsByUserId(event.getUserId())) {
                log.warn("Candidate profile already exists for userId={}", event.getUserId());
                return;
            }

            CandidateProfile profile = CandidateProfile.builder()
                    .location("")
                    .bio("")
                    .skills(List.of())
                    .experience(0)
                    .dob(LocalDate.of(2000, 1, 1))
                    .gender("Not Specified")
                    .resumeUrl("")
                    .addresses(List.of())
                    .build();

            profile.setUserId(event.getUserId());
            profile.setFullName("New Candidate");
            profile.setEmail(event.getEmail());
            profile.setMobile(event.getMobileNumber());
            profile.setActive(true);
            profile.setCreatedAt(LocalDateTime.now());

            candidateRepository.save(profile);

            log.info("Candidate profile created for {}", event.getEmail());

        } else if ("RECRUITER".equalsIgnoreCase(event.getRole())) {

            if (recruiterRepository.existsByUserId(event.getUserId())) {
                log.warn("Recruiter profile already exists for userId={}", event.getUserId());
                return;
            }

            RecruiterProfile profile = RecruiterProfile.builder()
                    .companyName("")
                    .companySize("")
                    .industry("")
                    .website("")
                    .officeLocation("")
                    .build();

            profile.setUserId(event.getUserId());
            profile.setFullName("New Recruiter");
            profile.setEmail(event.getEmail());
            profile.setMobile(event.getMobileNumber());
            profile.setActive(true);
            profile.setCreatedAt(LocalDateTime.now());

            recruiterRepository.save(profile);

            log.info("Recruiter profile created for {}", event.getEmail());

        } else {
            log.warn("Unknown role received in UserCreatedEvent: {}", event.getRole());
        }
    }
}