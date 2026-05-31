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
            JsonObject response = httpManager.POSTRequest("http://api.playnexa.lol/api/test/gameToken", obj);

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
        JsonObject response = httpManager.POSTRequest("http://api.playnexa.lol/api/txn/createGamepassTxn", obj);

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

        JsonObject response = httpManager.POSTRequest("http://api.playnexa.lol/api/gamepasses/ownsGamepass", obj);

        if (response.get("status").getAsBoolean()) {
            return response.get("data").getAsBoolean();
        } else {
            return false;
        }
    }
}
