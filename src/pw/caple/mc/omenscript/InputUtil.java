package pw.caple.mc.omenscript;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.getspout.spoutapi.inventory.SpoutItemStack;
import org.getspout.spoutapi.material.CustomItem;
import org.getspout.spoutapi.material.MaterialData;

public class InputUtil {

	private InputUtil() {}

	final private static Map<String, EntityType> entityTypes = new HashMap<>();
	final private static Map<EntityType, String> entityName = new HashMap<>();
	final private static Map<EntityType, String> entityNamePlural = new HashMap<>();
	final private static Map<String, Material> materials = new HashMap<>();
	final private static Map<String, ItemStack> specialMaterials = new HashMap<>();
	final private static Map<String, CustomItem> customItems = new HashMap<>();

	final private static TokenReplacer tokenReplacer = new TokenReplacer();

	final private static String Digits = "(\\p{Digit}+)";
	final private static String HexDigits = "(\\p{XDigit}+)";
	// an exponent is 'e' or 'E' followed by an optionally 
	// signed decimal integer.
	final private static String Exp = "[eE][+-]?" + Digits;
	final private static String decimalRegex =
			("[\\x00-\\x20]*" + // Optional leading "whitespace"
					"[+-]?(" + // Optional sign character
					"NaN|" + // "NaN" string
					"Infinity|" + // "Infinity" string

					// A decimal floating-point string representing a finite positive
					// number without a leading sign has at most five basic pieces:
					// Digits . Digits ExponentPart FloatTypeSuffix
					// 
					// Since this method allows integer-only strings as input
					// in addition to strings of floating-point literals, the
					// two sub-patterns below are simplifications of the grammar
					// productions from the Java Language Specification, 2nd 
					// edition, section 3.10.2.

					// Digits ._opt Digits_opt ExponentPart_opt FloatTypeSuffix_opt
					"(((" + Digits + "(\\.)?(" + Digits + "?)(" + Exp + ")?)|" +

					// . Digits ExponentPart_opt FloatTypeSuffix_opt
					"(\\.(" + Digits + ")(" + Exp + ")?)|" +

					// Hexadecimal strings
					"((" +
					// 0[xX] HexDigits ._opt BinaryExponent FloatTypeSuffix_opt
					"(0[xX]" + HexDigits + "(\\.)?)|" +

					// 0[xX] HexDigits_opt . HexDigits BinaryExponent FloatTypeSuffix_opt
					"(0[xX]" + HexDigits + "?(\\.)" + HexDigits + ")" +

					")[pP][+-]?" + Digits + "))" +
					"[fFdD]?))" +
			"[\\x00-\\x20]*");// Optional trailing "whitespace"

	static {

		for (EntityType entityType : EntityType.values()) {
			String name = entityType.name().toLowerCase().replace('_', ' ');
			entityTypes.put(name, entityType);
			entityTypes.put(name + "s", entityType);
			entityTypes.put(name + "es", entityType);
			entityTypes.put(name.substring(0, name.length() - 1) + "ies", entityType);;
			String getName = entityType.getName();
			if (getName != null) {
				getName = getName.toLowerCase();
				entityTypes.put(getName, entityType);
				entityTypes.put(getName + "s", entityType);
				entityTypes.put(getName + "es", entityType);
				entityTypes.put(getName.substring(0, getName.length() - 1) + "ies", entityType);
			}
		}

		entityTypes.put("xp orbs", EntityType.EXPERIENCE_ORB);
		entityTypes.put("xp orb", EntityType.EXPERIENCE_ORB);
		entityTypes.put("xp", EntityType.EXPERIENCE_ORB);

		entityTypes.put("painting", EntityType.PAINTING);
		entityTypes.put("paintings", EntityType.PAINTING);

		entityTypes.put("arrow", EntityType.ARROW);
		entityTypes.put("arrows", EntityType.ARROW);

		entityTypes.put("fireball", EntityType.FIREBALL);
		entityTypes.put("fireballs", EntityType.FIREBALL);
		entityTypes.put("fire ball", EntityType.FIREBALL);
		entityTypes.put("fire balls", EntityType.FIREBALL);

		entityTypes.put("small fireball", EntityType.SMALL_FIREBALL);
		entityTypes.put("small fireballs", EntityType.SMALL_FIREBALL);
		entityTypes.put("small fire ball", EntityType.SMALL_FIREBALL);
		entityTypes.put("small fire balls", EntityType.SMALL_FIREBALL);

		entityTypes.put("snowball", EntityType.SNOWBALL);
		entityTypes.put("snowballs", EntityType.SNOWBALL);
		entityTypes.put("snow ball", EntityType.SNOWBALL);
		entityTypes.put("snow balls", EntityType.SNOWBALL);

		entityTypes.put("enderpearl", EntityType.ENDER_PEARL);
		entityTypes.put("enderpearls", EntityType.ENDER_PEARL);
		entityTypes.put("ender pearl", EntityType.ENDER_PEARL);
		entityTypes.put("ender pearls", EntityType.ENDER_PEARL);

		entityTypes.put("xp bottle", EntityType.THROWN_EXP_BOTTLE);
		entityTypes.put("xp bottles", EntityType.THROWN_EXP_BOTTLE);

		entityTypes.put("itemframe", EntityType.ITEM_FRAME);
		entityTypes.put("itemrames", EntityType.ITEM_FRAME);
		entityTypes.put("item frame", EntityType.ITEM_FRAME);
		entityTypes.put("item frames", EntityType.ITEM_FRAME);

		entityTypes.put("witherskull", EntityType.WITHER_SKULL);
		entityTypes.put("witherskulls", EntityType.WITHER_SKULL);
		entityTypes.put("wither skull", EntityType.WITHER_SKULL);
		entityTypes.put("wither skulls", EntityType.WITHER_SKULL);

		entityTypes.put("tnt", EntityType.PRIMED_TNT);

		entityTypes.put("firework", EntityType.FIREWORK);
		entityTypes.put("fireworks", EntityType.FIREWORK);

		entityTypes.put("boat", EntityType.BOAT);
		entityTypes.put("boats", EntityType.BOAT);

		entityTypes.put("minecart", EntityType.MINECART);
		entityTypes.put("minecarts", EntityType.MINECART);
		entityTypes.put("mine cart", EntityType.MINECART);
		entityTypes.put("mine carts", EntityType.MINECART);

		entityTypes.put("chestminecart", EntityType.MINECART_CHEST);
		entityTypes.put("chestminecarts", EntityType.MINECART_CHEST);
		entityTypes.put("chest minecart", EntityType.MINECART_CHEST);
		entityTypes.put("chest minecarts", EntityType.MINECART_CHEST);
		entityTypes.put("chest mine cart", EntityType.MINECART_CHEST);
		entityTypes.put("chest mine carts", EntityType.MINECART_CHEST);
		entityTypes.put("minecart chest", EntityType.MINECART_CHEST);
		entityTypes.put("minecart chests", EntityType.MINECART_CHEST);
		entityTypes.put("mine cart chest", EntityType.MINECART_CHEST);
		entityTypes.put("mine cart chests", EntityType.MINECART_CHEST);

		entityTypes.put("furnaceminecart", EntityType.MINECART_FURNACE);
		entityTypes.put("furnaceminecarts", EntityType.MINECART_FURNACE);
		entityTypes.put("furnace minecart", EntityType.MINECART_FURNACE);
		entityTypes.put("furnace minecarts", EntityType.MINECART_FURNACE);
		entityTypes.put("furnace mine cart", EntityType.MINECART_FURNACE);
		entityTypes.put("furnace mine carts", EntityType.MINECART_FURNACE);
		entityTypes.put("minecart furnace", EntityType.MINECART_FURNACE);
		entityTypes.put("minecart furnaces", EntityType.MINECART_FURNACE);
		entityTypes.put("mine cart furnace", EntityType.MINECART_FURNACE);
		entityTypes.put("mine cart furnaces", EntityType.MINECART_FURNACE);
		entityTypes.put("ovenminecart", EntityType.MINECART_FURNACE);
		entityTypes.put("ovenminecarts", EntityType.MINECART_FURNACE);
		entityTypes.put("oven minecart", EntityType.MINECART_FURNACE);
		entityTypes.put("oven minecarts", EntityType.MINECART_FURNACE);
		entityTypes.put("oven mine cart", EntityType.MINECART_FURNACE);
		entityTypes.put("oven mine carts", EntityType.MINECART_FURNACE);
		entityTypes.put("minecart oven", EntityType.MINECART_FURNACE);
		entityTypes.put("minecart ovens", EntityType.MINECART_FURNACE);
		entityTypes.put("mine cart oven", EntityType.MINECART_FURNACE);
		entityTypes.put("mine cart ovens", EntityType.MINECART_FURNACE);

		entityTypes.put("tntminecart", EntityType.MINECART_TNT);
		entityTypes.put("tntminecarts", EntityType.MINECART_TNT);
		entityTypes.put("tnt minecart", EntityType.MINECART_TNT);
		entityTypes.put("tnt minecarts", EntityType.MINECART_TNT);
		entityTypes.put("tnt mine cart", EntityType.MINECART_TNT);
		entityTypes.put("tnt mine carts", EntityType.MINECART_TNT);
		entityTypes.put("minecart tnt", EntityType.MINECART_TNT);
		entityTypes.put("mine cart tnt", EntityType.MINECART_TNT);

		entityTypes.put("hopperminecart", EntityType.MINECART_HOPPER);
		entityTypes.put("hopperminecarts", EntityType.MINECART_HOPPER);
		entityTypes.put("hopper minecart", EntityType.MINECART_HOPPER);
		entityTypes.put("hopper minecarts", EntityType.MINECART_HOPPER);
		entityTypes.put("hopper mine cart", EntityType.MINECART_HOPPER);
		entityTypes.put("hopper mine carts", EntityType.MINECART_HOPPER);
		entityTypes.put("minecart hopper", EntityType.MINECART_HOPPER);
		entityTypes.put("mine cart hoppers", EntityType.MINECART_HOPPER);

		entityTypes.put("mobspawnerminecart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mobspawnerminecarts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mobspawner minecart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mobspawner minecarts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mobspawner mine cart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mobspawner mine carts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("minecart mobspawner", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mine cart mobspawners", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mob spawner minecart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mob spawner minecarts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mob spawner mine cart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mob spawner mine carts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("minecart mob spawner", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mine cart mob spawners", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("spawner minecart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("spawner minecarts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("spawner mine cart", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("spawner mine carts", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("minecart spawner", EntityType.MINECART_MOB_SPAWNER);
		entityTypes.put("mine cart spawners", EntityType.MINECART_MOB_SPAWNER);

		entityTypes.put("creeper", EntityType.CREEPER);
		entityTypes.put("creepers", EntityType.CREEPER);

		entityTypes.put("skeleton", EntityType.SKELETON);
		entityTypes.put("skeletons", EntityType.SKELETON);

		entityTypes.put("spider", EntityType.SPIDER);
		entityTypes.put("spiders", EntityType.SPIDER);

		entityTypes.put("giant", EntityType.GIANT);
		entityTypes.put("giants", EntityType.GIANT);

		entityTypes.put("zombie", EntityType.ZOMBIE);
		entityTypes.put("zombies", EntityType.ZOMBIE);

		entityTypes.put("silme", EntityType.SLIME);
		entityTypes.put("slimes", EntityType.SLIME);

		entityTypes.put("ghast", EntityType.GHAST);
		entityTypes.put("ghasts", EntityType.GHAST);

		entityTypes.put("pigzombie", EntityType.PIG_ZOMBIE);
		entityTypes.put("pigzombies", EntityType.PIG_ZOMBIE);
		entityTypes.put("pig zombie", EntityType.PIG_ZOMBIE);
		entityTypes.put("pig zombies", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombiepig", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombiepigs", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombie pig", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombie pigs", EntityType.PIG_ZOMBIE);
		entityTypes.put("pigman", EntityType.PIG_ZOMBIE);
		entityTypes.put("pigmen", EntityType.PIG_ZOMBIE);
		entityTypes.put("pig man", EntityType.PIG_ZOMBIE);
		entityTypes.put("pig men", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombiepigman", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombiepigmen", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombie pigman", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombie pigmen", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombie pig man", EntityType.PIG_ZOMBIE);
		entityTypes.put("zombie pig men", EntityType.PIG_ZOMBIE);

		entityTypes.put("enderman", EntityType.ENDERMAN);
		entityTypes.put("endermen", EntityType.ENDERMAN);
		entityTypes.put("ender man", EntityType.ENDERMAN);
		entityTypes.put("ender men", EntityType.ENDERMAN);

		entityTypes.put("cavespider", EntityType.CAVE_SPIDER);
		entityTypes.put("cavespiders", EntityType.CAVE_SPIDER);
		entityTypes.put("cave spider", EntityType.CAVE_SPIDER);
		entityTypes.put("cave spiders", EntityType.CAVE_SPIDER);

		entityTypes.put("silverfish", EntityType.SILVERFISH);

		entityTypes.put("blaze", EntityType.BLAZE);
		entityTypes.put("blazes", EntityType.BLAZE);

		entityTypes.put("magmacube", EntityType.MAGMA_CUBE);
		entityTypes.put("magmacubes", EntityType.MAGMA_CUBE);
		entityTypes.put("magma cube", EntityType.MAGMA_CUBE);
		entityTypes.put("magma cubes", EntityType.MAGMA_CUBE);
		entityTypes.put("lavaslime", EntityType.MAGMA_CUBE);
		entityTypes.put("lavaslimes", EntityType.MAGMA_CUBE);
		entityTypes.put("lava slime", EntityType.MAGMA_CUBE);
		entityTypes.put("lava slimes", EntityType.MAGMA_CUBE);

		entityTypes.put("enderdragon", EntityType.ENDER_DRAGON);
		entityTypes.put("enderdragons", EntityType.ENDER_DRAGON);
		entityTypes.put("ender dragon", EntityType.ENDER_DRAGON);
		entityTypes.put("ender dragons", EntityType.ENDER_DRAGON);

		entityTypes.put("wither", EntityType.WITHER);
		entityTypes.put("withers", EntityType.WITHER);

		entityTypes.put("bat", EntityType.BAT);
		entityTypes.put("bats", EntityType.BAT);

		entityTypes.put("witch", EntityType.WITCH);
		entityTypes.put("witches", EntityType.WITCH);

		entityTypes.put("pig", EntityType.PIG);
		entityTypes.put("pigs", EntityType.PIG);

		entityTypes.put("sheep", EntityType.SHEEP);

		entityTypes.put("cow", EntityType.COW);
		entityTypes.put("cows", EntityType.COW);

		entityTypes.put("chicken", EntityType.CHICKEN);
		entityTypes.put("chickens", EntityType.CHICKEN);

		entityTypes.put("squid", EntityType.SQUID);

		entityTypes.put("wolf", EntityType.WOLF);
		entityTypes.put("wolves", EntityType.WOLF);

		entityTypes.put("mushroomcow", EntityType.MUSHROOM_COW);
		entityTypes.put("mushroomcows", EntityType.MUSHROOM_COW);
		entityTypes.put("mushroom cow", EntityType.MUSHROOM_COW);
		entityTypes.put("mushroom cows", EntityType.MUSHROOM_COW);
		entityTypes.put("mooshroom", EntityType.MUSHROOM_COW);
		entityTypes.put("mooshrooms", EntityType.MUSHROOM_COW);

		entityTypes.put("snowman", EntityType.SNOWMAN);
		entityTypes.put("snowmen", EntityType.SNOWMAN);

		entityTypes.put("ocelot", EntityType.OCELOT);
		entityTypes.put("ocelots", EntityType.OCELOT);

		entityTypes.put("irongolem", EntityType.IRON_GOLEM);
		entityTypes.put("irongolems", EntityType.IRON_GOLEM);
		entityTypes.put("iron golem", EntityType.IRON_GOLEM);
		entityTypes.put("iron golems", EntityType.IRON_GOLEM);

		entityTypes.put("villager", EntityType.VILLAGER);
		entityTypes.put("villagers", EntityType.VILLAGER);

		entityTypes.put("endercrystal", EntityType.ENDER_CRYSTAL);
		entityTypes.put("endercrystals", EntityType.ENDER_CRYSTAL);
		entityTypes.put("ender crystal", EntityType.ENDER_CRYSTAL);
		entityTypes.put("ender crystals", EntityType.ENDER_CRYSTAL);

		entityTypes.put("splashpotion", EntityType.SPLASH_POTION);
		entityTypes.put("splashpotions", EntityType.SPLASH_POTION);
		entityTypes.put("splash potion", EntityType.SPLASH_POTION);
		entityTypes.put("splash potions", EntityType.SPLASH_POTION);

		entityTypes.put("egg", EntityType.EGG);
		entityTypes.put("eggs", EntityType.EGG);

		entityTypes.put("fishinghook", EntityType.FISHING_HOOK);
		entityTypes.put("fishinghooks", EntityType.FISHING_HOOK);
		entityTypes.put("fishing hook", EntityType.FISHING_HOOK);
		entityTypes.put("fishing hooks", EntityType.FISHING_HOOK);

		entityTypes.put("lightning", EntityType.LIGHTNING);
		entityTypes.put("lightning bolt", EntityType.LIGHTNING);
		entityTypes.put("lightning bolts", EntityType.LIGHTNING);

		entityTypes.put("player", EntityType.PLAYER);
		entityTypes.put("players", EntityType.PLAYER);

		entityName.put(EntityType.EXPERIENCE_ORB, "xp orb");
		entityName.put(EntityType.PAINTING, "painting");
		entityName.put(EntityType.ARROW, "arrow");;
		entityName.put(EntityType.FIREBALL, "fireball");
		entityName.put(EntityType.SMALL_FIREBALL, "small fireball");
		entityName.put(EntityType.SNOWBALL, "snowball");
		entityName.put(EntityType.ENDER_PEARL, "enderpearl");
		entityName.put(EntityType.THROWN_EXP_BOTTLE, "xp bottle");
		entityName.put(EntityType.ITEM_FRAME, "item frame");
		entityName.put(EntityType.WITHER_SKULL, "wither skull");
		entityName.put(EntityType.PRIMED_TNT, "tnt");
		entityName.put(EntityType.FIREWORK, "firework");
		entityName.put(EntityType.BOAT, "boat");
		entityName.put(EntityType.MINECART, "minecart");
		entityName.put(EntityType.MINECART_CHEST, "chest minecart");
		entityName.put(EntityType.MINECART_FURNACE, "furnace minecart");
		entityName.put(EntityType.MINECART_TNT, "tnt minecart");
		entityName.put(EntityType.MINECART_HOPPER, "hopper minecart");
		entityName.put(EntityType.MINECART_MOB_SPAWNER, "mob spawner minecart");
		entityName.put(EntityType.CREEPER, "creeper");
		entityName.put(EntityType.SKELETON, "skeleton");
		entityName.put(EntityType.SPIDER, "spider");
		entityName.put(EntityType.GIANT, "giant");
		entityName.put(EntityType.ZOMBIE, "zombie");
		entityName.put(EntityType.SLIME, "silme");
		entityName.put(EntityType.GHAST, "ghast");
		entityName.put(EntityType.PIG_ZOMBIE, "zombie pigman");
		entityName.put(EntityType.ENDERMAN, "enderman");
		entityName.put(EntityType.CAVE_SPIDER, "cave spider");
		entityName.put(EntityType.SILVERFISH, "silverfish");
		entityName.put(EntityType.BLAZE, "blaze");
		entityName.put(EntityType.MAGMA_CUBE, "magma cube");
		entityName.put(EntityType.ENDER_DRAGON, "ender dragon");
		entityName.put(EntityType.WITHER, "wither");
		entityName.put(EntityType.BAT, "bat");
		entityName.put(EntityType.WITCH, "witch");
		entityName.put(EntityType.PIG, "pig");
		entityName.put(EntityType.SHEEP, "sheep");
		entityName.put(EntityType.COW, "cow");
		entityName.put(EntityType.CHICKEN, "chicken");
		entityName.put(EntityType.SQUID, "squid");
		entityName.put(EntityType.WOLF, "wolf");
		entityName.put(EntityType.MUSHROOM_COW, "mooshroom");
		entityName.put(EntityType.SNOWMAN, "snowman");
		entityName.put(EntityType.OCELOT, "ocelot");
		entityName.put(EntityType.IRON_GOLEM, "iron golem");
		entityName.put(EntityType.VILLAGER, "villager");
		entityName.put(EntityType.ENDER_CRYSTAL, "ender crystal");
		entityName.put(EntityType.SPLASH_POTION, "splash potion");
		entityName.put(EntityType.EGG, "egg");
		entityName.put(EntityType.FISHING_HOOK, "fishing hook");
		entityName.put(EntityType.LIGHTNING, "lightning bolt");
		entityName.put(EntityType.PLAYER, "player");

		entityNamePlural.put(EntityType.EXPERIENCE_ORB, "xp orbs");
		entityNamePlural.put(EntityType.PAINTING, "paintings");
		entityNamePlural.put(EntityType.ARROW, "arrows");;
		entityNamePlural.put(EntityType.FIREBALL, "fireballs");
		entityNamePlural.put(EntityType.SMALL_FIREBALL, "small fireballs");
		entityNamePlural.put(EntityType.SNOWBALL, "snowballs");
		entityNamePlural.put(EntityType.ENDER_PEARL, "enderpearls");
		entityNamePlural.put(EntityType.THROWN_EXP_BOTTLE, "xp bottles");
		entityNamePlural.put(EntityType.ITEM_FRAME, "item frames");
		entityNamePlural.put(EntityType.WITHER_SKULL, "wither skulls");
		entityNamePlural.put(EntityType.PRIMED_TNT, "tnt");
		entityNamePlural.put(EntityType.FIREWORK, "fireworks");
		entityNamePlural.put(EntityType.BOAT, "boats");
		entityNamePlural.put(EntityType.MINECART, "minecarts");
		entityNamePlural.put(EntityType.MINECART_CHEST, "chest minecarts");
		entityNamePlural.put(EntityType.MINECART_FURNACE, "furnace minecarts");
		entityNamePlural.put(EntityType.MINECART_TNT, "tnt minecarts");
		entityNamePlural.put(EntityType.MINECART_HOPPER, "hopper minecarts");
		entityNamePlural.put(EntityType.MINECART_MOB_SPAWNER, "mob spawner minecarts");
		entityNamePlural.put(EntityType.CREEPER, "creepers");
		entityNamePlural.put(EntityType.SKELETON, "skeletons");
		entityNamePlural.put(EntityType.SPIDER, "spiders");
		entityNamePlural.put(EntityType.GIANT, "giants");
		entityNamePlural.put(EntityType.ZOMBIE, "zombies");
		entityNamePlural.put(EntityType.SLIME, "silmes");
		entityNamePlural.put(EntityType.GHAST, "ghasts");
		entityNamePlural.put(EntityType.PIG_ZOMBIE, "zombie pigmen");
		entityNamePlural.put(EntityType.ENDERMAN, "endermen");
		entityNamePlural.put(EntityType.CAVE_SPIDER, "cave spiders");
		entityNamePlural.put(EntityType.SILVERFISH, "silverfish");
		entityNamePlural.put(EntityType.BLAZE, "blazes");
		entityNamePlural.put(EntityType.MAGMA_CUBE, "magma cubes");
		entityNamePlural.put(EntityType.ENDER_DRAGON, "ender dragons");
		entityNamePlural.put(EntityType.WITHER, "withers");
		entityNamePlural.put(EntityType.BAT, "bats");
		entityNamePlural.put(EntityType.WITCH, "witches");
		entityNamePlural.put(EntityType.PIG, "pigs");
		entityNamePlural.put(EntityType.SHEEP, "sheep");
		entityNamePlural.put(EntityType.COW, "cows");
		entityNamePlural.put(EntityType.CHICKEN, "chickens");
		entityNamePlural.put(EntityType.SQUID, "squid");
		entityNamePlural.put(EntityType.WOLF, "wolves");
		entityNamePlural.put(EntityType.MUSHROOM_COW, "mooshrooms");
		entityNamePlural.put(EntityType.SNOWMAN, "snowmen");
		entityNamePlural.put(EntityType.OCELOT, "ocelots");
		entityNamePlural.put(EntityType.IRON_GOLEM, "iron golems");
		entityNamePlural.put(EntityType.VILLAGER, "villagers");
		entityNamePlural.put(EntityType.ENDER_CRYSTAL, "ender crystals");
		entityNamePlural.put(EntityType.SPLASH_POTION, "splash potions");
		entityNamePlural.put(EntityType.EGG, "eggs");
		entityNamePlural.put(EntityType.FISHING_HOOK, "fishing hooks");
		entityNamePlural.put(EntityType.LIGHTNING, "lightning bolts");
		entityNamePlural.put(EntityType.PLAYER, "players");

		for (Material material : Material.values()) {
			String name = material.name().toLowerCase();
			materials.put(name, material);
			materials.put(name + "s", material);
			materials.put(name + "es", material);
			materials.put(name.substring(0, name.length() - 1) + "ies", material);;
			if (name.indexOf('_') > -1) {
				String alteredName = name.replace('_', ' ');
				materials.put(alteredName, material);
				materials.put(alteredName + "s", material);
				materials.put(alteredName + "es", material);
				materials.put(alteredName.substring(0, alteredName.length() - 1) + "ies", material);;
			}
		}

		materials.put("plank", Material.WOOD);
		materials.put("planks", Material.WOOD);
		materials.put("wood plank", Material.WOOD);
		materials.put("wood planks", Material.WOOD);
		materials.put("wooden plank", Material.WOOD);
		materials.put("wooden planks", Material.WOOD);

		materials.put("adminium", Material.BEDROCK);

		materials.put("raw wood", Material.LOG);

		materials.put("block of iron", Material.IRON_BLOCK);
		materials.put("block of gold", Material.GOLD_BLOCK);
		materials.put("block of diamond", Material.DIAMOND_BLOCK);
		materials.put("block of lapis", Material.LAPIS_BLOCK);
		materials.put("blocks of iron", Material.IRON_BLOCK);
		materials.put("blocks of gold", Material.GOLD_BLOCK);
		materials.put("blocks of diamond", Material.DIAMOND_BLOCK);
		materials.put("blocks of lapis", Material.LAPIS_BLOCK);

		materials.put("music block", Material.NOTE_BLOCK);
		materials.put("music blocks", Material.NOTE_BLOCK);

		materials.put("bed", Material.BED_BLOCK);
		materials.put("beds", Material.BED_BLOCK);

		materials.put("spider web", Material.WEB);
		materials.put("spider webs", Material.WEB);

		materials.put("flower", Material.YELLOW_FLOWER);
		materials.put("flowers", Material.YELLOW_FLOWER);

		materials.put("rose", Material.RED_ROSE);
		materials.put("roses", Material.RED_ROSE);

		materials.put("mushroom", Material.BROWN_MUSHROOM);
		materials.put("mushrooms", Material.BROWN_MUSHROOM);

		materials.put("slab", Material.STEP);
		materials.put("slabs", Material.STEP);
		materials.put("double slab", Material.DOUBLE_STEP);
		materials.put("double slabs", Material.DOUBLE_STEP);

		materials.put("wooden stairs", Material.WOOD_STAIRS);

		materials.put("wire", Material.REDSTONE_WIRE);
		materials.put("wires", Material.REDSTONE_WIRE);

		materials.put("crafting table", Material.WORKBENCH);
		materials.put("crafting tables", Material.WORKBENCH);
		materials.put("workbench", Material.WORKBENCH);
		materials.put("workbenches", Material.WORKBENCH);

		materials.put("oven", Material.FURNACE);
		materials.put("ovens", Material.FURNACE);

		materials.put("door", Material.WOODEN_DOOR);
		materials.put("doors", Material.WOODEN_DOOR);
		materials.put("wood door", Material.WOODEN_DOOR);
		materials.put("wood doors", Material.WOODEN_DOOR);

		materials.put("rail", Material.RAILS);

		materials.put("cobble stairs", Material.COBBLESTONE_STAIRS);

		materials.put("stone pressure plate", Material.STONE_PLATE);
		materials.put("stone pressure plates", Material.STONE_PLATE);

		materials.put("wood pressure plate", Material.WOOD_PLATE);
		materials.put("wood pressure plates", Material.WOOD_PLATE);
		materials.put("wooden pressure plate", Material.WOOD_PLATE);
		materials.put("wooden pressure plates", Material.WOOD_PLATE);
		materials.put("pressure plate", Material.WOOD_PLATE);
		materials.put("pressure plates", Material.WOOD_PLATE);

		materials.put("redstone torch", Material.REDSTONE_TORCH_ON);
		materials.put("redstone torches", Material.REDSTONE_TORCH_ON);

		materials.put("repeater", Material.DIODE);
		materials.put("repeaters", Material.DIODE);
		materials.put("redstone repeater", Material.DIODE);
		materials.put("redstone repeaters", Material.DIODE);

		materials.put("lilly", Material.WATER_LILY);
		materials.put("lillys", Material.WATER_LILY);
		materials.put("lilly pad", Material.WATER_LILY);
		materials.put("lilly pads", Material.WATER_LILY);

		materials.put("comparator", Material.REDSTONE_COMPARATOR);
		materials.put("comparators", Material.REDSTONE_COMPARATOR);

		materials.put("weighted plate", Material.GOLD_PLATE);
		materials.put("weighted plates", Material.GOLD_PLATE);
		materials.put("weighted pressure plate", Material.GOLD_PLATE);
		materials.put("weighted pressure plates", Material.GOLD_PLATE);
		materials.put("gold weighted plate", Material.GOLD_PLATE);
		materials.put("gold weighted plates", Material.GOLD_PLATE);
		materials.put("gold weighted pressure plate", Material.GOLD_PLATE);
		materials.put("gold weighted pressure plates", Material.GOLD_PLATE);
		materials.put("weighted gold plate", Material.GOLD_PLATE);
		materials.put("weighted gold plates", Material.GOLD_PLATE);
		materials.put("weighted gold pressure plate", Material.GOLD_PLATE);
		materials.put("weighted gold pressure plates", Material.GOLD_PLATE);
		materials.put("light weighted plate", Material.GOLD_PLATE);
		materials.put("light weighted plates", Material.GOLD_PLATE);
		materials.put("light weighted pressure plate", Material.GOLD_PLATE);
		materials.put("light weighted pressure plates", Material.GOLD_PLATE);
		materials.put("weighted plate (light)", Material.GOLD_PLATE);
		materials.put("weighted plates (light)", Material.GOLD_PLATE);
		materials.put("weighted pressure plate (light)", Material.GOLD_PLATE);
		materials.put("weighted pressure plates (light)", Material.GOLD_PLATE);

		materials.put("iron weighted plate", Material.IRON_PLATE);
		materials.put("iron weighted plates", Material.IRON_PLATE);
		materials.put("iron weighted pressure plate", Material.IRON_PLATE);
		materials.put("iron weighted pressure plates", Material.IRON_PLATE);
		materials.put("weighted iron plate", Material.IRON_PLATE);
		materials.put("weighted iron plates", Material.IRON_PLATE);
		materials.put("weighted iron pressure plate", Material.IRON_PLATE);
		materials.put("weighted iron pressure plates", Material.IRON_PLATE);
		materials.put("heavy weighted plate", Material.IRON_PLATE);
		materials.put("heavy weighted plates", Material.IRON_PLATE);
		materials.put("heavy weighted pressure plate", Material.IRON_PLATE);
		materials.put("heavy weighted pressure plates", Material.IRON_PLATE);
		materials.put("weighted plate (heavy)", Material.IRON_PLATE);
		materials.put("weighted plates (heavy)", Material.IRON_PLATE);
		materials.put("weighted pressure plate (heavy)", Material.IRON_PLATE);
		materials.put("weighted pressure plates (heavy)", Material.IRON_PLATE);

		materials.put("gunpowder", Material.SULPHUR);
		materials.put("gun powder", Material.SULPHUR);

		materials.put("porkchop", Material.PORK);
		materials.put("porkchops", Material.PORK);

		materials.put("cooked porkchop", Material.GRILLED_PORK);
		materials.put("cooked porkchops", Material.GRILLED_PORK);

		materials.put("gold apple", Material.GOLDEN_APPLE);

		materials.put("gold carrot", Material.GOLDEN_CARROT);

		materials.put("chest minecart", Material.STORAGE_MINECART);

		materials.put("clock", Material.WATCH);

		materials.put("fish", Material.RAW_FISH);

		materials.put("steak", Material.COOKED_BEEF);

		materials.put("nether wart", Material.NETHER_WARTS);
		materials.put("netherwart", Material.NETHER_WARTS);

		materials.put("nether wart seed", Material.NETHER_STALK);
		materials.put("nether wart seeds", Material.NETHER_STALK);
		materials.put("netherwart seed", Material.NETHER_STALK);
		materials.put("netherwart seeds", Material.NETHER_STALK);

		materials.put("tnt minecart", Material.EXPLOSIVE_MINECART);
		materials.put("tnt minecarts", Material.EXPLOSIVE_MINECART);

		// Materials with special damage values...

		String[] names = { "oak", "spruce", "birch", "jungle" };
		Byte[] data = { 0, 1, 2, 3 };

		putSpecialTypes(Material.WOOD, names, data, new String[] {
				"wood plank",
				"wood planks",
				"wooden plank",
				"wooden planks",
				"plank",
				"planks"
		});
		putSpecialTypes(Material.SAPLING, names, data, new String[] {
				"sapling",
				"saplings"
		});
		putSpecialTypes(Material.LOG, names, data, new String[] {
				"wood",
				"log",
				"logs",
				"wood log",
				"wood logs"
		});
		putSpecialTypes(Material.LEAVES, names, data, new String[] {
				"leaf",
				"leafs",
				"leaves"
		});
		putSpecialTypes(Material.WOOD_STEP, names, data, new String[] {
				"slab",
				"slabs",
				"step",
				"steps"
		});
		putSpecialTypes(Material.WOOD_DOUBLE_STEP, names, data, new String[] {
				"double slab",
				"double slabs",
				"double step",
				"double steps"
		});

		String[] stairNames = { "wooden stairs", "wood stairs", "stairs", "stair" };
		specialMaterials.put("stairs", new ItemStack(Material.WOOD_STAIRS));
		putSpecialTypes(Material.WOOD_STAIRS, "oak", stairNames);
		putSpecialTypes(Material.SPRUCE_WOOD_STAIRS, "spruce", stairNames);
		putSpecialTypes(Material.BIRCH_WOOD_STAIRS, "birch", stairNames);
		putSpecialTypes(Material.JUNGLE_WOOD_STAIRS, "jungle", stairNames);

		names = new String[] { "stone", "sandstone", "sand stone", "wooden", "wood", "cobblestone", "cobble", "brick", "stonebrick", "stone brick", "netherbrick", "nether brick", "quartz" };
		data = new Byte[] { 0, 1, 1, 2, 2, 3, 3, 4, 5, 5, 6, 6, 7 };
		putSpecialTypes(Material.STEP, names, data, new String[] {
				"slab",
				"slabs",
				"step",
				"steps"
		});
		putSpecialTypes(Material.DOUBLE_STEP, names, data, new String[] {
				"double slab",
				"double slabs",
				"double step",
				"double steps"
		});

		names = new String[] { "mossy", "cracked", "chisled" };
		data = new Byte[] { 1, 2, 3 };
		putSpecialTypes(Material.SMOOTH_BRICK, names, data, new String[] {
				"stone bricks",
				"bricks",
		});

		names = new String[] { "stone", "cobblestone", "cobble", "stone bricks", "bricks" };
		data = new Byte[] { 0, 1, 1, 2, 2 };
		putSpecialTypes(Material.SMOOTH_BRICK, names, data, new String[] {
				"silverfish",
				"bricks",
		});
		specialMaterials.put("silverfish stone", new ItemStack(Material.SMOOTH_BRICK, 0, (byte) 0));
		specialMaterials.put("silverfish cobblestone", new ItemStack(Material.SMOOTH_BRICK, 0, (byte) 1));
		specialMaterials.put("silverfish cobble", new ItemStack(Material.SMOOTH_BRICK, 0, (byte) 1));
		specialMaterials.put("silverfish stone bricks", new ItemStack(Material.SMOOTH_BRICK, 0, (byte) 2));
		specialMaterials.put("silverfish bricks", new ItemStack(Material.SMOOTH_BRICK, 0, (byte) 2));

		specialMaterials.put("skeleton head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 0));
		specialMaterials.put("skeleton skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 0));
		specialMaterials.put("skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 0));
		specialMaterials.put("wither head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 1));
		specialMaterials.put("wither skeleton head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 1));
		specialMaterials.put("wither skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 1));
		specialMaterials.put("wither skeleton skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 1));
		specialMaterials.put("zombie head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 2));
		specialMaterials.put("zombie skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 2));
		specialMaterials.put("steve head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 3));
		specialMaterials.put("steve skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 3));
		specialMaterials.put("player head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 3));
		specialMaterials.put("player skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 3));
		specialMaterials.put("head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 3));
		specialMaterials.put("creeper head", new ItemStack(Material.SKULL_ITEM, 0, (byte) 4));
		specialMaterials.put("creeper skull", new ItemStack(Material.SKULL_ITEM, 0, (byte) 4));

		names = new String[] { "white", "orange", "magenta", "light blue", "yellow", "lime", "pink", "gray", "gray", "light gray", "light grey", "cyan", "purple", "blue", "brown", "green", "red", "black" };
		data = new Byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 7, 8, 8, 9, 10, 11, 12, 13, 14, 15 };
		putSpecialTypes(Material.WOOL, names, data, new String[] { "wool" });

		// NOT RELEASED
		//		putSpecialTypes(Material.STAINED_CLAY, names, data, new String[] {
		//				"clay",
		//				"stained clay",
		//		});
		//		putSpecialTypes(Material.CARPET, names, data, new String[] {
		//				"carpet",
		//		});

		specialMaterials.put("rose red dye", new ItemStack(Material.INK_SACK, 0, (byte) 1));
		specialMaterials.put("red dye", new ItemStack(Material.INK_SACK, 0, (byte) 1));
		specialMaterials.put("cactus green dye", new ItemStack(Material.INK_SACK, 0, (byte) 2));
		specialMaterials.put("green dye", new ItemStack(Material.INK_SACK, 0, (byte) 2));
		specialMaterials.put("coca bean", new ItemStack(Material.INK_SACK, 0, (byte) 3));
		specialMaterials.put("cocabean", new ItemStack(Material.INK_SACK, 0, (byte) 3));
		specialMaterials.put("brown dye", new ItemStack(Material.INK_SACK, 0, (byte) 3));
		specialMaterials.put("lapis", new ItemStack(Material.INK_SACK, 0, (byte) 4));
		specialMaterials.put("lazuli", new ItemStack(Material.INK_SACK, 0, (byte) 4));
		specialMaterials.put("lapis lazuli", new ItemStack(Material.INK_SACK, 0, (byte) 4));
		specialMaterials.put("blue dye", new ItemStack(Material.INK_SACK, 0, (byte) 4));
		specialMaterials.put("purple dye", new ItemStack(Material.INK_SACK, 0, (byte) 5));
		specialMaterials.put("cyan dye", new ItemStack(Material.INK_SACK, 0, (byte) 6));
		specialMaterials.put("light gray dye", new ItemStack(Material.INK_SACK, 0, (byte) 7));
		specialMaterials.put("light grey dye", new ItemStack(Material.INK_SACK, 0, (byte) 7));
		specialMaterials.put("gray dye", new ItemStack(Material.INK_SACK, 0, (byte) 8));
		specialMaterials.put("grey dye", new ItemStack(Material.INK_SACK, 0, (byte) 8));
		specialMaterials.put("pink dye", new ItemStack(Material.INK_SACK, 0, (byte) 9));
		specialMaterials.put("lime dye", new ItemStack(Material.INK_SACK, 0, (byte) 10));
		specialMaterials.put("dandelion yellow dye", new ItemStack(Material.INK_SACK, 0, (byte) 11));
		specialMaterials.put("yellow dye", new ItemStack(Material.INK_SACK, 0, (byte) 11));
		specialMaterials.put("light blue dye", new ItemStack(Material.INK_SACK, 0, (byte) 12));
		specialMaterials.put("magenta dye", new ItemStack(Material.INK_SACK, 0, (byte) 13));
		specialMaterials.put("orange dye", new ItemStack(Material.INK_SACK, 0, (byte) 14));
		specialMaterials.put("bone meal", new ItemStack(Material.INK_SACK, 0, (byte) 15));
		specialMaterials.put("bonemeal", new ItemStack(Material.INK_SACK, 0, (byte) 15));

		names = new String[] {
				"creeper",
				"skeleton",
				"spider",
				"zombie",
				"slime",
				"ghast",
				"zombie pigman", "zombie pigmen", "pigman", "pigmen",
				"enderman", "endermen",
				"cave spider",
				"silverfish",
				"blaze",
				"magma cube", "lava slime",
				"bat",
				"witch",
				"pig",
				"sheep",
				"cow",
				"chicken",
				"squid",
				"wolf",
				"mooshroom", "mushroom cow",
				"ocelot",
				"horse",
				"villager"
		};
		data = new Byte[] { 50, 51, 52, 54, 55, 56, 57, 57, 57, 57, 58, 58, 59, 60, 61, 62, 62, 65, 66, 90, 91, 92, 93, 94, 95, 96, 96, 98, 100, 120 };
		putSpecialTypes(Material.MONSTER_EGG, names, data, new String[] {
				"spawn egg",
				"mob egg",
				"monster egg",
				"egg"
		});
	}

	private static void putSpecialTypes(Material material, String[] names, Byte[] data, String[] synonyms) {
		ItemStack[] items = new ItemStack[data.length];
		for (int i = 0; i < data.length; i++) {
			items[i] = new ItemStack(material, 1, data[i]);
		}
		for (String synonym : synonyms) {
			for (int i = 0; i < data.length; i++) {
				specialMaterials.put(names[i] + ' ' + synonym, items[i]);
			}
		}
	}

	private static void putSpecialTypes(Material material, String name, String[] synonyms) {
		ItemStack item = new ItemStack(material);
		for (String synonym : synonyms) {
			specialMaterials.put(name + ' ' + synonym, item);
		}
	}

	public static TokenReplacer getTokenReplacer() {
		return tokenReplacer;
	}

	static void reindexCustomItems() {
		customItems.clear();
		CustomItem[] items = MaterialData.getCustomItems();
		for (CustomItem item : items) {
			String name = item.getName().toLowerCase();
			customItems.put(name, item);
			customItems.put(name + "s", item);
			customItems.put(name + "es", item);
			customItems.put(name.substring(0, name.length() - 1) + "ies", item);
			if (name.indexOf('_') > -1) {
				String alteredName = name.replace('_', ' ');
				customItems.put(alteredName, item);
				customItems.put(alteredName + "s", item);
				customItems.put(alteredName + "es", item);
				customItems.put(alteredName.substring(0, alteredName.length() - 1) + "ies", item);
			}
		}
	}

	public static SpoutItemStack getItems(String input) {
		input = input.trim().toLowerCase();
		String[] parts = input.split("\\s+");
		int amount = 1;
		if (parts.length > 1 && isInteger(parts[0])) {
			amount = Integer.parseInt(parts[0]);
			input = input.substring(parts[0].length() + 1);
		}
		if (materials.containsKey(input)) {
			Material material = materials.get(input);
			ItemStack bukkitStack = new ItemStack(material, amount);
			return new SpoutItemStack(bukkitStack);
		} else if (specialMaterials.containsKey(input)) {
			ItemStack specialStack = specialMaterials.get(input).clone();
			specialStack.setAmount(amount);
			return new SpoutItemStack(specialStack);
		} else if (customItems.containsKey(input)) {
			CustomItem customItem = customItems.get(input);
			return new SpoutItemStack(customItem, amount);
		} else {
			return null;
		}
	}

	public static EntityType getEntityType(String input) {
		return entityTypes.get(input.trim().toLowerCase());
	}

	public static String getEntityName(EntityType type, boolean plural) {
		if (plural && entityNamePlural.containsKey(type)) {
			return entityNamePlural.get(type);
		} else if (!plural && entityName.containsKey(type)) {
			return entityName.get(type);
		} else if (type.getName() != null) {
			return type.getName().toLowerCase();
		} else {
			return type.name().toLowerCase().replace('_', ' ');
		}
	}

	public static boolean isInteger(String string) {
		if (string == null || string.isEmpty()) return false;
		boolean first = true;
		for (char character : string.toCharArray()) {
			if (first) {
				first = false;
				if (character == '-') continue;
			}
			if (!Character.isDigit(character)) return false;
		}
		return true;
	}

	public static boolean isDecimal(String string) {
		return (Pattern.matches(decimalRegex, string));
	}

}
