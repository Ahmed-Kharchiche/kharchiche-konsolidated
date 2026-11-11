
package model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Gestion des chemins de stockage de l'application.
 */
public final class AppPaths {

    private AppPaths() {
    }

    public static Path dataDirectory() throws IOException {
        String override = System.getProperty("kharchiche.dataDir");
        String local = System.getenv("LOCALAPPDATA");

        Path path;

        if (override != null && !override.isBlank()) {
            path = Path.of(override);
        } else if (local != null && !local.isBlank()) {
            path = Path.of(local, "Kharchiche Konsolidated");
        } else {
            path = Path.of(
                    System.getProperty("user.home"),
                    ".kharchiche-konsolidated"
            );
        }

        return Files.createDirectories(path.toAbsolutePath());
    }

    public static Path database() throws IOException {
        Path legacy = Path.of("hotel.db").toAbsolutePath();

        if (!Boolean.getBoolean("kharchiche.packaged")
                && System.getProperty("kharchiche.dataDir") == null
                && Files.isRegularFile(legacy)) {
            return legacy;
        }

        return dataDirectory().resolve("hotel.db");
    }

    public static String jdbcUrl() throws IOException {
        return "jdbc:sqlite:" + database();
    }

    public static String ticket(String filename) throws IOException {
        Path dossierTickets = Files.createDirectories(
                dataDirectory().resolve("tickets")
        );

        return dossierTickets.resolve(filename).toString();
    }
}
