package com.phesus.cotizatodo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.io.Serializable;
import java.util.HashSet;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 04/02/15
 * Time: 15:29
 */
public class CotizadoraPermissionEvaluator implements PermissionEvaluator {

    Logger log = LoggerFactory.getLogger(CotizadoraPermissionEvaluator.class);

    @Autowired
    QuoteRepository quoteRepository;

    @Override
    public boolean hasPermission(Authentication authentication,
            Object targetDomainObject, Object permission) {
        //log.info("hasPermission(Authentication, Object, Object) called");
        MediUser activeUser = (MediUser) authentication.getPrincipal();

        if(permission instanceof String && ((String) permission).equalsIgnoreCase("quote")) {
            if(targetDomainObject != null && !(targetDomainObject instanceof Long)) return false;
            Long id = (Long) targetDomainObject;

            //Si es ID es nulo se está guardando una nueva cotización, no aplica este permiso.
            if(id == null) return true;
            Quote q = quoteRepository.findOne((Long) id);
            if(q != null && q.getUsername() != null && q.getUsername().equals(activeUser.getUsername())) return true;
        }

        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication,
            Serializable targetId, String targetType, Object permission) {
        log.info("hasPermission(Authentication, Serializable, String, Object) called");
        log.info("TargetID: "+targetId+", targetType: "+targetType+", permission: "+permission);

        //Guardo los roles del usuario actual en un simple HashSet
        HashSet<Roles> roles = new HashSet<>();
        for(GrantedAuthority ga : authentication.getAuthorities()) {
            roles.add(Roles.valueOf( ga.getAuthority() ));
        }

        //Por ahora todos tienen todos los permisos
        return true;
    }

}
