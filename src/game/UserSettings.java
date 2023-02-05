package sekelsta.game;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

public class UserSettings {
    private final Game game;
    private final String filePath;

    public String lastJoinedIP;
    private float volume;
    public float uiScale;

    public UserSettings(String filePath, Game game) {
        this.game = game;
        this.filePath = filePath;

        Toml toml = new Toml();
        try {
            FileInputStream initconfig = new FileInputStream(filePath);
            toml.read(initconfig);
        }
        catch (FileNotFoundException e) { }

        Double configVolume = toml.getDouble("volume");
        float v = configVolume == null? 0.5f : (float)configVolume.doubleValue();
        setVolume(v);

        lastJoinedIP = toml.getString("lastJoinedIP");
        if (lastJoinedIP == null) {
            lastJoinedIP = "";
        }

        Double configScale = toml.getDouble("uiScale");
        uiScale = configScale == null? 1f : (float)configScale.doubleValue();
    }

    public float getVolume() {
        return volume;
    }

    public void setVolume(float volume) {
        this.volume = Math.min(1f, Math.max(0f, volume));
        game.setVolume(this.volume);
    }

    public void save() {
        TomlWriter tomlWriter = new TomlWriter();
        HashMap<String, Object> map = new HashMap<>();

        map.put("lastJoinedIP", this.lastJoinedIP);
        map.put("volume", this.volume);
        map.put("uiScale", this.uiScale);

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
