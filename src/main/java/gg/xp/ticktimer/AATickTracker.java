package gg.xp.ticktimer;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivdata.data.Job;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.StatusEffectRepository;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

@ScanMe
public class AATickTracker {

	private final XivState state;
	private final StatusEffectRepository buffs;
	private volatile @Nullable TimedAutoAttackEvent last;

	// TODO: rewrite basically this whole thing
	/*
		There are several issues:
		1. We need to account for casts, including canceled casts
		2. Haste buffs DO apply immediately, they do not wait for the next auto
		Thus, it should be rewritten to simply use a refresh loop, which advances
		a counter from 0.0 to 1.0 by (time delta / auto time), or zero if a cast
		is in progress.
	 */

	public AATickTracker(XivState state, StatusEffectRepository buffs) {
		this.state = state;
		this.buffs = buffs;
	}

	public TimedAutoAttackEvent getLast() {
		return last;
	}

	@HandleEvents
	public void handleAA(EventContext context, AbilityUsedEvent event) {
		if (event.getSource().isThePlayer() && event.abilityIdMatches(0x7, 0x8)) {
			Duration duration = calcDuration();
			if (duration != null) {
				TimedAutoAttackEvent newEvent = new TimedAutoAttackEvent(event, duration);
				last = newEvent;
				context.accept(newEvent);
			}
		}
	}

	private boolean playerHasBuff(long id) {
		return buffs.findStatusOnTarget(state.getPlayer(), id) != null;
	}

	@SuppressWarnings("MagicNumber")
	private @Nullable Duration calcDuration() {
		double aaBase;
		double hasteModifier;
		Job playerJob = state.getPlayerJob();
		if (playerJob == null) {
			return null;
		}
		// You can find these by looking at the "delay" stat on a weapon
		aaBase = switch (playerJob) {
			case PLD -> 2.240;
			case MNK, NIN -> 2.560;
			case MCH, SAM -> 2.640;
			case DRG, GNB, SGE -> 2.800;
			case DRK, MRD, ARC -> 2.960;
			case BRD -> 3.040;
			case SMN, SCH, DNC -> 3.120;
			case RPR, AST -> 3.200;
			case BLM, BLU -> 3.280;
			case RDM -> 3.340;
			case WAR -> 3.360;
			case WHM -> 3.440;
			// I don't know, just make a guess
			default -> 3.000;
		};
		hasteModifier = switch (playerJob) {
			case MNK -> playerHasBuff(0xA7F) ? 0.6 : 0.2;
			case SAM -> {
				if (playerHasBuff(0x513)) {
					if (state.getPlayer().getLevel() >= 78) {
						yield 0.13;
					}
					else {
						yield 0.1;
					}
				}
				else {
					yield 0;
				}
			}
			case BRD -> {
				// TODO - need BRD gauge
				yield 0.0;
			}
			case WHM -> playerHasBuff(0x9D) ? 0.2 : 0.0;
			default -> 0.0;
		};
		double aaTime = aaBase * (1 - hasteModifier);
		return Duration.ofMillis((long) (aaTime * 1000.0));
	}
}
