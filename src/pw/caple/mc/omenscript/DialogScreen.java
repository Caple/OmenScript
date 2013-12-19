package pw.caple.mc.omenscript;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Player;
import org.getspout.spoutapi.SpoutManager;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericGradient;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.GenericTexture;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * GUI screen shown to allow a player to interact with an NPC
 */
public final class DialogScreen extends GenericPopup {

	private final int windowWidth = 306;
	private final int windowHeight = 230;
	private final int initialShift = 17;
	private final int dialogShift = 10;
	private final int buttonSpacing = 20;
	private final int buttonMargin = 7;
	private final int rewardYMargin = 5;
	private final int rewardBorderSize = 1;
	private final int rewardXMargin = 8;
	private final Color normalColor = new Color(1F, 1F, 1F, 1F);
	private final Color screenBGColorA = new Color(0.3f, 0.3f, 0.3f, 0.1f);
	private final Color screenBGColorB = new Color(0.8f, 0.8f, 0.8f, 0.1f);
	private final Color screenBGColorC = new Color(0.0f, 0.0f, 0.0f, 0.1f);
	private final Color rewardBGColorA = new Color(1f, 1f, 0f, 1f);
	private final Color rewardBGColorB = new Color(1f, 1f, .8f, 1f);
	private final Color rewardBGColorC = new Color(0.0f, 0.0f, 0.0f, 1f);
	private final Color dialogButtonColor = new Color(0.3f, 0.3f, 0.3f, 0.8f);
	private final Color rewardButtonColor = new Color(1f, 1f, .8f, 0.8f);

	private final NPC npc;
	private final SpoutPlayer player;
	private final List<DialogNode> rootCollection;
	private final Map<DialogButton, DialogNode> buttonNodeMap;
	private DialogContainer dialogContainer;
	private int choicesY = 0;
	private int rootNodesVisible = 0;

	public DialogScreen(SpoutPlayer thisPlayer, List<DialogNode> dialog, NPC npc) {
		buttonNodeMap = new HashMap<>();
		rootCollection = dialog;
		player = thisPlayer;
		this.npc = npc;
		attachWidget(OmenScript.getInstance(), new MainContainer());
		loadDialog(null, rootCollection);
	}

	long tick;

	//	@Override
	//	public void onTick() {
	//		tick++;
	//		if (tick > 40) {
	//			if (player.getLocation().distance(npc.getLocation()) > 5d) {
	//				player.getMainScreen().closePopup();
	//			}
	//		}
	//	}

	public boolean hasDialogVisible() {
		return rootNodesVisible > 0;
	}

	private void loadDialog(DialogNode parent, List<DialogNode> nodes) {
		if (dialogContainer != null) {
			removeWidget(dialogContainer);
			buttonNodeMap.clear();
		}
		if (nodes.isEmpty()) {
			if (parent == null || parent.getResponse() == null) {
				player.getMainScreen().closePopup();
			} else {
				DialogNode defaultNode = new DialogNode("Goodbye.");
				List<DialogNode> defaultList = new ArrayList<>();
				defaultList.add(defaultNode);
				dialogContainer = new DialogContainer(parent, defaultList);
				attachWidget(OmenScript.getInstance(), dialogContainer);
			}
		} else {
			dialogContainer = new DialogContainer(parent, nodes);
			attachWidget(OmenScript.getInstance(), dialogContainer);
		}
	}

	public class MainContainer extends GenericContainer {

		public MainContainer() {
			int currentY = initialShift;

			setAnchor(WidgetAnchor.CENTER_CENTER);
			setWidth(windowWidth).setHeight(windowHeight);
			shiftXPos(windowWidth / 2 * -1).shiftYPos(windowHeight / 2 * -1);

			GenericGradient bgA = new GenericGradient();
			bgA.setTopColor(screenBGColorA).setBottomColor(screenBGColorB);
			bgA.setWidth(windowWidth - 2).setHeight(windowHeight - 2);
			bgA.shiftXPos(1).shiftYPos(1);
			bgA.setPriority(RenderPriority.High);

			GenericGradient bgB = new GenericGradient();
			bgB.setTopColor(screenBGColorC).setBottomColor(screenBGColorC);
			bgB.setWidth(windowWidth).setHeight(windowHeight);
			bgB.setPriority(RenderPriority.Highest);

			GenericTexture npcFace = new GenericTexture(SpoutManager.getPlayer((Player) npc.getBukkitEntity()).getSkin());
			npcFace.setWidth(8).setHeight(8);
			npcFace.shiftXPos(initialShift).shiftYPos(currentY);
			npcFace.setTop(8).setLeft(8);

			GenericLabel nameLabel = new GenericLabel(npc.getName());
			nameLabel.setWidth(90).setHeight(12);
			nameLabel.shiftXPos(initialShift + 12).shiftYPos(currentY);
			nameLabel.setScale(1.2f).setTextColor(normalColor);
			addChildren(bgA, bgB, npcFace, nameLabel);
			currentY += 11;
			choicesY = currentY + 23;
		}

		@Override
		public void onTick() {}
	}

	public class DialogContainer extends GenericContainer {

		public DialogContainer(DialogNode parent, List<DialogNode> nodes) {
			setAnchor(WidgetAnchor.CENTER_CENTER);
			setWidth(windowWidth).setHeight(windowHeight);
			shiftXPos(windowWidth / 2 * -1).shiftYPos(windowHeight / 2 * -1 + choicesY);
			int currentY = 0;
			if (parent != null && parent.getResponse() != null) {
				GenericLabel responseLabel = new GenericLabel(parent.getResponse());
				responseLabel.setWidth(windowWidth - initialShift * 2).shiftXPos(initialShift);
				responseLabel.setScale(1f);
				GUIUtil.makeLabelMultiline(responseLabel);
				addChild(responseLabel);
				currentY += responseLabel.getHeight() + 25;
			}
			int choiceWidth = windowWidth - initialShift * 2 - dialogShift * 2;
			for (DialogNode node : nodes) {
				if (node.isVisibleTo(player)) {
					rootNodesVisible++;
					if (node.finishesQuest()) {

						//reward background and border
						GenericGradient rewardBG = new GenericGradient();
						rewardBG.setTopColor(rewardBGColorA);
						rewardBG.setBottomColor(rewardBGColorB);
						rewardBG.setWidth(choiceWidth + buttonMargin * 2 - rewardBorderSize * 2);
						rewardBG.shiftYPos(currentY - rewardYMargin + rewardBorderSize);
						rewardBG.shiftXPos(initialShift + dialogShift - buttonMargin + rewardBorderSize);
						rewardBG.setPriority(RenderPriority.High);

						GenericGradient rewardBorder = new GenericGradient();
						rewardBorder.setColor(rewardBGColorC);
						rewardBorder.setWidth(choiceWidth + buttonMargin * 2);
						rewardBorder.shiftYPos(currentY - rewardYMargin);
						rewardBorder.shiftXPos(initialShift + dialogShift - buttonMargin);
						rewardBorder.setPriority(RenderPriority.Highest);

						//reward information
						GenericContainer rewardInformation = node.getRewardsInformation();
						rewardInformation.setWidth(choiceWidth - rewardXMargin * 2);
						rewardInformation.shiftYPos(currentY);
						rewardInformation.shiftXPos(initialShift + dialogShift - buttonMargin + rewardXMargin);
						rewardInformation.setPriority(RenderPriority.Lowest);
						currentY += rewardInformation.getHeight();

						//normal controls
						GenericLabel label = new GenericLabel(node.getText());
						label.setWidth(choiceWidth - rewardXMargin * 2);
						label.shiftYPos(currentY);
						label.shiftXPos(initialShift + dialogShift + rewardXMargin);
						label.setScale(1f);
						label.setPriority(RenderPriority.Lowest);
						label.setTextColor(new Color(0f, 0f, 0f, 1f));
						label.setShadow(false);
						GUIUtil.makeLabelMultiline(label);

						DialogButton button = new DialogButton();
						button.shiftYPos(currentY - buttonMargin);
						button.shiftXPos(initialShift + dialogShift - buttonMargin + rewardXMargin);
						button.setWidth(choiceWidth + buttonMargin * 2 - rewardXMargin * 2);
						button.setHeight(label.getHeight() + buttonMargin * 2);
						button.setPriority(RenderPriority.Normal);

						GenericGradient buttonOverlay = new GenericGradient(rewardButtonColor);
						buttonOverlay.shiftYPos(currentY - buttonMargin);
						buttonOverlay.shiftXPos(initialShift + dialogShift - buttonMargin + rewardXMargin);
						buttonOverlay.setWidth(choiceWidth + buttonMargin * 2 - rewardXMargin * 2);
						buttonOverlay.setHeight(label.getHeight() + buttonMargin * 2);
						buttonOverlay.setPriority(RenderPriority.Low);

						//Stretch the reward background.
						int totalControlsHeight = rewardInformation.getHeight() + label.getHeight() + buttonMargin;
						rewardBG.setHeight(totalControlsHeight + rewardYMargin * 2 - rewardBorderSize * 2);
						rewardBorder.setHeight(totalControlsHeight + rewardYMargin * 2);

						// Finally...
						currentY += label.getHeight() + buttonSpacing + rewardYMargin;
						buttonNodeMap.put(button, node);
						addChildren(rewardBG, rewardBorder, rewardInformation, button, buttonOverlay, label);
					} else {

						GenericLabel label = new GenericLabel(node.getText());
						label.setWidth(choiceWidth);
						label.shiftYPos(currentY);
						label.shiftXPos(initialShift + dialogShift);
						label.setScale(1f);
						label.setPriority(RenderPriority.Lowest);
						GUIUtil.makeLabelMultiline(label);

						DialogButton button = new DialogButton();
						button.shiftYPos(currentY - buttonMargin);
						button.shiftXPos(initialShift + dialogShift - buttonMargin);
						button.setWidth(choiceWidth + buttonMargin * 2);
						button.setHeight(label.getHeight() + buttonMargin * 2);
						button.setPriority(RenderPriority.Normal);

						GenericGradient buttonOverlay = new GenericGradient(dialogButtonColor);
						buttonOverlay.shiftYPos(currentY - buttonMargin);
						buttonOverlay.shiftXPos(initialShift + dialogShift - buttonMargin);
						buttonOverlay.setWidth(choiceWidth + buttonMargin * 2);
						buttonOverlay.setHeight(label.getHeight() + buttonMargin * 2);
						buttonOverlay.setPriority(RenderPriority.Low);

						currentY += label.getHeight() + buttonSpacing;
						buttonNodeMap.put(button, node);
						addChildren(button, buttonOverlay, label);
					}

				}
			}
		}

		@Override
		public void onTick() {}
	}

	public class DialogButton extends GenericButton {

		public DialogButton() {
			super();
			setText("");
		}

		@Override
		public void onButtonClick(ButtonClickEvent event) {
			DialogNode node = buttonNodeMap.get(this);
			if (node.shouldContinue(player)) {
				node.runActions(player);
				loadDialog(node, node.getChildren());
			} else {
				player.getMainScreen().closePopup();
			}
		}
	}

}
