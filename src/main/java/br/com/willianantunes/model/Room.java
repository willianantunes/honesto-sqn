package br.com.willianantunes.model;

import java.time.LocalDateTime;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.PrePersist;
import javax.persistence.Table;

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
@NamedQueries({ @NamedQuery(name = Room.NAMED_QUERY_SELECT_BY_CHAT_ID, query = "SELECT r FROM Room r WHERE r.chatId = :chatId")})
public class Room {

    public static final String NAMED_QUERY_SELECT_BY_CHAT_ID = "ROOM_NAMED_QUERY_SELECT_BY_CHAT_ID";
    
    @Id
    @GeneratedValue
    private Long id;
    @Column(unique = true)
    private Integer chatId;
    @Column
    @ManyToMany(cascade = { CascadeType.PERSIST })
    private List<Politician> politicians;
    @Column
    private LocalDateTime createdAt;    
    
    @PrePersist
    private void prePersiste() {
        
        createdAt = LocalDateTime.now();
    }    
}