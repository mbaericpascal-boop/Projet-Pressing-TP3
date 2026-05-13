package com.tp.pressing.controller;

import com.tp.pressing.entity.Commande;
import com.tp.pressing.entity.Vetement;
import com.tp.pressing.service.CommandeService;
import com.tp.pressing.service.PdfService;
import com.tp.pressing.service.VetementService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PressingController {

    private final CommandeService commandeService;
    private final VetementService vetementService;
    private final PdfService pdfService;

    public PressingController(CommandeService commandeService,
                               VetementService vetementService,
                               PdfService pdfService) {
        this.commandeService = commandeService;
        this.vetementService = vetementService;
        this.pdfService = pdfService;
    }

    // ════════════════════════════════════════════════════════════
    //  COMMANDES
    // ════════════════════════════════════════════════════════════

    /** Créer une commande — envoie email confirmation + PDF automatiquement
     *  Body JSON exemple :
     *  {
     *    "client": "Jean Dupont",
     *    "emailClient": "jean@gmail.com",
     *    "typeCommande": "EXPRESS",
     *    "nombreHabits": 3
     *  }
     */
    @PostMapping("/commandes")
    public Commande ajouterCommande(@Valid @RequestBody Commande commande) {
        return commandeService.ajouterCommande(commande);
    }

    @GetMapping("/commandes")
    public List<Commande> listerCommandes() {
        return (List<Commande>) commandeService.getAllCommandes();
    }

    @GetMapping("/commandes/page")
    public Page<Commande> commandesPaginees(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return commandeService.getCommandesPaginees(page, size);
    }

    @GetMapping("/commandes/{id}")
    public Commande getCommande(@PathVariable Long id) {
        return commandeService.getCommandeById(id);
    }

    /** Valider une commande : EN_ATTENTE → EN_COURS */
    @PutMapping("/commandes/{id}/valider")
    public Commande validerCommande(@PathVariable Long id) {
        return commandeService.validerCommande(id);
    }

    /** Terminer une commande : EN_COURS → TERMINEE
     *  Déclenche automatiquement :
     *  - Email "vêtements prêts"
     *  - Email reçu final avec facture PDF en pièce jointe
     */
    @PutMapping("/commandes/{id}/terminer")
    public Commande terminerCommande(@PathVariable Long id) {
        return commandeService.terminerCommande(id);
    }

    /** Annuler une commande */
    @PutMapping("/commandes/{id}/annuler")
    public Commande annulerCommande(@PathVariable Long id) {
        return commandeService.annulerCommande(id);
    }

    @DeleteMapping("/commandes/{id}")
    public String supprimerCommande(@PathVariable Long id) {
        commandeService.supprimerCommande(id);
        return "Commande " + id + " supprimée avec succès";
    }

    /** Télécharger la facture PDF d'une commande (avec signature numérique)
     *  Testable directement dans Postman : GET /api/commandes/{id}/facture
     *  → cliquer sur "Save Response" pour télécharger le PDF
     */
    @GetMapping("/commandes/{id}/facture")
    public ResponseEntity<byte[]> telechargerFacture(@PathVariable Long id) {
        try {
            byte[] pdf = pdfService.genererFacture(id);
            String nomFichier = "facture_" + String.format("%05d", id) + ".pdf";
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "attachment; filename=\"" + nomFichier + "\"")
                    .header("Content-Length", String.valueOf(pdf.length))
                    .body(pdf);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ════════════════════════════════════════════════════════════
    //  VÊTEMENTS
    // ════════════════════════════════════════════════════════════

    @PostMapping("/vetements")
    public Vetement ajouterVetement(@RequestBody Vetement vetement) {
        vetement.calculerCategorie();
        return vetementService.ajouterVetement(vetement);
    }

    @GetMapping("/vetements")
    public List<Vetement> listerVetements() {
        return vetementService.getAllVetements();
    }

    @GetMapping("/vetements/page")
    public Page<Vetement> vetementsPagines(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return vetementService.getVetementsPagines(page, size);
    }

    @PostMapping("/vetements/{id}/photo")
    public Vetement uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        return vetementService.sauvegarderPhoto(id, file);
    }

    @GetMapping("/vetements/recherche/couleur")
    public Page<Vetement> rechercherParCouleur(
            @RequestParam String couleur,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return vetementService.rechercherParCouleur(couleur, page, size);
    }

    @GetMapping("/vetements/recherche/matiere")
    public Page<Vetement> rechercherParMatiere(
            @RequestParam String matiere,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return vetementService.rechercherParMatiere(matiere, page, size);
    }

    @GetMapping("/vetements/recherche/nom")
    public Page<Vetement> rechercherParNom(
            @RequestParam String nom,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return vetementService.rechercherParNom(nom, page, size);
    }

    @GetMapping("/vetements/recherche/potentiel")
    public Page<Vetement> rechercherParPotentiel(
            @RequestParam(defaultValue = "1") int min,
            @RequestParam(defaultValue = "10") int max,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return vetementService.rechercherParPotentiel(min, max, page, size);
    }

    @GetMapping("/vetements/filtrer")
    public Page<Vetement> filtrerVetements(
            @RequestParam(required = false) String couleur,
            @RequestParam(required = false) String matiere,
            @RequestParam(required = false) String categorie,
            @RequestParam(defaultValue = "nom") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return vetementService.filtrerEtTrierVetements(
                couleur, matiere, categorie, sortBy, direction, page, size);
    }
}