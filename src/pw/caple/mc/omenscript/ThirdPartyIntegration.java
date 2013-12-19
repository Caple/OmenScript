package pw.caple.mc.omenscript;

import me.ThaH3lper.com.EpicBoss;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.herocraftonline.heroes.Heroes;
import com.herocraftonline.heroes.characters.CharacterManager;
import com.herocraftonline.heroes.characters.Hero;

/**
 * Integration with third party plugins.
 */
public class ThirdPartyIntegration {

	final private boolean usingVault;
	final private Economy economy;
	final private Heroes heroes;
	final private EpicBoss epicBoss;

	ThirdPartyIntegration() {
		Plugin plugin;

		plugin = Bukkit.getServer().getPluginManager().getPlugin("Vault");
		usingVault = (plugin != null && plugin.isEnabled());
		if (usingVault) {
			RegisteredServiceProvider<Economy> registration = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
			if (registration != null) {
				economy = registration.getProvider();
			} else {
				economy = null;
			}
		} else {
			economy = null;
		}

		plugin = Bukkit.getServer().getPluginManager().getPlugin("Heroes");
		if (plugin != null && plugin.isEnabled()) {
			heroes = (Heroes) plugin;
		} else {
			heroes = null;
		}

		plugin = Bukkit.getServer().getPluginManager().getPlugin("EpicBossRecoded");
		if (plugin != null && plugin.isEnabled()) {
			epicBoss = (EpicBoss) plugin;
		} else {
			epicBoss = null;
		}
	}

	public boolean isRunningVault() {
		return usingVault;
	}

	public boolean hasHookedEconomy() {
		return economy != null;
	}

	public boolean isRunningHeroes() {
		return heroes != null;
	}

	public boolean isRunningEpicBoss() {
		return epicBoss != null;
	}

	public void giveMoney(Player player, double amount) {
		economy.depositPlayer(player.getName(), amount);
	}

	public String getCurrency(boolean plural) {
		if (plural) {
			return economy.currencyNamePlural();
		} else {
			return economy.currencyNameSingular();
		}
	}

	public void giveHeroesXP(Player player, double xp) {
		CharacterManager manager = heroes.getCharacterManager();
		Hero hero = manager.getHero(player);
		hero.addExp(xp, hero.getHeroClass(), player.getLocation());
	}
}
