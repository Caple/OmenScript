package pw.caple.mc.omenscript;

import org.getspout.spoutapi.gui.GenericLabel;

/**
 * Graphical user interface static utility class
 */
public final class GUIUtil {

	private GUIUtil() {}

	private static GUINotifications notifications = new GUINotifications();

	public static GUINotifications getNotifier() {
		return notifications;
	}

	public static void makeLabelMultiline(GenericLabel label) {
		String labelText = label.getText();
		int labelWidth = label.getWidth();
		float labelScale = label.getScale();
		if (GenericLabel.getStringWidth(labelText, labelScale) >= labelWidth) {
			String[] words = labelText.split(" ");
			StringBuilder builder = new StringBuilder(labelText.length() + 10);
			int blankSize = GenericLabel.getStringWidth(" ", labelScale);
			int spaceRemaining = labelWidth;
			int lines = 1;
			for (int i = 0; i < words.length; i++) {
				int size = GenericLabel.getStringWidth(words[i], labelScale) + blankSize;
				if (size > spaceRemaining) {
					builder.append("\n");
					spaceRemaining = labelWidth;
					lines++;
				}
				builder.append(words[i]);
				builder.append(" ");
				spaceRemaining -= size;
			}
			label.setText(builder.toString());
			label.setHeight(GenericLabel.getStringHeight(labelText, labelScale) * lines);
		} else {
			label.setHeight(GenericLabel.getStringHeight(labelText, labelScale));
		}
	}

}
