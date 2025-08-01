package space.nadamu.nadamuPillar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Config {

    public static void init(Path dataFolder, InputStream configStream) {
        if(Files.notExists(dataFolder)) {
            try {
                Files.createDirectory(dataFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        final Path configFile = dataFolder.resolve("config.toml");
        try {
            if (Files.notExists(configFile) || Files.size(configFile) == 0) {
                assert configStream != null;
                Files.copy(configStream, configFile);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
