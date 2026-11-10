package model;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class ReservationBataBase {

    private static Connection connect() throws Exception {
        return DriverManager.getConnection(AppPaths.jdbcUrl());
    }

    public static void init() {

        String sql = """
            CREATE TABLE IF NOT EXISTS reservation(
                num INTEGER PRIMARY KEY AUTOINCREMENT,
                id INTEGER,
                typeChambre TEXT,
                prixTotal INTEGER,
                dateDebut DATE,
                dateFin DATE,
                options TEXT,
                FOREIGN KEY(id) REFERENCES client(id)
            );
           """;

        try (Connection c = connect();
             Statement s = c.createStatement()) {

            s.execute(sql);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addReservation(Reservation r) {

        String sql = """
                INSERT INTO reservation
                (id, typeChambre, prixTotal, dateDebut, dateFin, options)
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, ClientDataBase.getIdByMail(r.getClient().getEmail()));
            ps.setString(2, r.getChambre().getType());
            ps.setInt(3, r.getPrixTotal());
            ps.setString(4, r.getDateDebut().toString());
            ps.setString(5, r.getDateFin().toString());
            ps.setString(6, r.getOptions());

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteReservation(int num) {

        String sql = "DELETE FROM reservation WHERE num = ?";

        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, num);

            int rows = ps.executeUpdate();
            System.out.println("Lignes supprimées = " + rows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getNum(int id,
                             String typeChambre,
                             int prixTotal,
                             LocalDate dateDebut,
                             LocalDate dateFin,
                             String options) {

        String sql = """
        SELECT num FROM reservation
        WHERE id = ?
        AND typeChambre = ?
        AND prixTotal = ?
        AND dateDebut = ?
        AND dateFin = ?
        AND options = ?
        LIMIT 1
    """;

        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setString(2, typeChambre);
            ps.setInt(3, prixTotal);
            ps.setString(4, dateDebut.toString());
            ps.setString(5, dateFin.toString());
            ps.setString(6, options);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("num");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static void updateReservation(int num, Reservation nouvelleReservation) {

        String sql = """
        UPDATE reservation
        SET id = ?,
            typeChambre = ?,
            prixTotal = ?,
            dateDebut = ?,
            dateFin = ?,
            options = ?
        WHERE num = ?
    """;

        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, ClientDataBase.getIdByMail(
                    nouvelleReservation.getClient().getEmail()
            ));

            ps.setString(2, nouvelleReservation.getChambre().getType());

            ps.setInt(3, nouvelleReservation.getPrixTotal());

            ps.setString(4, nouvelleReservation.getDateDebut().toString());

            ps.setString(5, nouvelleReservation.getDateFin().toString());

            ps.setString(6, nouvelleReservation.getOptions());

            ps.setInt(7, num);

            int rows = ps.executeUpdate();

            System.out.println("Lignes modifiées = " + rows);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}