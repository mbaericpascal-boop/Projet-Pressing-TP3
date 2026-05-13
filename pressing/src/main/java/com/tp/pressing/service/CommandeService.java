package com.tp.pressing.service;

import java.time.LocalDateTime;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import com.tp.pressing.entity.Commande;
import com.tp.pressing.repository.CommandeRepository;

@Service
public class CommandeService {

    private final CommandeRepository commandeRepository;
    private final EmailService emailService;

    public CommandeService(CommandeRepository commandeRepository, EmailService emailService) {
        this.commandeRepository = commandeRepository;
        this.emailService = emailService;
    }

    /**
     * Crée une commande, calcule le prix, définit le statut initial 
     * et envoie l'email de confirmation.
     */
    public Commande ajouterCommande(Commande commande) {
        commande.calculerPrix(); // Utilise ta logique métier (Express, Rapide, etc.)
        commande.setStatut("EN_ATTENTE");
        commande.setDateCreation(LocalDateTime.now());
        
        Commande sauvegardee = commandeRepository.save(commande);

        // Envoi de la notification de confirmation
        if (sauvegardee.getEmailClient() != null && !sauvegardee.getEmailClient().isEmpty()) {
            try {
                emailService.envoyerNotificationCommande(
                    sauvegardee.getEmailClient(),
                    sauvegardee.getClient(),
                    sauvegardee.getTypeCommande(),
                    sauvegardee.getPrixTotal(),
                    sauvegardee.getId()
                );
            } catch (Exception e) {
                System.err.println("⚠️ Erreur lors de l'envoi de l'email de confirmation : " + e.getMessage());
            }
        }
        return sauvegardee;
    }
     /**
 * Valide la commande : EN_ATTENTE → EN_COURS uniquement.
 */
public Commande validerCommande(Long id) {
    Commande c = getCommandeById(id);

    if (!c.getStatut().equals("EN_ATTENTE")) {
        throw new RuntimeException(
            "Impossible de valider la commande #" + id +
            " — statut actuel : " + c.getStatut() +
            " (attendu : EN_ATTENTE)"
        );
    }

    c.setStatut("EN_COURS");
    return commandeRepository.save(c);
}

/**
 * Termine la commande : EN_COURS → TERMINEE uniquement.
 */
public Commande terminerCommande(Long id) {
    Commande c = getCommandeById(id);

    if (!c.getStatut().equals("EN_COURS")) {
        throw new RuntimeException(
            "Impossible de terminer la commande #" + id +
            " — statut actuel : " + c.getStatut() +
            " (attendu : EN_COURS)"
        );
    }

    c.setStatut("TERMINEE");
    c.setDateTerminaison(LocalDateTime.now());

    Commande sauvegardee = commandeRepository.save(c);

    if (sauvegardee.getEmailClient() != null && !sauvegardee.getEmailClient().isEmpty()) {
        try {
            emailService.envoyerNotificationPret(
                sauvegardee.getEmailClient(),
                sauvegardee.getClient(),
                sauvegardee.getTypeCommande()
            );
            emailService.envoyerRecuCommande(
                sauvegardee.getEmailClient(),
                sauvegardee.getClient(),
                sauvegardee.getId(),
                sauvegardee.getTypeCommande(),
                sauvegardee.getNombreHabits(),
                sauvegardee.getPrixTotal(),
                sauvegardee.getDateCreation(),
                sauvegardee.getDateTerminaison()
            );
        } catch (Exception e) {
            System.err.println("⚠️ Erreur envoi reçu final : " + e.getMessage());
        }
    }
    return sauvegardee;
}

/**
 * Annule la commande : impossible si déjà TERMINEE.
 */
public Commande annulerCommande(Long id) {
    Commande c = getCommandeById(id);

    if (c.getStatut().equals("TERMINEE")) {
        throw new RuntimeException(
            "Impossible d'annuler la commande #" + id +
            " — elle est déjà terminée."
        );
    }

    if (c.getStatut().equals("ANNULEE")) {
        throw new RuntimeException(
            "La commande #" + id + " est déjà annulée."
        );
    }

    c.setStatut("ANNULEE");
    c.setDateTerminaison(LocalDateTime.now());
    return commandeRepository.save(c);
}

    public Commande getCommandeById(Long id) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void supprimerCommande(Long id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supprimerCommande'");
    }

    public @Nullable Object getAllCommandes() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getAllCommandes'");
    }

    public Page<Commande> getCommandesPaginees(int page, int size) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCommandesPaginees'");
    }
}

   