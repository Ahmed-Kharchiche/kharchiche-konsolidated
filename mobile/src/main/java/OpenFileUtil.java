import java.io.IOException;
import javafx.scene.control.Alert;

/** Mobile documents stay in private storage; desktop opening commands are not compiled. */
public final class OpenFileUtil {
    private OpenFileUtil() {}
    public static void openFile(String path) throws IOException {
        if (Boolean.getBoolean("kharchiche.disableOpenFile")) return;
        var alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText("Ticket enregistre");
        alert.setContentText(path + "\nLe partage vers une application PDF reste a integrer sur mobile.");
        alert.show();
    }
}
