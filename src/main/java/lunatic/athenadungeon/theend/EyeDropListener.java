package lunatic.athenadungeon.theend;

import io.lumine.mythic.api.mobs.MythicMob;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import lunatic.athenadungeon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Blaze;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

public class EyeDropListener implements Listener {
    private Main plugin;
    private Map<UUID, Integer> killCounts = new HashMap<>();
    private Map<UUID, BossBar> bossBars = new HashMap<>();

    public EyeDropListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEndermanEndDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        if (event.getEntity().getKiller() instanceof Player) {
            Player player = event.getEntity().getKiller();

            // Check if the killed entity is Enderfiend or Abyssal Spectre and if a player killed it
            if (player != null && entity.getCustomName() != null &&
                    (entity.getCustomName().equals("§4Enderking"))) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material DRAGOPTICS_LOST_EYES " + player.getName() + " 1");

            }
            if (player != null && entity.getCustomName() != null &&
                    (entity.getCustomName().equals("§5Enderfiend") || entity.getCustomName().equals("§5Abyssal Spectre"))) {

                if (player.getItemInHand().getItemMeta().getDisplayName().equals("§c§lDragoptics Eye Summoner")) {
                    MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("Enderking").orElse(null);
                    Location spawnLocation = entity.getLocation();
                    if (mob != null) {
                        // spawns mob
                        ActiveMob enderKingMobs = mob.spawn(BukkitAdapter.adapt(spawnLocation), 1);

                        // get mob as bukkit entity
                        Entity enderKing = enderKingMobs.getEntity().getBukkitEntity();

                        MythicMob crown = MythicBukkit.inst().getMobManager().getMythicMob("EnderkingCrown").orElse(null);
                        ActiveMob enderKingCrown = crown.spawn(BukkitAdapter.adapt(spawnLocation), 1);
                        Entity entityEnderCrown = enderKingCrown.getEntity().getBukkitEntity();

                        player.sendMessage("");
                        player.sendMessage("§c§lYou just spawned Enderking!");
                        player.sendMessage("");

                        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_WARN, 100f, 0f);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!enderKing.isDead()) {
                                    entityEnderCrown.teleport(enderKing.getLocation().add(0, 1.8, 0));
                                } else {
                                    enderKingCrown.remove();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 1, 1);

                        // Make the Blaze ride the Enderking
                        entity.addPassenger(entityEnderCrown);
                    }
                }

                UUID playerUUID = player.getUniqueId();
                String entityKey = entity.getCustomName();

                int killCount = killCounts.getOrDefault(playerUUID, 0) + 1;
                killCounts.put(playerUUID, killCount);

                // Check for the 1% chance
                if (new Random().nextInt(1500) < 4) {
                    // Execute console command "mi give"
                    MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("Enderking").orElse(null);
                    Location spawnLocation = entity.getLocation();
                    if (mob != null) {
                        // spawns mob
                        ActiveMob enderKingMobs = mob.spawn(BukkitAdapter.adapt(spawnLocation), 1);

                        // get mob as bukkit entity
                        Entity enderKing = enderKingMobs.getEntity().getBukkitEntity();

                        MythicMob crown = MythicBukkit.inst().getMobManager().getMythicMob("EnderkingCrown").orElse(null);
                        ActiveMob enderKingCrown = crown.spawn(BukkitAdapter.adapt(spawnLocation), 1);
                        Entity entityEnderCrown = enderKingCrown.getEntity().getBukkitEntity();

                        player.sendMessage("");
                        player.sendMessage("§c§lYou just spawned Enderking!");
                        player.sendMessage("");

                        player.playSound(player.getLocation(), Sound.ENTITY_GHAST_WARN, 100f, 0f);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                if (!enderKing.isDead()) {
                                    entityEnderCrown.teleport(enderKing.getLocation().add(0, 1.8, 0));
                                } else {
                                    enderKingCrown.remove();
                                    cancel();
                                }
                            }
                        }.runTaskTimer(plugin, 1, 1);

                        // Make the Blaze ride the Enderking
                        entity.addPassenger(entityEnderCrown);
                    }

                    if (player.getUniqueId() != null) {

                        // Show boss bar to the player who killed the mobs
                        BossBar bossBar = bossBars.computeIfAbsent(playerUUID,
                                uuid -> Bukkit.createBossBar("§c☤ Lost Eyes Meter §7(§e" + killCount + "§7/§e1500§7)", BarColor.RED, BarStyle.SEGMENTED_10));
                        bossBar.setProgress(0);
                        bossBar.addPlayer(player);
                        bossBar.setVisible(true);


                        // Schedule boss bar to disappear after 2 seconds
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            bossBar.removeAll();
                            bossBars.remove(playerUUID); // Remove from the map after removal
                            // Reset kill count after boss bar has been displayed
                            killCounts.put(playerUUID, 0);
                        }, 30L);

                    }

                } else {
                    // Update boss bar progress
                    double progress = (double) killCount / 1500;

                    // Check if it's time to execute the command again
                    if (killCount % 1500 == 0) {
                        MythicMob mob = MythicBukkit.inst().getMobManager().getMythicMob("Enderking").orElse(null);
                        Location spawnLocation = entity.getLocation();
                        if (mob != null) {
                            // spawns mob
                            ActiveMob enderKingMobs = mob.spawn(BukkitAdapter.adapt(spawnLocation), 1);

                            // get mob as bukkit entity
                            Entity enderKing = enderKingMobs.getEntity().getBukkitEntity();

                            MythicMob crown = MythicBukkit.inst().getMobManager().getMythicMob("EnderkingCrown").orElse(null);
                            ActiveMob enderKingCrown = crown.spawn(BukkitAdapter.adapt(spawnLocation), 1);
                            Entity entityEnderCrown = enderKingCrown.getEntity().getBukkitEntity();

                            player.sendMessage("");
                            player.sendMessage("§c§lYou just spawned Enderking!");
                            player.sendMessage("");

                            player.playSound(player.getLocation(), Sound.ENTITY_GHAST_WARN, 100f, 0f);

                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    if (!enderKing.isDead()) {
                                        entityEnderCrown.teleport(enderKing.getLocation().add(0, 1.8, 0));
                                    } else {
                                        enderKingCrown.remove();
                                        cancel();
                                    }
                                }
                            }.runTaskTimer(plugin, 1, 1);

                            // Make the Blaze ride the Enderking
                            entity.addPassenger(entityEnderCrown);
                        }
                    }

                    if (player.getUniqueId() != null) {
                        // Show boss bar to the player who killed the mobs
                        BossBar bossBar = bossBars.computeIfAbsent(playerUUID,
                                uuid -> Bukkit.createBossBar("§c☤ Lost Eyes Meter §7(§e" + killCount + "§7/§e1500§7)", BarColor.RED, BarStyle.SEGMENTED_10));
                        bossBar.setProgress(progress);
                        bossBar.addPlayer(player);
                        bossBar.setVisible(true);

                        // Schedule boss bar to disappear after 2 seconds
                        Bukkit.getScheduler().runTaskLater(plugin, () -> {
                            bossBar.removeAll();
                            bossBars.remove(playerUUID); // Remove from the map after removal
                        }, 30L);
                    }
                }
            }
        }
    }
    private Entity spawnEntity(Location location, EntityType entityType) {
        return location.getWorld().spawnEntity(location, entityType);
    }
}
