package pw.caple.mc.omenscript;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.OfflinePlayer;

final public class QuestTracker {

	private final HashMap<OfflinePlayer, Set<Quest>> playersOnQuests = new HashMap<>();

	final boolean isActive(OfflinePlayer player, Quest quest) {
		checkInit(player);
		Set<Quest> quests = playersOnQuests.get(player);
		return quests.contains(quest);
	}

	final Set<Quest> getAll(OfflinePlayer player) {
		checkInit(player);
		return playersOnQuests.get(player);
	}

	final void add(OfflinePlayer player, Quest quest) {
		checkInit(player);
		Set<Quest> quests = playersOnQuests.get(player);
		quests.add(quest);
	}

	final void remove(OfflinePlayer player, Quest quest) {
		checkInit(player);
		Set<Quest> quests = playersOnQuests.get(player);
		quests.remove(quest);
	}

	final private void checkInit(OfflinePlayer player) {
		if (!playersOnQuests.containsKey(player)) {
			playersOnQuests.put(player, new HashSet<Quest>());
		}
	}

}
