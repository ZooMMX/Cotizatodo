package com.phesus.cotizatodo;

import com.phesus.cotizatodo.entity.User;
import com.phesus.cotizatodo.entity.UserRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.elytron.security.common.BcryptUtil;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

/**
 * Solo en modo dev: crea el usuario admin/admin para poder probar la app
 * contra la H2 en memoria.
 */
@ApplicationScoped
@IfBuildProfile("dev")
public class DevStartup {

    private static final Logger LOG = Logger.getLogger(DevStartup.class);

    @Inject
    UserRepository users;

    @Transactional
    void onStart(@Observes StartupEvent ev) {
        if (users.findByUsername("admin") == null) {
            User admin = new User("admin", BcryptUtil.bcryptHash("admin"), true);
            admin.setFullname("Administrador (dev)");
            admin.setEmail("admin@example.com");
            admin.setCountry("MX");
            admin.addUserRole("ROLE_ADMIN");
            admin.addUserRole("ROLE_USER");
            users.persist(admin);
            LOG.info("Usuario dev creado: admin/admin");
        }
    }
}
