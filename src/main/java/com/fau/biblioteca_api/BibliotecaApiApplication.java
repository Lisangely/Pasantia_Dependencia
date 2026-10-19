package com.fau.biblioteca_api;

import com.fau.biblioteca_api.repository.EditorialRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BibliotecaApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(BibliotecaApiApplication.class, args);
    }

    @Bean
    CommandLineRunner pruebaConexion(
            EditorialRepository editorialRepository) {

        return args -> {

            System.out.println("PRUEBA");

            long cantidad = editorialRepository.count();

            System.out.println(
                    "Cantidad de editoriales: " + cantidad
            );
        };
    }
}