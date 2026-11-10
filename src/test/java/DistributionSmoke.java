import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import model.*;

/** Invoked only by native packaging with the distributed Java and application JARs. */
public final class DistributionSmoke {
    private static void require(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        Path data = Files.createTempDirectory("kharchiche-package-test-");
        System.setProperty("kharchiche.dataDir", data.toString());
        System.setProperty("kharchiche.disableOpenFile", "true");
        try {
            ClientDataBase.init();
            ReservationBataBase.init();
            Client client = new Client("Test", "Fictif", "package@example.invalid",
                    "fictif", LocalDate.of(1990, 1, 1));
            ClientDataBase.addClient(client);
            require(ClientDataBase.login(client.getEmail(), client.getPassword()) != null, "SQLite login");
            Reservation reservation = new Reservation(client, new Chambre("CLASSIQUE", 78, "wifi"),
                    156, LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 3), "wifi");
            ReservationBataBase.addReservation(reservation);
            require(ClientDataBase.getReservations(client).size() == 1, "SQLite history");
            PanierReservation panier = new PanierReservation();
            panier.listeReservations.add(reservation);
            TicketPDF.creerTicket(panier);
            try (var pdf = new com.lowagie.text.pdf.PdfReader(data.resolve("tickets/ticket.pdf").toString())) {
                require(pdf.getNumberOfPages() > 0, "PDF");
            }
            CountDownLatch ready = new CountDownLatch(1);
            Platform.startup(ready::countDown);
            require(ready.await(20, TimeUnit.SECONDS), "JavaFX startup");
            FutureTask<Void> task = new FutureTask<>(() -> {
                Platform.setImplicitExit(false);
                Stage stage = new Stage();
                new Main().start(stage);
                require(stage.isShowing(), "Main scene");
                for (String screen : new String[]{"hotel", "connexion", "inscription",
                        "reservation", "mesReservations"}) {
                    FXMLLoader.load(Main.class.getResource("/view/" + screen + ".fxml"));
                }
                stage.close();
                return null;
            });
            Platform.runLater(task);
            task.get(30, TimeUnit.SECONDS);
            System.out.println("DISTRIBUTION_SMOKE_OK");
        } finally {
            Platform.exit();
            try (var paths = Files.walk(data)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }
}