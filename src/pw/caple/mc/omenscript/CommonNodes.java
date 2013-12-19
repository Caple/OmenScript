package pw.caple.mc.omenscript;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Contains the rewards given for turning in a quest.
 */
public final class CommonNodes {

	// third party
	public BigDecimal money = new BigDecimal("0");
	public double heroesXP = 0;

	// normal
	public String soundURL;
	public final List<SpoutItemStack> itemsToAdd = new ArrayList<>();
	public final List<SpoutItemStack> itemsToRemove = new ArrayList<>();
	public final List<String> commands = new ArrayList<>();
	public int xp = 0;

	public void run(SpoutPlayer player) {
		if (money.compareTo(BigDecimal.ZERO) > 0) {
			OmenScript.getInstance().getThirdPartyPlugins().giveMoney(player, money.doubleValue());
		}
		for (SpoutItemStack item : itemsToAdd) {
			HashMap<Integer, ItemStack> leftovers = player.getInventory().addItem(item.clone());
			for (ItemStack leftover : leftovers.values()) {
				player.getWorld().dropItem(player.getLocation(), leftover);
			}
		}
		for (SpoutItemStack item : itemsToRemove) {
			InventoryUtil.remove(item, player);
		}
		if (xp > 0) {
			player.giveExp(xp);
		}
		if (heroesXP > 0) {
			OmenScript.getInstance().getThirdPartyPlugins().giveHeroesXP(player, heroesXP);
		}
		if (soundURL != null) {
			SpoutManager.getSoundManager().playCustomSoundEffect(OmenScript.getInstance(), player, soundURL, false);
		}
		if (!commands.isEmpty()) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("player", player.getName());
			for (String command : commands) {
				String finalCommand = InputUtil.getTokenReplacer().replaceTokens(command, map);
				Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), finalCommand);
			}
		}
	}

}
