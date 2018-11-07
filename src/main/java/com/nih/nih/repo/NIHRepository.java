package com.nih.nih.repo;

import com.nih.nih.model.NIHNewModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NIHRepository extends JpaRepository<NIHNewModel, Integer> {

    boolean existsByPhone(String phone);

}
