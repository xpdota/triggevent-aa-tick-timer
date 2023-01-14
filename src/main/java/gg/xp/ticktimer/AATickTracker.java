package gg.xp.ticktimer;

import gg.xp.reevent.events.BaseEvent;
import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivdata.data.Job;
import gg.xp.xivsupport.events.actlines.events.AbilityCastStart;
import gg.xp.xivsupport.events.actlines.events.AbilityUsedEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.ActiveCastRepository;
import gg.xp.xivsupport.events.state.combatstate.CastResult;
import gg.xp.xivsupport.events.state.combatstate.CastTracker;
import gg.xp.xivsupport.events.state.combatstate.StatusEffectRepository;
import gg.xp.xivsupport.gui.overlay.RefreshLoop;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;

@ScanMe
public class AATickTracker {

	private final XivState state;
	private final StatusEffectRepository buffs;
	private final ActiveCastRepository acr;
	private final RefreshLoop<AATickTracker> refresher;
	private volatile BaseEvent lastAuto;
	private BaseEvent basis;
	private Instant lastComputationTime;
	private double progress;
	private volatile AAInfo info;

	// TODO: rewrite basically this whole thing
	/*
		There are several issues:
		1. We need to account for casts, including canceled casts
		2. Haste buffs DO apply immediately, they do not wait for the next auto
		Thus, it should be rewritten to simply use a refresh loop, which advances
		a counter from 0.0 to 1.0 by (time delta / auto time), or zero if a cast
		is in progress.

		Also, use relevant events as a 'refresh now' trigger.
	 */

	public AATickTracker(XivState state, StatusEffectRepository buffs, ActiveCastRepository acr) {
		this.state = state;
		this.buffs = buffs;
		this.acr = acr;
		refresher = new RefreshLoop<>("AATickTracker", this, AATickTracker::refresh, unused -> 100L);
		refresher.start();
	}

	@HandleEvents
	public void handleAA(EventContext context, AbilityUsedEvent event) {
		if (event.getSource().isThePlayer() && event.abilityIdMatches(0x7, 0x8)) {
			lastAuto = event;
			refresher.refreshNow();
		}
	}

	// TODO: are these really worth it? There's also concurrency issues (potentially)
//	// Should come before the ACR
//	@HandleEvents(order = -100_000)
//	public void handleCastStartPre(EventContext context, AbilityCastStart event) {
//		if (event.getSource().isThePlayer()) {
//			refresher.refreshNow();
//		}
//	}
//
//	// Should come after the ACR
//	@HandleEvents(order = 100_000)
//	public void handleCastStartPost(EventContext context, AbilityCastStart event) {
//		if (event.getSource().isThePlayer()) {
//			refresher.refreshNow();
//		}
//	}

	public @Nullable AAInfo getInfo() {
		refresher.refreshNow();
		return info;
	}

	private void refresh() {
		BaseEvent lastAuto = this.lastAuto;
		if (lastAuto == null) {
			return;
		}
		if (basis != lastAuto) {
			resetTo(lastAuto);
		}
		Instant now = lastAuto.effectiveTimeNow();
		Instant then = lastComputationTime;
		Duration delta = Duration.between(then, now);

		double raw = calcEffectiveAaRate();
		double adjustment = raw * delta.toMillis() / 1000.0;
		progress = Math.min(progress + adjustment, 1.0);
		lastComputationTime = now;
		info = new AAInfo(progress, calcDuration(), raw == 0);
	}

	private void resetTo(BaseEvent lastAuto) {
		progress = 0;
		basis = lastAuto;
		lastComputationTime = basis.effectiveTimeNow();
	}

	private boolean playerHasBuff(long id) {
		return buffs.findStatusOnTarget(state.getPlayer(), id) != null;
	}

	private Duration calcDuration() {
		double raw = calcRawAaRate();
		return Duration.ofMillis((long) (1_000 / raw));
	}

	private double calcEffectiveAaRate() {
		double raw = calcRawAaRate();
		if (raw == 0) {
			return 0;
		}
		CastTracker ct = acr.getCastFor(state.getPlayer());
		if ( ct != null && ct.getResult() == CastResult.IN_PROGRESS){
			return 0;
		}
		return raw;

	}

	@SuppressWarnings("MagicNumber")
	private double calcRawAaRate() {
		double aaBase;
		double hasteModifier;
		Job playerJob = state.getPlayerJob();
		if (playerJob == null) {
			playerJob = Job.ADV;
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
		return 1 / aaTime;
	}
}
