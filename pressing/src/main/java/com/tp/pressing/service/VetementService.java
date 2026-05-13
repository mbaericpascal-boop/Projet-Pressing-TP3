package com.tp.pressing.service;

import com.tp.pressing.entity.Vetement;
import com.tp.pressing.repository.VetementRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Service
public class VetementService {

    private final VetementRepository vetementRepository;

    @Value("${file.upload-dir}")
    private String uploadDir;

    public VetementService(VetementRepository vetementRepository) {
        this.vetementRepository = vetementRepository;
    }

    public Vetement ajouterVetement(Vetement vetement) {
        vetement.calculerCategorie(); 
        return vetementRepository.save(vetement);
    }

    public List<Vetement> getAllVetements() {
        return vetementRepository.findAll();
    }

    public Page<Vetement> getVetementsPagines(int page, int size) {
        return vetementRepository.findAll(PageRequest.of(page, size));
    }

    public Page<Vetement> rechercherParCouleur(String couleur, int page, int size) {
        return vetementRepository.findByCouleurContainingIgnoreCase(couleur, PageRequest.of(page, size));
    }

    public Page<Vetement> rechercherParMatiere(String matiere, int page, int size) {
        return vetementRepository.findByMatiereContainingIgnoreCase(matiere, PageRequest.of(page, size));
    }

    public Page<Vetement> rechercherParPotentiel(int min, int max, int page, int size) {
        return vetementRepository.findByPotentielColorationBetween(min, max, PageRequest.of(page, size));
    }

    public Page<Vetement> rechercherParNom(String nom, int page, int size) {
        return vetementRepository.findByNomContainingIgnoreCase(nom, PageRequest.of(page, size));
    }

    public Vetement getVetementById(Long id) {
        return vetementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Vêtement introuvable ID: " + id));
    }

    /**
     * Système de filtrage dynamique (7 paramètres pour compatibilité contrôleurs).
     */
    public Page<Vetement> filtrerEtTrierVetements(String couleur, String matiere, String categorie,
                                                  String sortBy, String direction,
                                                  int page, int size) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);

        // Si le champ couleur est utilisé pour chercher un nom de client
        if (couleur != null && !couleur.isBlank()) {
            Page<Vetement> parProprietaire = vetementRepository.findByNomProprietaireContainingIgnoreCase(couleur, pageable);
            if (parProprietaire.hasContent()) {
                return parProprietaire;
            }
            return vetementRepository.findByCouleurContainingIgnoreCase(couleur, pageable);
        }

        if (categorie != null && !categorie.isBlank()) {
            return vetementRepository.findByCategorieContainingIgnoreCase(categorie, pageable);
        }
        
        if (matiere != null && !matiere.isBlank()) {
            return vetementRepository.findByMatiereContainingIgnoreCase(matiere, pageable);
        }

        return vetementRepository.findAll(pageable);
    }

    public Vetement sauvegarderPhoto(Long id, MultipartFile file) throws IOException {
        Vetement v = getVetementById(id);
        Path dossier = Paths.get(uploadDir);
        if (!Files.exists(dossier)) {
            Files.createDirectories(dossier);
        }
        String original = file.getOriginalFilename();
        String safeName = (original != null) ? original.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_") : "image_" + id;
        String nomFichier = id + "_" + safeName;
        Path chemin = dossier.resolve(nomFichier);
        Files.copy(file.getInputStream(), chemin, StandardCopyOption.REPLACE_EXISTING);
        v.setImageName(nomFichier);
        return vetementRepository.save(v);
    }
}