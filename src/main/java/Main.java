import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import model.ClientDataBase;
import model.ReservationBataBase;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/hotel.fxml"));
        Scene accueilScene = new Scene(loader.load());
        Navigation.history.put("accueil", accueilScene);
        stage.setTitle("Accueil");
        stage.setScene(accueilScene);
        stage.show();
        ClientDataBase.init();
        ReservationBataBase.init();
    }



    public static void main(String[] args) {

        launch();

    }
}
