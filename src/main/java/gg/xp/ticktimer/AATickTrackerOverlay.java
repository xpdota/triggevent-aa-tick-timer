package gg.xp.ticktimer;

import gg.xp.reevent.events.EventContext;
import gg.xp.reevent.scan.HandleEvents;
import gg.xp.reevent.scan.ScanMe;
import gg.xp.xivsupport.events.actlines.events.HpMpTickEvent;
import gg.xp.xivsupport.events.state.XivState;
import gg.xp.xivsupport.events.state.combatstate.HpMpTickTracker;
import gg.xp.xivsupport.events.state.combatstate.TickInfo;
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
	private final HpMpTickTracker mpTracker;
	private final XivState state;
	private final AATickSettings settings;
	private final ResourceBar aaBar;
	private final ResourceBar mpBar;
	private HpMpTickEvent lastTickEvent;
	private Color aaFill;
	private Color aaReady;
	private Color aaPaused;

	public AATickTrackerOverlay(AATickTracker tracker, OverlayConfig oc, PersistenceProvider pers, HpMpTickTracker mpTracker, XivState state, AATickSettings settings) {
		super("AA/Tick Tracker", "aa-tick-tracker.overlay", oc, pers);
		this.aaTracker = tracker;
		this.mpTracker = mpTracker;
		this.state = state;
		this.settings = settings;

		JPanel panel = new JPanel();
		panel.setLayout(null);
		panel.setOpaque(false);

		Dimension barSize = new Dimension(120, 20);

		aaBar = new ResourceBar();
		setAAbarText(null);
		aaBar.setBounds(0, 0, 120, 20);
		aaBar.setMaximumSize(barSize);
		aaBar.setOpaque(false);
//		aaBar.setVisible(false);
		panel.add(aaBar);

		mpBar = new ResourceBar();
		mpBar.setBounds(0, 20, 120, 20);
		mpBar.setSize(barSize);
		mpBar.setOpaque(false);
//		mpBar.setVisible(false);
		panel.add(mpBar);
		panel.setPreferredSize(new Dimension(120, 40));
		panel.revalidate();

		getPanel().add(panel);

//		panel.setPreferredSize(new Dimension(, 40));

		new RefreshLoop<>("aa-tick-tracker-refresh", this, AATickTrackerOverlay::refresh, unused -> calculateScaledFrameTime(30)).start();
		settings.addAndRunListener(this::applySettings);
		mpBar.invalidate();
		// TODO: is this still needed?
		SwingUtilities.invokeLater(() -> mpBar.revalidate());
	}

	@Override
	protected void onBecomeVisible() {
		super.onBecomeVisible();
		// TODO: is this still needed?
		mpBar.revalidate();
	}

	@Override
	public void finishInit() {
		super.finishInit();
		// TODO: is this still needed?
		mpBar.revalidate();
	}

	private void applySettings() {
		aaBar.setColor3(settings.getAaBgColor().get());
		this.aaFill = settings.getAaFillColor().get();
		this.aaReady = settings.getAaReadyColor().get();
		this.aaPaused = settings.getAaPausedColor().get();
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
	private void setAAbarText(@Nullable AAInfo auto) {
		AATextOptions textOption = settings.getAaShowText().get();
		if (auto == null) {
			switch (textOption) {
				case NOTHING -> aaBar.setTextOptions("");
				case PLAIN_TEXT, AA_INTERVAL, AA_INTERVAL_ONLY -> aaBar.setTextOptions("Auto-Attack", "AA");
			}
		}
		else {
			switch (textOption) {
				case NOTHING -> aaBar.setTextOptions("");
				case PLAIN_TEXT -> aaBar.setTextOptions("Auto-Attack", "AA");
				case AA_INTERVAL -> {
					String aaTime = String.format("%.03f", auto.aaTime().toMillis() / 1000.0);
					aaBar.setTextOptions("Auto-Attack: " + aaTime, "AA: " + aaTime);
					aaBar.revalidate();
				}
				case AA_INTERVAL_ONLY -> {
					String aaTime = String.format("%.03f", auto.aaTime().toMillis() / 1000.0);
					aaBar.setTextOptions(aaTime);
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
		AAInfo last = aaTracker.getInfo();
		if (last == null) {
			aaBar.setVisible(false);
		}
		else {
			double pct = last.aaProgress();
			aaBar.setPercent1(pct);
			Color barColor;
			if (last.isPaused()) {
				barColor = aaPaused;
			}
			else {
				barColor = pct > 0.999 ? aaReady : aaFill;
			}
			aaBar.setColor1(barColor);
			setAAbarText(last);
			aaBar.setVisible(true);
		}
		TickInfo tick = mpTracker.getTick(player);
		HpMpTickEvent lastTickEvent = this.lastTickEvent;
		if (tick == null || lastTickEvent == null) {
			mpBar.setVisible(false);
		}
		else {
			long fromLast = tick.getMsFromLastTick(lastTickEvent.effectiveTimeNow());
			mpBar.setPercent1(fromLast / (double) tick.getIntervalMs());
			if (!mpBar.isVisible()) {
				mpBar.setVisible(true);
				SwingUtilities.invokeLater(mpBar::revalidate);
			}
		}
		aaBar.repaint();
		mpBar.repaint();
	}


	@HandleEvents
	public void ticker(EventContext context, HpMpTickEvent event) {
		if (event.getTarget().isThePlayer()) {
			lastTickEvent = event;
		}
	}
}
