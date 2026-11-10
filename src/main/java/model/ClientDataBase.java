package model;

import model.Client;


import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;


public class ClientDataBase {

    private static Connection connect() throws Exception {
        return DriverManager.getConnection(AppPaths.jdbcUrl());
    }

    public static void init() {
        String sql = """
            CREATE TABLE IF NOT EXISTS client (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nom TEXT,
                prenom TEXT,
                email TEXT UNIQUE,
                password TEXT,
                dateAnniv DATE
            );
        """;

        try (Connection c = connect();
             Statement s = c.createStatement()) {
            s.execute(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void addClient(Client c) {
        String sql = "INSERT INTO client(nom, prenom, email, password, dateAnniv) VALUES(?, ?, ?, ?, ?)";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, c.getNom());
            ps.setString(2, c.getPrenom());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getPassword());
            ps.setString(5, c.getDateAnniv().format(formatter));

            ps.executeUpdate();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static boolean exists(String email, String password) {
        String sql = "SELECT 1 FROM client WHERE email=? AND password=?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            return ps.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean existsMail(String email) {
        String sql = "SELECT 1 FROM client WHERE email=?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);

            return ps.executeQuery().next();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Client login(String email, String password) {
        String sql = "SELECT * FROM client WHERE email=? AND password=?";

        try (Connection conn = connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Client(
                        rs.getString("nom"),
                        rs.getString("prenom"),
                        rs.getString("email"),
                        rs.getString("password"),
                        LocalDate.parse(
                                rs.getString("dateAnniv"),
                                DateTimeFormatter.ofPattern("dd/MM/yyyy"))
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getIdByMail(String mail) {

        String sql = "SELECT id FROM client WHERE email = ?";

        try (Connection c = connect();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, mail);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getInt("id");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public static ArrayList<Reservation> getReservations(Client c) {

        ArrayList<Reservation> allHistoric = new ArrayList<>();

        String sql = """
            SELECT * FROM reservation
            WHERE id = ?
            """;

        try (Connection co = connect();
             PreparedStatement ps = co.prepareStatement(sql)) {

            int idClient = ClientDataBase.getIdByMail(c.getEmail());
            ps.setInt(1, idClient);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String typeChambre = rs.getString("typeChambre");
                Integer prix = Chambre.TYPES.get(typeChambre);
                int prixTotal = rs.getInt("prixTotal");
                String options = rs.getString("options");

                Chambre chambre = new Chambre(typeChambre,prix,options);

                LocalDate dateDebut =
                        LocalDate.parse(rs.getString("dateDebut"));

                LocalDate dateFin =
                        LocalDate.parse(rs.getString("dateFin"));

                Reservation r = new Reservation(
                        c,
                        chambre,
                        prixTotal,
                        dateDebut,
                        dateFin,
                        options
                );

                allHistoric.add(r);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return allHistoric;
    }
}