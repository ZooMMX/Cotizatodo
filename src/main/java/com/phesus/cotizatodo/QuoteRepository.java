package com.phesus.cotizatodo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 04/02/15
 * Time: 15:31
 */
public interface QuoteRepository extends CrudRepository<Quote, Long> {
    @Query("SELECT COUNT(q.id) FROM Quote q WHERE q.username = :username")
    public Long countUserQuotes(@Param("username") String username);
}
