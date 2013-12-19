package pw.caple.mc.omenscript;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.OfflinePlayer;

/**
 * Contains all information about a specific stage of a quest.
 */
public final class QuestStep implements NeedsToBeUnregistered {

	private final List<QuestObjective> objectives = new ArrayList<>();
	private String description = "";

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void addObjective(QuestObjective objective) {
		objectives.add(objective);
	}

	public List<QuestObjective> getAllObjectives() {
		return objectives;
	}

	public boolean hasCompleted(OfflinePlayer player) {
		for (QuestObjective objective : objectives) {
			if (!objective.hasBeenCompletedBy(player)) return false;
		}
		return true;
	}

	public void addPlayer(OfflinePlayer player) {
		for (QuestObjective objective : objectives) {
			objective.startEvaluating(player);
		}
	}

	public void removePlayer(OfflinePlayer player) {
		for (QuestObjective objective : objectives) {
			objective.stopEvaluating(player);
		}
	}

	@Override
	public void unregister() {
		for (QuestObjective objective : objectives) {
			objective.unregister();
		}
	}

}
