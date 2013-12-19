package pw.caple.mc.omenscript;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.World;

public class LocationManager {

	private final HashMap<String, String> properNames = new HashMap<>();
	private final HashMap<String, Location> locations = new HashMap<>();
	private final HashMap<String, Set<Location>> radiusSets = new HashMap<>();

	final public boolean isLocationDefined(String name) {
		return locations.containsKey(name);
	}

	final public Location getLocationByName(String name) {
		return locations.get(name);
	}

	final public Set<Location> getRadiusSetByName(String name) {
		return radiusSets.get(name);
	}

	final public String getProperNameByLowerCaseName(String name) {
		return properNames.get(name);
	}

	final public void addLocation(String name, Location location, int radius) {
		World world = location.getWorld();
		Set<Location> set = new HashSet<>();
		for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
			for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
				for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
					set.add(new Location(world, x, y, z));
				}
			}
		}
		String nameToLowerCase = name.toLowerCase();
		radiusSets.put(nameToLowerCase, set);
		locations.put(nameToLowerCase, location);
		properNames.put(nameToLowerCase, name);
	}

	final void clear() {
		locations.clear();
		radiusSets.clear();
		properNames.clear();
	}
}
