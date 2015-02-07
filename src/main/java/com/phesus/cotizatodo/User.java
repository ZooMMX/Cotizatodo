package com.phesus.cotizatodo;

import javax.persistence.Entity;

import java.util.HashSet;
import java.util.Set;
import javax.persistence.*;

/**
 * Proyecto Cotizatodo
 * User: octavioruizcastillo
 * Date: 02/02/15
 * Time: 11:30
 */

@Entity
public class User {
    private String fullname;

    private String username;
    private String password;
    private boolean enabled = true;
    private Set<UserRole> userRole = new HashSet<UserRole>(0);
    public User() {
    }

    public User(String username, String password, boolean enabled) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
    }

    public User(String username, String password,
        boolean enabled, Set<UserRole> userRole) {
        this.username = username;
        this.password = password;
        this.enabled = enabled;
        this.userRole = userRole;
    }

    @Id
    @Column(name = "username", unique = true,
        nullable = false, length = 45)
    public String getUsername() {
        return this.username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Column
    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    @Column(name = "password",
        nullable = false, length = 60)
    public String getPassword() {
        return this.password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "enabled", nullable = false)
    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<UserRole> getUserRole() {
        return this.userRole;
    }

    public void setUserRole(Set<UserRole> userRole) {
        this.userRole = userRole;
    }

    @Transient
    public String getUserRoleString() {
        StringBuilder builder = new StringBuilder();
        for(UserRole rol : userRole) {
            if(builder.length() > 0)
                builder.append(", ");
            builder.append(rol.getRole());
        }
        return builder.toString();
    }

    @Transient
    public String getUserActionsHtml() {
        StringBuilder builder = new StringBuilder();
        builder.append(new String("<a href=\"usuario/"+ getUsername() +"\" data-target=\"#ajax\" data-toggle=\"modal\" class=\"btn btn-xs blue\"><i class=\"fa fa-search\"></i> Ver</a>"));
        builder.append(new String("<a href=\"usuarioEdicion/"+ getUsername() + "\" class=\"btn btn-xs default\"><i class=\"icon-pencil\"></i> Editar</a>"));
        return builder.toString();
    }

    public void addUserRole(String role) {
        if(userRole == null)
            userRole = new HashSet<UserRole>();

        UserRole ur = new UserRole();
        ur.setUser(this);
        ur.setRole(role);
        userRole.add(ur);
    }

}