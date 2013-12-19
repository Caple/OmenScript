package pw.caple.mc.omenscript;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * A quest and all the information needed to run it.
 */
public final class Quest implements NeedsToBeUnregistered {

	private final String title;
	private final QuestTracker tracker;
	private final List<QuestStep> steps = new ArrayList<>();
	private final List<Quest> prerequisiteQuests = new ArrayList<>();
	private final Map<OfflinePlayer, Integer> stepIndices = new HashMap<>();
	private final Map<OfflinePlayer, Integer> completionCount = new HashMap<>();
	private final CommonNodes onStart = new CommonNodes();
	private final CommonNodes onUpdate = new CommonNodes();
	private final CommonNodes onComplete = new CommonNodes();

	private boolean isRepeatable = false;
	private boolean disableSaving = false;
	private String readyToTurnInDescription = "This quest is ready to turn in.";

	public Quest(String title, QuestTracker parentTracker) {
		this.title = title;
		tracker = parentTracker;
	}

	public String getTitle() {
		return title;
	}

	void addStep(QuestStep part) {
		steps.add(part);
	}

	List<QuestStep> getAllSteps() {
		return steps;
	}

	void setRepeatable(boolean value) {
		isRepeatable = value;
	}

	void addPrerequisiteQuest(Quest quest) {
		prerequisiteQuests.add(quest);
	}

	public QuestStep getStep(int index) {
		if (index > -1 && index < steps.size()) {
			return steps.get(index);
		} else {
			return null;
		}
	}

	public int getStepIndex(OfflinePlayer player) {
		Integer index = stepIndices.get(player);
		if (index != null) {
			return index;
		} else {
			return -1;
		}
	}

	void addPlayer(OfflinePlayer player) {
		if (player.isOnline()) {
			Player onlinePlayer = player.getPlayer();
			if (onlinePlayer instanceof SpoutPlayer) {
				onStart.run((SpoutPlayer) onlinePlayer);
			}
		}
		setStepIndex(player, 0);
		recheckStepCompletion(player);
		savePlayerData(player);
		tracker.add(player, this);
	}

	private void setStepIndex(OfflinePlayer player, int index) {
		if (player.isOnline() && player.getPlayer() instanceof SpoutPlayer) {
			SpoutPlayer spoutPlayer = (SpoutPlayer) player.getPlayer();
			if (!stepIndices.containsKey(player)) {
				GUIUtil.getNotifier().showQuestNotification(spoutPlayer, this, "Quest Added");
			} else if (stepIndices.get(player) != index) {
				onUpdate.run(spoutPlayer);
				GUIUtil.getNotifier().showQuestNotification(spoutPlayer, this, "Quest Updated");
			}
		}
		stepIndices.put(player, index);
		if (index > 0) {
			steps.get(index - 1).removePlayer(player);
		}
		if (steps.size() > index) {
			QuestStep step = steps.get(index);
			step.addPlayer(player);
		}
	}

	void recheckStepCompletion(OfflinePlayer player) {
		int index = getStepIndex(player);
		if (index > -1) {
			QuestStep step = getStep(index);
			while (step != null && step.hasCompleted(player)) {
				index++;
				setStepIndex(player, index);
				step = getStep(index);
			}
		}
	}

	private boolean savingDelayed = false;
	private long lastSave = 0;

	void savePlayerData(final OfflinePlayer player) {
		if (disableSaving || savingDelayed) return;
		long timeNow = new Date().getTime();
		if (timeNow - 2500 > lastSave) {
			lastSave = timeNow;
			overwriteDBRecord(player);
		} else {
			savingDelayed = true;
			Bukkit.getScheduler().scheduleSyncDelayedTask(OmenScript.getInstance(), new Runnable() {
				@Override
				public void run() {
					overwriteDBRecord(player);
					savingDelayed = false;
				}
			}, 60L);
		}
		return;
	}

	private void overwriteDBRecord(OfflinePlayer player) {
		try (
			Connection connection = OmenScript.getInstance().getDBConnection()) {
			String deleteStatement = "DELETE FROM quests WHERE player=? AND quest=?";
			PreparedStatement statement = connection.prepareStatement(deleteStatement);
			statement.setString(1, player.getName());
			statement.setString(2, getTitle());
			statement.executeUpdate();
			statement.close();
			String insertStatement = "INSERT INTO quests VALUES (?,?,?,?,?);";
			statement = connection.prepareStatement(insertStatement);
			statement.setString(1, getTitle());
			statement.setString(2, player.getName());
			statement.setInt(3, timesCompleted(player));
			statement.setInt(4, getStepIndex(player));
			statement.setString(5, null);
			QuestStep step = getStep(getStepIndex(player));
			if (step != null) {
				List<QuestObjective> objectives = step.getAllObjectives();
				if (objectives.size() > 1) {
					StringBuilder list = new StringBuilder();
					for (QuestObjective objective : objectives) {
						list.append(objective.getProgressValue(player));
						list.append(',');
					}
					list.setLength(list.length() - 1);
					statement.setString(5, list.toString());
				} else if (objectives.size() > 0) {
					statement.setString(5, Integer.toString(objectives.get(0).getProgressValue(player)));
				}
			}
			statement.executeUpdate();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	void loadAllPlayerData() {
		try (
			Connection connection = OmenScript.getInstance().getDBConnection()) {
			disableSaving = true;
			String selectStatement = "SELECT * FROM quests WHERE quest=?";
			PreparedStatement statement = connection.prepareStatement(selectStatement);
			statement.setString(1, getTitle());
			ResultSet result = statement.executeQuery();
			while (result.next()) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(result.getString(2));
				completionCount.put(player, result.getInt(3));
				int stepIndex = result.getInt(4);
				if (stepIndex > -1) {
					stepIndices.put(player, stepIndex);
					if (stepIndex < steps.size()) {
						QuestStep step = steps.get(stepIndex);
						step.addPlayer(player);
						String valuesStringArray = result.getString(5);
						if (valuesStringArray != null) {
							List<QuestObjective> objectives = step.getAllObjectives();
							String[] valuesAsString = valuesStringArray.split(",");
							for (int index = 0; index < valuesAsString.length; index++) {
								int value = Integer.parseInt(valuesAsString[index]);
								if (index < objectives.size()) {
									QuestObjective objective = objectives.get(index);
									if (!objective.isEvaluating(player)) {
										objective.startEvaluating(player);
									}
									objective.addProgress(player, value);
								}
							}
						}
					}
					tracker.add(player, this);
					recheckStepCompletion(player);
				}
			}
			result.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			disableSaving = false;
		}
	}

	public boolean isQualifiedToStart(OfflinePlayer player) {
		for (Quest prereq : prerequisiteQuests) {
			if (prereq.timesCompleted(player) < 1) return false;
		}
		return !isOnQuest(player) && (isRepeatable || timesCompleted(player) == 0);
	}

	public boolean isOnQuest(OfflinePlayer player) {
		return stepIndices.containsKey(player);
	}

	public boolean isReadyToTurnIn(OfflinePlayer player) {
		if (stepIndices.containsKey(player)) {
			int stepIndex = stepIndices.get(player);
			return stepIndex >= steps.size();
		}
		return false;
	}

	public void turnIn(SpoutPlayer player) {
		for (QuestStep step : steps) {
			step.removePlayer(player);
		}
		stepIndices.remove(player);
		if (completionCount.containsKey(player)) {
			completionCount.put(player, completionCount.get(player) + 1);
		} else {
			completionCount.put(player, 1);
		}
		tracker.remove(player, this);
		savePlayerData(player);
		onComplete.run(player);
	}

	public void drop(SpoutPlayer player) {
		for (QuestStep step : steps) {
			step.removePlayer(player);
		}
		stepIndices.remove(player);
		tracker.remove(player, this);
		savePlayerData(player);
	}

	void untrackAll() {
		for (OfflinePlayer player : stepIndices.keySet()) {
			tracker.remove(player, this);
		}
	}

	public int timesCompleted(OfflinePlayer player) {
		if (completionCount.containsKey(player)) {
			return completionCount.get(player);
		} else {
			return 0;
		}
	}

	public CommonNodes getStartTriggers() {
		return onStart;
	}

	public CommonNodes getUpdateTriggers() {
		return onUpdate;
	}

	public CommonNodes getTurnInTriggers() {
		return onComplete;
	}

	@Override
	public void unregister() {
		for (QuestStep step : steps) {
			step.unregister();
		}
	}

	public void setReadyToTurnInDescription(String value) {
		readyToTurnInDescription = value;
	}

	public String getReadyToTurnInDescription() {
		return readyToTurnInDescription;
	}

}
