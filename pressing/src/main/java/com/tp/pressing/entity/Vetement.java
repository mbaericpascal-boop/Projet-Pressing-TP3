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
    private String nom;

    @NotBlank(message = "La matière est obligatoire")
    private String matiere;

    @Min(value = 1, message = "Le potentiel doit être au moins 1")
    @Max(value = 10, message = "Le potentiel ne peut pas dépasser 10")
    private int potentielColoration;

    private String photo;   // ← URL de la photo ou chemin

    private String couleur;
    public String getCouleur() { return couleur; }
    public void setCouleur(String couleur) { this.couleur = couleur; }

    private String categorie;

    public void calculerCategorie() {
        String typeMatiere = getTypeMatiere();
        String niveau = getNiveauColoration();
        this.categorie = niveau + "_" + typeMatiere;
    }

    private String getTypeMatiere() {
        String m = matiere.toLowerCase().trim();
        if (m.contains("coton") || m.contains("lin") || m.contains("soie") || m.contains("chanvre")) {
            return "NATURELLE";
        } else if (m.contains("polyester") || m.contains("nylon") || m.contains("acrylique")) {
            return "SYNTHETIQUE";
        } else {
            return "MIXTE";
        }
    }

    private String getNiveauColoration() {
        if (potentielColoration >= 8) return "EXCELLENT";
        else if (potentielColoration >= 5) return "MOYEN";
        else return "DIFFICILE";
    }

    // Getters et Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getMatiere() { return matiere; }
    public void setMatiere(String matiere) { this.matiere = matiere; }

    public int getPotentielColoration() { return potentielColoration; }
    public void setPotentielColoration(int potentielColoration) { this.potentielColoration = potentielColoration; }

    public String getPhoto() { return photo; }
    public void setPhoto(String photo) { this.photo = photo; }

    public String getCategorie() { return categorie; }
    public void setCategorie(String categorie) { this.categorie = categorie; }
}