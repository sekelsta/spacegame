package sekelsta.game;

import com.moandjiezana.toml.Toml;
import com.moandjiezana.toml.TomlWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

public class UserSettings {
    private String filePath;

    public String lastJoinedIP;
    public float volume;

    public UserSettings(String filePath) {
        this.filePath = filePath;

        Toml toml = new Toml();
        try {
            FileInputStream initconfig = new FileInputStream(filePath);
            toml.read(initconfig);
        }
        catch (FileNotFoundException e) { }

        Double configVolume = toml.getDouble("volume");
        volume = configVolume == null? 1 : (float)configVolume.doubleValue();
        volume = (float)Math.min(1, Math.max(0, volume));

        lastJoinedIP = toml.getString("lastJoinedIP");
        if (lastJoinedIP == null) {
            lastJoinedIP = "";
        }
    }



    public void save() {
        TomlWriter tomlWriter = new TomlWriter();
        HashMap<String, Object> map = new HashMap<>();

        map.put("volume", this.volume);

        map.put("lastJoinedIP", this.lastJoinedIP);

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
