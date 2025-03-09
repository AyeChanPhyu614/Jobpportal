package com.capstone.jobportal.controller;

import com.capstone.jobportal.entity.JobSeekerProfile;
import com.capstone.jobportal.entity.Skills;
import com.capstone.jobportal.entity.Users;
import com.capstone.jobportal.repository.UsersRepository;
import com.capstone.jobportal.services.JobSeekerProfileService;
import com.capstone.jobportal.services.SkillsService;
import com.capstone.jobportal.util.FileDownloadUtil;
import com.capstone.jobportal.util.FileUploadUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@Controller
@RequestMapping("/job-seeker-profile")
public class JobSeekerProfileController {

    private final JobSeekerProfileService jobSeekerProfileService;
    private final UsersRepository usersRepository;

    @Autowired
    private SkillsService skillsService;

    @Autowired
    public JobSeekerProfileController(JobSeekerProfileService jobSeekerProfileService, UsersRepository usersRepository) {
        this.jobSeekerProfileService = jobSeekerProfileService;
        this.usersRepository = usersRepository;
    }

    @GetMapping("/")
    public String jobSeekerProfile(Model model) {
        JobSeekerProfile jobSeekerProfile = new JobSeekerProfile();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Set<Skills> skills = new HashSet<>();

        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));

            Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(user.getUserId());

            if (seekerProfile.isPresent()) {
                jobSeekerProfile = seekerProfile.get();
                skills = jobSeekerProfile.getSkills();
            }

            model.addAttribute("skills", skills.isEmpty() ? new HashSet<>() : skills);
            model.addAttribute("profile", jobSeekerProfile);
        }

        return "job-seeker-profile";
    }

    @PostMapping("/addNew")
    public String addNew(JobSeekerProfile jobSeekerProfile,
                         @RequestParam("image") MultipartFile image,
                         @RequestParam("pdf") MultipartFile pdf,
                         Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!(authentication instanceof AnonymousAuthenticationToken)) {
            Users user = usersRepository.findByEmail(authentication.getName())
                    .orElseThrow(() -> new UsernameNotFoundException("User not found."));
            jobSeekerProfile.setUserId(user);
            jobSeekerProfile.setUserAccountId(user.getUserId());
        }

        Set<Skills> skillsSet = jobSeekerProfile.getSkills() != null ? jobSeekerProfile.getSkills() : new HashSet<>();
        for (Skills skill : skillsSet) {
            skill.setJobSeekerProfile(jobSeekerProfile);
        }
        jobSeekerProfile.setSkills(skillsSet);

        String imageName = "";
        String resumeName = "";

        // Profile Image Upload
        if (!image.isEmpty()) {
            imageName = StringUtils.cleanPath(Objects.requireNonNull(image.getOriginalFilename()));
            jobSeekerProfile.setProfilePhoto(imageName);
        }

        // Resume Upload
        if (!pdf.isEmpty()) {
            resumeName = StringUtils.cleanPath(Objects.requireNonNull(pdf.getOriginalFilename()));
            jobSeekerProfile.setResume(resumeName);
        }

        JobSeekerProfile seekerProfile = jobSeekerProfileService.addNew(jobSeekerProfile);

        try {
            String uploadDir = "photos/candidate/" + jobSeekerProfile.getUserAccountId();
            if (!image.isEmpty()) {
                FileUploadUtil.saveFile(uploadDir, imageName, image);
            }
            if (!pdf.isEmpty()) {
                FileUploadUtil.saveFile(uploadDir, resumeName, pdf);
            }
        } catch (IOException ex) {
            throw new RuntimeException("File upload failed: " + ex.getMessage(), ex);
        }

        return "redirect:/dashboard/";
    }

    @GetMapping("/{id}")
    public String candidateProfile(@PathVariable("id") int id, Model model) {
        Optional<JobSeekerProfile> seekerProfile = jobSeekerProfileService.getOne(id);
        if (seekerProfile.isPresent()) {
            model.addAttribute("profile", seekerProfile.get());
            return "job-seeker-profile";
        } else {
            return "error/404"; // Redirect to an error page if not found
        }
    }

    @GetMapping("/downloadResume")
    public ResponseEntity<?> downloadResume(@RequestParam(value = "fileName") String fileName,
                                            @RequestParam(value = "userID") String userId) {

        FileDownloadUtil downloadUtil = new FileDownloadUtil();
        Resource resource;

        try {
            resource = downloadUtil.getFileAsResourse("photos/candidate/" + userId, fileName);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("File download failed");
        }

        if (resource == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
        }

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/profile-get")
    public String showProfileForm(Model model) {
        model.addAttribute("jobSeekerProfile", new JobSeekerProfile());
        model.addAttribute("allSkills", skillsService.getAllSkills());
        return "profile"; // Thymeleaf template name
    }

    @PostMapping("/profile-post")
    public String saveProfile(@ModelAttribute JobSeekerProfile jobSeekerProfile) {
        jobSeekerProfileService.saveProfile(jobSeekerProfile);
        return "redirect:/profile";
    }
}
