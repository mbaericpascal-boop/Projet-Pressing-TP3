package com.tp.pressing.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.LineSeparator; // Import pour la ligne
import com.tp.pressing.entity.Commande;
import com.tp.pressing.repository.CommandeRepository;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest; // Import pour le Hash
import java.time.format.DateTimeFormatter;
import java.util.Base64; // Import pour l'affichage du Hash

@Service
public class PdfService {

    private final CommandeRepository commandeRepository;

    public PdfService(CommandeRepository commandeRepository) {
        this.commandeRepository = commandeRepository;
    }

    public byte[] genererFacture(Long idCommande) throws Exception {
        Commande commande = commandeRepository.findById(idCommande)
                .orElseThrow(() -> new Exception("Commande introuvable pour le PDF"));

        Document document = new Document();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        
        PdfWriter.getInstance(document, out);
        document.open();

        // --- Tes styles existants ---
        Font fontTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 20, BaseColor.DARK_GRAY);
        Font fontSousTitre = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, BaseColor.GRAY);
        Font fontNormal = FontFactory.getFont(FontFactory.HELVETICA, 12, BaseColor.BLACK);
        Font fontHeaderTable = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, BaseColor.WHITE);
        Font fontSignature = FontFactory.getFont(FontFactory.COURIER, 9, BaseColor.DARK_GRAY);

        // --- Ton en-tête ---
        Paragraph titre = new Paragraph("PROFESSIONAL PRESSING", fontTitre);
        titre.setAlignment(Element.ALIGN_CENTER);
        document.add(titre);

        Paragraph adresse = new Paragraph("Services de Nettoyage Haute Qualité\nYaoundé, Cameroun\nEmail: contact@pressing-pro.com", fontNormal);
        adresse.setAlignment(Element.ALIGN_CENTER);
        document.add(adresse);
        document.add(new Paragraph("\n"));

        // --- Tes infos client ---
        document.add(new Paragraph("REÇU DE COMMANDE #" + commande.getId(), fontSousTitre));
        document.add(new Paragraph("Date: " + commande.getDateCreation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")), fontNormal));
        document.add(new Paragraph("Client: " + commande.getClient(), fontNormal));
        document.add(new Paragraph("Email: " + (commande.getEmailClient() != null ? commande.getEmailClient() : "N/A"), fontNormal));
        document.add(new Paragraph("\n"));

        // --- Ton tableau de données ---
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        String[] headers = {"Description", "Quantité", "Prix Total"};
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fontHeaderTable));
            cell.setBackgroundColor(BaseColor.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
        table.addCell(new Phrase("Lavage " + commande.getTypeCommande(), fontNormal));
        table.addCell(new Phrase(String.valueOf(commande.getNombreHabits()), fontNormal));
        table.addCell(new Phrase(commande.getPrixTotal() + " FCFA", fontNormal));
        document.add(table);

        // --- Ton Total ---
        Paragraph total = new Paragraph("MONTANT TOTAL À PAYER : " + commande.getPrixTotal() + " FCFA", fontTitre);
        total.setAlignment(Element.ALIGN_RIGHT);
        document.add(total);

        // =========================================================
        // AJOUT UNIQUEMENT DE LA SIGNATURE NUMÉRIQUE (POINT SÉCURITÉ)
        // =========================================================
        document.add(new Paragraph("\n\n"));
        document.add(new LineSeparator());
        
        // Création de l'empreinte numérique unique (Hash)
        String dataToHash = commande.getId() + commande.getClient() + commande.getPrixTotal() + "KEY_2026";
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(dataToHash.getBytes("UTF-8"));
        String signatureHash = Base64.getEncoder().encodeToString(hash);

        // Bloc gris d'authentification
        PdfPTable sigTable = new PdfPTable(1);
        sigTable.setWidthPercentage(100);
        PdfPCell sigCell = new PdfPCell();
        sigCell.setBackgroundColor(new BaseColor(245, 245, 245));
        sigCell.setBorder(Rectangle.NO_BORDER);
        sigCell.setPadding(8);
        
        sigCell.addElement(new Paragraph("AUTHENTIFICATION NUMÉRIQUE", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8)));
        sigCell.addElement(new Paragraph("Signature d'intégrité (SHA-256) : " + signatureHash, fontSignature));
        
        sigTable.addCell(sigCell);
        document.add(sigTable);
        // =========================================================

        document.add(new Paragraph("\n"));
        Paragraph merci = new Paragraph("Merci de votre fidélité. Les vêtements non récupérés après 30 jours seront soumis à des frais de garde.", 
                FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, BaseColor.RED));
        merci.setAlignment(Element.ALIGN_CENTER);
        document.add(merci);

        document.close();
        return out.toByteArray();
    }
}