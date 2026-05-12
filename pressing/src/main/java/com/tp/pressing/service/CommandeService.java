package com.tp.pressing.service;

import com.tp.pressing.entity.Commande;
import com.tp.pressing.repository.CommandeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

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
     * Valide la commande : passe de EN_ATTENTE à EN_COURS.
     */
    public Commande validerCommande(Long id) {
        Commande c = getCommandeById(id);
        c.setStatut("EN_COURS");
        return commandeRepository.save(c);
    }

    /**
     * Termine la commande : passe à TERMINEE, enregistre la date de fin,
     * et envoie l'email de prêt ainsi que le REÇU PDF.
     */
    public Commande terminerCommande(Long id) {
        Commande c = getCommandeById(id);
        c.setStatut("TERMINEE");
        c.setDateTerminaison(LocalDateTime.now());
        
        Commande sauvegardee = commandeRepository.save(c);

        if (sauvegardee.getEmailClient() != null && !sauvegardee.getEmailClient().isEmpty()) {
            try {
                // 1. Notification simple de disponibilité
                emailService.envoyerNotificationPret(
                    sauvegardee.getEmailClient(), 
                    sauvegardee.getClient(), 
                    sauvegardee.getTypeCommande()
                );
                
                // 2. Envoi du Reçu Final avec le PDF généré
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
                System.err.println("⚠️ Erreur lors de l'envoi du reçu final : " + e.getMessage());
            }
        }
        return sauvegardee;
    }

    /**
     * Annule la commande.
     */
    public Commande annulerCommande(Long id) {
        Commande c = getCommandeById(id);
        c.setStatut("ANNULEE");
        c.setDateTerminaison(LocalDateTime.now());
        return commandeRepository.save(c);
    }

    // --- Méthodes de lecture et gestion ---

    public List<Commande> getAllCommandes() {
        return commandeRepository.findAll();
    }

    public Page<Commande> getCommandesPaginees(int page, int size) {
        return commandeRepository.findAll(PageRequest.of(page, size));
    }

    public Commande getCommandeById(Long id) {
        return commandeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commande introuvable avec l'ID : " + id));
    }

    public void supprimerCommande(Long id) {
        commandeRepository.deleteById(id);
    }
}