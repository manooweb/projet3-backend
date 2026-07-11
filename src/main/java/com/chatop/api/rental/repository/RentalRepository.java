package com.chatop.api.rental.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatop.api.rental.model.Rental;

public interface RentalRepository extends JpaRepository<Rental, Integer> {
}
