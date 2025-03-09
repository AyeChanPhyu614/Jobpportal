package com.capstone.jobportal.repository;

import com.capstone.jobportal.entity.Skills;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SkillsRepository extends JpaRepository<Skills, Integer> {
    List<Skills> findAll();
}
