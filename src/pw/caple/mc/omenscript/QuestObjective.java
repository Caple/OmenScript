package pw.caple.mc.omenscript;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

/**
 * An objective that needs to be met before a quest can be turned in.
 */
public abstract class QuestObjective implements Listener, NeedsToBeUnregistered {

	final private Quest quest;
	final private Map<OfflinePlayer, Integer> progressValues = new HashMap<>();
	final private Map<OfflinePlayer, Boolean> hasReachedGoal = new HashMap<>();
	final private int goalValue;

	public QuestObjective(Quest quest, int goalValue) {
		this.goalValue = goalValue;
		this.quest = quest;
		Bukkit.getServer().getPluginManager().registerEvents(this, OmenScript.getInstance());
	}

	@Override
	final public void unregister() {
		HandlerList.unregisterAll(this);
	}

	final public void startEvaluating(OfflinePlayer player) {
		if (!progressValues.containsKey(player)) {
			progressValues.put(player, 0);
			hasReachedGoal.put(player, false);
			onPlayerAdded(player);
		}
	}

	final public void stopEvaluating(OfflinePlayer player) {
		progressValues.remove(player);
		hasReachedGoal.remove(player);
		onPlayerRemoved(player);
	}

	final public int getProgressValue(OfflinePlayer player) {
		if (isEvaluating(player)) {
			return progressValues.get(player);
		} else {
			return -1;
		}
	}

	final public boolean hasBeenCompletedBy(OfflinePlayer player) {
		if (isEvaluating(player)) {
			return hasReachedGoal.get(player);
		} else {
			return false;
		}

	}

	final public boolean isEvaluating(OfflinePlayer player) {
		return progressValues.containsKey(player);
	}

	final protected void setProgress(OfflinePlayer player, int value) {
		if (progressValues.get(player) == value) return;
		if (value > goalValue) value = goalValue;
		progressValues.put(player, value);
		addProgress(player, 0);
	}

	final protected void setProgressNoSaveUnlessComplete(OfflinePlayer player, int value) {
		if (progressValues.get(player) == value) return;
		if (value > goalValue) value = goalValue;
		progressValues.put(player, value);
		if (value >= goalValue) {
			addProgress(player, 0);
		}
	}

	final protected void addProgress(OfflinePlayer player) {
		addProgress(player, 1);
	}

	final protected void addProgress(OfflinePlayer player, int value) {
		int progress = progressValues.get(player) + value;
		if (hasReachedGoal.get(player)) {
			if (progress < goalValue) {
				hasReachedGoal.put(player, false);
				quest.savePlayerData(player);
			}
		} else {
			if (progress >= goalValue) {
				progressValues.put(player, goalValue);
				hasReachedGoal.put(player, true);
				quest.recheckStepCompletion(player);
			} else {
				progressValues.put(player, progress);
			}
			quest.savePlayerData(player);
		}
	}

	final public String getObjectiveText(OfflinePlayer player) {
		if (isEvaluating(player)) {
			return generateObjectiveText(player, getProgressValue(player), goalValue);
		} else {
			return generateObjectiveText(player, goalValue, goalValue);
		}
	}

	protected void onPlayerAdded(OfflinePlayer player) {
		// optional override
	}

	protected void onPlayerRemoved(OfflinePlayer player) {
		// optional override
	}

	protected abstract String generateObjectiveText(OfflinePlayer player, int progress, int goal);

}
