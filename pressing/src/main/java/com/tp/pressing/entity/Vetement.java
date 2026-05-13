package com.tp.pressing.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "vetements")
public class Vetement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le nom du vêtement est obligatoire")
    private String nom; // Exemple: "Robe rose"

    @NotBlank(message = "Le nom du propriétaire est obligatoire")
    private String nomProprietaire; // <-- AJOUTÉ : Pour le tri par client (ex: "Jean")

    @NotBlank(message = "La matière est obligatoire")
    private String matiere;

    @Min(value = 1, message = "Le potentiel doit être au moins 1")
    @Max(value = 10, message = "Le potentiel ne peut pas dépasser 10")
    private int potentielColoration;

    private String imageName; // <-- CHANGÉ : "photo" devient "imageName" pour matcher le HTML

    private String couleur;
    
    private String categorie;

    // Logique de calcul automatique
    public void calculerCategorie() {
        String typeMatiere = getTypeMatiere();
        String niveau = getNiveauColoration();
        this.categorie = niveau + "_" + typeMatiere;
    }

    private String getTypeMatiere() {
        String m = matiere.toLowerCase().trim();
        if (m.contains("coton") || m.contains("lin") || m.contains("soie") || m.contains("chanvre")) return "NATURELLE";
        else if (m.contains("polyester") || m.contains("nylon") || m.contains("acrylique")) return "SYNTHETIQUE";
        else return "MIXTE";
    }

    private String getNiveauColoration() {
        if (potentielColoration >= 8) return "EXCELLENT";
        else if (potentielColoration >= 5) return "MOYEN";
        else return "DIFFICILE";
    }

    // ================= GETTERS ET SETTERS =================

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getNomProprietaire() { return nomProprietaire; }
    public void setNomProprietaire(String nomProprietaire) { this.nomProprietaire = nomProprietaire; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public int getPotentielColoration() { return potentielColoration; }
    public void setPotentielColoration(int potentielColoration) { this.potentielColoration = potentielColoration; }

    public String getImageName() { return imageName; }
    public void setImageName(String imageName) { this.imageName = imageName; }

    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}