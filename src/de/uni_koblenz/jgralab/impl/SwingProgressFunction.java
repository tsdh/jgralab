package de.uni_koblenz.jgralab.impl;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import de.uni_koblenz.jgralab.ProgressFunction;

/**
 * A ProgressFunction to display a graphical progress bar in a Swing GUI. A
 * JFrame is automatically created on initialisation, and also automatically
 * disposed after finishing.
 * 
 * @author riediger
 * 
 */
public class SwingProgressFunction implements ProgressFunction, ActionListener {
	private JFrame wnd;
	private JProgressBar pb;
	private String title;
	private String label;
	private long totalElements;
	private long updateInterval;
	private BoundedRangeModel brm;
	private JLabel lbl;
	private long startTime;
	private Timer timer;

	/**
	 * Creates a ProgressFunction to display progress in a Swing GUI.
	 * 
	 * @param title
	 *            title for the JFrame of the progress display
	 * @param label
	 *            text to be displayed above the progress bar
	 */
	public SwingProgressFunction(String title, String label) {
		this.title = title;
		this.label = label;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#finished()
	 */
	@Override
	public void finished() {
		invoke(new Runnable() {
			public void run() {
				brm.setValue(brm.getMaximum());
				setTimeText();
				timer = new Timer(1000, SwingProgressFunction.this);
				timer.start();
			};
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#getUpdateInterval()
	 */
	@Override
	public long getUpdateInterval() {
		return updateInterval;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#init(long)
	 */
	@Override
	public void init(final long totalElements) {
		invoke(new Runnable() {

			@Override
			public void run() {
				SwingProgressFunction.this.totalElements = totalElements;

				wnd = new JFrame(title);
				wnd.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
				wnd.setResizable(false);
				wnd.setLayout(new BorderLayout());
				wnd.setMinimumSize(new Dimension(200, 100));

				pb = new JProgressBar();
				brm = new DefaultBoundedRangeModel();
				pb.setModel(brm);
				updateInterval = brm.getMaximum() > totalElements ? 1
						: totalElements / brm.getMaximum();
				lbl = new JLabel("####### elements, ###.#s", JLabel.CENTER);

				wnd.getContentPane().add(new JLabel(label, JLabel.CENTER),
						BorderLayout.NORTH);
				wnd.getContentPane().add(pb, BorderLayout.CENTER);
				wnd.getContentPane().add(lbl, BorderLayout.SOUTH);
				wnd.getContentPane().add(new JPanel(), BorderLayout.WEST);
				wnd.getContentPane().add(new JPanel(), BorderLayout.EAST);
				startTime = System.currentTimeMillis();
				wnd.pack();
				setTimeText();
				wnd.setVisible(true);
			}
		});
	}

	class TimeTextUpdater implements Runnable {
		public void run() {
			lbl.setText(totalElements + " elements, "
					+ ((System.currentTimeMillis() - startTime) / 100) / 10.0
					+ "s");
		}
	};

	TimeTextUpdater tm = new TimeTextUpdater();

	private void setTimeText() {
		invoke(tm);
	}

	private void invoke(Runnable r) {
		if (SwingUtilities.isEventDispatchThread()) {
			r.run();
		} else {
			try {
				SwingUtilities.invokeAndWait(r);
			} catch (InterruptedException e) {
			} catch (InvocationTargetException e) {
			}
		}
	}

	class ProgressUpdater implements Runnable {
		@Override
		public void run() {
			if (brm.getValue() < brm.getMaximum()) {
				brm.setValue(brm.getValue() + 1);
				setTimeText();
			}
		}
	}

	ProgressUpdater pu = new ProgressUpdater();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.uni_koblenz.jgralab.ProgressFunction#progress(long)
	 */
	@Override
	public void progress(long processedElements) {
		invoke(pu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		timer.stop();
		wnd.dispose();
	}
}