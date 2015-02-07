package com.phesus.cotizatodo;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 04/02/15
 * Time: 15:54
 */
/**
 * Proyecto Omoikane: SmartPOS 2.0
 * User: octavioruizcastillo
 * Date: 03/12/14
 * Time: 14:06
 *
 * Crédito a  Yogender Butola @ http://javahotpot.blogspot.in
 */
import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

public class MediUser extends User {

     private static final long serialVersionUID = -3531439484732724601L;

     private final String displayName;

     public MediUser(String username, String password, boolean enabled,
         boolean accountNonExpired, boolean credentialsNonExpired,
         boolean accountNonLocked,
         Collection authorities,
         String displayName) {

             super(username, password, enabled, accountNonExpired,
                credentialsNonExpired, accountNonLocked, authorities);

             this.displayName = displayName;
     }

     public static long getSerialversionuid() {
        return serialVersionUID;
     }

     public String getDisplayName() {
        return displayName;
     }

  }

