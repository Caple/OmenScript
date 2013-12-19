package pw.caple.mc.omenscript;

import java.util.List;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.getspout.spoutapi.player.SpoutPlayer;

/**
 * All bukkit events in the plugin are handled here.
 */
public final class GeneralEvents implements Listener {

	private final ScriptManager manager;

	public GeneralEvents(ScriptManager manager) {
		this.manager = manager;
	}

	@EventHandler
	public void onInteract(NPCRightClickEvent event) {
		List<DialogNode> dialog = manager.getDialog(event.getNPC().getName());
		if (dialog != null) {
			SpoutPlayer clicker = (SpoutPlayer) event.getClicker();
			DialogScreen screen = new DialogScreen(clicker, dialog, event.getNPC());
			clicker.getMainScreen().attachPopupScreen(screen);
		}
	}
}
