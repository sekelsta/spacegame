package sekelsta.engine;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

// For settings that need to be known immediately upon startup
public class InitialConfig {
    private String filePath;

    public Long windowWidth;
    public Long windowHeight;
    public Long windowPosX;
    public Long windowPosY;
    public boolean fullscreen;
    public boolean maximized;

    public InitialConfig(String filePath) {
        this.filePath = filePath;

        Toml toml = new Toml();
        try {
            FileInputStream initconfig = new FileInputStream(filePath);
            toml.read(initconfig);
        }
        catch (FileNotFoundException e) { }

        windowWidth = toml.getLong("windowWidth");
        windowHeight = toml.getLong("windowHeight");
        windowPosX = toml.getLong("windowPosX");
        windowPosY = toml.getLong("windowPosY");

        Boolean configFullscreen = toml.getBoolean("fullscreen");
        Boolean configMaximized = toml.getBoolean("maximized");
        maximized = configMaximized == null? false : configMaximized.booleanValue();
        fullscreen = configFullscreen == null? true : configFullscreen.booleanValue();
    }

    public void save() {
        TomlWriter tomlWriter = new TomlWriter();
        HashMap<String, Object> map = new HashMap<>();

        map.put("windowWidth", this.windowWidth);
        map.put("windowHeight", this.windowHeight);
        map.put("windowPosX", this.windowPosX);
        map.put("windowPosY", this.windowPosY);
        map.put("fullscreen", this.fullscreen);
        map.put("maximized", this.maximized);

        try {
            File config = new File(filePath);
            config.getParentFile().mkdirs();
            config.createNewFile();
            FileOutputStream out = new FileOutputStream(config);
            tomlWriter.write(map, out);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
