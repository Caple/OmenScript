package pw.caple.mc.omenscript;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.getspout.spoutapi.inventory.SpoutItemStack;

/**
 * Objective met by killing a certain number of a specified entity type.
 */
public class ObjectiveGather extends QuestObjective {

	private final SpoutItemStack items;

	public ObjectiveGather(Quest quest, SpoutItemStack items) {
		super(quest, items.getAmount());
		this.items = items;

	}

	@Override
	public String generateObjectiveText(OfflinePlayer player, int progress, int goal) {
		return "Gather " + progress + " / " + goal + " " + items.getMaterial().getName();
	}

	private class ScanTimer implements Runnable {
		int id = 0;
		final private OfflinePlayer player;

		public ScanTimer(OfflinePlayer player) {
			this.player = player;
		}

		@Override
		public void run() {
			if (!isEvaluating(player)) {
				Bukkit.getScheduler().cancelTask(id);
			} else if (player.isOnline()) {
				int numberOfItems = InventoryUtil.getNumberOf(items, player.getPlayer());
				setProgressNoSaveUnlessComplete(player, numberOfItems);
			}
		}

	}

	@Override
	final protected void onPlayerAdded(final OfflinePlayer player) {
		ScanTimer scanTimer = new ScanTimer(player);
		final int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(OmenScript.getInstance(), scanTimer, 1, 40);
		scanTimer.id = taskID;

	}

	//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	//	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
	//		Player player = event.getPlayer();
	//		if (isEvaluating(player)) {
	//			SpoutItemStack pickedUp = new SpoutItemStack(event.getItem().getItemStack());
	//			if (InventoryUtil.itemsAreTheSame(items, pickedUp)) {
	//				setProgress(player, InventoryUtil.getNumberOf(items, player) + pickedUp.getAmount());
	//			}
	//		}
	//	}
	//
	//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	//	public void onPlayerDropItem(PlayerDropItemEvent event) {
	//		Player player = event.getPlayer();
	//		if (isEvaluating(player)) {
	//			SpoutItemStack dropped = new SpoutItemStack(event.getItemDrop().getItemStack());
	//			if (InventoryUtil.itemsAreTheSame(items, dropped)) {
	//				setProgress(player, InventoryUtil.getNumberOf(items, player));
	//			}
	//		}
	//	}
	//
	//	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	//	public void onInventoryCloseEvent(InventoryCloseEvent event) {
	//		if (event.getPlayer() instanceof Player) {
	//			Player player = (Player) event.getPlayer();
	//			if (isEvaluating(player)) {
	//				setProgress(player, InventoryUtil.getNumberOf(items, player));
	//			}
	//		}
	//	}
}
