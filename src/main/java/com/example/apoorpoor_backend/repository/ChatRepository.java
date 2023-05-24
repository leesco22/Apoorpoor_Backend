package com.example.apoorpoor_backend.repository;

import com.example.apoorpoor_backend.entity.Chat;
import com.example.apoorpoor_backend.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    Optional<ChatRoom> findByRoomId(String roomId);
}
