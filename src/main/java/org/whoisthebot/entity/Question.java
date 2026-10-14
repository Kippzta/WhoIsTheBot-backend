package org.whoisthebot.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity 
public class Question {
    
    @Id 
    @GeneratedValue 
    private Long id;

    private String text;

    public String getText() {
        return text;
    }
}
