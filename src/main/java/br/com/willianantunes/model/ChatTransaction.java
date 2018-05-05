package br.com.willianantunes.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
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
@Table(name = "TB_MESSAGES")
@NamedQueries({ @NamedQuery(name = Messages.NAMED_QUERY_SELECT_ALL, query = "SELECT m FROM Messages m") })
public class Messages {

    public static final String NAMED_QUERY_SELECT_ALL = "SELECT-ALL";

    @Id
    @GeneratedValue
    private Integer id;
    @Column
    private String userName;
    @Column
    private String screenName;
    @Column
    private LocalDateTime createdAt;
    @Column
    private String text;
}
