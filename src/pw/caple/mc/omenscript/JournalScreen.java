package pw.caple.mc.omenscript;

import java.util.ArrayList;
import org.bukkit.ChatColor;
import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericButton;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericGradient;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.GenericListWidget;
import org.getspout.spoutapi.gui.GenericPopup;
import org.getspout.spoutapi.gui.ListWidgetItem;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.WidgetAnchor;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * GUI screen shown to player detailing their active quests.
 */
public final class JournalScreen extends GenericPopup {

	private final SpoutPlayer player;
	private QuestList questList;
	private JournalEntry journalEntry;

	public JournalScreen(SpoutPlayer player) {
		this.player = player;
		attachWidget(OmenScript.getInstance(), new Window());
	}

	private final class Window extends GenericContainer {

		private static final int headerTitleShift = 12;
		private static final int windowWidth = 306;
		private static final int windowHeight = 230;
		private static final int entryMargin = 5;
		private static final int listWidth = 120;
		private static final int listMarginX = 2;

		private final Color screenBGColorA = new Color(0.0f, 0.1f, 0.0f, 1f);
		private final Color screenBGColorB = new Color(0.0f, 0.3f, 0.1f, 1f);
		private final Color borderColor = new Color(0.9f, 0.9f, 0.9f, 0.1f);
		private final Color listBGColor = new Color(0.0f, 0.0f, 0.0f, 0f);
		private final Color headerTextColor = new Color(1F, 1F, 1F, 0.8F);

		public Window() {

			setAnchor(WidgetAnchor.CENTER_CENTER);
			setWidth(windowWidth).setHeight(windowHeight);
			shiftXPos(windowWidth / 2 * -1).shiftYPos(windowHeight / 2 * -1);

			GenericGradient bgA = new GenericGradient();
			bgA.setTopColor(screenBGColorA).setBottomColor(screenBGColorB);
			bgA.setWidth(windowWidth - 2).setHeight(windowHeight - 2);
			bgA.shiftXPos(1).shiftYPos(1);
			bgA.setPriority(RenderPriority.High);
			GenericGradient bgB = new GenericGradient();
			bgB.setTopColor(borderColor).setBottomColor(borderColor);
			bgB.setWidth(windowWidth).setHeight(windowHeight);
			bgB.setPriority(RenderPriority.Highest);

			int currentY = headerTitleShift;
			GenericLabel dialogHeader = new GenericLabel("Journal (Quest Log)");
			dialogHeader.setWidth(90).setHeight(15);
			dialogHeader.shiftXPos(headerTitleShift).shiftYPos(headerTitleShift - 4);
			dialogHeader.setScale(1.3f).setTextColor(headerTextColor);
			currentY += headerTitleShift;

			GenericGradient lineX = new GenericGradient();
			lineX.setColor(borderColor);
			lineX.setWidth(windowWidth - 2).setHeight(1);
			lineX.shiftXPos(1).shiftYPos(currentY);
			GenericGradient lineY = new GenericGradient();
			lineY.setColor(borderColor);
			lineY.setWidth(1).setHeight(windowHeight - currentY - 2);
			lineY.shiftXPos(listWidth + listMarginX).shiftYPos(currentY + 1);

			int journalWidth = windowWidth - listWidth - listMarginX - entryMargin * 2 - 3;
			int journalHeight = windowHeight - currentY - entryMargin;
			journalEntry = new JournalEntry(journalWidth, journalHeight);
			journalEntry.shiftXPos(listWidth + listMarginX + 3 + entryMargin);
			journalEntry.shiftYPos(currentY + entryMargin);

			questList = new QuestList();
			questList.setWidth(listWidth).setHeight(windowHeight - currentY - headerTitleShift + 5);
			questList.shiftXPos(listMarginX).shiftYPos(currentY);
			questList.setBackgroundColor(listBGColor);
			questList.setColor(new Color(0f, 0f, 0f, 1f));

			addChildren(bgA, bgB, lineX, lineY, dialogHeader, questList, journalEntry);

			if (questList.getItems().length > 0) {
				questList.setSelection(0);
				questList.onSelected(0, false);
			}
		}

		@Override
		public void onTick() {}
	}

	private final class QuestList extends GenericListWidget {

		private final ArrayList<Quest> quests;
		private int currentSelection = -1;

		public QuestList() {
			super();
			quests = new ArrayList<>();
			for (Quest quest : OmenScript.getInstance().getQuestTracker().getAll(player)) {
				ListWidgetItem item = new ListWidgetItem(quest.getTitle(), "");
				addItem(item);
				quests.add(quest);
			}
		}

		public Quest getSelected() {
			if (currentSelection < quests.size() - 1 || quests.size() < 1) return null;
			return quests.get(currentSelection);
		}

		public void removeSelected() {
			quests.remove(currentSelection);
			removeItem(getSelectedItem());
			setDirty(true);
			if (quests.size() < 1) {
				journalEntry.update(null);
			} else {
				int newSelection = currentSelection - 1;
				if (newSelection < 0) newSelection = 0;
				setSelection(newSelection);
				onSelected(newSelection, false);
			}
		}

		@Override
		public void onSelected(int item, boolean doubleClick) {
			if (currentSelection == item) return;
			currentSelection = item;
			Quest quest = quests.get(item);
			journalEntry.update(quest);
		}

	}

	private final class DropButton extends GenericContainer {

		private static final int buttonWidth = 80;
		private static final int buttonHeight = 20;

		private final Color buttonColor = new Color(0.0f, 0.3f, 0.1f, 0.8f);

		private final GenericButton button;
		private final GenericGradient gradient;
		private final GenericLabel label;

		public DropButton() {
			super();

			setWidth(buttonWidth);
			setHeight(buttonHeight);

			button = new DropButtonInternal();
			button.setWidth(buttonWidth);
			button.setHeight(buttonHeight);

			gradient = new GenericGradient(buttonColor);
			gradient.setWidth(buttonWidth);
			gradient.setHeight(buttonHeight);
			gradient.setPriority(RenderPriority.Low);

			label = new GenericLabel("Drop Quest");
			label.setScale(1f);
			label.setWidth(GenericLabel.getStringWidth(label.getText(), label.getScale()));
			label.setHeight(GenericLabel.getStringHeight(label.getText(), label.getScale()));
			int centerY = (int) ((double) buttonHeight - label.getHeight()) / 2;
			int centerX = (int) ((double) buttonWidth - label.getWidth()) / 2;
			label.shiftYPos(centerY + 1);
			label.shiftXPos(centerX);
			label.setPriority(RenderPriority.Lowest);

			addChildren(button, gradient, label);
		}

		public void show() {
			button.setVisible(true);
			gradient.setVisible(true);
			label.setVisible(true);
		}

		public void hide() {
			label.setVisible(false);
			gradient.setVisible(false);
			button.setVisible(false);
		}

		private final class DropButtonInternal extends GenericButton {

			public DropButtonInternal() {
				super();
				setText("");
			}

			@Override
			public void onButtonClick(ButtonClickEvent event) {
				questList.getSelected().drop(event.getPlayer());
				questList.removeSelected();
			}
		}

		@Override
		public void onTick() {}

	}

	private final class JournalEntry extends GenericContainer {

		private static final int buttonMargin = 8;
		private static final int objectiveMargin = 8;

		private final Color textColor = new Color(1F, 1F, 1F, 0.8F);
		private final Color objectiveColor = new Color(0.5F, 0.5F, 0.5F, 0.5F);

		private final GenericLabel title;
		private final GenericLabel objective;
		private final GenericLabel log;
		private final GenericLabel emptyLabel;
		private final DropButton button;

		public JournalEntry(int width, int height) {

			setWidth(width);
			setHeight(height);

			title = new GenericLabel("");
			title.setWidth(getWidth());
			title.setScale(1.4f).setTextColor(textColor);
			title.shiftYPos(5);

			objective = new GenericLabel("");
			objective.setWidth(getWidth() - objectiveMargin * 2);
			objective.shiftXPos(objectiveMargin);
			objective.setScale(0.87f).setTextColor(objectiveColor);

			log = new GenericLabel("");
			log.setWidth(getWidth());
			log.setScale(0.87f).setTextColor(textColor);

			emptyLabel = new GenericLabel("No Quests. Seek adventure.");
			emptyLabel.setWidth(100);
			emptyLabel.setHeight(30).shiftXPos(20).shiftYPos(30);
			emptyLabel.setScale(0.87f).setTextColor(textColor);

			button = new DropButton();
			button.shiftYPos(getHeight() - button.getHeight() - buttonMargin);
			button.shiftXPos(getWidth() - button.getWidth() - buttonMargin);
			button.hide();

			addChildren(title, objective, log, emptyLabel, button);
		}

		public void update(Quest quest) {
			if (quest != null) {
				emptyLabel.setVisible(false);

				title.setText(quest.getTitle());
				GUIUtil.makeLabelMultiline(title);

				int verticleMargin = 7;
				int currentY = title.getY() + title.getHeight() + verticleMargin;
				int stepIndex = quest.getStepIndex(player);
				QuestStep step = quest.getStep(stepIndex);
				objective.setY(currentY);
				if (step != null) {
					StringBuilder objectiveText = new StringBuilder();
					for (QuestObjective objective : step.getAllObjectives()) {
						if (objective.hasBeenCompletedBy(player)) {
							objectiveText.append(ChatColor.STRIKETHROUGH);
						}
						objectiveText.append(objective.getObjectiveText(player));
						objectiveText.append('\n');
					}
					objective.setText(objectiveText.toString());
					GUIUtil.makeLabelMultiline(objective);
					currentY += objective.getHeight() + verticleMargin;

					log.setY(currentY);
					log.setText(step.getDescription());
					GUIUtil.makeLabelMultiline(log);
					currentY += log.getHeight() + verticleMargin + 3;
				} else {
					step = quest.getStep(stepIndex - 1);
					if (step != null) {
						StringBuilder objectiveText = new StringBuilder();
						for (QuestObjective objective : step.getAllObjectives()) {
							objectiveText.append(ChatColor.STRIKETHROUGH);
							objectiveText.append(objective.getObjectiveText(player));
							objectiveText.append('\n');
						}
						objective.setText(objectiveText.toString());
						GUIUtil.makeLabelMultiline(objective);
						currentY += objective.getHeight() + verticleMargin;

						log.setY(currentY);
						log.setText(quest.getReadyToTurnInDescription());
						GUIUtil.makeLabelMultiline(log);
						currentY += log.getHeight() + verticleMargin + 3;
					} else {
						objective.setText("This quest is finished.");
						GUIUtil.makeLabelMultiline(objective);
						currentY += objective.getHeight() + verticleMargin;

						log.setY(currentY);
						log.setText(quest.getReadyToTurnInDescription());
						GUIUtil.makeLabelMultiline(log);
						currentY += log.getHeight() + verticleMargin + 3;
					}
				}

				button.show();

			} else {
				title.setText("");
				objective.setText("");
				log.setText("");
				button.hide();
				emptyLabel.setVisible(true);
			}
		}

		@Override
		public void onTick() {}
	}

}
