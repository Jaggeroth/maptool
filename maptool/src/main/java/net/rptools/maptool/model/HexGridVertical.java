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
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarVertHexEuclideanWalker;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;

/*
 * @formatter:off
 * Vertical Hex grids produce columns of hexes
 * and have their points at the side
 *  \_/ \
 *  / \_/
 *  \_/ \
 *  / \_/
 *  \_/ \
 *  
 * @formatter:on
 */
public class HexGridVertical extends HexGrid {

	private static final int[] ALL_ANGLES = new int[] { -150, -120, -90, -60, -30, 0, 30, 60, 90, 120, 150, 180 };
	private static int[] FACING_ANGLES; // = new int[] {-150, -120, -90, -60, -30, 0, 30, 60, 90, 120, 150, 180};
	private static List<TokenFootprint> footprintList;

	private static final OffsetTranslator OFFSET_TRANSLATOR = new OffsetTranslator() {
		public void translate(CellPoint originPoint, CellPoint offsetPoint) {
			if (Math.abs(originPoint.x) % 2 == 1 && Math.abs(offsetPoint.x) % 2 == 0) {
				offsetPoint.y++;
			}
		}
	};

	public HexGridVertical() {
		super();
		if (FACING_ANGLES == null) {
			boolean faceEdges = AppPreferences.getFaceEdge();
			boolean faceVertices = AppPreferences.getFaceVertex();
			setFacings(faceEdges, faceVertices);
		}
	}

	public HexGridVertical(boolean faceEdges, boolean faceVertices) {
		super();
		setFacings(faceEdges, faceVertices);
	}

	@Override
	public void setFacings(boolean faceEdges, boolean faceVertices) {
		if (faceEdges && faceVertices) {
			FACING_ANGLES = ALL_ANGLES;
		} else if (!faceEdges && faceVertices) {
			FACING_ANGLES = new int[] { -120, -60, 0, 60, 120, 180 };
		} else if (faceEdges && !faceVertices) {
			FACING_ANGLES = new int[] { -150, -90, -30, 30, 90, 150 };
		} else {
			FACING_ANGLES = new int[] { 90 };
		}
	}

	@Override
	public int[] getFacingAngles() {
		return FACING_ANGLES;
	}

	/*
	 * For a horizontal hex grid we want the following layout:
	 * @formatter:off
	 *
	 *		7	8	9
	 *	-		5		-
	 *		1	2	3
	 *
	 * @formatter:off
	 * (non-Javadoc)
	 * @see net.rptools.maptool.model.Grid#installMovementKeys(net.rptools.maptool.client.tool.PointerTool, java.util.Map)
	 */
	@Override
	public Dimension getMovementVector(int keyEvent, boolean snapToGrid) {
		int size = snapToGrid ? getSize() : 1;
		int sizeH = snapToGrid ? (int) getVRadius() : 1;
		int sizeV = snapToGrid ? (int) (getURadius() * 1.5) : 1;
		// same result regardless of snaptogrid value
		switch (keyEvent) {
		case KeyEvent.VK_NUMPAD1 :
			return new Dimension(-sizeV, sizeH);
		case KeyEvent.VK_NUMPAD2 :
			return new Dimension(0, size);
		case KeyEvent.VK_NUMPAD3 :
			return new Dimension(sizeV, sizeH);
		case KeyEvent.VK_NUMPAD7 :
			return new Dimension(-sizeV, -sizeH);
		case KeyEvent.VK_NUMPAD8 :
			return new Dimension(0, -size);
		case KeyEvent.VK_NUMPAD9 :
			return new Dimension(sizeV, -sizeH);
		case KeyEvent.VK_LEFT :
			return new Dimension(-sizeV, snapToGrid ? -sizeH : 0);
		case KeyEvent.VK_RIGHT :
			return new Dimension(sizeV, snapToGrid ? sizeH : 0);
		case KeyEvent.VK_UP :
			return new Dimension(0, -size);
		case KeyEvent.VK_DOWN :
			return new Dimension(0, size);
		}
		return new Dimension(0, 0);
	}

	@Override
	public List<TokenFootprint> getFootprints() {
		if (footprintList == null) {
			try {
				footprintList = loadFootprints("net/rptools/maptool/model/hexGridVertFootprints.xml", getOffsetTranslator());
			} catch (IOException ioe) {
				MapTool.showError("Could not load Hex Grid footprints", ioe);
			}
		}
		return footprintList;
	}

	@Override
	public BufferedImage getCellHighlight() {
		return pathHighlight;
	}

	@Override
	public double getCellHeight() {
		return getVRadius() * 2;
	}

	@Override
	public double getCellWidth() {
		return getURadius() * 2;
	}

	@Override
	protected Dimension setCellOffset() {
		return new Dimension((int) getCellOffsetU(), (int) getCellOffsetV());
	}

	@Override
	public ZoneWalker createZoneWalker() {
		return new AStarVertHexEuclideanWalker(getZone());
	}

	@Override
	protected void setGridDrawTranslation(Graphics2D g, double U, double V) {
		g.translate(U, V);
	}

	@Override
	protected double getRendererSizeV(ZoneRenderer renderer) {
		return renderer.getSize().getHeight();
	}

	@Override
	protected double getRendererSizeU(ZoneRenderer renderer) {
		return renderer.getSize().getWidth();
	}

	@Override
	protected int getOffV(ZoneRenderer renderer) {
		return (int) (renderer.getViewOffsetY() + getOffsetY() * renderer.getScale());
	}

	@Override
	protected int getOffU(ZoneRenderer renderer) {
		return (int) (renderer.getViewOffsetX() + getOffsetX() * renderer.getScale());
	}

	@Override
	public CellPoint convert(ZonePoint zp) {
		return convertZP(zp.x, zp.y);
	}

	@Override
	protected int getOffsetU() {
		return getOffsetX();
	}

	@Override
	protected int getOffsetV() {
		return getOffsetY();
	}

	@Override
	public ZonePoint convert(CellPoint cp) {
		return convertCP(cp.x, cp.y);
	}

	@Override
	protected OffsetTranslator getOffsetTranslator() {
		return OFFSET_TRANSLATOR;
	}
	
	/**
	 * Returns the cell centre as well as nearest vertex
	 */
	@Override
	public ZonePoint getNearestVertex(ZonePoint point) {
		double heightHalf = getURadius() / 2;
		//
		double isoY = ((point.y - getOffsetY()) / getVRadius() + (point.x - getOffsetX()) / heightHalf) / 2;
		double isoX = ((point.x - getOffsetX()) / heightHalf - (point.y - getOffsetY()) / getVRadius()) / 2;
		int newX = (int) Math.floor(isoX);
		int newY = (int) Math.floor(isoY);
		//
		double mapY = (newY - newX) * getVRadius();
		double mapX = ((newX + newY) * heightHalf) + heightHalf;
		return new ZonePoint((int) (mapX) + getOffsetX(), (int) (mapY) + getOffsetY());
	}
}
