package lunatic.athenadungeon.theend;

import lunatic.athenadungeon.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;
import java.util.stream.Collectors;

public class DragoDeathListener extends DragonGetterSetter implements Listener {
    private Main plugin;
    private Map<String, Double> leaderboard;
    private List<Map.Entry<String, Double>> sortedEntries; // Declare sortedEntries at class level

    DragonGetterSetter checkDragon;

    public DragoDeathListener(Main plugin, DragonGetterSetter checkDragon) {
        this.plugin = plugin;
        this.leaderboard = new HashMap<>();
        this.checkDragon = checkDragon;
    }

    @EventHandler
    public void onDragoDeath(EntityDeathEvent event) {
        if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase("§c§lDrago")) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§c§lDrago §fis down!");
            Bukkit.broadcastMessage("");
            displayLeaderboard();
            giveRewards();
            plugin.eyesPlacedMap.clear();
            leaderboard.clear();
            sortedEntries.clear();
        }
    }

    @EventHandler
    public void onPlayerHitDrago(EntityDamageByEntityEvent event) {
        if (event.getEntity() == null || event.getDamager() == null) {
            return; // Check for null to avoid NullPointerException
        }

        if (event.getEntity().getCustomName() != null && event.getEntity().getCustomName().equalsIgnoreCase("§c§lDrago")) {
            if (event.getDamager() instanceof Player) {
                Player player = (Player) event.getDamager();
                increasePlayerScore(player.getName(), event.getDamage()); // Increase score by damage on hitting Drago
            } else if (event.getDamager() instanceof Projectile) {
                Projectile projectile = (Projectile) event.getDamager();

                if (projectile.getShooter() instanceof Player) {
                    Player player = (Player) projectile.getShooter();
                    increasePlayerScore(player.getName(), event.getDamage());
                }
            }
        }
    }

    private void increasePlayerScore(String playerName, double amount) {
        leaderboard.put(playerName, leaderboard.getOrDefault(playerName, 0.0) + amount);
    }

    private void displayLeaderboard() {
        Bukkit.broadcastMessage("§e╔══════════════════════╗");
        Bukkit.broadcastMessage("§6      §l===== Leaderboard =====");

        int rank = 1;
        int maxNameLength = 15; // Adjust as needed

        // Sort the entries by damage in descending order
        sortedEntries = leaderboard.entrySet()
                .stream()
                .sorted(Comparator.comparing(Map.Entry::getValue, Comparator.reverseOrder()))
                .collect(Collectors.toList());

        // Iterate up to the minimum of 5 and the size of sortedEntries
        int iterations = Math.min(5, sortedEntries.size());
        for (int i = 0; i < iterations; i++) {
            Map.Entry<String, Double> entry = sortedEntries.get(i);
            String playerName = entry.getKey();
            double score = entry.getValue();

            // Ensure a consistent length for the player name
            playerName = playerName.length() > maxNameLength ? playerName.substring(0, maxNameLength) : playerName;

            // Format the double value with one decimal place
            String formattedScore = String.format("%.1f", score);

            String message = String.format("      §e §e#%d   §7%-1s - §b%s Damage      ", rank, playerName, formattedScore);
            Bukkit.broadcastMessage(message);

            rank++;
        }

        Bukkit.broadcastMessage("§e╚══════════════════════╝");
    }
    private void giveRewards() {
        // Example: Give rewards based on leaderboard position
        for (Map.Entry<String, Double> entry : sortedEntries) {
            String playerName = entry.getKey();
            OfflinePlayer player = Bukkit.getOfflinePlayer(playerName);
            double score = entry.getValue();

            // Check the player's position and give rewards accordingly
            if (score > 0) {
                if (score == sortedEntries.get(0).getValue()) {
                    // First place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 5000");
                    dropReward(player);
                } else if (score == sortedEntries.get(1).getValue()) {
                    // Second place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 4000");
                    dropReward(player);
                } else if (score == sortedEntries.get(2).getValue()) {
                    // Third place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 3000");
                    dropReward(player);
                } else if (score == sortedEntries.get(3).getValue()) {
                    // Fourth place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 2000");
                    dropReward(player);
                } else if (score == sortedEntries.get(4).getValue()) {
                    // Fifth place reward
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "eco give " + playerName + " 1000");
                    dropReward(player);
                }
            }
        }
        leaderboard.clear();
    }

    public void dropReward(OfflinePlayer player) {
        if (player != null) {
            // Assuming you have a method to get the number of eyes placed by a player
            int eyesPlaced = plugin.getPlayerEyesPlaced(player.getPlayer());

            // Define the base chance and increase it based on the number of eyes placed
            double baseChance = 0.1; // 10% base chance
            double chanceIncreasePerEye = 0.1; // 5% increase per eye

            // Calculate the final chance
            double finalChance = baseChance + (eyesPlaced * chanceIncreasePerEye);

            // Generate a random number between 0 and 1
            double randomValue = Math.random();

            // Check if the random value is within the calculated chance

            // Reward scenarios based on leaderboard position
            int playerRank = getPlayerRank(player.getName());

            if (playerRank == 1) {
                if (randomValue <= finalChance) {
                    if (eyesPlaced < 2) {
                        player.getPlayer().sendMessage("§c〖☤〗 §fKamu setidaknya perlu memasang §c2 Dragoptics Lost Eyes §funtuk mendapatkan §eDrop Special§f!");
                    }
                    String item = getRandomItemPerPlace(1, eyesPlaced);
                    if (!item.equalsIgnoreCase("NULL")) {
                        if (item.contains("ARTIFACT")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                        }
                        String itemBroadcast = formatItemName(item);
                        Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                        playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                    }
                }
            } else if (playerRank == 2) {
                if (randomValue <= finalChance) {
                    String item = getRandomItemPerPlace(2, eyesPlaced);
                    if (!item.equalsIgnoreCase("NULL")) {
                        if (item.contains("ARTIFACT")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                        }
                        String itemBroadcast = formatItemName(item);
                        Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                        playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                    }
                }
            } else if (playerRank == 3) {
                if (randomValue <= finalChance) {
                    String item = getRandomItemPerPlace(3, eyesPlaced);
                    if (!item.equalsIgnoreCase("NULL")) {
                        if (item.contains("ARTIFACT")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                        }
                        String itemBroadcast = formatItemName(item);
                        Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                        playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                    }
                }
            } else if (playerRank == 4) {
                if (randomValue <= finalChance) {
                    String item = getRandomItemPerPlace(4, eyesPlaced);
                    if (!item.equalsIgnoreCase("NULL")) {
                        if (item.contains("ARTIFACT")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                        }
                        String itemBroadcast = formatItemName(item);
                        Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                        playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                    }
                }
            } else if (playerRank == 5) {
                if (randomValue <= finalChance) {
                    String item = getRandomItemPerPlace(5, eyesPlaced);
                    if (!item.equalsIgnoreCase("NULL")) {
                        if (item.contains("ARTIFACT")) {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give TALISMAN " + item + " " + player.getName() + " 1");
                        } else {
                            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi give ARMOR " + item + " " + player.getName() + " 1");
                        }
                        String itemBroadcast = formatItemName(item);
                        Bukkit.broadcastMessage("§c〖☤〗 §e" + player.getName() + " §fbaru saja mendapatkan §c" + itemBroadcast + "§f!");
                        playSoundToAllPlayers(Sound.ENTITY_WITHER_SHOOT, 100, 0f);
                    }
                }
            }
        }
    }
    private String formatItemName(String itemName) {
        String[] words = itemName.toLowerCase().split("_");
        StringBuilder formattedName = new StringBuilder();

        for (String word : words) {
            if (word.length() > 0) {
                formattedName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1)).append(" ");
            }
        }

        return formattedName.toString().trim();
    }

    private String getRandomItemPerPlace(int place, int eyesPlaced) {
        // Define items and their corresponding chances
        Map<String, Double> itemChances = new HashMap<>();
        itemChances.put("DRAGON_ARTIFACT", 0.02);
        if (eyesPlaced >= 2) {
            if (place == 1) {
                itemChances.put("DRAGOPTICS_HELMET", 0.4);   // 40% chance
                itemChances.put("DRAGOPTICS_CHESTPLATE", 0.1);  // 10% chance
                itemChances.put("DRAGOPTICS_LEGGINGS", 0.2);   // 20% chance
                itemChances.put("DRAGOPTICS_BOOTS", 0.3);  // 30% chance
            }
            if (place == 2) {
                itemChances.put("DRAGOPTICS_HELMET", 0.4);
                itemChances.put("DRAGOPTICS_LEGGINGS", 0.3);
                itemChances.put("DRAGOPTICS_BOOTS", 0.3);
            }
            if (place == 3) {
                itemChances.put("DRAGOPTICS_HELMET", 0.3);
                itemChances.put("DRAGOPTICS_LEGGINGS", 0.3);
                itemChances.put("DRAGOPTICS_BOOTS", 0.4);
            }
            if (place == 4) {
                itemChances.put("DRAGOPTICS_HELMET", 0.4);
                itemChances.put("DRAGOPTICS_LEGGINGS", 0.1);
                itemChances.put("DRAGOPTICS_BOOTS", 0.5);
            }
            if (place == 5) {
                itemChances.put("DRAGOPTICS_HELMET", 0.5);
                itemChances.put("DRAGOPTICS_BOOTS", 0.5);
            }
        }


        // Normalize chances to ensure they add up to 1.0
        double totalChance = itemChances.values().stream().mapToDouble(Double::doubleValue).sum();
        for (Map.Entry<String, Double> entry : itemChances.entrySet()) {
            entry.setValue(entry.getValue() / totalChance);
        }

        // Generate a random number between 0 and 1
        double randomValue = Math.random();

        // Iterate through items and check if the random value falls within their chance range
        double cumulativeChance = 0.0;
        for (Map.Entry<String, Double> entry : itemChances.entrySet()) {
            cumulativeChance += entry.getValue();
            if (randomValue <= cumulativeChance) {
                return entry.getKey();
            }
        }

        // Default to returning "helm" if no item is selected (this should not happen)
        return "NULL";
    }

    private int getPlayerRank(String playerName) {
        // Find the player's rank based on the sorted leaderboard entries
        for (int i = 0; i < sortedEntries.size(); i++) {
            if (sortedEntries.get(i).getKey().equalsIgnoreCase(playerName)) {
                return i + 1; // Rank is 1-indexed
            }
        }
        return -1; // Player not found in the leaderboard
    }
    public void playSoundToAllPlayers(Sound sound, float volume, float pitch){
        for (Player player : Bukkit.getOnlinePlayers()){
            player.playSound(player.getLocation(), sound, volume, pitch);
        }
    }
}

