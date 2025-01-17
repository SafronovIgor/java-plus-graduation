package ru.practicum.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import ru.practicum.dto.event.request.Status;

import java.time.LocalDateTime;

@Entity
@Table(name = "requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Request {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "REQ_ID_SEQ")
    @SequenceGenerator(name = "REQ_ID_SEQ", sequenceName = "REQ_ID_SEQ", allocationSize = 1)
    private Long id;
    @CreationTimestamp
    private LocalDateTime created;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = User.class)
    @JoinColumn(name = "USER_ID", referencedColumnName = "ID", nullable = false)
    private User requester;
    @ManyToOne(fetch = FetchType.LAZY, targetEntity = Event.class)
    @JoinColumn(name = "EVENT_ID", referencedColumnName = "ID", nullable = false)
    private Event event;
    @Enumerated(EnumType.STRING)
    private Status status;
}
