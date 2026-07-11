package com.phesus.cotizatodo.entity;

import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class UserRepository implements PanacheRepositoryBase<User, String> {

    public User findByUsername(String username) {
        return findById(username);
    }

    public boolean exists(String username) {
        return findById(username) != null;
    }
}
