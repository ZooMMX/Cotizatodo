package com.phesus.cotizatodo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.util.*;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 06/03/15
 * Time: 13:58
 */

@Controller
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    EntityManager em;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/usuario/{username}")
    public String usuarioVer(@PathVariable("username") String username, Model model) {
        User user = userRepository.findByUsername(username);

        //Construyo un HashSet con los roles actuales
        HashSet userRoles = new HashSet();
        for(UserRole ur : user.getUserRole())
            userRoles.add(ur.getRole());

        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("user", user);
        model.addAttribute("roleTypes", Roles.values());
        model.addAttribute("userRoles", userRoles);

        return "user_profile";
    }

    /**
     * Editar usuario. Primer paso GET
     * @param username
     * @param model
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping("/usuarioEdicion/{username}")
    public String usuarioNuevo(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = "false") Boolean successfulChange,
            @RequestParam(required = false, defaultValue = "false") Boolean successfulRegister,
            Model model) {

        User user = userRepository.findByUsername(username);

        model.addAttribute("user", user);
        model.addAttribute("successfulChange", successfulChange);
        model.addAttribute("successfulRegister", successfulRegister);

        return "usuario_edicion";
    }

    /**
     * Editar usuario. 2do Paso POST
     * @param username
     * @param user
     * @param roles
     * @param model
     * @return
     */
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @RequestMapping(value = "/usuarioEdicion/{username}", method = RequestMethod.POST)
        public String usuarioEdicionPost(
                @PathVariable String username,
                @ModelAttribute User user,
                @RequestParam("roles") String roles,
                Model model) {

            User userFromBd = userRepository.findByUsername(user.getUsername());

            //Construyo un HashSet con los roles actuales
            HashSet userRoles = (HashSet) user.getUserRole();

            /* Actualizar campos modificados */
            userFromBd.setEnabled( user.isEnabled() );
            userFromBd.setFullname(user.getFullname());

            /* Cambiar y Encriptar password si ha sido cambiado*/
            if(!user.getPassword().isEmpty())
                userFromBd.setPassword( passwordEncoder.encode(user.getPassword()) );

            /* Limpiar y reasignar roles */
            // Para cada rol marcado en el formulario
            for( String role : roles.split(",") ) {
                //Para cada rol del usuario en la BD
                Boolean alreadyExists = false;
                for( UserRole ur : userFromBd.getUserRole() ) {
                    //Verifico si ya existía ese rol
                    if( ur.getRole().equals(role) )
                        alreadyExists = true;
                }
                //Agregar rol si no lo tiene
                if(alreadyExists)  { /* Does nothing */ } else
                    userFromBd.addUserRole(role);
            }

            //Quitar rol: si roles en el formulario no lo contiene y la BD sí
            ArrayList<UserRole> quitarRoles = new ArrayList<UserRole>();
            for( UserRole ur : userFromBd.getUserRole() ) {
                if(!Arrays.asList( roles.split(",") ).contains(ur.getRole()))  {
                    quitarRoles.add(ur);
                }
            }
            for(UserRole ur : quitarRoles)
                userFromBd.getUserRole().remove(ur);

            /* Guardar registro */
            modificarUsuario(userFromBd);

            /* Preparar html */

            //Construyo un HashSet con los roles actuales
            for(UserRole ur : userFromBd.getUserRole())
                userRoles.add(ur.getRole());


            return "redirect:/usuarioEdicion/"+username+"?successfulChange=true";

        }

    @Transactional
    public void modificarUsuario(User user) {
        userRepository.save(user);
    }

}