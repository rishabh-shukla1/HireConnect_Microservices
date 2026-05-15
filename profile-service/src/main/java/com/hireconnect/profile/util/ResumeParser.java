package com.hireconnect.profile.util;

import com.hireconnect.profile.dto.ResumeParseResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class ResumeParser {

    private static final List<String> COMMON_SKILLS = Arrays.asList(
            "Java", "Spring Boot", "Spring", "Hibernate", "JPA", "REST", "GraphQL",
            "Python", "Django", "Flask", "FastAPI",
            "JavaScript", "TypeScript", "React", "Vue", "Angular", "Next.js", "Node.js", "Express",
            "HTML", "CSS", "Tailwind", "SASS",
            "SQL", "MySQL", "PostgreSQL", "MongoDB", "Redis", "Cassandra", "Elasticsearch",
            "Docker", "Kubernetes", "Helm", "Terraform", "Ansible", "Jenkins",
            "AWS", "Azure", "GCP", "Lambda", "S3", "EC2",
            "Git", "GitHub", "GitLab",
            "Kafka", "RabbitMQ",
            "Machine Learning", "Deep Learning", "TensorFlow", "PyTorch", "NLP", "LLM",
            "Microservices", "CI/CD", "Agile", "Scrum"
    );

    private static final Pattern EMAIL_RE = Pattern.compile("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}");
    private static final Pattern PHONE_RE = Pattern.compile("(?:\\+?\\d{1,3}[\\s-]?)?[6-9]\\d{9}");
    private static final Pattern SECTION_EXPERIENCE_RE = Pattern.compile("(?i)(experience|work\\s*experience|employment|professional\\s*experience)\\s*[:\\-\\n]");

    public ResumeParseResponse parse(MultipartFile file) throws IOException {
        String text = extractText(file);
        return ResumeParseResponse.builder()
                .fullName(extractName(text))
                .email(extractFirstMatch(text, EMAIL_RE))
                .phone(extractFirstMatch(text, PHONE_RE))
                .skills(extractSkills(text))
                .experience(extractExperience(text))
                .build();
    }

    private String extractText(MultipartFile file) throws IOException {
        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String contentType = file.getContentType() == null ? "" : file.getContentType();

        if (name.endsWith(".pdf") || contentType.contains("pdf")) {
            try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
                return new PDFTextStripper().getText(doc);
            }
        }
        if (name.endsWith(".docx") || contentType.contains("officedocument")) {
            try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                 XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
                return ex.getText();
            }
        }
        
        return new String(file.getBytes());
    }

    private String extractFirstMatch(String text, Pattern p) {
        if (text == null) return null;
        Matcher m = p.matcher(text);
        return m.find() ? m.group() : null;
    }

    private String extractName(String text) {
        if (text == null || text.isBlank()) return null;
       
        for (String raw : text.split("\\r?\\n")) {
            String line = raw.trim();
            if (line.isEmpty() || line.length() > 60) continue;
            if (EMAIL_RE.matcher(line).find() || PHONE_RE.matcher(line).find()) continue;
            if (line.contains("@") || line.contains("http")) continue;
            if (!line.matches("^[A-Za-z][A-Za-z .,'\\-]*$")) continue;
            if (line.equalsIgnoreCase("resume") || line.equalsIgnoreCase("curriculum vitae")) continue;
            return line;
        }

        return null;
    }

    private List<String> extractSkills(String text) {
        if (text == null) return List.of();
        String lower = text.toLowerCase();
        Set<String> hits = new LinkedHashSet<>();
        for (String skill : COMMON_SKILLS) {
            if (lower.contains(skill.toLowerCase())) {
                hits.add(skill);
            }
        }
        return new ArrayList<>(hits);
    }

    private List<String> extractExperience(String text) {
        if (text == null) return List.of();
        Matcher section = SECTION_EXPERIENCE_RE.matcher(text);
        if (!section.find()) return List.of();
        String tail = text.substring(section.end());
    
        String[] stop = {"EDUCATION", "Education", "SKILLS", "Skills", "PROJECTS", "Projects", "CERTIFICATIONS", "Certifications"};

        int cut = tail.length();
        for (String s : stop) {
            int i = tail.indexOf(s);
            if (i > 0 && i < cut) cut = i;
        }
        
        String block = tail.substring(0, cut);
        List<String> items = new ArrayList<>();
        for (String line : block.split("\\r?\\n")) {
            String l = line.trim();
            if (l.isEmpty()) continue;
            if (l.length() < 10) continue;
            items.add(l);
            if (items.size() >= 5) break;
        }
        return items;
    }
}
