package pw.caple.mc.omenscript;

import java.math.BigDecimal;
import java.util.List;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.getspout.spoutapi.inventory.SpoutItemStack;

/**
 * Builds all the NPCs, quests, and dialog as specified by each script and
 * stores that information in a {@link ScriptManager} for later use.
 */
public final class ScriptCompiler {

	private final ScriptManager manager;
	private final ThirdPartyIntegration thirdPartyIntegration;
	private int errorCount;

	public ScriptCompiler(ScriptManager manager) {
		this.manager = manager;
		thirdPartyIntegration = OmenScript.getInstance().getThirdPartyPlugins();
	}

	final public void compile(List<ScriptNode> nodes) {
		for (ScriptNode node : nodes) {
			processRootNode(node);
		}
	}

	final public int getErrorCount() {
		return errorCount;
	}

	final private void showScriptError(ScriptNode node, String message) {
		errorCount++;
		OmenScript.getInstance().getLogger().warning("Script error! [" + node.getScriptName() + "]: " + message + " at line " + node.getLineNumber() + " -> " + node.getLine().trim());
	}

	final private void showNoChildrenAllowedError(ScriptNode node) {
		errorCount++;
		OmenScript.getInstance().getLogger().warning("Script error! [" + node.getScriptName() + "]: " + node.getKey().toUpperCase() + " node does not support subkeys - at line " + node.getLineNumber() + " -> " + node.getLine().trim());
	}

	final private void showInvalidSubkeyError(ScriptNode node) {
		errorCount++;
		OmenScript.getInstance().getLogger().warning("Script error! [" + node.getScriptName() + "]: invalid subkey at line " + node.getLineNumber() + " -> " + node.getLine().trim());
	}

	final private void processRootNode(ScriptNode node) {
		switch (node.getKey()) {
		case "dialog": {
			String NPCName = node.getValue();
			DialogNode root = new DialogNode("-");
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfDialog(root, child);
			}
			for (DialogNode child : root.getChildren()) {
				manager.addDialog(NPCName, child);
			}
			break;
		}
		case "quest": {
			Quest quest = manager.getOrCreateQuest(node.getValue());
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfQuest(quest, child);
			}
			//make sure descriptions default to last step description if empty
			String description = "";
			for (QuestStep part : quest.getAllSteps()) {
				if (part.getDescription().isEmpty()) {
					part.setDescription(description);
				} else {
					description = part.getDescription();
				}
			}
			break;
		}
		case "location": {
			if (node.getValue().isEmpty()) {
				showScriptError(node, "Root node location must have a value (name)");
				return;
			}
			Location location = new Location(Bukkit.getWorlds().get(0), 0, 0, 0);
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfLocation(location, child);
			}
			manager.getLocationManager().addLocation(node.getValue(), location, 4);
			break;
		}
		default: {
			showScriptError(node, "Invalid root key");
			break;
		}
		}
	}

	final private void processSubNodeOfLocation(Location location, ScriptNode node) {
		String[] args = node.getValue().split("\\s+");
		switch (node.getKey()) {
		case "world": {
			if (args.length == 1) {
				World world = Bukkit.getWorld(node.getValue());
				if (world != null) {
					location.setWorld(world);
				} else {
					showScriptError(node, "Given world name does not exist");
				}
			} else {
				showScriptError(node, "Expected name of world");
			}
			break;
		}
		case "x": {
			if (args.length == 1 && InputUtil.isInteger(args[0])) {
				location.setX(Integer.valueOf(args[0]));
			} else {
				showScriptError(node, "Expected whole number");
			}
			break;
		}
		case "y": {
			if (args.length == 1 && InputUtil.isInteger(args[0])) {
				location.setY(Integer.valueOf(args[0]));
			} else {
				showScriptError(node, "Expected whole number");
			}
			break;
		}
		case "z": {
			if (args.length == 1 && InputUtil.isInteger(args[0])) {
				location.setZ(Integer.valueOf(args[0]));
			} else {
				showScriptError(node, "Expected whole number");
			}
			break;
		}
		case "coords":
		case "xyz": {
			for (int i = 0; i < args.length; i++) {
				args[i].replace(',', ' ');
			}
			if (args.length == 3 && InputUtil.isInteger(args[0]) && InputUtil.isInteger(args[1]) && InputUtil.isInteger(args[2])) {
				location.setX(Integer.valueOf(args[0]));
				location.setY(Integer.valueOf(args[1]));
				location.setZ(Integer.valueOf(args[2]));
			} else {
				showScriptError(node, "Expected 3 whole numbers representing X, Y, and Z");
			}
			break;
		}
		default: {
			showInvalidSubkeyError(node);
			break;
		}
		}
	}

	final private void processSubNodeOfQuest(Quest quest, ScriptNode node) {
		switch (node.getKey()) {
		case "repeatable": {
			switch (node.getValue().toLowerCase()) {
			case "true":
			case "yes":
				quest.setRepeatable(true);
				break;
			case "false":
			case "no":
				quest.setRepeatable(false);
				break;
			default:
				showScriptError(node, "Invalid value (expected yes, no, true, or false)");
				return;
			}
			if (node.hasChildren()) showNoChildrenAllowedError(node);
			break;
		}
		case "prerequisites": {
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfPrerequisites(quest, child);
			}
			break;
		}
		case "rewards":
		case "oncomplete": {
			for (ScriptNode child : node.getChildren()) {
				processCommonNodes(quest.getTurnInTriggers(), child);
			}
			break;
		}
		case "onupdate": {
			for (ScriptNode child : node.getChildren()) {
				processCommonNodes(quest.getUpdateTriggers(), child);
			}
			break;
		}
		case "onstart": {
			for (ScriptNode child : node.getChildren()) {
				processCommonNodes(quest.getStartTriggers(), child);
			}
			break;
		}
		case "finishedtext": {
			quest.setReadyToTurnInDescription(node.getValue());
			break;
		}
		case "step": {
			QuestStep step = new QuestStep();
			if (node.getValue().length() > 0) {
				step.setDescription(node.getValue());
			}
			quest.addStep(step);
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfStep(quest, step, child);
			}
			break;
		}
		default: {
			showInvalidSubkeyError(node);
			break;
		}
		}
	}

	final private void processSubNodeOfPrerequisites(Quest quest, ScriptNode node) {
		switch (node.getKey()) {
		case "quest": {
			if (node.getValue().length() > 0) {
				Quest prereqQuest = manager.getOrCreateQuest(node.getValue());
				quest.addPrerequisiteQuest(prereqQuest);
			} else {
				showScriptError(node, "Expected value");
			}
			break;
		}
		default: {
			showInvalidSubkeyError(node);
			break;
		}
		}
		if (node.hasChildren()) showNoChildrenAllowedError(node);
	}

	final private void processSubNodeOfStep(Quest quest, QuestStep step, ScriptNode node) {
		String[] args = node.getValue().split("\\s+");
		switch (node.getKey()) {
		case "kill": {
			if (args.length > 0) {
				int amount = 1;
				String entityName;
				if (InputUtil.isInteger(args[0])) {
					amount = Integer.valueOf(args[0]);
					entityName = node.getValue().substring(args[0].length() + 1);
				} else {
					entityName = node.getValue();
				}
				EntityType type = InputUtil.getEntityType(entityName);
				if (type != null) {
					ObjectiveKill objective = new ObjectiveKill(quest, type, amount);
					step.addObjective(objective);
				} else {
					showScriptError(node, "Invalid entity name");
				}
			} else {
				showScriptError(node, "Expected value");
			}
			break;
		}
		case "killepicboss": {
			if (thirdPartyIntegration.isRunningEpicBoss()) {
				if (args.length == 1) {
					ObjectiveKillEpicBoss objective = new ObjectiveKillEpicBoss(quest, args[0], 1);
					step.addObjective(objective);
				} else if (args.length == 2 && InputUtil.isInteger(args[0])) {
					int amount = Integer.valueOf(args[0]);
					ObjectiveKillEpicBoss objective = new ObjectiveKillEpicBoss(quest, args[1], amount);
					step.addObjective(objective);
				} else {
					showScriptError(node, "Expected epic boss name or whole number and boss name");
				}
			} else {
				showScriptError(node, "EpicBossRecoded plugin is required");
			}
			break;
		}
		case "travelto": {
			if (args.length == 4 && InputUtil.isInteger(args[1]) && InputUtil.isInteger(args[2]) && InputUtil.isInteger(args[3])) {
				String worldName = args[0];
				World world = Bukkit.getServer().getWorld(worldName);
				if (world != null) {
					int x = Integer.parseInt(args[1]);
					int y = Integer.parseInt(args[2]);
					int z = Integer.parseInt(args[3]);
					Location location = new Location(world, x, y, z);
					String locationName = quest.getTitle() + "-" + new Random().nextInt();
					manager.getLocationManager().addLocation(locationName, location, 4);
					ObjectiveTravelTo objective = new ObjectiveTravelTo(quest, manager.getLocationManager(), locationName, false);
					step.addObjective(objective);
				} else {
					showScriptError(node, "Invalid world name");
				}
			} else if (args.length > 0) {
				ObjectiveTravelTo objective = new ObjectiveTravelTo(quest, manager.getLocationManager(), node.getValue(), true);
				step.addObjective(objective);
			} else {
				showScriptError(node, "Expected location name or 'world x y z' (NO DECIMALS)");
			}
			break;
		}
		case "gather": {
			SpoutItemStack stack = InputUtil.getItems(node.getValue());
			if (stack != null) {
				ObjectiveGather objective = new ObjectiveGather(quest, stack);
				step.addObjective(objective);
			} else {
				showScriptError(node, "No item by this name exists.");
			}
			break;
		}
		default: {
			showInvalidSubkeyError(node);
			break;
		}
		}
		if (node.hasChildren()) showNoChildrenAllowedError(node);
	}

	final private void processSubNodeOfDialog(DialogNode root, ScriptNode node) {
		switch (node.getKey()) {
		case "option": {
			DialogNode dialog = new DialogNode(node.getValue());
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfOption(dialog, child);
			}
			root.addChild(dialog);
			break;
		}
		default: {
			showInvalidSubkeyError(node);
			break;
		}
		}
	}

	final private void processSubNodeOfOption(DialogNode dialogParent, ScriptNode node) {
		switch (node.getKey()) {
		case "option": {
			DialogNode dialog = new DialogNode(node.getValue());
			for (ScriptNode child : node.getChildren()) {
				processSubNodeOfOption(dialog, child);
			}
			dialogParent.addChild(dialog);
			return;
		}
		case "response": {
			dialogParent.addResponse(node.getValue());
			break;
		}
		case "givequest": {
			if (node.getValue().length() > 0) {
				Quest quest = manager.getOrCreateQuest(node.getValue());
				dialogParent.addQuestToStart(quest);
			} else {
				showScriptError(node, "Expected value");
			}
			break;
		}
		case "turnin": {
			if (node.getValue().length() > 0) {
				Quest quest = manager.getOrCreateQuest(node.getValue());
				dialogParent.addQuestToFinish(quest);
			} else {
				showScriptError(node, "Expected value");
			}
			break;
		}
		default: {
			processCommonNodes(dialogParent.getOnClick(), node);
			break;
		}
		}
		if (node.hasChildren()) showNoChildrenAllowedError(node);
	}

	final private void processCommonNodes(CommonNodes trigger, ScriptNode node) {
		switch (node.getKey()) {
		case "command": {
			trigger.commands.add(node.getValue());
			break;
		}
		case "sound": {
			trigger.soundURL = node.getValue();
			break;
		}
		case "item":
		case "items":
		case "additem":
		case "additems":
		case "giveitem":
		case "giveitems": {
			SpoutItemStack stack = InputUtil.getItems(node.getValue());
			if (stack != null) {
				trigger.itemsToAdd.add(stack);
			} else {
				showScriptError(node, "No item by this name exists.");
			}
			break;
		}
		case "takeitem":
		case "takeitems":
		case "removeitem":
		case "removeitems": {
			SpoutItemStack stack = InputUtil.getItems(node.getValue());
			if (stack != null) {
				trigger.itemsToRemove.add(stack);
			} else {
				showScriptError(node, "No item by this name exists.");
			}
			break;
		}
		case "givexp":
		case "giveexp":
		case "xp":
		case "exp": {
			if (InputUtil.isInteger(node.getValue())) {
				int xp = Integer.parseInt(node.getValue());
				trigger.xp += xp;
			} else {
				showScriptError(node, "Expected whole number");
			}
			break;
		}
		case "givecash":
		case "givemoney":
		case "cash":
		case "money": {
			if (thirdPartyIntegration.hasHookedEconomy()) {
				String formatted = node.getValue().replace(",", "");
				if (InputUtil.isDecimal(formatted)) {
					BigDecimal money = new BigDecimal(node.getValue());
					trigger.money = trigger.money.add(money);
				} else {
					showScriptError(node, "Expected decimal or whole number");
				}
			} else {
				showScriptError(node, "Vault and an economy plugin are required");
			}
			break;
		}
		case "heroesxp":
		case "heroxp":
		case "heroesexp":
		case "heroexp": {
			if (thirdPartyIntegration.isRunningHeroes()) {
				String formatted = node.getValue().replace(",", "");
				if (InputUtil.isDecimal(formatted)) {
					double xp = Double.valueOf(node.getValue());
					trigger.heroesXP += xp;
				} else {
					showScriptError(node, "Expected decimal or whole number");
				}
			} else {
				showScriptError(node, "Heroes plugin is required");
			}
			break;
		}
		default: {
			showInvalidSubkeyError(node);
			break;
		}
		}
		if (node.hasChildren()) showNoChildrenAllowedError(node);
	}

}
