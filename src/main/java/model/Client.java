package model;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Client {
    public static int NB_CLIENT=1;
    public String nom;
    public String prenom;
    private String email;
    private String password;
    public LocalDate dateAnniv;
    public List<Reservation> mesReservations ;

    public Client(String nom, String prenom, String email, String password, LocalDate dateAnniv){
        this.nom=nom;
        this.prenom=prenom;
        this.email=email;
        this.password=password;
        this.dateAnniv=dateAnniv;
    }



    public String getEmail(){
        return email;
    }

    public String getPassword() {
        return password;
    }



    public String getNom() {
        return nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public LocalDate getDateAnniv() {
        return dateAnniv;
    }

    public int age(){
        LocalDate aujourdHui = LocalDate.now();
        int age = Period.between(dateAnniv, aujourdHui).getYears();
        return age;
    }

    @Override
    public String toString(){
        return "Client :\n"
                + "Nom : " + nom + "\n"
                + "Prénom : " + prenom + "\n"
                + "Email : " + email + "\n"
                + "Mot de passe : ******** \n"
                + "Date de naissance : " + dateAnniv;
    }
}
