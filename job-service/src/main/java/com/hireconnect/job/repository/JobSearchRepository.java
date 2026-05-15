package com.hireconnect.job.repository;

import com.hireconnect.job.document.JobDocument;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobSearchRepository extends ElasticsearchRepository<JobDocument, Long> {

    @Query("{" +
           "  \"bool\": {" +
           "    \"must\": [" +
           "      { \"match\": { \"status\": \"OPEN\" } }," +
           "      { \"multi_match\": { \"query\": \"?0\", \"fields\": [\"title^3\", \"description\", \"location\"] } }" +
           "    ]" +
           "  }" +
           "}")
    List<JobDocument> searchByKeyword(String keyword);

    @Query("{" +
           "  \"bool\": {" +
           "    \"must\": [" +
           "      { \"match\": { \"status\": \"OPEN\" } }" +
           "    ]," +
           "    \"filter\": [" +
           "      { \"term\": { \"category\": \"?0\" } }" +
           "    ]" +
           "  }" +
           "}")
    List<JobDocument> searchByCategory(String category);

    @Query("{" +
           "  \"bool\": {" +
           "    \"must\": [" +
           "      { \"match\": { \"status\": \"OPEN\" } }" +
           "    ]," +
           "    \"filter\": [" +
           "      { \"range\": { \"minSalary\": { \"gte\": ?0 } } }," +
           "      { \"range\": { \"maxSalary\": { \"lte\": ?1 } } }" +
           "    ]" +
           "  }" +
           "}")
    List<JobDocument> searchBySalaryRange(Double minSalary, Double maxSalary);

    @Query("{" +
           "  \"bool\": {" +
           "    \"must\": [" +
           "      { \"match\": { \"status\": \"OPEN\" } }" +
           "    ]," +
           "    \"filter\": [" +
           "      { \"range\": { \"experienceRequired\": { \"lte\": ?0 } } }" +
           "    ]" +
           "  }" +
           "}")
    List<JobDocument> searchByExperience(Integer experience);

    List<JobDocument> findByStatus(String status);
}