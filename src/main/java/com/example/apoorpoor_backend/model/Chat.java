package com.example.apoorpoor_backend.model;

import com.example.apoorpoor_backend.model.enumType.MessageType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

@Entity(name = "CHAT")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String sender;

    private String message;

    @ColumnDefault("1")
    @Column(nullable = false)
    private Long level;

    @Enumerated(EnumType.STRING)
    private MessageType type;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beggar_id")
    private Beggar beggar;
}