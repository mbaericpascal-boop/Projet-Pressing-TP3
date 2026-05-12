package com.tp.pressing.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final PdfService pdfService;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender, PdfService pdfService) {
        this.mailSender = mailSender;
        this.pdfService = pdfService;
    }

    /**
     * Notification simple lors de la création d'une commande.
     */
    public void envoyerNotificationCommande(String to, String nomClient, String type, Double prix, Long id) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Confirmation de votre commande Pressing #" + id);
        message.setText("Bonjour " + nomClient + ",\n\n" +
                "Votre commande de type [" + type + "] a bien été enregistrée.\n" +
                "Montant total à régler : " + prix + " FCFA.\n\n" +
                "Merci de votre confiance !");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Échec envoi notification création: " + e.getMessage());
        }
    }

    /**
     * Notification envoyée quand les habits sont prêts.
     */
    public void envoyerNotificationPret(String to, String nomClient, String type) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Votre linge est prêt !");
        message.setText("Cher(e) " + nomClient + ",\n\n" +
                "Bonne nouvelle ! Vos vêtements (" + type + ") sont prêts à être récupérés.\n" +
                "À très bientôt dans notre boutique !");
        
        try {
            mailSender.send(message);
        } catch (Exception e) {
            System.err.println("❌ Échec envoi notification prêt: " + e.getMessage());
        }
    }

    /**
     * ENVOI DU REÇU FINAL AVEC PIÈCE JOINTE PDF.
     * C'est la méthode la plus complexe qui assure le côté pro du projet.
     */
    public void envoyerRecuCommande(String to, String nomClient, Long id, String type, 
                                    Integer nbHabits, Double prix, LocalDateTime dateC, LocalDateTime dateT) {
        
        MimeMessage message = mailSender.createMimeMessage();
        
        try {
            // MimeMessageHelper avec 'true' pour indiquer qu'on veut du multipart (pièce jointe)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Votre reçu final - Pressing #" + id);
            
            String dateFmt = dateT != null ? dateT.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "N/A";
            
            String corpsMail = "Bonjour " + nomClient + ",\n\n" +
                    "Veuillez trouver ci-joint votre reçu détaillé pour la commande #" + id + ".\n\n" +
                    "Détails :\n" +
                    "- Type : " + type + "\n" +
                    "- Nombre d'articles : " + nbHabits + "\n" +
                    "- Prix total : " + prix + " FCFA\n" +
                    "- Terminée le : " + dateFmt + "\n\n" +
                    "Cordialement,\nL'équipe Professional Pressing.";
            
            helper.setText(corpsMail);

            // Génération du PDF à la volée
            byte[] pdfBytes = pdfService.genererFacture(id);
            
            // Ajout de la pièce jointe
            helper.addAttachment("Recu_Pressing_" + id + ".pdf", new ByteArrayResource(pdfBytes));

            mailSender.send(message);
            System.out.println("✅ Reçu PDF envoyé avec succès à " + to);

        } catch (MessagingException e) {
            System.err.println("❌ Erreur technique lors de la préparation du mail PDF: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("❌ Erreur générale envoi reçu: " + e.getMessage());
        }
    }
}