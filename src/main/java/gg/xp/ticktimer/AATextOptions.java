package gg.xp.ticktimer;

import gg.xp.xivsupport.gui.util.HasFriendlyName;

public enum AATextOptions implements HasFriendlyName {
	NOTHING("Nothing"),
	PLAIN_TEXT("Fixed Text"),
	AA_INTERVAL("AA Duration"),
	AA_INTERVAL_ONLY("AA Duration, Shorter");

	private final String friendlyName;

	AATextOptions(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	@Override
	public String getFriendlyName() {
		return friendlyName;
	}
}
