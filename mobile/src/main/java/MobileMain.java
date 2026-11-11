import com.gluonhq.attach.storage.StorageService;
import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;

/** Starts the existing application with private storage and a phone-sized viewport. */
public final class MobileMain extends Application {
    @Override public void start(Stage stage) throws Exception {
        var storage = StorageService.create().orElseThrow(() -> new IllegalStateException("Stockage mobile indisponible"));
        var directory = storage.getPrivateStorage().orElseThrow(() -> new IllegalStateException("Stockage prive indisponible"));
        System.setProperty("kharchiche.packaged", "true");
        System.setProperty("kharchiche.dataDir", directory.toPath().resolve("kharchiche").toString());
        Class.forName("org.sqlite.JDBC");
        // Verify native SQLite before the existing init methods (which log and catch errors).
        try (var connection = java.sql.DriverManager.getConnection(model.AppPaths.jdbcUrl());
             var statement = connection.createStatement()) {
            statement.execute("SELECT sqlite_version()");
        }
        stage.sceneProperty().addListener((observable, previous, scene) -> fit(scene));
        new Main().start(stage);
    }

    static void fit(Scene scene) {
        if (scene == null || scene.getRoot() instanceof ScrollPane) return;
        var content = scene.getRoot();
        if (!(content instanceof Region region)) return;
        double width = region.prefWidth(-1);
        double height = region.prefHeight(width);
        region.setMinSize(width, height);
        region.setPrefSize(width, height);
        region.setMaxSize(width, height);
        // A few original images extend past the FXML pane. Clip at its design size,
        // as the original fixed-size window does, so those bounds cannot force horizontal overflow.
        region.setClip(new Rectangle(width, height));
        // Detach the FXML root before putting it inside a scrollable viewport.
        scene.setRoot(new Group());
        var scaled = new Group(region);
        var viewport = new ScrollPane(scaled);
        viewport.setPannable(true);
        viewport.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        viewport.viewportBoundsProperty().addListener((o, old, bounds) -> {
            double scale = Math.min(1, Math.max(1, bounds.getWidth()) / width);
            region.setScaleX(scale);
            region.setScaleY(scale);
            region.setTranslateX((scale - 1) * width / 2);
            region.setTranslateY((scale - 1) * height / 2);
        });
        scene.setRoot(viewport);
    }

    public static void main(String[] args) { launch(args); }
}
