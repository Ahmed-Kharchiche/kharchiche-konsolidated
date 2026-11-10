import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import model.PanierReservation;
import model.Reservation;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.time.format.DateTimeFormatter;

public class TicketPDF {

    public static Reservation reservationAnnulee ;

    public static void creerTicket(PanierReservation panier) {

        try {

            String chemin = model.AppPaths.ticket("ticket.pdf");

            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(chemin));

            document.open();

            // =========================
            // POLICES
            // =========================

            Font titre = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font sousTitre = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font texte = new Font(Font.HELVETICA, 12);
            Font totalFont = new Font(Font.HELVETICA, 16, Font.BOLD);

            // =========================
            // TITRE
            // =========================

            Paragraph titreDoc = new Paragraph("HOTEL RESERVATION", titre);
            titreDoc.setAlignment(Element.ALIGN_CENTER);

            document.add(titreDoc);

            DateTimeFormatter format =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Paragraph date = new Paragraph(
                    "Date : " + java.time.LocalDateTime.now().format(format),
                    texte
            );

            date.setAlignment(Element.ALIGN_CENTER);

            document.add(date);

            document.add(new Paragraph("Paiement : Carte bancaire \nTransaction : VALIDEE"));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("===================================="));
            document.add(new Paragraph(" "));

            // =========================
            // RESERVATIONS
            // =========================

            int numero = 1;

            for (Reservation reservation : panier.listeReservations) {

                Paragraph reservationTitre =
                        new Paragraph(
                                "Reservation #" + numero,
                                sousTitre
                        );

                document.add(reservationTitre);

                document.add(new Paragraph(
                        "Client : "
                                + reservation.getClient().getPrenom()
                                + " "
                                + reservation.getClient().getNom(),
                        texte
                ));

                document.add(new Paragraph(
                        "Chambre : "
                                + reservation.getChambre(),
                        texte
                ));

                document.add(new Paragraph(
                        "Date d'arrivee : "
                                + reservation.getDateDebut(),
                        texte
                ));

                document.add(new Paragraph(
                        "Date de depart : "
                                + reservation.getDateFin(),
                        texte
                ));

                document.add(new Paragraph(
                        "Duree : "
                                + Reservation.getDuree(
                                reservation.getDateDebut(),
                                reservation.getDateFin()
                        )
                                + " nuit(s)",
                        texte
                ));

                document.add(new Paragraph(
                        "Options : "
                                + reservation.getOptions(),
                        texte
                ));

                document.add(new Paragraph(
                        "Prix : "
                                + reservation.getPrixTotal()
                                + " €",
                        texte
                ));

                document.add(new Paragraph("------------------------------------"));

                numero++;
            }

            // =========================
            // TOTAL
            // =========================

            document.add(new Paragraph(" "));

            Paragraph total = new Paragraph(
                    "TOTAL : " + panier.total() + " €",
                    totalFont
            );

            total.setAlignment(Element.ALIGN_RIGHT);

            document.add(total);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("Merci pour votre reservation !"));

            // =========================
            // FIN
            // =========================

            document.close();

            //Desktop.getDesktop().open(new File(chemin));
            OpenFileUtil.openFile(chemin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void creerTicketRemboursement(
            Reservation ancien,
            Reservation nouvelle,
            int remboursement) {

        try {

            String chemin = model.AppPaths.ticket("ticket_remboursement.pdf");

            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(chemin));

            document.open();

            // =========================
            // POLICES
            // =========================

            Font titre = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font sousTitre = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font texte = new Font(Font.HELVETICA, 12);
            Font totalFont = new Font(Font.HELVETICA, 16, Font.BOLD);

            // =========================
            // TITRE
            // =========================

            Paragraph titreDoc =
                    new Paragraph("TICKET DE REMBOURSEMENT", titre);

            titreDoc.setAlignment(Element.ALIGN_CENTER);

            document.add(titreDoc);

            DateTimeFormatter format =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Paragraph date = new Paragraph(
                    "Date : "
                            + java.time.LocalDateTime.now().format(format),
                    texte
            );

            date.setAlignment(Element.ALIGN_CENTER);

            document.add(date);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "===================================="
            ));
            document.add(new Paragraph(" "));

            // =========================
            // ANCIENNE RESERVATION
            // =========================

            Paragraph ancienneTitre =
                    new Paragraph("Ancienne reservation", sousTitre);

            document.add(ancienneTitre);

            document.add(new Paragraph(
                    "Client : "
                            + ancien.getClient().getPrenom()
                            + " "
                            + ancien.getClient().getNom(),
                    texte
            ));

            document.add(new Paragraph(
                    "Chambre : "
                            + ancien.getChambre(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date d'arrivee : "
                            + ancien.getDateDebut(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date de depart : "
                            + ancien.getDateFin(),
                    texte
            ));

            document.add(new Paragraph(
                    "Prix total : "
                            + ancien.getPrixTotal()
                            + " €",
                    texte
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("------------------------------------"));
            document.add(new Paragraph(" "));

            // =========================
            // NOUVELLE RESERVATION
            // =========================

            Paragraph nouvelleTitre =
                    new Paragraph("Nouvelle reservation", sousTitre);

            document.add(nouvelleTitre);

            document.add(new Paragraph(
                    "Client : "
                            + nouvelle.getClient().getPrenom()
                            + " "
                            + nouvelle.getClient().getNom(),
                    texte
            ));

            document.add(new Paragraph(
                    "Chambre : "
                            + nouvelle.getChambre(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date d'arrivee : "
                            + nouvelle.getDateDebut(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date de depart : "
                            + nouvelle.getDateFin(),
                    texte
            ));

            document.add(new Paragraph(
                    "Prix total : "
                            + nouvelle.getPrixTotal()
                            + " €",
                    texte
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("------------------------------------"));
            document.add(new Paragraph(" "));

            // =========================
            // REMBOURSEMENT
            // =========================

            Paragraph remboursementTexte =
                    new Paragraph(
                            "MONTANT REMBOURSE : "
                                    + remboursement
                                    + " €",
                            totalFont
                    );

            remboursementTexte.setAlignment(Element.ALIGN_RIGHT);

            document.add(remboursementTexte);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Le remboursement a ete effectue avec succes."
            ));

            // =========================
            // FIN
            // =========================

            document.close();

            //Desktop.getDesktop().open(new File(chemin));
            OpenFileUtil.openFile(chemin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void creerTicketRajout(
            Reservation ancien,
            Reservation nouvelle,
            int rajout) {

        try {

            String chemin = model.AppPaths.ticket("ticket_rajout.pdf");

            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(chemin));

            document.open();

            // =========================
            // POLICES
            // =========================

            Font titre = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font sousTitre = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font texte = new Font(Font.HELVETICA, 12);
            Font totalFont = new Font(Font.HELVETICA, 16, Font.BOLD);

            // =========================
            // TITRE
            // =========================

            Paragraph titreDoc =
                    new Paragraph("TICKET DE RAJOUT", titre);

            titreDoc.setAlignment(Element.ALIGN_CENTER);

            document.add(titreDoc);

            DateTimeFormatter format =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Paragraph date = new Paragraph(
                    "Date : "
                            + java.time.LocalDateTime.now().format(format),
                    texte
            );

            date.setAlignment(Element.ALIGN_CENTER);

            document.add(date);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "===================================="
            ));
            document.add(new Paragraph(" "));

            // =========================
            // ANCIENNE RESERVATION
            // =========================

            Paragraph ancienneTitre =
                    new Paragraph("Ancienne reservation", sousTitre);

            document.add(ancienneTitre);

            document.add(new Paragraph(
                    "Client : "
                            + ancien.getClient().getPrenom()
                            + " "
                            + ancien.getClient().getNom(),
                    texte
            ));

            document.add(new Paragraph(
                    "Chambre : "
                            + ancien.getChambre(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date d'arrivee : "
                            + ancien.getDateDebut(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date de depart : "
                            + ancien.getDateFin(),
                    texte
            ));

            document.add(new Paragraph(
                    "Prix total : "
                            + ancien.getPrixTotal()
                            + " €",
                    texte
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("------------------------------------"));
            document.add(new Paragraph(" "));

            // =========================
            // NOUVELLE RESERVATION
            // =========================

            Paragraph nouvelleTitre =
                    new Paragraph("Nouvelle reservation", sousTitre);

            document.add(nouvelleTitre);

            document.add(new Paragraph(
                    "Client : "
                            + nouvelle.getClient().getPrenom()
                            + " "
                            + nouvelle.getClient().getNom(),
                    texte
            ));

            document.add(new Paragraph(
                    "Chambre : "
                            + nouvelle.getChambre(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date d'arrivee : "
                            + nouvelle.getDateDebut(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date de depart : "
                            + nouvelle.getDateFin(),
                    texte
            ));

            document.add(new Paragraph(
                    "Prix total : "
                            + nouvelle.getPrixTotal()
                            + " €",
                    texte
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("------------------------------------"));
            document.add(new Paragraph(" "));

            // =========================
            // RAJOUT
            // =========================

            Paragraph rajoutTexte =
                    new Paragraph(
                            "MONTANT AJOUTE : "
                                    + rajout
                                    + " €",
                            totalFont
                    );

            rajoutTexte.setAlignment(Element.ALIGN_RIGHT);

            document.add(rajoutTexte);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(
                    "Le paiement supplementaire a ete effectue avec succes."
            ));

            // =========================
            // FIN
            // =========================

            document.close();

            //Desktop.getDesktop().open(new File(chemin));
            OpenFileUtil.openFile(chemin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void creerTicketModificationSimple(
            Reservation ancien,
            Reservation nouvelle) {

        try {

            String chemin = model.AppPaths.ticket("ticket_modification.pdf");

            Document document = new Document();

            PdfWriter.getInstance(document, new FileOutputStream(chemin));

            document.open();

            // =========================
            // POLICES
            // =========================

            Font titre = new Font(Font.HELVETICA, 22, Font.BOLD);
            Font sousTitre = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font texte = new Font(Font.HELVETICA, 12);

            // =========================
            // TITRE
            // =========================

            Paragraph titreDoc =
                    new Paragraph("TICKET DE MODIFICATION", titre);

            titreDoc.setAlignment(Element.ALIGN_CENTER);

            document.add(titreDoc);

            DateTimeFormatter format =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            Paragraph date = new Paragraph(
                    "Date : "
                            + java.time.LocalDateTime.now().format(format),
                    texte
            );

            date.setAlignment(Element.ALIGN_CENTER);

            document.add(date);

            document.add(new Paragraph(" "));
            document.add(new Paragraph("===================================="));
            document.add(new Paragraph(" "));

            // =========================
            // ANCIENNE RESERVATION
            // =========================

            Paragraph ancienneTitre =
                    new Paragraph("Ancienne reservation", sousTitre);

            document.add(ancienneTitre);

            document.add(new Paragraph(
                    "Client : "
                            + ancien.getClient().getPrenom()
                            + " "
                            + ancien.getClient().getNom(),
                    texte
            ));

            document.add(new Paragraph(
                    "Chambre : "
                            + ancien.getChambre(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date d'arrivee : "
                            + ancien.getDateDebut(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date de depart : "
                            + ancien.getDateFin(),
                    texte
            ));

            document.add(new Paragraph(
                    "Prix total : "
                            + ancien.getPrixTotal()
                            + " €",
                    texte
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("------------------------------------"));
            document.add(new Paragraph(" "));

            // =========================
            // NOUVELLE RESERVATION
            // =========================

            Paragraph nouvelleTitre =
                    new Paragraph("Nouvelle reservation", sousTitre);

            document.add(nouvelleTitre);

            document.add(new Paragraph(
                    "Client : "
                            + nouvelle.getClient().getPrenom()
                            + " "
                            + nouvelle.getClient().getNom(),
                    texte
            ));

            document.add(new Paragraph(
                    "Chambre : "
                            + nouvelle.getChambre(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date d'arrivee : "
                            + nouvelle.getDateDebut(),
                    texte
            ));

            document.add(new Paragraph(
                    "Date de depart : "
                            + nouvelle.getDateFin(),
                    texte
            ));

            document.add(new Paragraph(
                    "Prix total : "
                            + nouvelle.getPrixTotal()
                            + " €",
                    texte
            ));

            document.add(new Paragraph(" "));
            document.add(new Paragraph("===================================="));
            document.add(new Paragraph(" "));

            // =========================
            // INFO
            // =========================

            document.add(new Paragraph(
                    "Modification effectuee sans ajustement de paiement."
            ));

            document.add(new Paragraph("Merci pour votre confiance."));

            // =========================
            // FIN
            // =========================

            document.close();

            //Desktop.getDesktop().open(new File(chemin));
            OpenFileUtil.openFile(chemin);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
