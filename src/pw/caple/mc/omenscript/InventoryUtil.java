package pw.caple.mc.omenscript;

import java.util.Iterator;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.inventory.SpoutItemStack;

public class InventoryUtil {

	private InventoryUtil() {}

	public static boolean itemsAreTheSame(SpoutItemStack itemsA, SpoutItemStack itemsB) {
		if (itemsA.isCustomItem()) {
			return (itemsB.isCustomItem() && itemsA.getMaterial().equals(itemsB.getMaterial()));
		} else {
			return (!itemsB.isCustomItem() && itemsA.isSimilar(itemsB));
		}
	}

	public static int getNumberOf(SpoutItemStack items, Player player) {
		int count = 0;
		Iterator<ItemStack> iterator = player.getInventory().iterator();
		while (iterator.hasNext()) {
			ItemStack next = iterator.next();
			if (next != null) {
				if (itemsAreTheSame(items, new SpoutItemStack(next))) {
					count += next.getAmount();
				}
			}
		}
		return count;
	}

	public static void remove(SpoutItemStack items, Player player) {
		int left = items.getAmount();
		int slotID = 0;
		Iterator<ItemStack> iterator = player.getInventory().iterator();
		while (iterator.hasNext() && left > 0) {
			ItemStack next = iterator.next();
			if (next != null) {
				if (itemsAreTheSame(items, new SpoutItemStack(next))) {
					if (left >= next.getAmount()) {
						player.getInventory().setItem(slotID, null);
						left = left - next.getAmount();
					} else {
						next.setAmount(next.getAmount() - left);
						left = 0;
					}
				}
			}
			slotID++;
		}
	}

}
