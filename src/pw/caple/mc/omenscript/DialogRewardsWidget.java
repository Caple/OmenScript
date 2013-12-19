package pw.caple.mc.omenscript;

import org.getspout.spoutapi.gui.Color;
import org.getspout.spoutapi.gui.GenericContainer;
import org.getspout.spoutapi.gui.GenericLabel;
import org.getspout.spoutapi.gui.RenderPriority;
import org.getspout.spoutapi.gui.WidgetAnchor;

/**
 * A collection of labels describing a quests's rewards.
 */
public final class DialogRewardsWidget extends GenericContainer {

	private final int rowHeight = 11;
	private final float textScale = 0.85f;
	private int currentY = 2;

	public DialogRewardsWidget() {
		setAnchor(WidgetAnchor.CENTER_CENTER);
		setHeight(10);
	}

	public void addLabel(String text) {
		GenericLabel label = new GenericLabel(text);
		label.setWidth(150).setHeight(rowHeight);
		label.shiftYPos(currentY).shiftXPos(5);
		label.setScale(textScale);
		label.setPriority(RenderPriority.Lowest);
		label.setTextColor(new Color(0f, 0f, 0f, 1f));
		label.setShadow(false);
		addChild(label);
		currentY += rowHeight;
		setHeight(currentY + 10);
	}

	@Override
	public void onTick() {}

}
