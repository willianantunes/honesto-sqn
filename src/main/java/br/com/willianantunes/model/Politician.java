package br.com.willianantunes.model;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
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
@Table(name = "TB_POLITICIAN")
public class Politician {

    @Id
    @GeneratedValue
    private Long id;
    @Column
    private String name;
    @Column
    private LocalDateTime createdAt;
    
    @PrePersist
    private void prePersiste() {
        
        createdAt = LocalDateTime.now();
    }
}