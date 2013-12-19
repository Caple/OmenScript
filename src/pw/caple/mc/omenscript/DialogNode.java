package pw.caple.mc.omenscript;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.bukkit.ChatColor;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * Describes a dialog option and it's consequences.
 */
public final class DialogNode {

	private String text;
	private final CommonNodes onClick = new CommonNodes();
	private final List<DialogNode> children = new ArrayList<>();
	private final List<String> responses = new ArrayList<>();
	private final List<Quest> questsToStart = new ArrayList<>();
	private final List<Quest> questsToFinish = new ArrayList<>();

	public DialogNode(String newText) {
		text = newText;
	}

	public void addChild(DialogNode node) {
		children.add(node);
	}

	public List<DialogNode> getChildren() {
		return children;
	}

	public void setText(String newText) {
		text = newText;
	}

	public String getText() {
		return text;
	}

	public void addResponse(String newResponse) {
		responses.add(newResponse);
	}

	public String getResponse() {
		if (responses.size() > 0) {
			int index = new Random().nextInt(responses.size());
			return responses.get(index);
		} else {
			return null;
		}
	}

	public void addQuestToStart(Quest quest) {
		questsToStart.add(quest);
	}

	public void addQuestToFinish(Quest quest) {
		questsToFinish.add(quest);
	}

	public boolean shouldContinue(SpoutPlayer player) {
		for (Quest quest : questsToFinish) {
			boolean meetsConditions = true;
			for (SpoutItemStack stack : quest.getTurnInTriggers().itemsToRemove) {
				if (InventoryUtil.getNumberOf(stack, player) < stack.getAmount()) {
					meetsConditions = false;
					player.sendMessage(ChatColor.RED + "That quest requires " + stack.getAmount() + " " + stack.getMaterial().getName() + " to turn in.");
				}
			}
			if (!meetsConditions) return false;
		}
		return true;
	}

	public void runActions(SpoutPlayer player) {
		for (Quest quest : questsToStart) {
			quest.addPlayer(player);
		}
		for (Quest quest : questsToFinish) {
			quest.turnIn(player);
		}
		onClick.run(player);
	}

	public boolean isVisibleTo(SpoutPlayer player) {
		for (Quest quest : questsToStart) {
			if (!quest.isQualifiedToStart(player)) return false;
		}
		for (Quest quest : questsToFinish) {
			if (!quest.isReadyToTurnIn(player)) return false;
		}
		return true;
	}

	public boolean finishesQuest() {
		return questsToFinish.size() > 0;
	}

	public DialogRewardsWidget getRewardsInformation() {
		DialogRewardsWidget widget = new DialogRewardsWidget();
		BigDecimal money = new BigDecimal("0");
		for (Quest quest : questsToFinish) {
			CommonNodes completion = quest.getTurnInTriggers();
			money = money.add(completion.money);
			for (SpoutItemStack stack : completion.itemsToAdd) {
				String itemName;
				if (stack.getMaterial() != null) {
					itemName = stack.getMaterial().getName();
				} else if (stack.getData() != null) {
					itemName = stack.getData().toString();
				} else {
					itemName = stack.toString();
				}
				widget.addLabel("+ " + stack.getAmount() + ' ' + itemName);
			}
			for (SpoutItemStack stack : completion.itemsToRemove) {
				String itemName;
				if (stack.getMaterial() != null) {
					itemName = stack.getMaterial().getName();
				} else if (stack.getData() != null) {
					itemName = stack.getData().toString();
				} else {
					itemName = stack.toString();
				}
				widget.addLabel(ChatColor.DARK_RED + "- " + stack.getAmount() + ' ' + itemName);
			}
			if (completion.xp > 0) {
				widget.addLabel("+ " + completion.xp + " XP");
			}
			if (completion.heroesXP > 0) {
				widget.addLabel("+ " + completion.heroesXP + " Hero EXP");
			}
		}
		if (money.compareTo(BigDecimal.ZERO) != 0) {
			DecimalFormat formater = new DecimalFormat("#,##0.00");
			String currencyName = OmenScript.getInstance().getThirdPartyPlugins().getCurrency(money.compareTo(BigDecimal.ONE) != 0);
			if (money.compareTo(BigDecimal.ZERO) > 0) {
				widget.addLabel("+ " + formater.format(money) + ' ' + currencyName);
			} else {
				widget.addLabel(ChatColor.RED + formater.format(money) + ' ' + currencyName);
			}
		}
		return widget;
	}

	public CommonNodes getOnClick() {
		return onClick;
	}

}
