package com.capstone.jobportal.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "user_skills")
public class UserSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private JobSeekerProfile jobSeeker;

    @ManyToOne
    @JoinColumn(name = "skill_id", nullable = false)
    private Skills skill;

    private int yearsOfExperience;

    // Constructors
    public UserSkill() {}

    public UserSkill(JobSeekerProfile jobSeeker, Skills skill, int yearsOfExperience) {
        this.jobSeeker = jobSeeker;
        this.skill = skill;
        this.yearsOfExperience = yearsOfExperience;
    }

    // Getters & Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public JobSeekerProfile getJobSeeker() { return jobSeeker; }
    public void setJobSeeker(JobSeekerProfile jobSeeker) { this.jobSeeker = jobSeeker; }

    public Skills getSkill() { return skill; }
    public void setSkill(Skills skill) { this.skill = skill; }

    public int getYearsOfExperience() { return yearsOfExperience; }
    public void setYearsOfExperience(int yearsOfExperience) { this.yearsOfExperience = yearsOfExperience; }
}
