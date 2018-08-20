/*
 * This software copyright by various authors including the RPTools.net
 * development team, and licensed under the LGPL Version 3 or, at your option,
 * any later version.
 *
 * Portions of this software were originally covered under the Apache Software
 * License, Version 1.1 or Version 2.0.
 *
 * See the file LICENSE elsewhere in this distribution for license details.
 */

package net.rptools.maptool.model;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.Area;
import java.io.IOException;
import java.util.List;

import net.rptools.maptool.client.MapTool;

public class GridlessGrid extends Grid {
	private static List<TokenFootprint> footprintList;

	// @formatter:off
	private static final GridCapabilities GRID_CAPABILITIES= new GridCapabilities() {
		public boolean isPathingSupported() {return false;}
		public boolean isSnapToGridSupported() {return false;}
		public boolean isPathLineSupported() {return false;}
		public boolean isSecondDimensionAdjustmentSupported() {return false;}
		public boolean isCoordinatesSupported() {return false;}
	};
	// @formatter:on

	private static final int[] FACING_ANGLES = new int[] { -135, -90, -45, 0, 45, 90, 135, 180 };

	@Override
	public List<TokenFootprint> getFootprints() {
		if (footprintList == null) {
			try {
				footprintList = loadFootprints("net/rptools/maptool/model/gridlessGridFootprints.xml");
			} catch (IOException ioe) {
				MapTool.showError("GridlessGrid.error.notLoaded", ioe);
			}
		}
		return footprintList;
	}

	@Override
	public int[] getFacingAngles() {
		return FACING_ANGLES;
	}

	@Override
	public Dimension getMovementVector(int keyEvent, boolean snapToGrid) {
		// same result regardless of snaptogrid value
		Rectangle r = getFootprint(null).getBounds(this);
		int sizeX = snapToGrid ? r.width : 1;
		int sizeY = snapToGrid ? r.height : 1;
		switch (keyEvent) {
		case KeyEvent.VK_NUMPAD1:
			return new Dimension(-sizeX, sizeY);
		case KeyEvent.VK_NUMPAD2:
			return new Dimension(0, sizeY);
		case KeyEvent.VK_NUMPAD3:
			return new Dimension(sizeX, sizeY);
		case KeyEvent.VK_NUMPAD4:
			return new Dimension(-sizeX, 0);
		case KeyEvent.VK_NUMPAD6:
			return new Dimension(sizeX, 0);
		case KeyEvent.VK_NUMPAD7:
			return new Dimension(-sizeX, -sizeY);
		case KeyEvent.VK_NUMPAD8:
			return new Dimension(0, -sizeY);
		case KeyEvent.VK_NUMPAD9:
			return new Dimension(sizeX, -sizeY);
		case KeyEvent.VK_LEFT:
			return new Dimension(-sizeX, 0);
		case KeyEvent.VK_RIGHT:
			return new Dimension(sizeX, 0);
		case KeyEvent.VK_UP:
			return new Dimension(0, -sizeY);
		case KeyEvent.VK_DOWN:
			return new Dimension(0, sizeY);
		}
		return new Dimension(0, 0);
	}

	@Override
	public Rectangle getBounds(CellPoint cp) {
		return new Rectangle(cp.x, cp.y, getSize(), getSize());
	}

	@Override
	public ZonePoint convert(CellPoint cp) {
		return new ZonePoint(cp.x, cp.y);
	}

	@Override
	public CellPoint convert(ZonePoint zp) {
		return new CellPoint(zp.x, zp.y);
	}

	@Override
	protected Area createCellShape(int size) {
		// Doesn't do this
		return null;
	}

	@Override
	public GridCapabilities getCapabilities() {
		return GRID_CAPABILITIES;
	}

	@Override
	public double getCellWidth() {
		return getSize();
	}

	@Override
	public double getCellHeight() {
		return getSize();
	}
}
