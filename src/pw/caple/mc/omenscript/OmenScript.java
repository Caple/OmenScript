package pw.caple.mc.omenscript;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.input.KeyBindingEvent;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.keyboard.BindingExecutionDelegate;
import org.getspout.spoutapi.keyboard.Keyboard;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Plugin entry point.
 */
public final class OmenScript extends JavaPlugin {

	private static OmenScript instance;

	/**
	 * Get's instance of plugin or null if not enabled.
	 */
	public static OmenScript getInstance() {
		return instance;
	}

	private boolean skipDisable;
	private QuestTracker questTracker;
	private ThirdPartyIntegration thirdPartyIntegration;
	private Properties dbProperties;
	private String dbURL;

	private ScriptManager manager;

	@Override
	public void onEnable() {
		instance = this;
		thirdPartyIntegration = new ThirdPartyIntegration();

		try {
			Class.forName("org.sqlite.JDBC");
		} catch (ClassNotFoundException e1) {
			getLogger().warning("SQLite driver not loaded.");
		}

		if (!new File(getDataFolder().toString()).exists()) {
			saveDefaultConfig();
			getLogger().info("Default config file created.");
		}

		FileConfiguration config = getConfig();
		Boolean useMySQL = config.getBoolean("usemysql");
		if (useMySQL) {
			String dbUsername = config.getString("mysql.username");
			String dbPassword = config.getString("mysql.password");
			dbURL = config.getString("mysql.url");
			if (dbUsername == null || dbPassword == null || dbURL == null) {
				getLogger().severe("MySQL config information invalid or missing.");
				skipDisable = true;
				getServer().getPluginManager().disablePlugin(this);
				return;
			}
			dbProperties = new Properties();
			dbProperties.put("user", dbUsername);
			dbProperties.put("password", dbPassword);
			dbProperties.put("autoReconnect", "true");
		} else {
			dbURL = "jdbc:sqlite:" + getDataFolder() + "/data.db";
			dbProperties = new Properties();
			dbProperties.put("user", "omenscript");
			dbProperties.put("password", "omenscript");
			dbProperties.put("autoReconnect", "true");
		}

		try {
			Connection connection = getDBConnection();
			if (connection == null) {
				skipDisable = true;
				getPluginLoader().disablePlugin(this);
				return;
			}
			Statement statement = connection.createStatement();
			ResultSet result;
			if (useMySQL) {
				result = statement.executeQuery("SHOW TABLES LIKE 'quests';");
			} else {
				result = statement.executeQuery("SELECT * FROM sqlite_master WHERE name ='quests' and type='table';");
			}
			if (!result.next()) {
				String createTable = "CREATE  TABLE `quests` (" +
						"`quest` VARCHAR(45) NOT NULL ," +
						"`player` VARCHAR(16) NOT NULL ," +
						"`timesCompleted` INT NOT NULL ," +
						"`step` INT NOT NULL ," +
						"`objectives` VARCHAR(45) NULL ," +
						"PRIMARY KEY (`player`, `quest`) );";
				statement.execute(createTable);
				getLogger().log(Level.INFO, "Default MySQL database structure created.");
			}
			result.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			getLogger().severe(e.getMessage());
			getLogger().severe("Could not create quests table.");
		}

		SpoutManager.getKeyBindingManager().registerBinding("OmenScript.Journal", Keyboard.KEY_J, "Show Journal (Quest Log)", new BindingExecutionDelegate() {
			@Override
			public void keyPressed(KeyBindingEvent event) {
				if (event.getScreenType() == ScreenType.GAME_SCREEN || event.getScreenType() == ScreenType.UNKNOWN) {
					SpoutPlayer player = event.getPlayer();
					player.getMainScreen().attachPopupScreen(new JournalScreen(player));
				}
			}

			@Override
			public void keyReleased(KeyBindingEvent event) {}
		}, this);

		questTracker = new QuestTracker();
		manager = new ScriptManager(questTracker, getDataFolder().getAbsolutePath());
		getCommand("scripts").setExecutor(new Commands(manager));
		Bukkit.getServer().getPluginManager().registerEvents(new GeneralEvents(manager), this);
		Bukkit.getScheduler().scheduleSyncDelayedTask(this, new Runnable() {
			@Override
			public void run() {
				InputUtil.reindexCustomItems();
				String reloadMessage = ChatColor.stripColor(manager.reload());
				OmenScript.getInstance().getLogger().info(reloadMessage);
			}
		}, 2L);

	}

	@Override
	public void onDisable() {
		instance = null;
		if (!skipDisable) {
			manager.unregister();
		}
	}

	QuestTracker getQuestTracker() {
		return questTracker;
	}

	ThirdPartyIntegration getThirdPartyPlugins() {
		return thirdPartyIntegration;
	}

	Connection getDBConnection() {
		try {
			return DriverManager.getConnection(dbURL, dbProperties);
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}

	ScriptManager getScriptManager() {
		return manager;
	}

}
