package com.flow.forum.service;

import com.flow.forum.dao.AlphaDao;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AlphaService {
    @Autowired
    private AlphaDao alphaDao;

    public AlphaService() {
        System.out.println("AlphaService constructor");
    }

    @PostConstruct
    public void init() {
        System.out.println("AlphaService init");
    }

    @PreDestroy
    public void destroy() {
        System.out.println("AlphaService destroy");
    }

    public String find() {
        return alphaDao.select();
    }

}
