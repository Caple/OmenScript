package pw.caple.mc.omenscript;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.bukkit.ChatColor;

/**
 * Represents all of the information from a set of scripts and is reloadable.
 */
public final class ScriptManager implements NeedsToBeUnregistered {

	private final HashMap<String, Quest> quests = new HashMap<>();
	private final HashMap<String, List<DialogNode>> dialog = new HashMap<>();
	private final LocationManager locations = new LocationManager();
	private final QuestTracker tracker;
	private final String baseDirectory;
	private int scriptsCount = 0;

	public ScriptManager(QuestTracker tracker, String baseDirectory) {
		this.baseDirectory = baseDirectory;
		this.tracker = tracker;
	}

	final LocationManager getLocationManager() {
		return locations;
	}

	final void addDialog(String npcName, DialogNode node) {
		if (dialog.containsKey(npcName)) {
			dialog.get(npcName).add(node);
		} else {
			ArrayList<DialogNode> list = new ArrayList<>();
			list.add(node);
			dialog.put(npcName, list);
		}
	}

	final List<DialogNode> getDialog(String npcName) {
		return dialog.get(npcName);
	}

	final Quest getOrCreateQuest(String title) {
		if (quests.containsKey(title)) {
			return quests.get(title);
		} else {
			Quest quest = new Quest(title, tracker);
			quests.put(title, quest);
			return quest;
		}
	}

	/**
	 * Reloads all scripts.
	 * 
	 * @return returns load message
	 */
	final public String reload() {
		long before = System.nanoTime();
		for (Quest quest : quests.values()) {
			quest.untrackAll();
		}
		unregister();
		locations.clear();
		dialog.clear();
		quests.clear();
		scriptsCount = 0;
		FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				String lowerCase = name.toLowerCase();
				if (!lowerCase.equals("config.yml") && !lowerCase.equals("data.db")) {
					return true;
				} else {
					return false;
				}
			}
		};
		List<List<ScriptNode>> allNodes = new ArrayList<>();
		ScriptReader reader = new ScriptReader();
		File scriptFolder = new File(baseDirectory);
		for (File file : scriptFolder.listFiles(filter)) {
			List<ScriptNode> nodes = reader.read(file);
			if (nodes != null) {
				allNodes.add(nodes);
			}
			scriptsCount++;
		}
		ScriptCompiler compiler = new ScriptCompiler(this);
		for (List<ScriptNode> nodes : allNodes) {
			compiler.compile(nodes);
		}
		for (Quest quest : quests.values()) {
			quest.loadAllPlayerData();
		}

		//generate reload message
		int errorCount = compiler.getErrorCount();
		long after = System.nanoTime();
		int miliseconds = (int) Math.round((after - before) / 1E6);
		String start;
		if (scriptsCount != 1) {
			start = ChatColor.GOLD + "Reloaded " + scriptsCount + " scripts" + ChatColor.WHITE + " [";
		} else {
			start = ChatColor.GOLD + "Reloaded 1 script" + ChatColor.WHITE + " [";
		}
		String end = ChatColor.WHITE + "]";
		String time;
		String errors;
		if (miliseconds != 1) {
			time = ChatColor.AQUA + String.valueOf(miliseconds) + " milliseconds";
		} else {
			time = ChatColor.AQUA + "1 millisecond";
		}
		if (errorCount > 0) {
			if (errorCount != 1) {
				errors = ChatColor.WHITE + ", " + ChatColor.RED + errorCount + " errors";
			} else {
				errors = ChatColor.WHITE + ", " + ChatColor.RED + "1 error";
			}
		} else {
			errors = "";
		}
		return start + time + errors + end;
	}

	/**
	 * Loads a specific script. Reloads it if it is already loaded.
	 * 
	 * @return number of script errors
	 */
	final public int load(String scriptPath) {
		return 1;
		//TODO: finish load specific
	}

	/**
	 * Unloads a specific script. Ignores request if script is not loaded.
	 */
	final public void unload(String scriptPath) {
		return;
		//TODO: finish unload specific
	}

	/**
	 * Gets the number of scripts currently loaded into this manager.
	 */
	final public int getScriptsCount() {
		return scriptsCount;
	}

	@Override
	public void unregister() {
		for (Quest quest : quests.values()) {
			quest.unregister();
		}
	}
}
