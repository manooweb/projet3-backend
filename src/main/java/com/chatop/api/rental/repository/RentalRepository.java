package com.chatop.api.rental.repository;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chatop.api.rental.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Integer> {

    @Override
    @EntityGraph(attributePaths = "owner")
    List<Rental> findAll();
}
