package pw.caple.mc.omenscript;

/**
 * Pattern specifying that this object or one of it's members needs to be
 * unregistered from a separate system before it can properly unload.
 */
public interface NeedsToBeUnregistered {
	abstract void unregister();
}
