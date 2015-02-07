package com.phesus.cotizatodo;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 04/02/15
 * Time: 15:36
 */
public interface UserRepository extends CrudRepository<User, String> {
    User findByUsername(String username);
    List<User> findByOrderByUsernameAsc();

    @Query("SELECT u FROM User u WHERE u.enabled = 1 ORDER BY u.fullname ASC")
    public List<User> findActiveUsers();
}
