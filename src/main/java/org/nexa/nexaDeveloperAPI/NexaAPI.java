package org.nexa.nexaDeveloperAPI;

import com.google.gson.JsonObject;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class NexaAPI {
    private JavaPlugin plugin;
    private FileManager manager;
    private HTTPManager httpManager;
    private String token;

    public NexaAPI(JavaPlugin plugin, FileManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.httpManager = new HTTPManager();

        String token = manager.getToken();

        this.token = token;

        if (token.isBlank()) {
            plugin.getLogger().severe("Game Token not set in Config File. Please read the documentation!");
            plugin.getServer().shutdown();


        } else {
            JsonObject obj = new JsonObject();
            obj.addProperty("gameToken", token);
            JsonObject response = httpManager.POSTRequest("https://api.playnexa.lol/api/test/gameToken", obj);

            if (response.get("status").getAsBoolean()) {
                plugin.getLogger().info("Token is Verified and Is Valid!");
            } else {
                plugin.getLogger().severe("Token is Invalid!");
                plugin.getServer().shutdown();
            }
        }
    }

    /**
     * Create a Transaction for a Gamepass.
     * @param player The player Instance
     * @param gamepassID The Game pass ID, Obtained from the panel.
     * @return If Successful, Returns the ID Of Transaction, If not, Response will start with "ERROR_" and the error from server.
     */

    public String createGamepassTXN(Player player, String gamepassID) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameToken", token);
        obj.addProperty("username", player.getName());
        obj.addProperty("gamepassid", gamepassID);
        JsonObject response = httpManager.POSTRequest("https://api.playnexa.lol/api/txn/createGamepassTxn", obj);

        if (response.get("status").getAsBoolean()) {
            player.sendMessage(Component.text("[Nexa] Transaction Created, Click here to confirm!").clickEvent(ClickEvent.openUrl("http://localhost:3000/txn/" + response.get("id").getAsString())).color(TextColor.color(52, 70, 235)));

            return response.get("id").getAsString();
        } else {
            player.sendMessage(Component.text("[Nexa]" + response.get("info").getAsString()).color(TextColor.color(255, 0, 0)));

            return "ERROR_" + response.get("info").getAsString().toUpperCase();
        }

    }

    /**
     * Check if a User owns a Game Pass.
     * @param player The Player Instance.
     * @param gamepassId The Game Pass ID obtained from the panel.
     * @return True if the user owns the Game Pass, false if not.
     */

    public Boolean checkIfUserOwnsGamepass(Player player, String gamepassId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameId", token);
        obj.addProperty("gamepassId", gamepassId);
        obj.addProperty("username", player.getName());

        JsonObject response = httpManager.POSTRequest("https://api.playnexa.lol/api/gamepasses/ownsGamepass", obj);

        if (response.get("status").getAsBoolean()) {
            return response.get("data").getAsBoolean();
        } else {
            return false;
        }
    }

    /**
     * Add a data store to your game.
     * @param key The key / the name of the data store.
     * @param value The value of the data store.
     * @return A Boolean, True if success, False if not.
     */

    public Boolean addDataStore(String key, String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameToken", token);
        obj.addProperty("key", key);
        obj.addProperty("value", value);

        JsonObject resp = httpManager.POSTRequest("https://api.playnexa.lol/api/datastore/createDataStore", obj);

        return resp.get("status").getAsBoolean();
    }

    /**
     * Edit a data store.
     * @param key The name of the data store / the key.
     * @param value The value you want changed.
     * @return A Boolean, True if success, False if not.
     */

    public Boolean editDataStore(String key, String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameToken", token);
        obj.addProperty("key", key);
        obj.addProperty("value", value);

        JsonObject resp = httpManager.POSTRequest("https://api.playnexa.lol/api/datastore/editDataStore", obj);

        return resp.get("status").getAsBoolean();
    }

    /**
     * Get a Data Store
     * @param key The name of the Data Store.
     * @return The value of data store.
     */

    public String getDataStore(String key) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameToken", token);
        obj.addProperty("key", key);

        JsonObject resp = httpManager.POSTRequest("https://api.playnexa.lol/api/datastore/getDataStore", obj);

        return resp.get("data").getAsJsonObject().get("value").getAsString();
    }

    /**
     * Check if a User owns a custom Cosmetic.
     * @param player The Player Instance.
     * @param cosmeticId The Cosmetic ID.
     * @return True if the user owns the cosmetic, false if not.
     */
    public Boolean checkIfUserOwnsCosmetic(Player player, String cosmeticId) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameToken", token);
        obj.addProperty("cosmeticId", cosmeticId);
        obj.addProperty("username", player.getName());

        JsonObject response = httpManager.POSTRequest("https://api.playnexa.lol/api/cosmetics/ownsCosmetic", obj);

        if (response.get("status").getAsBoolean()) {
            return response.get("data").getAsBoolean();
        } else {
            return false;
        }
    }

    /**
     * Check if a User is a Cracked client.
     * @param player The Player Instance.
     * @return True if the user is cracked, false if premium.
     */
    public Boolean checkIfUserIsCracked(Player player) {
        JsonObject obj = new JsonObject();
        obj.addProperty("gameToken", token);
        obj.addProperty("username", player.getName());

        JsonObject response = httpManager.POSTRequest("https://api.playnexa.lol/api/users/checkCracked", obj);

        if (response.get("status").getAsBoolean()) {
            return response.get("cracked").getAsBoolean();
        } else {
            return false;
        }
    }

    /**
     * Redeem a Promo Code.
     * @param player The Player Instance.
     * @param code The promo code string.
     * @return JsonObject response from the backend.
     */
    public JsonObject redeemPromoCode(Player player, String code) {
        JsonObject obj = new JsonObject();
        obj.addProperty("code", code);
        obj.addProperty("username", player.getName());

        try {
            return httpManager.POSTRequest("https://api.playnexa.lol/api/codes/redeem", obj);
        } catch (Exception e) {
            JsonObject err = new JsonObject();
            err.addProperty("status", false);
            err.addProperty("info", "Failed to contact Nexa API");
            return err;
        }
    }

    /**
     * Get owned cosmetics details of a user.
     * @param username The player's username.
     * @return JsonArray of owned cosmetics.
     */
    public com.google.gson.JsonArray getOwnedCosmetics(String username) {
        JsonObject obj = new JsonObject();
        obj.addProperty("username", username);

        try {
            JsonObject response = httpManager.POSTRequest("https://api.playnexa.lol/api/cosmetics/owned", obj);
            if (response.get("status").getAsBoolean()) {
                return response.get("data").getAsJsonArray();
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to fetch owned cosmetics for " + username);
        }
        return new com.google.gson.JsonArray();
    }
}
