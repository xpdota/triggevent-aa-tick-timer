package gg.xp.ticktimer;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.events.actlines.events.TickEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.TickInfo;
import gg.xp.xivsupport.events.state.combatstate.TickTracker;
import gg.xp.xivsupport.gui.overlay.OverlayConfig;
import gg.xp.xivsupport.gui.overlay.RefreshLoop;
import gg.xp.xivsupport.gui.overlay.XivOverlay;
import gg.xp.xivsupport.gui.tables.renderers.ResourceBar;
import gg.xp.xivsupport.models.XivPlayerCharacter;
import gg.xp.xivsupport.persistence.PersistenceProvider;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

@ScanMe
public class AATickTrackerOverlay extends XivOverlay {
	private final AATickTracker aaTracker;
	private final TickTracker mpTracker;
	private final XivState state;
	private final AATickSettings settings;
	private final ResourceBar aaBar;
	private final ResourceBar mpBar;
	private TickEvent lastTickEvent;
	private Color aaFill;
	private Color aaReady;

	public AATickTrackerOverlay(AATickTracker tracker, OverlayConfig oc, PersistenceProvider pers, TickTracker mpTracker, XivState state, AATickSettings settings) {
		super("AA/Tick Tracker", "aa-tick-tracker.overlay", oc, pers);
		this.aaTracker = tracker;
		this.mpTracker = mpTracker;
		this.state = state;
		this.settings = settings;

		JPanel panel = getPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setOpaque(false);

		aaBar = new ResourceBar();
		setAAbarText(null);
		aaBar.setPreferredSize(new Dimension(120, 20));
		aaBar.setOpaque(false);
		aaBar.setVisible(false);
		panel.add(aaBar);

		mpBar = new ResourceBar();
		mpBar.setPreferredSize(new Dimension(120, 20));
		mpBar.setOpaque(false);
		mpBar.setVisible(false);
		panel.add(mpBar);

//		panel.setPreferredSize(new Dimension(, 40));

		new RefreshLoop<>("aa-tick-tracker-refresh", this, AATickTrackerOverlay::refresh, unused -> calculateScaledFrameTime(30)).start();
		settings.addAndRunListener(this::applySettings);
	}

	private void applySettings() {
		aaBar.setColor3(settings.getAaBgColor().get());
		this.aaFill = settings.getAaFillColor().get();
		this.aaReady = settings.getAaReadyColor().get();
		aaBar.setTextColor(settings.getAaTextColor().get());

		mpBar.setColor3(settings.getMpBgColor().get());
		mpBar.setColor1(settings.getMpFillColor().get());
		mpBar.setTextColor(settings.getMpTextColor().get());

		switch (settings.getMpShowText().get()) {
			case NONE -> mpBar.setTextOptions("");
			case PLAIN_TEXT -> mpBar.setTextOptions("Mana Ticks", "MP Tick", "MP");
			case CUR_MP, CUR_MAX_MP -> mpBar.setTextOptions("Not Yet Implemented", "NI");
		}
	}

	@SuppressWarnings("MagicNumber")
	private void setAAbarText(@Nullable TimedAutoAttackEvent auto) {
		AATextOptions textOption = settings.getAaShowText().get();
		if (auto == null) {
			switch (textOption) {

				case NOTHING -> aaBar.setTextOptions("");
				case PLAIN_TEXT, AA_INTERVAL -> aaBar.setTextOptions("Auto-Attack", "AA");
			}
		}
		else {
			switch (textOption) {
				case NOTHING -> aaBar.setTextOptions("");
				case PLAIN_TEXT -> aaBar.setTextOptions("Auto-Attack", "AA");
				case AA_INTERVAL -> {
					String aaTime = String.format("%.03f", auto.getInitialDuration().toMillis() / 1000.0);
					aaBar.setTextOptions("Auto-Attack: " + aaTime, "AA: " + aaTime);
					aaBar.revalidate();
				}
			}

		}
	}

	private void refresh() {
		XivPlayerCharacter player = state.getPlayer();
		if (player == null) {
			return;
		}
		TimedAutoAttackEvent last = aaTracker.getLast();
		if (last == null) {
			aaBar.setVisible(false);
		}
		else {
			double pct = last.getEstimatedElapsedDuration().toMillis() / (double) last.getInitialDuration().toMillis();
			aaBar.setPercent1(pct);
			aaBar.setColor1(pct > 0.999 ? aaReady : aaFill);
			setAAbarText(last);
			aaBar.setVisible(true);
		}
		TickInfo tick = mpTracker.getTick(player);
		TickEvent lastTickEvent = this.lastTickEvent;
		if (tick == null || lastTickEvent == null) {
			mpBar.setVisible(false);
		}
		else {
			long fromLast = tick.getMsFromLastTick(lastTickEvent.effectiveTimeNow());
			mpBar.setPercent1(fromLast / (double) tick.getIntervalMs());
			mpBar.setVisible(true);
		}
		aaBar.repaint();
		mpBar.repaint();
	}


	@HandleEvents
	public void ticker(EventContext context, TickEvent event) {
		if (event.getCombatant().isThePlayer()) {
			lastTickEvent = event;
		}
	}
}
