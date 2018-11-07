package com.nih.nih.Service.impl;

import com.nih.nih.Service.NIHService;
import com.nih.nih.model.NIHNewModel;
import com.nih.nih.repo.NIHRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.stereotype.Service;


@Service
@Configurable
public class NIHServiceImpl implements NIHService {
    @Autowired
    private NIHRepository repository;

    @Override
    public boolean saveRecord(NIHNewModel model) {
        return repository.save(model).equals(model);
    }
}
