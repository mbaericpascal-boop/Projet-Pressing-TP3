package com.tp.pressing.controller;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.data.domain.Page;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.tp.pressing.entity.Commande;
import com.tp.pressing.entity.Vetement;
import com.tp.pressing.service.CommandeService;
import com.tp.pressing.service.PdfService;
import com.tp.pressing.service.VetementService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/commandes")
public class WebController {

    private final VetementService vetementService;
    private final CommandeService commandeService;
    private final PdfService pdfService;

    public WebController(VetementService vetementService, CommandeService commandeService, PdfService pdfService) {
        this.vetementService = vetementService;
        this.commandeService = commandeService;
        this.pdfService = pdfService;
    }

    @InitBinder("commande")
    public void initBinderCommande(WebDataBinder binder) {
        // Sécurité pour empêcher la modification de champs sensibles via le formulaire
        binder.setDisallowedFields("id", "prixTotal", "statut", "dateCreation", "dateTerminaison");
    }

    // ====================== ACCUEIL & RECHERCHE VÊTEMENTS ======================
    @GetMapping("/accueil")
    public String home(
            @RequestParam(required = false) String couleur,
            @RequestParam(required = false) String matiere,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        Page<Vetement> vetementsPage = vetementService.filtrerEtTrierVetements(couleur, matiere, null, sortBy, "asc", page, 8);

        model.addAttribute("vetements", vetementsPage.getContent());
        model.addAttribute("page", vetementsPage);
        model.addAttribute("pageNumbers", IntStream.range(0, vetementsPage.getTotalPages()).boxed().collect(Collectors.toList()));
        model.addAttribute("filtreCouleur", couleur);
        model.addAttribute("filtreMatiere", matiere);

        return "index";
    }

    // ====================== GESTION DES COMMANDES ======================
    
    @PostMapping("/creer")
    public String creerCommande(@Valid @ModelAttribute("commande") Commande commande, BindingResult result) {
        if (result.hasErrors()) {
            return "redirect:/commandes/accueil?error=validation";
        }
        commandeService.ajouterCommande(commande);
        return "redirect:/commandes/historique?created=true";
    }

    @GetMapping("/historique")
    public String historique(Model model) {
        model.addAttribute("commandes", commandeService.getAllCommandes());
        return "historique";
    }

    @GetMapping("/{id}/valider")
    public String valider(@PathVariable Long id) {
        commandeService.validerCommande(id);
        return "redirect:/commandes/historique";
    }

    @GetMapping("/{id}/terminer")
    public String terminer(@PathVariable Long id) {
        commandeService.terminerCommande(id);
        return "redirect:/commandes/historique";
    }

    @GetMapping("/{id}/annuler")
    public String annuler(@PathVariable Long id) {
        commandeService.annulerCommande(id);
        return "redirect:/commandes/historique";
    }

    @GetMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        commandeService.supprimerCommande(id);
        return "redirect:/commandes/historique";
    }

    // ====================== GÉNÉRATION DE FACTURE PDF ======================
    
    @GetMapping("/{id}/facture")
    @ResponseBody
    public ResponseEntity<byte[]> voirFacture(@PathVariable Long id) {
        try {
            byte[] pdf = pdfService.genererFacture(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            // "inline" permet d'ouvrir le PDF dans le navigateur au lieu de le télécharger
            headers.setContentDisposition(ContentDisposition.inline().filename("facture_" + id + ".pdf").build());
            return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("❌ Erreur PDF : " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // ====================== NAVIGATION AUTRES ======================
    
   @GetMapping("/login")  // restera /commandes/login, donc on override avec le path complet
public String login() { return "login"; }

    @GetMapping("/vetements/ajouter")
    public String formVetement() { return "ajouter-vetement"; }
}