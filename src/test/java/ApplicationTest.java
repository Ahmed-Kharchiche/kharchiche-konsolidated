import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.image.ImageView;
import model.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    @TempDir Path temp;
    @Test void persistenceResourcesAndPdf() throws Exception {
        System.setProperty("kharchiche.dataDir", temp.toString());
        System.setProperty("kharchiche.disableOpenFile", "true");
        try {
            ClientDataBase.init(); ReservationBataBase.init();
            Client client = new Client("Test", "Fictif", "test@example.invalid", "fictif", LocalDate.of(1990,1,1));
            ClientDataBase.addClient(client);
            assertTrue(ClientDataBase.existsMail(client.getEmail()));
            assertTrue(ClientDataBase.exists(client.getEmail(), client.getPassword()));
            assertNotNull(ClientDataBase.login(client.getEmail(), client.getPassword()));
            Reservation r = new Reservation(client, new Chambre("CLASSIQUE",78,"wifi"),156,LocalDate.of(2030,1,1),LocalDate.of(2030,1,3),"wifi");
            ReservationBataBase.addReservation(r);
            assertEquals(1,ClientDataBase.getReservations(client).size());
            int num=ReservationBataBase.getNum(ClientDataBase.getIdByMail(client.getEmail()),"CLASSIQUE",156,r.getDateDebut(),r.getDateFin(),"wifi");
            assertTrue(num>0);
            r.setPrixTotal(200); ReservationBataBase.updateReservation(num,r);
            assertEquals(200,ClientDataBase.getReservations(client).get(0).getPrixTotal());
            PanierReservation panier=new PanierReservation(); panier.listeReservations.add(r);
            TicketPDF.creerTicket(panier);
            TicketPDF.creerTicketRemboursement(r,r,10);
            TicketPDF.creerTicketRajout(r,r,10);
            TicketPDF.creerTicketModificationSimple(r,r);
            for(String name : new String[]{"ticket.pdf","ticket_remboursement.pdf","ticket_rajout.pdf","ticket_modification.pdf"}) {
                try(com.lowagie.text.pdf.PdfReader pdf=new com.lowagie.text.pdf.PdfReader(temp.resolve("tickets").resolve(name).toString())) {
                    assertTrue(pdf.getNumberOfPages()>0);
                }
            }
            ReservationBataBase.deleteReservation(num);
            assertTrue(ClientDataBase.getReservations(client).isEmpty());
            CountDownLatch ready=new CountDownLatch(1); Platform.startup(ready::countDown);
            assertTrue(ready.await(20,TimeUnit.SECONDS));
            FutureTask<Void> task=new FutureTask<>(()->{
                javafx.stage.Stage stage = new javafx.stage.Stage();
                new Main().start(stage);
                assertTrue(stage.isShowing());
                stage.close();
                for(String name:new String[]{"hotel","connexion","inscription","reservation","mesReservations"}) {
                    Parent root=FXMLLoader.load(getClass().getResource("/view/"+name+".fxml"));
                    checkImages(root);
                }
                return null;
            });
            Platform.runLater(task); task.get(30,TimeUnit.SECONDS);
        } finally {
            Platform.exit(); System.clearProperty("kharchiche.dataDir"); System.clearProperty("kharchiche.disableOpenFile");
        }
    }
    private void checkImages(Parent parent) {
        if (parent instanceof javafx.scene.control.TabPane pane) {
            for (var tab : pane.getTabs()) if (tab.getContent() instanceof Parent content) checkImages(content);
        }
        for(var node:parent.getChildrenUnmodifiable()) {
            if(node instanceof ImageView view && view.getImage()!=null) assertFalse(view.getImage().isError(),view.getImage().getUrl());
            if(node instanceof Parent child) checkImages(child);
        }
    }
}
