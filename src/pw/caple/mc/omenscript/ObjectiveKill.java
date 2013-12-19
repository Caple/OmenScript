package pw.caple.mc.omenscript;

import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Objective met by killing a certain number of a specified entity type.
 */
public class ObjectiveKill extends QuestObjective {

	private final EntityType entityType;

	public ObjectiveKill(Quest quest, EntityType entityType, int killsRequired) {
		super(quest, killsRequired);
		this.entityType = entityType;
	}

	@Override
	public String generateObjectiveText(OfflinePlayer player, int progress, int goal) {
		String entityName;
		if (goal == 1) {
			entityName = InputUtil.getEntityName(entityType, false);
		} else {
			entityName = InputUtil.getEntityName(entityType, true);
		}
		return "Kill " + progress + " / " + goal + " " + entityName + ".";
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityDeath(EntityDeathEvent event) {
		if (event.getEntityType() == entityType) {
			Entity killer = event.getEntity().getKiller();
			if (killer instanceof Player) {
				Player player = (Player) killer;
				if (isEvaluating(player)) {
					addProgress(player);
				}
			}

		}
	}

}
