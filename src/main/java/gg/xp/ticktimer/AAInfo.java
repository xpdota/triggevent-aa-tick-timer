package gg.xp.ticktimer;

import java.time.Duration;

public record AAInfo(
		double aaProgress,
		Duration aaTime,
		boolean isPaused
) {
}
