// EyePlaceListener.java
package lunatic.athenadungeon.theend;

import lunatic.athenadungeon.Main;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.EndPortalFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;

import java.util.HashMap;
import java.util.Map;

public class EyePlaceListener implements Listener {
    private Main plugin;

    private Map<Location, Player> eyesPlacedOnBlock;
    
    private final long cooldownTimeMillis = 2000;

    private Map<Player, Long> lastInteractionTime = new HashMap<>();
    public Map<Location, Player> eyesOwnersMap = new HashMap<>();

    int totalEyesPlaced;
    DragonGetterSetter checkDragon;

    public EyePlaceListener(Main plugin, DragonGetterSetter checkDragon) {
        revertEndPortalFrames();
        this.plugin = plugin;
        this.eyesPlacedOnBlock = new HashMap<>();
        this.checkDragon = checkDragon;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            Block clickedBlock = event.getClickedBlock();

            ItemStack itemInHand = player.getInventory().getItemInMainHand();

            if (isPlayerHead(itemInHand) && isNamedPlayerHead(itemInHand, "§cDragoptics Lost Eyes")) {
                event.setCancelled(true);
                if (clickedBlock != null && clickedBlock.getType() == Material.END_PORTAL_FRAME) {
                    if (checkDragon.getDragonIsSpawned() == true) {
                        player.sendMessage("§cDrago already spawned!");
                        return;
                    }
                    if (player.getWorld().getName().equalsIgnoreCase("Drago")) {
                        long currentTimeMillis = System.currentTimeMillis();
                        long lastInteraction = lastInteractionTime.getOrDefault(player, 0L);

                        // Check if the cooldown has passed
                        if (currentTimeMillis - lastInteraction >= cooldownTimeMillis) {
                            lastInteractionTime.put(player, currentTimeMillis); // Update last interaction time

                            // Your existing code for placing and removing eyes goes here
                        } else {
                            player.sendMessage("§cYou must wait before interacting with Dragoptics Lost Eyes again.");
                            return;
                        }
                        event.setCancelled(true);
                        // Check if the player is holding the specified player head

                        int eyesPlaced = plugin.getPlayerEyesPlaced(player);
                        Location clickedBlockLocation = clickedBlock.getLocation();
                        Player eyeOwner = eyesOwnersMap.get(clickedBlockLocation);

                        // Check if the location of the clicked block is not present in the eyesPlacedOnBlock map
                        if (!eyesPlacedOnBlock.containsKey(clickedBlockLocation)) {
                            // Check if the player has placed less than 8 eyes
                            if (totalEyesPlaced < 8) {
                                // Place an Eye of Ender in the End Portal Frame
                                EndPortalFrame endPortalFrame = (EndPortalFrame) clickedBlock.getBlockData();
                                endPortalFrame.setEye(true);
                                clickedBlock.setBlockData(endPortalFrame);

                                setEyesPlacedOnBlock(clickedBlock.getLocation(), player);

                                // Increment the eyes placed counter
                                plugin.setPlayerEyesPlaced(player, eyesPlaced + 1);

                                totalEyesPlaced++;

                                // Set the eye owner if not already set

                                setEyeOwner(clickedBlock.getLocation(), player);

                                // Broadcast a message to all players
                                Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fplaced §cDragoptics Lost Eyes §f! §7(§7" + totalEyesPlaced + "/8)");
                                playSoundToAllPlayer(Sound.ENTITY_ENDERMAN_TELEPORT, 100, 2f);
                                itemInHand.setAmount(itemInHand.getAmount() - 1);

                                // Check if 8 eyes have been placed
                                if (totalEyesPlaced == 8) {
                                    // Execute console command "/mm m spawn"
                                    checkDragon.setDragonIsSpawned(true);
                                    Bukkit.broadcastMessage("§c〖☤〗 §cDrago §fwill spawned!");
                                    playSoundToAllPlayer(Sound.ENTITY_ENDER_DRAGON_GROWL, 100, 0);
                                    plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), "mm m spawn Drago 1 Drago,0,263,0");

                                }
                                return;
                            } else {
                                // Player has already placed 8 eyes, handle accordingly
                                // (You may want to notify the player or take other actions)
                            }
                        }

                    }
                }
            }
            if (clickedBlock != null && clickedBlock.getType() == Material.END_PORTAL_FRAME) {
                if (eyesOwnersMap.containsKey(clickedBlock.getLocation())) {
                    Player owner = eyesOwnersMap.get(clickedBlock.getLocation());
                    if (owner.equals(player)) {
                        // The player who clicked the block is the owner
                        if (!checkDragon.dragonIsSpawned) {
                            // Check if the player is the owner of the eye in the clicked portal frame
                            // Remove the Eye of Ender from the End Portal Frame
                            EndPortalFrame endPortalFrame = (EndPortalFrame) clickedBlock.getBlockData();
                            endPortalFrame.setEye(false);
                            clickedBlock.setBlockData(endPortalFrame);
                            clickedBlock.setMetadata("PlacedEyes", new FixedMetadataValue(plugin, true));

                            Location clickedBlockLocation = clickedBlock.getLocation();
                            int eyesPlaced = plugin.getPlayerEyesPlaced(player);

                            eyesPlacedOnBlock.remove(clickedBlockLocation);
                            eyesOwnersMap.remove(clickedBlockLocation);

                            // Decrement the eyes placed counter
                            plugin.setPlayerEyesPlaced(player, eyesPlaced - 1);

                            Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fremoved §cDragoptics Lost Eyes §f! §7(§7" + (eyesPlaced - 1) + "/8)");
                            playSoundToAllPlayer(Sound.ENTITY_ENDERMAN_TELEPORT, 100, 0f);

                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give material DRAGOPTICS_LOST_EYES " + player.getName() + " 1");

                            totalEyesPlaced--;
                        }
                    }
                }
            }

        }
    }

    // Add this method to set the eye owner in the eyesOwnersMap
    public void setEyeOwner(Location location, Player player) {
        eyesOwnersMap.put(location, player);
    }

    private boolean isPlayerHead(ItemStack item) {
        return item.getType() == Material.PLAYER_HEAD;
    }

    private boolean isNamedPlayerHead(ItemStack item, String name) {
        return item.getItemMeta() != null && item.getItemMeta().hasDisplayName() &&
                item.getItemMeta().getDisplayName().equals(name);
    }

    public void setEyesPlacedOnBlock(Location location, Player player) {
        eyesPlacedOnBlock.put(location, player);
    }

    public boolean isEyeOwner(Player player, Block block) {
        // Check if the block location is present in the eyesOwnersMap
        Location blockLocation = block.getLocation();
        if (eyesOwnersMap.containsKey(blockLocation)) {
            // Check if the stored owner matches the current player
            return eyesOwnersMap.get(blockLocation).equals(player);
        }
        return false;
    }

    public void playSoundToAllPlayer(Sound sound, float volume, float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }

    public synchronized void clearAllData() {
        eyesOwnersMap.clear();
        eyesPlacedOnBlock.clear();
        plugin.eyesPlacedMap.clear();
        totalEyesPlaced = 0;
        checkDragon.setDragonIsSpawned(false);
        revertEndPortalFrames();
    }

    public void revertEndPortalFrames() {
        // Iterate through all loaded chunks in the specified world
        World dragoWorld = Bukkit.getWorld("Drago");

        // Specify the location of the main End Portal Frame
        Location frame1Location = new Location(dragoWorld, -2, 260, -1);

        // Define the range to check for nearby blocks
        int range = 10;

        // Get the block at the specified location
        Block frame1Block = dragoWorld.getBlockAt(frame1Location);

        // Check if the block is an End Portal Frame
        if (frame1Block.getType() == Material.END_PORTAL_FRAME) {
            // Set the state of the main End Portal Frame to have no eye
            EndPortalFrame frame1 = (EndPortalFrame) frame1Block.getBlockData();
            frame1.setEye(false);
            frame1Block.setBlockData(frame1);

            // Iterate through nearby blocks in the specified range
            for (int x = -range; x <= range; x++) {
                for (int y = -range; y <= range; y++) {
                    for (int z = -range; z <= range; z++) {
                        // Get the block at the current location
                        Block nearbyBlock = dragoWorld.getBlockAt(frame1Location.clone().add(x, y, z));

                        // Check if the block is an End Portal Frame
                        if (nearbyBlock.getType() == Material.END_PORTAL_FRAME) {
                            // Set the state of the nearby End Portal Frame to have no eye
                            EndPortalFrame nearbyFrame = (EndPortalFrame) nearbyBlock.getBlockData();
                            nearbyFrame.setEye(false);
                            nearbyBlock.setBlockData(nearbyFrame);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDragoDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase("§c§lDrago")) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                clearAllData();
                Bukkit.broadcastMessage("§c〖☤〗 §fAltar can be used again!");
            }, 200L); // 20 ticks (1 second) * 2 = 40 ticks (2 seconds)
        }
    }
}
