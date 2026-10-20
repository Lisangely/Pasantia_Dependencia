package com.fau.biblioteca_api.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class ColumnasLectores implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public ColumnasLectores(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        jdbc.execute("ALTER TABLE lectores ADD COLUMN IF NOT EXISTS semestre VARCHAR(20)");
        jdbc.execute("ALTER TABLE lectores ADD COLUMN IF NOT EXISTS carrera VARCHAR(150)");
    }
}
