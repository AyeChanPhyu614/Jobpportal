package com.capstone.jobportal.services;

import com.capstone.jobportal.entity.Skills;
import com.capstone.jobportal.repository.SkillsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SkillsService {

    @Autowired
    private SkillsRepository skillsRepository;

    public List<Skills> getAllSkills() {
        return skillsRepository.findAll();
    }
}
