package com.phesus.cotizatodo;

import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * Proyecto Cotizatodo
 * User: octavioruizcastillo
 * Date: 29/01/15
 * Time: 20:30
 */
@Controller
public class HomeController implements ErrorController {
    @RequestMapping("/")
    public String home(Model model) {


        return "home";
    }

    @RequestMapping("/login")
    public String login(HttpServletRequest request, Model model) {

        String referrer = request.getHeader("Referer");
        request.getSession().setAttribute("url_prior_login", referrer);
        model.addAttribute("user", new User());

        return "login";
    }

    // Error page
    @RequestMapping("/error")
    public String error(HttpServletRequest request, Model model) {
        Integer errorCode = (Integer) request.getAttribute("javax.servlet.error.status_code");
        model.addAttribute("errorCode", errorCode);
        if(errorCode == 404) {
            return "404";
        } else if(errorCode == 403) {
            return "403";
        } else {
            Throwable throwable = (Throwable) request.getAttribute("javax.servlet.error.exception");
            if (throwable == null) throwable = new Throwable("...");
            model.addAttribute("throwable", throwable);
            return "error";
        }
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping("/403")
    public String forbidden(HttpServletRequest request, Model model) {
        return "403";
    }
}
