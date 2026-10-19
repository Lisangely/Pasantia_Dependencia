package com.fau.biblioteca_api.repository;

import com.fau.biblioteca_api.entity.Lector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LectorRepository
        extends JpaRepository<Lector, Integer> {
}