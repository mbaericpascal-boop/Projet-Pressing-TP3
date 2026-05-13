package com.tp.pressing.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Intercepte toutes les RuntimeException (commande introuvable, etc.)
     */
    @ExceptionHandler(RuntimeException.class)
    public String gererRuntimeException(RuntimeException ex, Model model) {
        model.addAttribute("erreurMessage", "Une erreur s'est produite.");
        model.addAttribute("erreurDetail", ex.getMessage());
        return "error";
    }

    /**
     * Intercepte toutes les autres exceptions non prévues
     */
    @ExceptionHandler(Exception.class)
    public String gererException(Exception ex, Model model) {
        model.addAttribute("erreurMessage", "Erreur inattendue du serveur.");
        model.addAttribute("erreurDetail", ex.getMessage());
        return "error";
    }
}