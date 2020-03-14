/**
 *
 */
package org.mmarini.routes.swing.v2;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.mmarini.routes.model.v2.MapModule;
import org.mmarini.routes.model.v2.Tuple;
import org.mmarini.routes.model.v2.Tuple2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import hu.akarnokd.rxjava3.swing.SwingObservable;
import io.reactivex.rxjava3.core.Observable;

/**
 * The module selector
 */
public class ModuleSelector {
	private static final int ICON_HEIGHT = 20;
	private static final int ICON_WIDTH = 20;
	public static final double EDGE_WIDTH = 5;
	private static final BasicStroke STROKE = new BasicStroke((float) EDGE_WIDTH, BasicStroke.CAP_ROUND,
			BasicStroke.JOIN_ROUND);

	private static final Logger logger = LoggerFactory.getLogger(ModuleSelector.class);

	/**
	 * Returns the icon for a given module
	 *
	 * @param module the module
	 */
	private static Icon createIcon(final MapModule module) {
		final BufferedImage image = new BufferedImage(ICON_WIDTH, ICON_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		final Graphics2D gr = image.createGraphics();
		Rectangle2D bound = new Rectangle2D.Double(0, 0, ICON_WIDTH, ICON_HEIGHT);
		bound = module.getBound();
		final double scale = Math.min(ICON_WIDTH / bound.getWidth(), ICON_HEIGHT / bound.getHeight());
		gr.translate(ICON_WIDTH * 0.5, ICON_HEIGHT * 0.5);
		gr.scale(scale, scale);
		gr.translate(-bound.getCenterX(), -bound.getCenterY());
		gr.setColor(Color.WHITE);
		gr.fill(bound);
		gr.setColor(Color.GRAY);
		module.getEdges().forEach(edge -> {
			gr.setStroke(STROKE);
			gr.draw(new Line2D.Double(edge.getBeginLocation(), edge.getEndLocation()));
		});
		final Icon icon = new ImageIcon(image);
		return icon;
	}

	private final JPopupMenu popupMenu;
	private final List<JMenuItem> items;
	private final JButton dropDownButton;
	private final Observable<Tuple2<ActionEvent, MapModule>> moduleObs;

	/**
	 * Creates the selector
	 *
	 * @param modules the list of modules
	 */
	public ModuleSelector(final List<MapModule> modules) {
		popupMenu = new JPopupMenu();
		this.dropDownButton = SwingUtils.createJButton("ModuleSelector.dropAction");
		final List<Tuple2<JMenuItem, MapModule>> list = modules.stream().map(m -> {
			final JMenuItem item = new JMenuItem();
			item.setIcon(createIcon(m));
			return Tuple.of(item, m);
		}).collect(Collectors.toList());
		items = list.stream().map(t -> t.get1()).collect(Collectors.toList());
		items.forEach(popupMenu::add);

		final List<Observable<Tuple2<ActionEvent, MapModule>>> obs = list.stream().map(t -> {
			final JMenuItem menu = t.get1();
			final MapModule module = t.get2();
			return SwingObservable.actions(menu).map(ev -> {
				return Tuple.of(ev, module);
			});
		}).collect(Collectors.toList());

		moduleObs = Observable.merge(Observable.fromIterable(obs));

		SwingObservable.actions(dropDownButton).subscribe(ev -> {
			final Dimension size = dropDownButton.getSize();
			popupMenu.show(dropDownButton, 0, size.height);
		}, e -> {
			logger.error(e.getMessage(), e);
		});
	}

	/** Returns the drop down button */
	public JButton getDropDownButton() {
		return dropDownButton;
	}

	/** Returns the items */
	public List<JMenuItem> getItems() {
		return items;
	}

	/** Returns the observable of selected module */
	public Observable<Tuple2<ActionEvent, MapModule>> getModuleObs() {
		return moduleObs;
	}
}
