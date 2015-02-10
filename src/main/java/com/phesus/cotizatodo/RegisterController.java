package com.phesus.cotizatodo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 07/02/15
 * Time: 15:22
 */
@Controller
public class RegisterController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public String usuarioNuevoPost(
            @ModelAttribute User user,
            Model model) {

        // Encriptar password //

        user.setPassword( passwordEncoder.encode(user.getPassword()) );

        // Asignar roles //

        user.addUserRole("ROLE_USER");

        // Guardar registro //

        userRepository.save(user);

        return "redirect:/login";

    }
    /*
    @RequestMapping(value = "/usuarioCheckUsername")
    public @ResponseBody String usuarioCheckUsername(@RequestParam("username") String username) {

        if(username == null)
            return "nombre inválido";
        else
            return String.valueOf(!userRepository.exists(username));
    }
      */
}
