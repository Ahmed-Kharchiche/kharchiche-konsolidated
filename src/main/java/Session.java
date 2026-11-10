import model.Client;
import model.PanierReservation;
import model.Reservation;

import java.time.LocalDate;

public class Session {
    public final static Client DEFAULT_USER= new Client("KHARCHICHE","Ahmed","kharchiche.ahmed@gmail.com","mouffy.213", LocalDate.of(2007, 9, 4));
    public static Client currentUser=null;
    public static PanierReservation panier = new PanierReservation();
    public static Reservation reservationAModifier=null;

}