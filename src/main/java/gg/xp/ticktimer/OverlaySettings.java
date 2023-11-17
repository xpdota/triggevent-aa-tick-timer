package gg.xp.ticktimer;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.events.triggers.duties.ewult.omega.BooleanSettingHidingPanel;
import gg.xp.xivsupport.gui.TitleBorderPanel;
import gg.xp.xivsupport.gui.extra.TopDownSimplePluginTab;
import gg.xp.xivsupport.gui.util.GuiUtil;
import gg.xp.xivsupport.persistence.gui.ColorSettingGui;
import gg.xp.xivsupport.persistence.gui.EnumSettingGui;
import org.swingexplorer.internal.GuiUtils;

import javax.swing.*;
import java.awt.*;

@ScanMe
public class OverlaySettings extends TopDownSimplePluginTab {

	private final AATickSettings settings;

	public OverlaySettings(AATickSettings settings) {
		super("AutoAttack/MP Tick Timer", 500);
		this.settings = settings;
	}

	@Override
	protected Component[] provideChildren(JPanel jPanel) {
		JPanel aaSettings = new TitleBorderPanel("AA Settings");
		GuiUtil.simpleTopDownLayout(aaSettings,
				new ColorSettingGui(settings.getAaFillColor(), "AA Fill", () -> true).getComponentReversed(),
				new ColorSettingGui(settings.getAaPausedColor(), "AA Paused", () -> true).getComponentReversed(),
				new ColorSettingGui(settings.getAaReadyColor(), "AA Ready", () -> true).getComponentReversed(),
				new ColorSettingGui(settings.getAaBgColor(), "AA Background", () -> true).getComponentReversed(),
				new ColorSettingGui(settings.getAaTextColor(), "AA Text", () -> true).getComponentReversed(),
				new EnumSettingGui<>(settings.getAaShowText(), "AA Text Display", () -> true).getComponent()
//				Box.createHorizontalStrut(300),
				);
		aaSettings.revalidate();
//		aaSettings.setPreferredSize(new Dimension(350, 350));
		JPanel mpSettings = new TitleBorderPanel("MP Settings");
		GuiUtil.simpleTopDownLayout(mpSettings,
				new ColorSettingGui(settings.getMpFillColor(), "MP Fill", () -> true).getComponentReversed(),
				new ColorSettingGui(settings.getMpBgColor(), "MP Background", () -> true).getComponentReversed(),
				new ColorSettingGui(settings.getMpTextColor(), "MP Text", () -> true).getComponentReversed(),
				new EnumSettingGui<>(settings.getMpShowText(), "MP Text Display", () -> true).getComponent()
//				Box.createHorizontalStrut(300),
				);
		aaSettings.revalidate();
//		mpSettings.setPreferredSize(new Dimension(350, 350));
		return new Component[]{
				new BooleanSettingHidingPanel(settings.getAaEnabled(), "AA Enabled", aaSettings, false),
				new BooleanSettingHidingPanel(settings.getMpEnabled(), "MP Enabled", mpSettings, false),
		};
	}

}
