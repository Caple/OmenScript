package pw.caple.mc.omenscript;

import java.util.Set;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 * Objective met by killing a certain number of a specified entity type.
 */
public class ObjectiveTravelTo extends QuestObjective {

	private final LocationManager manager;
	private final String locationName;
	private final boolean displayLocationName;

	public ObjectiveTravelTo(Quest quest, LocationManager manager, String locationName, boolean displayLocationName) {
		super(quest, 1);
		this.manager = manager;
		this.locationName = locationName.toLowerCase();
		this.displayLocationName = displayLocationName;
	}

	@Override
	public String generateObjectiveText(OfflinePlayer player, int progress, int goal) {
		if (manager.isLocationDefined(locationName)) {
			Location location = manager.getLocationByName(locationName);
			if (displayLocationName) {
				String properName = manager.getProperNameByLowerCaseName(locationName);
				return "Travel to " + properName + " \n       (" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
			} else {
				return "Travel to (" + location.getWorld().getName() + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ() + ")";
			}
		} else {
			if (displayLocationName) {
				String properName = manager.getProperNameByLowerCaseName(locationName);
				return "Travel to " + properName + " (UNKNOWN)";
			} else {
				return "Travel to (UNKNOWN)";
			}
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onMove(PlayerMoveEvent event) {
		if (isEvaluating(event.getPlayer())) {
			if (event.getFrom().getBlockX() != event.getTo().getBlockX() || event.getFrom().getBlockZ() != event.getTo().getBlockZ() || event.getFrom().getBlockY() != event.getTo().getBlockY()) {
				Set<Location> locations = manager.getRadiusSetByName(locationName);
				if (locations != null && locations.contains(event.getTo().getBlock().getLocation())) {
					setProgress(event.getPlayer(), 1);
				} else {
					setProgress(event.getPlayer(), 0);
				}
			}
		}
	}
}
