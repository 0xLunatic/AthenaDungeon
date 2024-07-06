package lunatic.athenadungeon.ancientdungeon;

import lunatic.athenadungeon.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WaterListener implements Listener {
    private Main plugin;

    public WaterListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();

        // Get the block at the player's location
        Block block = player.getLocation().getBlock();

        // Check if the player is in the "worldDungeon" world and standing in water
        if (world.getName().equalsIgnoreCase("worldDungeon") && (block.getType() == Material.WATER)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.POISON, 500 * 5, 150));
            player.damage(0.5);
        }
    }
    @EventHandler
    public void onPlayerDeathByWater(PlayerDeathEvent event){
        Player player = event.getEntity().getPlayer();
        World world = player.getWorld();

        if (world.getName().equalsIgnoreCase("worldDungeon")){
            if (player.getLocation().getBlock().getType() == Material.WATER){
                event.setDeathMessage("§f" +player.getName() + " trying to swim on §3Seawater Poison Dungeon");
            }
        }
    }

}
