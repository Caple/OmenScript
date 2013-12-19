package pw.caple.mc.omenscript;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * All bukkit commands in the plugin are handled here.
 */
public final class Commands implements CommandExecutor {

	private final ScriptManager manager;

	public Commands(ScriptManager manager) {
		this.manager = manager;
	}

	private final String usage = ChatColor.YELLOW + "Possible sub-commands: ver, reload";

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (args.length < 1) {
			sender.sendMessage(usage);
		} else if (args[0].equalsIgnoreCase("ver")) {
			sender.sendMessage(ChatColor.GOLD + "- Running " + OmenScript.getInstance().getDescription().getFullName() + " -");
		} else if (args[0].equalsIgnoreCase("reload")) {
			if (sender.hasPermission("omenscript.reload")) {
				sender.sendMessage(manager.reload());
			} else {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
			}
		} else {
			sender.sendMessage(usage);
		}
		return true;
	}

}
