package org.whoisthebot.repository;

import org.whoisthebot.entity.Question;

import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped 
public class QuestionRepository implements PanacheRepository<Question>{

}
