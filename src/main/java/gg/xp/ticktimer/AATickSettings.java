package gg.xp.ticktimer;

import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import gg.xp.xivsupport.persistence.settings.ColorSetting;
import gg.xp.xivsupport.persistence.settings.EnumSetting;
import gg.xp.xivsupport.persistence.settings.ObservableSetting;

import java.awt.*;
import java.util.List;

@ScanMe
public class AATickSettings extends ObservableSetting {
	private final ColorSetting aaTextColor;
	private final ColorSetting aaFillColor;
	private final ColorSetting aaBgColor;
	private final ColorSetting aaReadyColor;
	private final EnumSetting<AATextOptions> aaShowText;

	private final ColorSetting mpTextColor;
	private final ColorSetting mpFillColor;
	private final ColorSetting mpBgColor;
	private final EnumSetting<MPTextOptions> mpShowText;
	private final ColorSetting aaPausedColor;

	public AATickSettings(PersistenceProvider pers) {
		String settingKeyBase = "aa-tick-tracker.settings.";

		aaTextColor = new ColorSetting(pers, settingKeyBase + "aaTextColor", new Color(255, 255, 255));
		aaFillColor = new ColorSetting(pers, settingKeyBase + "aaFillColor", new Color(255, 0, 0, 128));
		aaBgColor = new ColorSetting(pers, settingKeyBase + "aaBgColor", new Color(128, 128, 128, 30));
		aaReadyColor = new ColorSetting(pers, settingKeyBase + "aaReadyColor", new Color(0, 255, 0, 128));
		aaPausedColor = new ColorSetting(pers, settingKeyBase + "aaPausedColor", new Color(255, 128, 0, 128));
		aaShowText = new EnumSetting<>(pers, settingKeyBase + "aaShowText", AATextOptions.class, AATextOptions.AA_INTERVAL);

		mpTextColor = new ColorSetting(pers, settingKeyBase + "mpTextColor", new Color(255, 255, 255));
		mpFillColor = new ColorSetting(pers, settingKeyBase + "mpFillColor", new Color(128, 0, 128, 192));
		mpBgColor = new ColorSetting(pers, settingKeyBase + "mpBgColor", new Color(128, 128, 128, 30));
		mpShowText = new EnumSetting<>(pers, settingKeyBase + "mpShowText", MPTextOptions.class, MPTextOptions.PLAIN_TEXT);
		List.of(aaTextColor, aaFillColor, aaBgColor, aaReadyColor, aaShowText, mpTextColor, mpFillColor, mpBgColor, mpShowText)
				.forEach(setting -> setting.addListener(this::notifyListeners));
	}

	public ColorSetting getAaTextColor() {
		return aaTextColor;
	}

	public ColorSetting getAaFillColor() {
		return aaFillColor;
	}

	public ColorSetting getAaBgColor() {
		return aaBgColor;
	}

	public ColorSetting getAaReadyColor() {
		return aaReadyColor;
	}

	public ColorSetting getAaPausedColor() {
		return aaPausedColor;
	}

	public EnumSetting<AATextOptions> getAaShowText() {
		return aaShowText;
	}

	public ColorSetting getMpTextColor() {
		return mpTextColor;
	}

	public ColorSetting getMpFillColor() {
		return mpFillColor;
	}

	public ColorSetting getMpBgColor() {
		return mpBgColor;
	}

	public EnumSetting<MPTextOptions> getMpShowText() {
		return mpShowText;
	}
}
