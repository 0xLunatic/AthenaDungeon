package lunatic.athenadungeon;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import lunatic.athenadungeon.ancientdungeon.AncientTentacle;
import lunatic.athenadungeon.ancientdungeon.EldoriaListener;
import lunatic.athenadungeon.ancientdungeon.WaterListener;
import lunatic.athenadungeon.heads.HeadList;
import lunatic.athenadungeon.theend.DragoDeathListener;
import lunatic.athenadungeon.theend.DragonGetterSetter;
import lunatic.athenadungeon.theend.EyeDropListener;
import lunatic.athenadungeon.theend.EyePlaceListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private DragonGetterSetter dragonGetterSetter;
    private DragoDeathListener dragoDeathListener;
    public Map<Player, Integer> eyesPlacedMap = new HashMap<>();

    AncientTentacle ancientTentacle;
    private static Economy economy;
    @Override
    public void onEnable() {
        // Plugin startup logic

        dragonGetterSetter = new DragonGetterSetter();
        ancientTentacle = new AncientTentacle(this);

        // Initialize the DragoDeathListener instance and pass the same DragonGetterSetter instance
        dragoDeathListener = new DragoDeathListener(this, dragonGetterSetter);

        getServer().getPluginManager().registerEvents(new EyeDropListener(this), this);

        getServer().getPluginManager().registerEvents(new WaterListener(this), this);
        getServer().getPluginManager().registerEvents(new AncientTentacle(this), this);
        getServer().getPluginManager().registerEvents(new EldoriaListener(this), this);
        getServer().getPluginManager().registerEvents(dragoDeathListener, this);

        getServer().getPluginManager().registerEvents(new EyePlaceListener(this, new DragonGetterSetter()), this);

        ancientTentacle.scheduleTentacleSpawning();

        for (Entity entity : Bukkit.getWorld("Drago").getEntities()){
            if (entity.getName().equalsIgnoreCase("§dDrago")){
                entity.remove();
            }
        }
        if (!setupEconomy()) {
            getLogger().severe("Vault plugin not found! Disabling plugin.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
    public int getPlayerEyesPlaced(Player player) {
        return eyesPlacedMap.getOrDefault(player, 0);
    }

    public void setPlayerEyesPlaced(Player player, int eyesPlaced) {
        eyesPlacedMap.put(player, eyesPlaced);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }

        economy = rsp.getProvider();
        return economy != null;
    }
    public static double getPlayerBalance(Player player) {
        if (economy == null) {
            // Handle the case where the economy is not set up
            return 0.0;
        }

        return economy.getBalance(player);
    }
    public static ItemStack createSkull(String url, String name) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD, 1, (short)3);
        if (url.isEmpty()) {
            return head;
        } else {
            SkullMeta headMeta = (SkullMeta)head.getItemMeta();
            GameProfile profile = new GameProfile(UUID.randomUUID(), (String)null);
            profile.getProperties().put("textures", new Property("textures", url));

            try {
                Field profileField = headMeta.getClass().getDeclaredField("profile");
                profileField.setAccessible(true);
                profileField.set(headMeta, profile);
            } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException var6) {
                var6.printStackTrace();
            }

            head.setItemMeta(headMeta);
            return head;
        }
    }

    public static ItemStack getHead(String name) {
        HeadList[] var1 = HeadList.values();
        int var2 = var1.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            HeadList head = var1[var3];
            if (head.getName().equals(name)) {
                return head.getItemStack();
            }
        }

        return null;
    }
}
