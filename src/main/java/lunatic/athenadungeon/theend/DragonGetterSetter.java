package lunatic.athenadungeon.theend;

import lunatic.athenadungeon.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class DragonGetterSetter {
    boolean dragonIsSpawned;
    private Main plugin;

    public boolean getDragonIsSpawned() {
        return dragonIsSpawned;
    }

    public void setDragonIsSpawned(boolean dragonIsSpawned) {
        this.dragonIsSpawned = dragonIsSpawned;
    }

}
