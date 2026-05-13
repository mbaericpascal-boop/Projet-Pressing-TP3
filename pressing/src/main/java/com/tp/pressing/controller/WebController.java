package com.tp.pressing.controller;

import com.tp.pressing.entity.Commande;
import com.tp.pressing.entity.Vetement;
import com.tp.pressing.service.*;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        binder.setDisallowedFields("id", "prixTotal", "statut", "dateCreation", "dateTerminaison");
    }

    // ====================== ACCUEIL & RECHERCHE ======================
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
        model.addAttribute("commande", new Commande()); 
        return "index";
    }

    // ====================== GESTION DES VÊTEMENTS ======================
    
    @GetMapping("/vetements/ajouter")
    public String formVetement() { 
        return "ajouter-vetement"; 
    }

    @PostMapping("/vetements/ajouter")
    public String ajouterVetement(
            @RequestParam("nom") String nom,
            @RequestParam("couleur") String couleur,
            @RequestParam("matiere") String matiere,
            @RequestParam(value = "potentielColoration", defaultValue = "5") int potentiel,
            @RequestParam("file") MultipartFile file,
            RedirectAttributes redirectAttributes) {
        
        try {
            Vetement v = new Vetement();
            v.setNom(nom);
            v.setCouleur(couleur);
            v.setMatiere(matiere);
            v.setPotentielColoration(potentiel);
            
            // Correction pour l'erreur 500 : On définit un propriétaire par défaut
            v.setNomProprietaire("Stock Magasin"); 
            
            v.calculerCategorie();

            // 1. Sauvegarde en BDD pour générer l'ID
            Vetement saved = vetementService.ajouterVetement(v);
            
            // 2. Sauvegarde de la photo si elle existe
            if (file != null && !file.isEmpty()) {
                vetementService.sauvegarderPhoto(saved.getId(), file);
            }
            
            redirectAttributes.addFlashAttribute("messageSucces", "Article ajouté au catalogue !");
            return "redirect:/commandes/accueil";
            
        } catch (Exception e) {
            // Log de l'erreur dans le terminal VS Code
            e.printStackTrace(); 
            redirectAttributes.addFlashAttribute("error", "Erreur lors de l'enregistrement : " + e.getMessage());
            return "redirect:/commandes/vetements/ajouter";
        }
    }

    @GetMapping("/catalogue")
    public String afficherCatalogue(Model model) {
        List<Vetement> tousLesVetements = vetementService.getAllVetements();
        Map<String, List<Vetement>> catalogueParClient = tousLesVetements.stream()
                .collect(Collectors.groupingBy(v -> 
                    (v.getNomProprietaire() != null && !v.getNomProprietaire().isBlank()) 
                    ? v.getNomProprietaire() : "Stock Magasin"));
                
        model.addAttribute("catalogue", catalogueParClient);
        return "catalogue";
    }

    // ====================== GESTION DES COMMANDES ======================
    
    @PostMapping("/creer")
    public String creerCommande(@Valid @ModelAttribute("commande") Commande commande, BindingResult result) {
        if (result.hasErrors()) return "redirect:/commandes/accueil?error=validation";
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

    @GetMapping("/{id}/supprimer")
    public String supprimer(@PathVariable Long id) {
        commandeService.supprimerCommande(id);
        return "redirect:/commandes/historique";
    }

    @GetMapping("/login")
    public String login() { return "login"; }
}