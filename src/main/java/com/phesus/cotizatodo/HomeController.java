package com.phesus.cotizatodo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Proyecto Cotizatodo
 * User: octavioruizcastillo
 * Date: 29/01/15
 * Time: 20:30
 */
@Controller
public class HomeController {
    @RequestMapping("/")
    public String home(Model model) {


        return "home";
    }

    @RequestMapping("/login")
    public String login(Model model) {
        model.addAttribute("user", new User());

        return "login";
    }
}
