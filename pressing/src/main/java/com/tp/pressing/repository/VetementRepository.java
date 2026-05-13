package com.tp.pressing.repository;

import com.tp.pressing.entity.Vetement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VetementRepository extends JpaRepository<Vetement, Long> {
    Page<Vetement> findByCouleurContainingIgnoreCase(String couleur, Pageable pageable);
    Page<Vetement> findByMatiereContainingIgnoreCase(String matiere, Pageable pageable);
    Page<Vetement> findByCategorieContainingIgnoreCase(String categorie, Pageable pageable);
    Page<Vetement> findByPotentielColorationBetween(int min, int max, Pageable pageable);
    Page<Vetement> findAll(Pageable pageable);
    Page<Vetement> findByNomContainingIgnoreCase(String nom, Pageable pageable);
    Page<Vetement> findByNomProprietaireContainingIgnoreCase(String nomProprietaire, Pageable pageable);
    Page<Vetement> findByImageNameContainingIgnoreCase(String imageName, Pageable pageable);
}