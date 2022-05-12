package me.kodysimpson.stattracker.db;

import me.kodysimpson.stattracker.model.PlayerStats;

import java.sql.*;

public class Database {

    private Connection connection;

    public Connection getConnection() throws SQLException {

        if(connection != null){
            return connection;
        }

        //Try to connect to my MySQL database running locally
        String url = "jdbc:mysql://localhost/stat_tracker";
        String user = "root";
        String password = "";

        Connection connection = DriverManager.getConnection(url, user, password);

        this.connection = connection;

        System.out.println("Connected to database.");

        return connection;
    }

    public void initializeDatabase() throws SQLException {

        Statement statement = getConnection().createStatement();

        //Create the player_stats table
        String sql = "CREATE TABLE IF NOT EXISTS player_stats (uuid varchar(36) primary key, deaths int, kills int, blocks_broken long, balance double, last_login DATE, last_logout DATE)";

        statement.execute(sql);

        statement.close();

    }

    public PlayerStats findPlayerStatsByUUID(String uuid) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("SELECT * FROM player_stats WHERE uuid = ?");
        statement.setString(1, uuid);

        ResultSet resultSet = statement.executeQuery();

        PlayerStats playerStats;

        if(resultSet.next()){

            playerStats = new PlayerStats(resultSet.getString("uuid"), resultSet.getInt("deaths"), resultSet.getInt("kills"), resultSet.getLong("blocks_broken"), resultSet.getDouble("balance"), resultSet.getDate("last_login"), resultSet.getDate("last_logout"));

            statement.close();

            return playerStats;
        }

        statement.close();

        return null;
    }

    public void createPlayerStats(PlayerStats playerStats) throws SQLException {

        PreparedStatement statement = getConnection()
                .prepareStatement("INSERT INTO player_stats(uuid, deaths, kills, blocks_broken, balance, last_login, last_logout) VALUES (?, ?, ?, ?, ?, ?, ?)");
        statement.setString(1, playerStats.getPlayerUUID());
        statement.setInt(2, playerStats.getDeaths());
        statement.setInt(3, playerStats.getKills());
        statement.setLong(4, playerStats.getBlocksBroken());
        statement.setDouble(5, playerStats.getBalance());
        statement.setDate(6, new Date(playerStats.getLastLogin().getTime()));
        statement.setDate(7, new Date(playerStats.getLastLogout().getTime()));

        statement.executeUpdate();

        statement.close();

    }

    public void updatePlayerStats(PlayerStats playerStats) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("UPDATE player_stats SET deaths = ?, kills = ?, blocks_broken = ?, balance = ?, last_login = ?, last_logout = ? WHERE uuid = ?");
        statement.setInt(1, playerStats.getDeaths());
        statement.setInt(2, playerStats.getKills());
        statement.setLong(3, playerStats.getBlocksBroken());
        statement.setDouble(4, playerStats.getBalance());
        statement.setDate(5, new Date(playerStats.getLastLogin().getTime()));
        statement.setDate(6, new Date(playerStats.getLastLogout().getTime()));
        statement.setString(7, playerStats.getPlayerUUID());

        statement.executeUpdate();

        statement.close();

    }

    public void deletePlayerStats(PlayerStats playerStats) throws SQLException {

        PreparedStatement statement = getConnection().prepareStatement("DELETE FROM player_stats WHERE uuid = ?");
        statement.setString(1, playerStats.getPlayerUUID());

        statement.executeUpdate();

        statement.close();

    }

}

