package pw.caple.mc.omenscript;

import me.ThaH3lper.com.Api.BossDeathEvent;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

/**
 * Objective met by killing a certain number of a specified entity type.
 */
public class ObjectiveKillEpicBoss extends QuestObjective {

	private final String bossName;

	public ObjectiveKillEpicBoss(Quest quest, String bossName, int amount) {
		super(quest, amount);
		this.bossName = bossName;
	}

	@Override
	public String generateObjectiveText(OfflinePlayer player, int progress, int goal) {
		if (goal > 1) {
			return "Kill " + progress + " / " + goal + " " + bossName;
		} else {
			return "Kill " + bossName;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onBossDeath(BossDeathEvent event) {
		if (isEvaluating(event.getPlayer())) {
			if (event.getBossName().equalsIgnoreCase(bossName)) {
				addProgress(event.getPlayer(), 1);
			}
		}
	}

}
