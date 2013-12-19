package pw.caple.mc.omenscript;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import org.bukkit.Bukkit;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.ScreenType;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

public class GUINotifications {

	private Set<SpoutPlayer> alreadyProcessing = new HashSet<>();
	private Map<SpoutPlayer, Queue<GenericContainer>> pending = new HashMap<>();

	private static class nullTypeContainer extends GenericContainer {
		@Override
		public void onTick() {}
	}

	public void showQuestNotification(SpoutPlayer player, Quest quest, String subtitle) {
		Color textColor = new Color(0.2f, 0.8f, 0f, 0.8f);
		final GenericLabel subtitleLabel = new GenericLabel(subtitle);
		subtitleLabel.setAnchor(WidgetAnchor.CENTER_CENTER);
		subtitleLabel.setScale(1f);
		subtitleLabel.setTextColor(textColor);
		subtitleLabel.setPriority(RenderPriority.Low);
		int widthA = GenericLabel.getStringWidth(subtitleLabel.getText(), subtitleLabel.getScale());
		int heightA = GenericLabel.getStringHeight(subtitleLabel.getText(), subtitleLabel.getScale());
		subtitleLabel.setHeight(heightA);
		subtitleLabel.setWidth(widthA);
		subtitleLabel.shiftXPos(widthA / 2 * -1);

		final GenericLabel titleLabel = new GenericLabel(quest.getTitle());
		titleLabel.setAnchor(WidgetAnchor.CENTER_CENTER);
		titleLabel.setScale(1.5f);
		titleLabel.setTextColor(textColor);
		titleLabel.setPriority(RenderPriority.Low);
		int widthB = GenericLabel.getStringWidth(titleLabel.getText(), titleLabel.getScale());
		int heightB = GenericLabel.getStringHeight(titleLabel.getText(), titleLabel.getScale());
		titleLabel.setHeight(heightB);
		titleLabel.setWidth(widthB);
		titleLabel.shiftXPos(widthB / 2 * -1);
		titleLabel.shiftYPos(heightA + 5);

		GenericContainer container = new nullTypeContainer();
		container.setWidth(300).setHeight(300);
		container.shiftYPos(-50);
		container.setAnchor(WidgetAnchor.CENTER_CENTER);
		container.addChildren(subtitleLabel, titleLabel);
		pushNotification(player, container);
	}

	private void pushNotification(SpoutPlayer player, GenericContainer container) {
		if (!pending.containsKey(player)) {
			pending.put(player, new LinkedList<GenericContainer>());
		}
		pending.get(player).add(container);
		processPendingNotifications(player);
	}

	private void processPendingNotifications(final SpoutPlayer player) {
		if (alreadyProcessing.contains(player)) return;
		ScreenType type = player.getMainScreen().getScreenType();
		if (player.getMainScreen().getActivePopup() != null || !(type == ScreenType.GAME_SCREEN || type == ScreenType.UNKNOWN)) {
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OmenScript.getInstance(), new Runnable() {
				public void run() {
					processPendingNotifications(player);
				}
			}, 20L);
			return;
		}
		Queue<GenericContainer> notifications = pending.get(player);
		if (notifications != null && !notifications.isEmpty()) {
			alreadyProcessing.add(player);
			final GenericContainer container = notifications.remove();
			player.getMainScreen().attachWidget(OmenScript.getInstance(), container);
			Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OmenScript.getInstance(), new Runnable() {
				public void run() {
					player.getMainScreen().removeWidget(container);
					Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(OmenScript.getInstance(), new Runnable() {
						public void run() {
							alreadyProcessing.remove(player);
							processPendingNotifications(player);
						}
					}, 20L);
				}
			}, 60L);

		}
	}

}
