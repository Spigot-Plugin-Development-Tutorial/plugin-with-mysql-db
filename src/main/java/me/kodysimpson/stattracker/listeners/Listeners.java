package me.kodysimpson.stattracker.listeners;

import me.kodysimpson.stattracker.db.Database;
import me.kodysimpson.stattracker.model.PlayerStats;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.Date;

public class Listeners implements Listener {

    private final Database database;

    public Listeners(Database database) {
        this.database = database;
    }

    public PlayerStats getPlayerStatsFromDatabase(Player player) throws SQLException {

        PlayerStats playerStats = database.findPlayerStatsByUUID(player.getUniqueId().toString());

        if (playerStats == null) {
            playerStats = new PlayerStats(player.getUniqueId().toString(), 0, 0, 0, 0.0, new Date(), new Date());
            database.createPlayerStats(playerStats);
        }

        return playerStats;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player p = event.getPlayer();
        try{
            PlayerStats playerStats = getPlayerStatsFromDatabase(p);
            playerStats.setLastLogin(new Date());
            database.updatePlayerStats(playerStats);
        }catch (SQLException e){
            e.printStackTrace();
            System.out.println("Could not update player stats after join.");
        }

    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e){

        Player p = e.getPlayer();
        try{
            PlayerStats playerStats = getPlayerStatsFromDatabase(p);
            playerStats.setLastLogout(new Date());
            database.updatePlayerStats(playerStats);
        }catch (SQLException e1){
            e1.printStackTrace();
            System.out.println("Could not update player stats after quit.");
        }

    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {

        Player p = e.getPlayer();

        try {
            PlayerStats playerStats = getPlayerStatsFromDatabase(p);
            playerStats.setBlocksBroken(playerStats.getBlocksBroken() + 1);
            playerStats.setBalance(playerStats.getBalance() + 0.50);
            database.updatePlayerStats(playerStats);
        } catch (SQLException e1) {
            e1.printStackTrace();
            System.out.println("Could not update player stats after block break.");
        }

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){

        //see if the player was killed by another player
        if(e.getEntity().getKiller() == null){
            return;
        }

        Player killer = e.getEntity().getKiller();
        Player p = e.getEntity();

        //Update the stats of both people
        try{
            PlayerStats killerStats = getPlayerStatsFromDatabase(killer);
            PlayerStats pStats = getPlayerStatsFromDatabase(p);

            killerStats.setKills(killerStats.getKills() + 1);
            killerStats.setBalance(killerStats.getBalance() + 1.0);

            pStats.setDeaths(pStats.getDeaths() + 1);
            pStats.setBalance(pStats.getBalance() - 1.0);

            database.updatePlayerStats(killerStats);
            database.updatePlayerStats(pStats);

        }catch (SQLException e1){
            e1.printStackTrace();
            System.out.println("Could not update player stats after death.");
        }

    }

}
