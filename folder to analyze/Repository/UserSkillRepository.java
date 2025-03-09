package com.capstone.jobportal.repository;

import com.capstone.jobportal.entity.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface UserSkillRepository extends JpaRepository<UserSkill, Integer> {

    @Query("SELECT u FROM UserSkill u WHERE u.jobSeeker.userAccountId = :userId")
    List<UserSkill> findSkillsByUserId(Integer userId);
}
