import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPCellEvent;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import model.PanierReservation;
import model.Reservation;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.text.NumberFormat;

/**
 * Documents PDF de Kharchiche Konsolidated.
 * Compatible avec OpenPDF 1.3.x et le fonctionnement existant de l'application.
 *
 * Image a placer ici : src/main/resources/images/vue_aerienne.jpg
 * Elle sera chargee depuis le JAR, y compris dans les distributions jpackage.
 */
public class TicketPDF {

    // Conserve pour compatibilite avec les classes qui utilisent cet attribut.
    public static Reservation reservationAnnulee;

    private static final Color NOIR = new Color(30, 32, 31);
    private static final Color OR = new Color(186, 144, 79);
    private static final Color IVOIRE = new Color(248, 246, 241);
    private static final Color GRIS = new Color(105, 107, 104);
    private static final Color LIGNE = new Color(231, 226, 217);
    private static final Color BLANC = Color.WHITE;

    private static final Locale FR = Locale.FRANCE;
    private static final DateTimeFormatter DATE_DOCUMENT =
            DateTimeFormatter.ofPattern("dd MMMM yyyy 'à' HH:mm", FR);
    private static final DateTimeFormatter DATE_SEJOUR =
            DateTimeFormatter.ofPattern("dd MMMM yyyy", FR);

    private static Font police(float taille, int style, Color couleur) {
        return new Font(Font.HELVETICA, taille, style, couleur);
    }

    private interface ContenuPDF {
        void ajouter(Document document) throws Exception;
    }

    // ===================== POINTS D'ENTREE EXISTANTS =====================

    public static void creerTicket(PanierReservation panier) {
        generer("ticket.pdf", "REÇU DE RÉSERVATION", "PAIEMENT PAR CARTE", document -> {
            int numero = 1;
            for (Reservation reservation : panier.listeReservations) {
                ajouterReservation(document, reservation, "SÉJOUR " + String.format("%02d", numero++));
            }
            ajouterMontant(document, "TOTAL RÉGLÉ", panier.total());
            ajouterMessage(document, "Merci pour votre réservation. Nous vous souhaitons un excellent séjour.");
        });
    }

    public static void creerTicketRemboursement(Reservation ancien,
                                                Reservation nouvelle,
                                                int remboursement) {
        generer("ticket_remboursement.pdf", "REÇU DE REMBOURSEMENT", "MODIFICATION DE SÉJOUR", document -> {
            ajouterReservation(document, ancien, "AVANT MODIFICATION");
            ajouterReservation(document, nouvelle, "APRÈS MODIFICATION");
            ajouterMontant(document, "MONTANT REMBOURSÉ", remboursement);
            ajouterMessage(document, "Le remboursement a été effectué avec succès.");
        });
    }

    public static void creerTicketRajout(Reservation ancien,
                                         Reservation nouvelle,
                                         int rajout) {
        generer("ticket_rajout.pdf", "COMPLÉMENT DE PAIEMENT", "MODIFICATION DE SÉJOUR", document -> {
            ajouterReservation(document, ancien, "AVANT MODIFICATION");
            ajouterReservation(document, nouvelle, "APRÈS MODIFICATION");
            ajouterMontant(document, "MONTANT AJOUTÉ", rajout);
            ajouterMessage(document, "Le paiement supplémentaire a été effectué avec succès.");
        });
    }

    public static void creerTicketModificationSimple(Reservation ancien,
                                                     Reservation nouvelle) {
        generer("ticket_modification.pdf", "MODIFICATION DE RÉSERVATION", "AUCUN AJUSTEMENT", document -> {
            ajouterReservation(document, ancien, "AVANT MODIFICATION");
            ajouterReservation(document, nouvelle, "APRÈS MODIFICATION");
            ajouterEncartInformation(document, "Modification effectuée sans ajustement de paiement.");
            ajouterMessage(document, "Merci pour votre confiance.");
        });
    }

    // ===================== GENERATION PARTAGEE =====================

    private static void generer(String nomFichier,
                                String titre,
                                String statut,
                                ContenuPDF contenu) {
        Document document = new Document(PageSize.A4, 43, 43, 36, 55);
        String chemin;

        try {
            chemin = model.AppPaths.ticket(nomFichier);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Erreur lors de la création du ticket PDF", e);
        }

        try {
            File fichier = new File(chemin);
            File dossier = fichier.getParentFile();
            if (dossier != null && !dossier.exists() && !dossier.mkdirs()) {
                throw new IllegalStateException("Impossible de créer le dossier : " + dossier);
            }

            try (FileOutputStream sortie = new FileOutputStream(fichier)) {
                PdfWriter writer = PdfWriter.getInstance(document, sortie);
                writer.setPageEvent(new PiedDePage());
                document.addTitle(titre + " - Kharchiche Konsolidated");
                document.addAuthor("Kharchiche Konsolidated");
                document.open();
                try {
                    ajouterEnTete(document, titre, statut, !"ticket.pdf".equals(nomFichier));
                    contenu.ajouter(document);
                } finally {
                    document.close();
                }
            }

            // Même fonctionnement qu'avant : ouverture du PDF après génération.
            OpenFileUtil.openFile(chemin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void ajouterEnTete(Document document, String titre, String statut, boolean compact)
            throws Exception {
        PdfPTable hero = new PdfPTable(1);
        hero.setWidthPercentage(100);

        PdfPCell photo = new PdfPCell();
        photo.setBorder(Rectangle.NO_BORDER);
        photo.setPadding(0);
        photo.setFixedHeight(compact ? 104 : 147);
        photo.setBackgroundColor(NOIR);

        // Resource accessible même après installation (JAR/jpackage).
        try (InputStream source = TicketPDF.class.getResourceAsStream("/images/vue_aerienne.jpg")) {
            if (source != null) {
                Image image = Image.getInstance(source.readAllBytes());
                photo.setCellEvent(new PhotoPanoramique(image, compact));
            } else {
                // Le PDF reste utilisable même si la photo n'est pas empaquetée.
                photo.addElement(new Paragraph("KHARCHICHE KONSOLIDATED",
                        police(18, Font.BOLD, OR)));
                photo.setPadding(20);
            }
        }

        hero.addCell(photo);
        document.add(hero);

        PdfPTable bande = new PdfPTable(new float[]{2.15f, 1f});
        bande.setWidthPercentage(100);
        PdfPCell marque = celluleSimple("KHARCHICHE KONSOLIDATED",
                police(10.5f, Font.BOLD, OR), NOIR, Element.ALIGN_LEFT, 10);
        PdfPCell signature = celluleSimple("HÔTEL & RÉSERVATIONS",
                police(8, Font.NORMAL, BLANC), NOIR, Element.ALIGN_RIGHT, 10);
        bande.addCell(marque);
        bande.addCell(signature);
        bande.setSpacingAfter(compact ? 12 : 17);
        document.add(bande);

        Paragraph titreDoc = new Paragraph(titre, police(20, Font.BOLD, NOIR));
        titreDoc.setSpacingAfter(compact ? 2 : 4);
        document.add(titreDoc);

        Paragraph sousTitre = new Paragraph("Votre document de séjour personnalisé",
                police(10, Font.NORMAL, GRIS));
        sousTitre.setSpacingAfter(compact ? 9 : 13);
        document.add(sousTitre);

        PdfPTable informations = new PdfPTable(new float[]{1.2f, 1f});
        informations.setWidthPercentage(100);
        informations.addCell(celluleInformation("DOCUMENT ÉMIS LE",
                LocalDateTime.now().format(DATE_DOCUMENT)));
        informations.addCell(celluleInformation("OPÉRATION", statut));
        informations.setSpacingAfter(compact ? 11 : 15);
        document.add(informations);
    }

    private static void ajouterReservation(Document document,
                                           Reservation reservation,
                                           String titre) throws Exception {
        PdfPTable fiche = new PdfPTable(new float[]{1f, 1f});
        fiche.setWidthPercentage(100);
        fiche.setKeepTogether(true);
        fiche.setSpacingAfter(9);

        PdfPCell entete = celluleSimple(titre,
                police(10.2f, Font.BOLD, OR), NOIR, Element.ALIGN_LEFT, 8);
        entete.setColspan(2);
        fiche.addCell(entete);

        ajouterChamp(fiche, "CLIENT",
                reservation.getClient().getPrenom() + " " + reservation.getClient().getNom());
        ajouterChamp(fiche, "CHAMBRE", valeur(reservation.getChambre()));

        ajouterChamp(fiche, "ARRIVÉE", date(reservation.getDateDebut()));
        ajouterChamp(fiche, "DÉPART", date(reservation.getDateFin()));

        ajouterChamp(fiche, "DURÉE",
                Reservation.getDuree(reservation.getDateDebut(), reservation.getDateFin()) + " nuit(s)");
        ajouterChamp(fiche, "OPTIONS", options(reservation.getOptions()));

        PdfPCell prix = celluleSimple("MONTANT DU SÉJOUR  ·  " + euros(reservation.getPrixTotal()),
                police(11.5f, Font.BOLD, NOIR), IVOIRE, Element.ALIGN_RIGHT, 8);
        prix.setColspan(2);
        prix.setBorder(Rectangle.TOP);
        prix.setBorderColor(LIGNE);
        fiche.addCell(prix);

        document.add(fiche);
    }

    private static void ajouterChamp(PdfPTable tableau, String label, String valeur) {
        PdfPCell cellule = new PdfPCell();
        cellule.setBackgroundColor(IVOIRE);
        cellule.setBorder(Rectangle.BOTTOM);
        cellule.setBorderColor(LIGNE);
        cellule.setPaddingLeft(11);
        cellule.setPaddingRight(9);
        cellule.setPaddingTop(5);
        cellule.setPaddingBottom(5);

        Paragraph etiquette = new Paragraph(label, police(7.5f, Font.BOLD, GRIS));
        etiquette.setSpacingAfter(3);
        cellule.addElement(etiquette);
        cellule.addElement(new Paragraph(valeur, police(10, Font.NORMAL, NOIR)));
        tableau.addCell(cellule);
    }

    private static PdfPCell celluleInformation(String label, String valeur) {
        PdfPCell cellule = new PdfPCell();
        cellule.setBorder(Rectangle.NO_BORDER);
        cellule.setBackgroundColor(IVOIRE);
        cellule.setPadding(9);

        Paragraph etiquette = new Paragraph(label, police(7.5f, Font.BOLD, GRIS));
        etiquette.setSpacingAfter(3);
        cellule.addElement(etiquette);
        cellule.addElement(new Paragraph(valeur, police(9.5f, Font.BOLD, NOIR)));
        return cellule;
    }

    private static PdfPCell celluleSimple(String texte,
                                          Font font,
                                          Color fond,
                                          int alignement,
                                          float marge) {
        PdfPCell cellule = new PdfPCell(new Phrase(texte, font));
        cellule.setBorder(Rectangle.NO_BORDER);
        cellule.setBackgroundColor(fond);
        cellule.setHorizontalAlignment(alignement);
        cellule.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cellule.setPadding(marge);
        return cellule;
    }

    private static void ajouterMontant(Document document, String label, Object montant)
            throws DocumentException {
        PdfPTable total = new PdfPTable(new float[]{1.3f, 1f});
        total.setWidthPercentage(100);
        total.setSpacingBefore(3);
        total.setSpacingAfter(12);
        total.addCell(celluleSimple(label, police(10, Font.BOLD, BLANC),
                NOIR, Element.ALIGN_LEFT, 14));
        total.addCell(celluleSimple(euros(montant), police(17, Font.BOLD, OR),
                NOIR, Element.ALIGN_RIGHT, 12));
        document.add(total);
    }

    private static void ajouterEncartInformation(Document document, String message)
            throws DocumentException {
        PdfPTable tableau = new PdfPTable(1);
        tableau.setWidthPercentage(100);
        tableau.setSpacingBefore(3);
        tableau.setSpacingAfter(12);
        tableau.addCell(celluleSimple(message,
                police(10.5f, Font.BOLD, NOIR), IVOIRE, Element.ALIGN_CENTER, 15));
        document.add(tableau);
    }

    private static void ajouterMessage(Document document, String message)
            throws DocumentException {
        Paragraph fin = new Paragraph(message, police(9.5f, Font.ITALIC, GRIS));
        fin.setAlignment(Element.ALIGN_CENTER);
        fin.setSpacingAfter(6);
        document.add(fin);
    }

    private static String euros(Object montant) {
        if (montant instanceof Number nombre) {
            NumberFormat format = NumberFormat.getNumberInstance(FR);
            format.setMinimumFractionDigits(2);
            format.setMaximumFractionDigits(2);
            String chiffre = format.format(nombre.doubleValue())
                    .replace('\u202f', ' ').replace('\u00a0', ' ');
            return chiffre + " €";
        }
        return valeur(montant) + " €";
    }

    private static String date(Object date) {
        if (date instanceof LocalDate jour) {
            return jour.format(DATE_SEJOUR);
        }
        if (date instanceof java.sql.Date jourSQL) {
            return jourSQL.toLocalDate().format(DATE_SEJOUR);
        }
        return valeur(date);
    }

    private static String valeur(Object objet) {
        return objet == null ? "Non renseigné" : String.valueOf(objet);
    }

    private static String options(Object objet) {
        if (objet == null) return "Aucune";
        String texte = String.valueOf(objet).trim();
        return texte.isEmpty() || texte.equals("[]") ? "Aucune" : texte;
    }

    // ===================== RENDU DES IMAGES ET DU PIED DE PAGE =====================

    private static class PhotoPanoramique implements PdfPCellEvent {
        private final Image image;
        private final boolean compact;

        PhotoPanoramique(Image image, boolean compact) {
            this.image = image;
            this.compact = compact;
        }

        @Override
        public void cellLayout(PdfPCell cellule, Rectangle zone, PdfContentByte[] calques) {
            PdfContentByte canvas = calques[PdfPTable.BACKGROUNDCANVAS];
            canvas.saveState();
            try {
                // Remplissage du bandeau avec recadrage centre, sans déformer l'image.
                canvas.rectangle(zone.getLeft(), zone.getBottom(), zone.getWidth(), zone.getHeight());
                canvas.clip();
                canvas.newPath();

                float ratio = Math.max(zone.getWidth() / image.getWidth(),
                        zone.getHeight() / image.getHeight());
                float largeur = image.getWidth() * ratio;
                float hauteur = image.getHeight() * ratio;
                float x = zone.getLeft() + (zone.getWidth() - largeur) / 2;
                // Sur les reçus compacts, on privilégie le haut de la façade et son enseigne.
                float decalage = compact ? Math.min(31, (hauteur - zone.getHeight()) / 2) : 0;
                float y = zone.getBottom() + (zone.getHeight() - hauteur) / 2 - decalage;
                canvas.addImage(image, largeur, 0, 0, hauteur, x, y);
            } catch (DocumentException e) {
                throw new IllegalStateException("Impossible d'insérer la photo dans le PDF", e);
            } finally {
                canvas.restoreState();
            }
        }
    }

    private static class PiedDePage extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte canvas = writer.getDirectContent();
                float gauche = document.left();
                float droite = document.right();
                float bas = document.bottom() - 20;

                canvas.saveState();
                canvas.setColorStroke(LIGNE);
                canvas.setLineWidth(0.8f);
                canvas.moveTo(gauche, bas + 12);
                canvas.lineTo(droite, bas + 12);
                canvas.stroke();
                canvas.restoreState();

                BaseFont base = BaseFont.createFont(BaseFont.HELVETICA,
                        BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
                Font pied = new Font(base, 8, Font.NORMAL, GRIS);

                ColumnText.showTextAligned(canvas, Element.ALIGN_LEFT,
                        new Phrase("KHARCHICHE KONSOLIDATED  |  HÔTEL & RÉSERVATIONS", pied),
                        gauche, bas, 0);
                ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                        new Phrase("PAGE " + writer.getPageNumber(), pied),
                        droite, bas, 0);
            } catch (Exception e) {
                // Un incident sur le pied de page ne doit pas masquer le reçu.
                e.printStackTrace();
            }
        }
    }
}
