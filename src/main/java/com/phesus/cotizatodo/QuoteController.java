package com.phesus.cotizatodo;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Proyecto cotizatodo
 * User: octavioruizcastillo
 * Date: 03/02/15
 * Time: 15:54
 */
@Controller
public class QuoteController {

    @RequestMapping("/quotes")
    public String quotes(Model model) {
        return "quotes";
    }

    @RequestMapping("/quotes/new")
        public String newQuote(Model model)
        {
            model.addAttribute("quote", new Quote());
            return "quote_new";
        }
}
