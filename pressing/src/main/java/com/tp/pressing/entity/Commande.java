package com.tp.pressing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "commandes")
public class Commande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du client est obligatoire")
    private String client;

    @Email(message = "L'email du client doit être valide")
    private String emailClient;

    @NotBlank(message = "Type obligatoire : EXPRESS, RAPIDE ou PROLONGEE")
    private String typeCommande;

    private double prixTotal;

    private String statut = "EN_ATTENTE";

    private LocalDateTime dateCreation;
    private LocalDateTime dateTerminaison;

    @Column(columnDefinition = "INTEGER DEFAULT 0")
    private Long vetementId;

    @Min(value = 1, message = "Au moins 1 habit")
    @Max(value = 10, message = "Maximum 10 habits par commande")
    private int nombreHabits;

    // Relation temporaire (peut être améliorée plus tard en @ManyToMany)
    @Transient
    private List<Vetement> vetements = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────
    // Méthodes métier
    // ─────────────────────────────────────────────────────────────
    public void calculerPrix() {
        double prixBase = nombreHabits * 500.0;
        double supplement = switch (typeCommande.toUpperCase()) {
            case "EXPRESS"   -> 4000.0;
            case "RAPIDE"    -> 3500.0;
            case "PROLONGEE" -> 5000.0;
            default -> 0.0;
        };
        this.prixTotal = prixBase + supplement;
        this.statut = "EN_ATTENTE";
        this.dateCreation = LocalDateTime.now();
    }

    public void valider() {
        this.statut = "EN_COURS";
    }

    public void terminer() {
        this.statut = "TERMINEE";
        this.dateTerminaison = LocalDateTime.now();
    }

    public void annuler() {
        this.statut = "ANNULEE";
        this.dateTerminaison = LocalDateTime.now();
    }

    // ─────────────────────────────────────────────────────────────
    // Getters & Setters
    // ─────────────────────────────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getClient() { return client; }
    public void setClient(String client) { this.client = client; }

    public String getEmailClient() { return emailClient; }
    public void setEmailClient(String emailClient) { this.emailClient = emailClient; }

    public String getTypeCommande() { return typeCommande; }
    public void setTypeCommande(String typeCommande) { this.typeCommande = typeCommande; }

    public double getPrixTotal() { return prixTotal; }
    public void setPrixTotal(double prixTotal) { this.prixTotal = prixTotal; }

    public String getStatut() { return statut; }
    public void setStatut(String statut) { this.statut = statut; }

    public LocalDateTime getDateCreation() { return dateCreation; }
    public void setDateCreation(LocalDateTime dateCreation) { this.dateCreation = dateCreation; }

    public LocalDateTime getDateTerminaison() { return dateTerminaison; }
    public void setDateTerminaison(LocalDateTime dateTerminaison) { this.dateTerminaison = dateTerminaison; }

    public List<Vetement> getVetements() { return vetements; }
    public void setVetements(List<Vetement> vetements) { this.vetements = vetements; }

    public int getNombreHabits() { return nombreHabits; }
    public void setNombreHabits(int nombreHabits) { this.nombreHabits = nombreHabits; }

    public Long getVetementId() { return vetementId; }
    public void setVetementId(Long vetementId) { this.vetementId = vetementId; }
}