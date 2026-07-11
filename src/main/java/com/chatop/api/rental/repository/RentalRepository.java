package com.chatop.api.rental.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.chatop.api.rental.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Integer> {

    @Override
    @EntityGraph(attributePaths = "owner")
    List<Rental> findAll();

    @Override
    @EntityGraph(attributePaths = "owner")
    Optional<Rental> findById(Integer id);
}
