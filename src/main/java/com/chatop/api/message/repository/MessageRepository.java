package com.chatop.api.message.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chatop.api.message.model.Message;

public interface MessageRepository extends JpaRepository<Message, Integer> {
}
