package lunatic.athenadungeon.ancientdungeon;

import lunatic.athenadungeon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class AncientTentacle implements Listener {

    private Main plugin;
    private Set<ArmorStand> tentacles = new HashSet<>();
    private Set<Ghast> ghasts = new HashSet<>();

    public AncientTentacle(Main plugin) {
        this.plugin = plugin;
        removeTentacles();
    }
    @EventHandler
    public void onGhastDeath(EntityDeathEvent event) {
        if (event.getEntity().getWorld().getName().equals("worldDungeon")) {
            if (event.getEntity() instanceof Ghast && event.getEntity().getCustomName() != null
                    && event.getEntity().getCustomName().equals("Dinnerbone")) {
                Player player = event.getEntity().getKiller();
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material GLOWING_PEARL " + player.getName() + " 1");
                removeTentacles();
            }
        }
    }

    @EventHandler
    public void onDeathByTentacle(PlayerDeathEvent event) {
        if (event.getEntity().getWorld().getName().equals("worldDungeon")) {
            for (Entity ent : event.getEntity().getNearbyEntities(10, 10, 10)) {
                if (ent instanceof Ghast) {
                    event.setDeathMessage("§f" + event.getEntity().getName() + " got swallowed by §3Seawater Tentacles");
                }
            }
        }
    }

    private void movePlayerTowardsGhast(Player player, Ghast ghast) {
        new BukkitRunnable() {
            int ticks = 0;

            @Override
            public void run() {
                if (ticks < 100) { // Adjust the number of ticks based on your desired duration
                    if (!ghast.isDead()) {
                        Location ghastLocation = ghast.getLocation().add(0, 5, 0);
                        Location playerLocation = player.getLocation();

                        double xDiff = ghastLocation.getX() - playerLocation.getX();
                        double zDiff = ghastLocation.getZ() - playerLocation.getZ();

                        double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

                        // Check if the player is not already at the center
                        if (distance > 0.1) {
                            double xRatio = xDiff / distance;
                            double zRatio = zDiff / distance;

                            double newX = playerLocation.getX() + 0.05 * xRatio;
                            double newZ = playerLocation.getZ() + 0.05 * zRatio;

                            // Check if the new position is close to the ghast location
                            if (ghastLocation.distance(new Location(playerLocation.getWorld(), newX, playerLocation.getY(), newZ)) > 1) {
                                player.teleport(new Location(playerLocation.getWorld(), newX, ghastLocation.getY(), newZ));
                            } else {
                                player.damage(1500);
                                cancel(); // Stop the task if the player is close to the ghast
                            }
                        } else {
                            cancel(); // Stop the task if the player is already at the center
                        }
                    } else {
                        removeTentacles();
                        cancel();
                    }

                    ticks++;
                } else {
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 0, 1);
    }

    public void spawnTentacles(double x, double y, double z) {
        World world = plugin.getServer().getWorld("worldDungeon");

        Ghast ghast = world.spawn(new Location(world, x, y, z), Ghast.class);
        ghast.setCustomName("Dinnerbone");
        ghast.setCustomNameVisible(false);
        ghast.setSilent(true);
        ghast.setAI(false);
        ghast.setMaxHealth(100);
        ghast.setHealth(100);

        ghasts.add(ghast);

        new BukkitRunnable() {
            @Override
            public void run() {
                // Check for nearby players within the specified detection radius
                for (Entity entity : ghast.getNearbyEntities(10, 10, 10)) {
                    if (entity instanceof Player) {
                        Player nearbyPlayer = (Player) entity;
                        movePlayerTowardsGhast(nearbyPlayer, ghast);
                    }
                }
                for (Entity arrow : ghast.getNearbyEntities(3, 3, 3)){
                    if (arrow instanceof Arrow){

                    }
                }
            }
        }.runTaskTimer(plugin, 0, 20);


        double stackRange = 0;
        for (int i = 0; i < 15; ++i) {
            stackRange += 0.55;
            ArmorStand armorStand = ghast.getLocation().getWorld().spawn(ghast.getLocation(), ArmorStand.class);
            armorStand.setGravity(false);
            armorStand.setInvisible(true);

            ArmorStand stackedArmorStand = armorStand.getWorld().spawn(armorStand.getLocation().add(0, 2.5 + stackRange, 0 + stackRange), ArmorStand.class);
            stackedArmorStand.setHelmet(Main.getHead("spirit"));
            stackedArmorStand.setGravity(false);
            stackedArmorStand.setInvisible(true);
            stackedArmorStand.setInvulnerable(true);
            stackedArmorStand.setPersistent(true);
            stackedArmorStand.setCustomName("AncientTentacle");
            stackedArmorStand.setMarker(true);

            // Schedule a task to make the tentacle move forward and backward slowly
            new TentacleMovementTask(stackedArmorStand, ghast).runTaskTimer(plugin, i + 10, 0);
            tentacles.add(armorStand);
            tentacles.add(stackedArmorStand);
        }
        stackRange = 0;
        for (int i = 0; i < 15; ++i) {
            stackRange += 0.55;
            ArmorStand armorStand = ghast.getLocation().getWorld().spawn(ghast.getLocation(), ArmorStand.class);
            armorStand.setGravity(false);
            armorStand.setInvisible(true);

            ArmorStand stackedArmorStand = armorStand.getWorld().spawn(armorStand.getLocation().add(0 + stackRange, 2.5 + stackRange, 0), ArmorStand.class);
            stackedArmorStand.setHelmet(Main.getHead("spirit"));
            stackedArmorStand.setGravity(false);
            stackedArmorStand.setInvisible(true);
            stackedArmorStand.setInvulnerable(true);
            stackedArmorStand.setPersistent(true);
            stackedArmorStand.setCustomName("AncientTentacle");
            stackedArmorStand.setMarker(true);

            // Schedule a task to make the tentacle move forward and backward slowly
            new TentacleMovementTask(stackedArmorStand, ghast).runTaskTimer(plugin, i + 10, 0);
            tentacles.add(armorStand);
            tentacles.add(stackedArmorStand);
        }
        stackRange = 0;
        for (int i = 0; i < 15; ++i) {
            stackRange += 0.55;
            ArmorStand armorStand = ghast.getLocation().getWorld().spawn(ghast.getLocation(), ArmorStand.class);
            armorStand.setGravity(false);
            armorStand.setInvisible(true);

            ArmorStand stackedArmorStand = armorStand.getWorld().spawn(armorStand.getLocation().add(0 - stackRange, 2.5 + stackRange, 0), ArmorStand.class);
            stackedArmorStand.setHelmet(Main.getHead("spirit"));
            stackedArmorStand.setGravity(false);
            stackedArmorStand.setInvisible(true);
            stackedArmorStand.setInvulnerable(true);
            stackedArmorStand.setPersistent(true);
            stackedArmorStand.setCustomName("AncientTentacle");
            stackedArmorStand.setMarker(true);

            // Schedule a task to make the tentacle move forward and backward slowly
            new TentacleMovementTask(stackedArmorStand, ghast).runTaskTimer(plugin, i + 10, 0);
            tentacles.add(armorStand);
            tentacles.add(stackedArmorStand);
        }
        stackRange = 0;
        for (int i = 0; i < 15; ++i) {
            stackRange += 0.55;
            ArmorStand armorStand = ghast.getLocation().getWorld().spawn(ghast.getLocation(), ArmorStand.class);
            armorStand.setGravity(false);
            armorStand.setInvisible(true);

            ArmorStand stackedArmorStand = armorStand.getWorld().spawn(armorStand.getLocation().add(0, 2.5 + stackRange, 0 - stackRange), ArmorStand.class);
            stackedArmorStand.setHelmet(Main.getHead("spirit"));
            stackedArmorStand.setGravity(false);
            stackedArmorStand.setInvisible(true);
            stackedArmorStand.setInvulnerable(true);
            stackedArmorStand.setPersistent(true);
            stackedArmorStand.setCustomName("AncientTentacle");
            stackedArmorStand.setMarker(true);

            // Schedule a task to make the tentacle move forward and backward slowly
            new TentacleMovementTask(stackedArmorStand, ghast).runTaskTimer(plugin, i + 10, 0);
            tentacles.add(armorStand);
            tentacles.add(stackedArmorStand);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                removeTentacles();
                cancel();
            }
        }.runTaskLater(plugin, 3600); // 100 ticks = 5 seconds (20 ticks per second)
    }

    private class TentacleMovementTask extends BukkitRunnable {
        private final ArmorStand armorStand;
        private final Ghast ghast;
        private boolean movingForward = true;

        public TentacleMovementTask(ArmorStand armorStand, Ghast ghast) {
            this.armorStand = armorStand;
            this.ghast = ghast;
        }

        @Override
        public void run() {
            if (!ghast.isDead()) {
                double movement = movingForward ? 0.3 : -0.3;

                Location newLocation = armorStand.getLocation().add(0, movement, 0);
                armorStand.teleport(newLocation);

                // Check if the armor stand reached the forward or backward limit
                if (Math.abs(armorStand.getLocation().getY()) >= 1) {
                    movingForward = !movingForward; // Reverse the direction
                }
            }else{
                removeTentacles();
                cancel();
            }
        }
    }

    private void removeTentacles() {
        for (ArmorStand armorStand : tentacles) {
            armorStand.remove();
        }
        for (Ghast ghast : ghasts) {
            ghast.remove();
        }
        World dungeon = Bukkit.getWorld("worldDungeon");
        for (Entity ent : dungeon.getEntities()){
            if (ent instanceof Ghast){
                ent.remove();
            }
            if (ent instanceof ArmorStand){
                ent.remove();
            }
        }
        ghasts.clear();
        tentacles.clear();
    }

    public void scheduleTentacleSpawning() {
        new BukkitRunnable() {
            @Override
            public void run() {
                spawnTentacles(44, 57, 93);
            }
        }.runTaskTimer(plugin, 0, 8 * 60 * 20); // 20 ticks per second, 2 minutes = 120 seconds
    }
}
