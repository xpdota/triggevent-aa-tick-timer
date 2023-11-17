package gg.xp.ticktimer;

import gg.xp.xivsupport.gui.util.HasFriendlyName;

public enum MPTextOptions implements HasFriendlyName {
	NONE("Nothing"),
	PLAIN_TEXT("Fixed Text"),
	CUR_MP("Current MP (Not Implemented)"),
	CUR_MAX_MP("Cur/Max MP (Not Implemented)");

	private final String friendlyName;

	MPTextOptions(String friendlyName) {
		this.friendlyName = friendlyName;
	}

	@Override
	public String getFriendlyName() {
		return friendlyName;
	}
}
