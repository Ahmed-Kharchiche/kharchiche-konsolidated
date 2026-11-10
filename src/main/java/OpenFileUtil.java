import java.io.File;
import java.io.IOException;

public class OpenFileUtil {

    public static void openFile(String chemin) throws IOException {
        if (Boolean.getBoolean("kharchiche.disableOpenFile")) return;
        File file = new File(chemin);

        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            // Windows
            java.awt.Desktop.getDesktop().open(file);
        } else if (os.contains("mac")) {
            // macOS
            Runtime.getRuntime().exec(new String[]{"open", file.getAbsolutePath()});
        } else {
            // Linux / autres
            Runtime.getRuntime().exec(new String[]{"xdg-open", file.getAbsolutePath()});
        }
    }
}
