package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Editorial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditorialRepository
        extends JpaRepository<Editorial, Integer> {
}