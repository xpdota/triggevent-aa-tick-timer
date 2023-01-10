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
		aaBase = switch (playerJob) {
			case PLD -> 2.240;
			case MNK, NIN -> 2.560;
			case WAR -> 3.360;
			case DRG, GNB -> 2.800;
			case BRD -> 3.040;
			case RDM -> 3.340;
			case BLM -> 3.280;
			case SMN, SCH, DNC -> 3.120;
			case MCH, SAM -> 2.640;
			case DRK -> 2.960;
			case MRD, ARC, AST -> 2.960;
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
