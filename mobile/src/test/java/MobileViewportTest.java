import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.stage.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/** All original screens must fit a narrow viewport while remaining vertically scrollable. */
class MobileViewportTest {
    @TempDir Path data;

    @Test void originalScreensFitPhoneWidth() throws Exception {
        System.setProperty("kharchiche.dataDir", data.toString());
        CountDownLatch started = new CountDownLatch(1);
        Platform.startup(started::countDown);
        assertTrue(started.await(20, TimeUnit.SECONDS));
        try {
            FutureTask<Void> check = new FutureTask<>(() -> {
                Platform.setImplicitExit(false);
                Stage stage = new Stage();
                try {
                    for (String screen : new String[]{"hotel", "connexion", "inscription", "reservation", "mesReservations"}) {
                        Region original = FXMLLoader.load(getClass().getResource("/view/" + screen + ".fxml"));
                        Scene scene = new Scene(original, 360, 640);
                        MobileMain.fit(scene);
                        assertInstanceOf(ScrollPane.class, scene.getRoot(), screen);
                        ScrollPane viewport = (ScrollPane) scene.getRoot();
                        MobileMain.fit(scene);
                        assertSame(viewport, scene.getRoot(), "Must not wrap the same scene twice");
                        stage.setScene(scene);
                        stage.show();
                        viewport.applyCss();
                        viewport.layout();
                        assertTrue(original.getScaleX() > 0 && original.getScaleX() < 1, screen);
                        assertTrue(viewport.getContent().getBoundsInParent().getWidth() <= viewport.getViewportBounds().getWidth() + 1, screen);
                        assertEquals(ScrollPane.ScrollBarPolicy.NEVER, viewport.getHbarPolicy());
                    }
                } finally { stage.close(); }
                return null;
            });
            Platform.runLater(check);
            check.get(30, TimeUnit.SECONDS);
        } finally {
            Platform.exit();
            System.clearProperty("kharchiche.dataDir");
        }
    }
}
