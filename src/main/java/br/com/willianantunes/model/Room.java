package br.com.willianantunes.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.*;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_ROOM")
@NamedQueries({ 
    @NamedQuery(name = Room.NQ_ROOM_SELECT_BY_CHAT_ID, 
        query = "SELECT r FROM Room r WHERE r.chatId = :chatId"), 
    @NamedQuery(name = Room.NQ_ROOM_SELECT_BY_CHAT_ID_AND_POLITICIAN_NAME, 
        query = "SELECT p FROM Room r INNER JOIN r.politicians p WHERE r.chatId = :chatId AND p.name = :name") })
public class Room {

    public static final String NQ_ROOM_SELECT_BY_CHAT_ID = "NQ_ROOM_SELECT_BY_CHAT_ID";
    public static final String NQ_ROOM_SELECT_BY_CHAT_ID_AND_POLITICIAN_NAME = "NQ_ROOM_SELECT_BY_CHAT_ID_AND_POLITICIAN_NAME";
    
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private Integer chatId;
    @Column
    @ManyToMany(cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    private List<Politician> politicians;
    @Column
    private LocalDateTime createdAt;
    
    @PrePersist
    private void prePersiste() {
        
        createdAt = LocalDateTime.now();
    }
}