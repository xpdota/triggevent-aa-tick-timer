package gg.xp.ticktimer;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.SystemEvent;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.actlines.events.HasDuration;

import java.io.Serial;
import java.time.Duration;

@SystemEvent
public class TimedAutoAttackEvent extends BaseEvent implements HasDuration {
	@Serial
	private static final long serialVersionUID = -8427829789753169207L;
	private final AbilityUsedEvent autoattack;
	private final Duration interval;

	public TimedAutoAttackEvent(AbilityUsedEvent autoattack, Duration interval) {
		this.autoattack = autoattack;
		this.interval = interval;
	}

	public AbilityUsedEvent getAutoattack() {
		return autoattack;
	}

	public Duration getInterval() {
		return interval;
	}

	@Override
	public Duration getInitialDuration() {
		return interval;
	}
}
