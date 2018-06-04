package br.com.willianantunes.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "TB_CHAT_TRANSACTION")
@NamedQueries({ @NamedQuery(name = ChatTransaction.CHAT_TRANSACTION_NAMED_QUERY_SELECT_NOT_FINISHED_BY_CHAT_ID,
        query = "SELECT m FROM ChatTransaction m WHERE m.chatId = :chatId AND finished = false") ,
        @NamedQuery(name = ChatTransaction.CHAT_TRANSACTION_NAMED_QUERY_FINISH_CONVERSATION_BY_CHAT_ID,
        query =  "UPDATE ChatTransaction m SET m.finished = true WHERE m.chatId = :chatId")
})
public class ChatTransaction {

    public static final String CHAT_TRANSACTION_NAMED_QUERY_SELECT_NOT_FINISHED_BY_CHAT_ID = "CHAT_TRANSACTION_NAMED_QUERY_SELECT_NOT_FINISHED_BY_CHAT_ID";
    public static final String CHAT_TRANSACTION_NAMED_QUERY_FINISH_CONVERSATION_BY_CHAT_ID = "CHAT_TRANSACTION_NAMED_QUERY_FINISH_CONVERSATION_BY_CHAT_ID";

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
    @ElementCollection(fetch = FetchType.EAGER)
    private Map<String, String> chatProperties;
    @Column
    private LocalDateTime createdAt;
    @Column
    private LocalDateTime sentAt;
    @Column
    private Boolean finished;
    @Column
    private boolean executed;
    
    @PrePersist
    private void prePersiste() {
        
        createdAt = LocalDateTime.now();
    }
}