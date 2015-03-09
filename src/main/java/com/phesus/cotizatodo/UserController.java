package com.phesus.cotizatodo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.persistence.EntityManager;
import java.security.Principal;
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

    @RequestMapping("/user")
    public String usuarioVer(
            @RequestParam(required = false, defaultValue = "false") Boolean successfulChange,
            @RequestParam(required = false, defaultValue = "false") Boolean unsuccessfulChange,
            @RequestParam(required = false, defaultValue = "false") Boolean wrongPassword,
            Model model,
            Principal principal
    ) {

        String username = principal.getName();
        User user = userRepository.findByUsername(username);

        //Construyo un HashSet con los roles actuales
        HashSet userRoles = new HashSet();
        for (UserRole ur : user.getUserRole())
            userRoles.add(ur.getRole());

        model.addAttribute("selectedMenu", "usuarios");
        model.addAttribute("user", user);
        model.addAttribute("roleTypes", Roles.values());
        model.addAttribute("userRoles", userRoles);
        model.addAttribute("successfulChange", successfulChange);
        model.addAttribute("unsuccessfulChange", unsuccessfulChange);
        model.addAttribute("wrongPassword", wrongPassword);

        return "user_profile";
    }

    /**
     * Editar usuario. 2do Paso POST
     * @param user
     * @param model
     * @return
     */
    @RequestMapping(value = "/user/update", method = RequestMethod.POST)
    public String usuarioEdicionPost(
            @ModelAttribute User user,
            Model model) {

        User userFromBd = userRepository.findByUsername(user.getUsername());

        //Construyo un HashSet con los roles actuales
        //HashSet userRoles = (HashSet) user.getUserRole();

        /* Actualizar campos modificados */
        //userFromBd.setEnabled( user.isEnabled() );
        userFromBd.setFullname(user.getFullname());
        userFromBd.setCity(user.getCity());
        userFromBd.setCountry(user.getCountry());
        userFromBd.setEmail(user.getEmail());


        /* Guardar registro */
        modificarUsuario(userFromBd);

        return "redirect:/user?successfulChange=true";

    }

    @RequestMapping(value = "/user/updatePassword", method = RequestMethod.POST)
    public String updatePassword(
            @RequestParam("oldPass") String oldPass,
            @RequestParam("newPass") String newPass,
            @RequestParam("reNewPass") String reNewPass,
            Model model,
            Principal principal
    ) {
        /* Load user */
        String username = principal.getName();
        if(username == null && username.length() == 0) return "redirect:/user?unsuccessfulChange=true";
        User userFromBd = userRepository.findByUsername(username);

        /* Verify that oldPass is correct */
        if(!passwordEncoder.matches(oldPass, userFromBd.getPassword())) return "redirect:/user?unsuccessfulChange=true&&wrongPassword=true";

        /* Verify that newPass and reNewPass are the same */
        if(!newPass.equals(reNewPass)) return "redirect:/user?unsuccessfulChange=true";

        /* Encode and update new password */
        if(newPass != null && !newPass.isEmpty())
            userFromBd.setPassword( passwordEncoder.encode(newPass) );
        else
            return "redirect:/user?unsuccessfulChange=true";

        /* If reached to this way, save changes */
        modificarUsuario(userFromBd);

        return "redirect:/user?successfulChange=true";
    }

    @Transactional
    public void modificarUsuario(User user) {
        userRepository.save(user);
    }

}