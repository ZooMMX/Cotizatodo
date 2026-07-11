package com.phesus.cotizatodo.entity;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class QuoteRepository implements PanacheRepositoryBase<Quote, Long> {

    public long countUserQuotes(String username) {
        return count("username = ?1", username);
    }
}
