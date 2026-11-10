package model;

import java.util.ArrayList;
import java.util.List;

import java.util.List;

public class PanierReservation {
    public List<Reservation> listeReservations = new ArrayList<>();

    public PanierReservation(){
        listeReservations= new ArrayList<>();
    }

    public int total(){
        int tot=0;
        for (Reservation reservation : listeReservations){
            tot+=reservation.getPrixTotal();
        }
        return tot;
    }

}
