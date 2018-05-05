package br.com.willianantunes.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
@Table(name = "TB_CHAT_TRANSACTION")
@NamedQueries({ @NamedQuery(name = ChatTransaction.NAMED_QUERY_SELECT_ALL, query = "SELECT m FROM ChatTransaction m"),
    @NamedQuery(name = ChatTransaction.NAMED_QUERY_SELECT_NOT_FINISHED_BY_CHAT_ID, 
        query = "SELECT m FROM ChatTransaction m WHERE m.chatId = :chatId AND finished = false")})
public class ChatTransaction {

    public static final String NAMED_QUERY_SELECT_ALL = "SELECT-ALL";
    public static final String NAMED_QUERY_SELECT_NOT_FINISHED_BY_CHAT_ID = "SELECT_BY_CHAT_ID";

    @Id
    @GeneratedValue
    private Long id;
    @Column
    private Integer messageId;
    @Column
    private String message;
    @Column
    private Integer chatId;
    @Column
    private String firstName;
    @Column
    private String lastName;
    @Column
    private String chatEndpoint;
    @Column
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime sentAt;
    @Column
    private Boolean finished;
    
    @PrePersist
    private void prePersiste() {
        
        createdAt = LocalDateTime.now();
    }
}