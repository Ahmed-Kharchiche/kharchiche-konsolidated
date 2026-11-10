package model;


import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class Reservation {
    private Client client;
    private Chambre chambre;
    private int prixTotal;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private String options;

    public Reservation(Client client,Chambre chambre,int prixTotal, LocalDate dateDebut, LocalDate dateFin, String options){
        this.client=client;
        this.chambre=chambre;
        this.prixTotal=prixTotal;
        this.dateDebut=dateDebut;
        this.dateFin=dateFin;
        this.options=options;
    }

    public Client getClient() {
        return client;
    }

    public Chambre getChambre() {
        return chambre;
    }
    public int getPrixTotal() {
        return prixTotal;
    }



    public LocalDate getDateDebut(){return dateDebut;}

    public LocalDate getDateFin() {return dateFin;}

    public String getOptions(){return options;}

    public static int getDuree(LocalDate dateDebut, LocalDate dateFin) {
        return (int) ChronoUnit.DAYS.between(dateDebut, dateFin);
    }

    public void setDateDebut(LocalDate dateDebut) {
        this.dateDebut = dateDebut;
    }

    public void setDateFin(LocalDate dateFin) {
        this.dateFin = dateFin;
    }

    public void setPrixTotal(int prixTotal) {
        this.prixTotal = prixTotal;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public void setChambre(Chambre chambre) {
        this.chambre = chambre;
    }


    @Override
    public String toString() {
        return "══════════════════════\n"
                + "🏨 RÉSERVATION\n"
                + "══════════════════════\n"
                + "👤 Client : " + client.getPrenom() + " " + client.getNom() + "\n"
                + "🛏 Chambre : " + chambre + "\n"
                + "📅 Arrivée : " + dateDebut + "\n"
                + "📅 Départ : " + dateFin + "\n"
                + "⏳ Durée : " + getDuree(dateDebut, dateFin) + " nuit(s)\n"
                + "⚙️ Options : " + options + "\n"
                + "💰 Prix total : "+ prixTotal
                + "€\n"
                + "══════════════════════";
    }

}
