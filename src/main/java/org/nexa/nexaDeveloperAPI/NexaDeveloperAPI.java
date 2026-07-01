package org.nexa.nexaDeveloperAPI;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class NexaDeveloperAPI extends JavaPlugin implements Listener {

    private NexaAPI nexaAPI;
    private FileManager manager;

    @Override
    public void onEnable() {
        this.manager = new FileManager(this);
        this.nexaAPI = new NexaAPI(this, manager);

        getServer().getPluginManager().registerEvents(this, this);

        getLogger().info("Nexa Developer API Starting...");
    }

    public NexaAPI getNexaAPI() {
        return nexaAPI;
    }

    @Override
    public void onDisable() {
        getLogger().info("Nexa Developer API Shutting down...");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use Nexa commands!");
            return true;
        }

        Player player = (Player) sender;

        if (label.equalsIgnoreCase("redeem")) {
            if (args.length < 1) {
                player.sendMessage("§cUsage: /redeem <code>");
                return true;
            }
            String code = args[0];
            player.sendMessage("§aContacting Nexa Platform...");
            
            // Run async to prevent lagging the server
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                JsonObject response = nexaAPI.redeemPromoCode(player, code);
                
                // Sync back to run messaging/effects
                Bukkit.getScheduler().runTask(this, () -> {
                    if (response.get("status").getAsBoolean()) {
                        player.sendMessage("§a§l[Nexa] §eSuccessfully redeemed code: §b" + code.toUpperCase());
                        String type = response.get("rewardType").getAsString();
                        String val = response.get("rewardValue").getAsString();
                        
                        if (type.equalsIgnoreCase("bucks")) {
                            player.sendMessage("§a+ " + val + " Nexa Bucks added to your account!");
                        } else {
                            player.sendMessage("§a+ Exclusive Cosmetic unlocked!");
                        }
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                        try {
                            player.spawnParticle(Particle.HAPPY_VILLAGER, player.getLocation().add(0, 1, 0), 20, 0.5, 0.5, 0.5, 0.1);
                        } catch (Throwable t) {}
                    } else {
                        player.sendMessage("§c§l[Nexa] §cFailed to redeem: " + response.get("info").getAsString());
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                });
            });
            return true;
        }

        if (label.equalsIgnoreCase("cosmetics")) {
            player.sendMessage("§aOpening Nexa Wardrobe...");
            
            Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
                JsonArray owned = nexaAPI.getOwnedCosmetics(player.getName());
                
                Bukkit.getScheduler().runTask(this, () -> {
                    Inventory inv = Bukkit.createInventory(null, 27, "§d§lNexa Wardrobe");
                    
                    ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                    ItemMeta fillerMeta = filler.getItemMeta();
                    if (fillerMeta != null) {
                        fillerMeta.setDisplayName(" ");
                        filler.setItemMeta(fillerMeta);
                    }
                    for (int i = 0; i < 27; i++) {
                        inv.setItem(i, filler);
                    }

                    int slot = 10;
                    for (JsonElement el : owned) {
                        if (slot > 16) break;
                        JsonObject cosmetic = el.getAsJsonObject();
                        
                        ItemStack item = new ItemStack(Material.GOLDEN_CARROT);
                        ItemMeta meta = item.getItemMeta();
                        if (meta != null) {
                            meta.setDisplayName("§b§l" + cosmetic.get("name").getAsString());
                            List<String> lore = new ArrayList<>();
                            lore.add("§7" + cosmetic.get("description").getAsString());
                            lore.add("");
                            lore.add("§d§lOwned Cosmetic");
                            lore.add("§eClick to equip in lobby");
                            lore.add("§8ID: " + cosmetic.get("id").getAsString());
                            meta.setLore(lore);
                            item.setItemMeta(meta);
                        }
                        inv.setItem(slot++, item);
                    }
                    
                    player.openInventory(inv);
                    player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 0.8f, 1.0f);
                });
            });
            return true;
        }

        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("§d§lNexa Wardrobe")) {
            event.setCancelled(true);
            
            ItemStack clicked = event.getCurrentItem();
            if (clicked == null || clicked.getType() == Material.AIR || clicked.getType() == Material.GRAY_STAINED_GLASS_PANE) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            ItemMeta meta = clicked.getItemMeta();
            if (meta != null && meta.hasDisplayName() && meta.hasLore()) {
                String cosmeticName = meta.getDisplayName();
                
                player.closeInventory();
                player.sendMessage("§a§l[Nexa] §eEquipped cosmetic " + cosmeticName + " §esuccessfully!");
                
                player.playSound(player.getLocation(), Sound.ENTITY_EVOKER_PREPARE_ATTACK, 1.0f, 1.2f);
                try {
                    player.spawnParticle(Particle.WITCH, player.getLocation().add(0, 1, 0), 30, 0.5, 1.0, 0.5, 0.15);
                } catch (Throwable t) {}
            }
        }
    }
}
