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
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.rptools.maptool.client.AppPreferences;
import net.rptools.maptool.client.MapTool;
import net.rptools.maptool.client.tool.PointerTool;
import net.rptools.maptool.client.ui.zone.ZoneRenderer;
import net.rptools.maptool.client.walker.ZoneWalker;
import net.rptools.maptool.client.walker.astar.AStarHorizHexEuclideanWalker;
import net.rptools.maptool.model.TokenFootprint.OffsetTranslator;

/*
 * Horizontal Hex grids produce rows of hexes
 * and have their points at the top
 *  /\ /\ /\ /\ /\ /\
 * |  |  |  |  |  |  |
 *  \/ \/ \/ \/ \/ \/
 *  */
public class HexGridHorizontal extends HexGrid {

	/* 
	 * Facings are set when a new map is created with a particular grid and these facings affect all maps with the same
	 * grid. Other maps with different grids will remain the same.
	 * 
	 * Facings are set when maps are loaded to the current preferences.
	 */
	private static int[] FACING_ANGLES; // =  new int[] {-150, -120, -90, -60, -30, 0, 30, 60, 90, 120, 150, 180};
	private static final int[] ALL_ANGLES = new int[] { -150, -120, -90, -60, -30, 0, 30, 60, 90, 120, 150, 180 };
	private static List<TokenFootprint> footprintList;

	private static final OffsetTranslator OFFSET_TRANSLATOR = new OffsetTranslator() {
		public void translate(CellPoint originPoint, CellPoint offsetPoint) {
			if (Math.abs(originPoint.y) % 2 == 1 && Math.abs(offsetPoint.y) % 2 == 0) {
				offsetPoint.x++;
			}
		}
	};

	public HexGridHorizontal() {
		super();
		if (FACING_ANGLES == null) {
			boolean faceEdges = AppPreferences.getFaceEdge();
			boolean faceVertices = AppPreferences.getFaceVertex();
			setFacings(faceEdges, faceVertices);
		}
	}

	public HexGridHorizontal(boolean faceEdges, boolean faceVertices) {
		super();
		setFacings(faceEdges, faceVertices);
	}

	/**
	 * Set available facings based on the passed parameters.
	 * 
	 * @param faceEdges
	 *            - Tokens can face cell faces if true.
	 * @param faceVertices
	 *            - Tokens can face cell vertices if true.
	 */
	@Override
	public void setFacings(boolean faceEdges, boolean faceVertices) {
		if (faceEdges && faceVertices) {
			FACING_ANGLES = ALL_ANGLES;
		} else if (!faceEdges && faceVertices) {
			FACING_ANGLES = new int[] { -150, -90, -30, 30, 90, 150 };
		} else if (faceEdges && !faceVertices) {
			FACING_ANGLES = new int[] { -120, -60, 0, 60, 120, 180 };
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
	 *		7	-	9
	 *	4		5		6
	 *		1	-	3
	 *
	 * @formatter:off
	 * (non-Javadoc)
	 * @see net.rptools.maptool.model.Grid#installMovementKeys(net.rptools.maptool.client.tool.PointerTool, java.util.Map)
	 */
	@Override
	public void installMovementKeys(PointerTool callback, Map<KeyStroke, Action> actionMap) {
		if (movementKeys == null) {
			movementKeys = new HashMap<KeyStroke, Action>(12); // parameter is 9/0.75 (load factor)
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD7, 0), new MovementKey(callback, -1, -1));
//			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD8, 0), new MovementKey(callback, 0, -1));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD9, 0), new MovementKey(callback, 1, -1));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD4, 0), new MovementKey(callback, -1, 0));
//			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD5, 0), new MovementKey(callback, 0, 0));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD6, 0), new MovementKey(callback, 1, 0));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD1, 0), new MovementKey(callback, -1, 1));
//			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD2, 0), new MovementKey(callback, 0, 1));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_NUMPAD3, 0), new MovementKey(callback, 1, 1));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), new MovementKey(callback, -1, 0));
			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), new MovementKey(callback, 1, 0));
//			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), new MovementKey(callback, 0, -1));
//			movementKeys.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), new MovementKey(callback, 0, 1));
		}
		actionMap.putAll(movementKeys);
	}

	@Override
	public void uninstallMovementKeys(Map<KeyStroke, Action> actionMap) {
		if (movementKeys != null) {
			for (KeyStroke key : movementKeys.keySet()) {
				actionMap.remove(key);
			}
		}
	}

	@Override
	public List<TokenFootprint> getFootprints() {
		if (footprintList == null) {
			try {
				footprintList = loadFootprints("net/rptools/maptool/model/hexGridHorizFootprints.xml", getOffsetTranslator());
			} catch (IOException ioe) {
				MapTool.showError("Could not load Hex Grid footprints", ioe);
			}
		}
		return footprintList;
	}

	@Override
	public BufferedImage getCellHighlight() {
		// rotate the default path highlight 90 degrees
		AffineTransform at = new AffineTransform();
		at.rotate(Math.toRadians(90.0), pathHighlight.getHeight() / 2, pathHighlight.getHeight() / 2);

		AffineTransformOp atOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

		return atOp.filter(pathHighlight, null);
	}

	@Override
	public double getCellHeight() {
		return getURadius() * 2;
	}

	@Override
	public double getCellWidth() {
		return getVRadius() * 2;
	}

	@Override
	public ZoneWalker createZoneWalker() {
		return new AStarHorizHexEuclideanWalker(getZone());
	}

	@Override
	protected Dimension setCellOffset() {
		return new Dimension((int) getCellOffsetV(), (int) getCellOffsetU());
	}

	@Override
	protected void orientHex(GeneralPath hex) {
		// flip the half-hex over y = x
		AffineTransform at = new AffineTransform();
		at.rotate(Math.toRadians(90.0));
		at.scale(1, -1);
		hex.transform(at);
	}

	@Override
	protected void setGridDrawTranslation(Graphics2D g, double U, double V) {
		g.translate(V, U);
	}

	@Override
	protected double getRendererSizeV(ZoneRenderer renderer) {
		return renderer.getSize().getWidth();
	}

	@Override
	protected double getRendererSizeU(ZoneRenderer renderer) {
		return renderer.getSize().getHeight();
	}

	@Override
	protected int getOffV(ZoneRenderer renderer) {
		return (int) (renderer.getViewOffsetX() + getOffsetX() * renderer.getScale());
	}

	@Override
	protected int getOffU(ZoneRenderer renderer) {
		return (int) (renderer.getViewOffsetY() + getOffsetY() * renderer.getScale());
	}

	@Override
	public CellPoint convert(ZonePoint zp) {
		CellPoint cp = convertZP(zp.y, zp.x);
		return new CellPoint(cp.y, cp.x);
	}

	@Override
	protected int getOffsetU() {
		return getOffsetY();
	}

	@Override
	protected int getOffsetV() {
		return getOffsetX();
	}

	@Override
	public ZonePoint convert(CellPoint cp) {
		ZonePoint zp = convertCP(cp.y, cp.x);
		return new ZonePoint(zp.y, zp.x);
	}

	@Override
	protected OffsetTranslator getOffsetTranslator() {
		return OFFSET_TRANSLATOR;
	}

	private boolean roundUp(CellPoint cell) {
		int standardY = (int) (getEdgeProjection() + getEdgeLength());
		ZonePoint zp = convert(cell);
		CellPoint cellPlus = new CellPoint(cell.x,cell.y);
		cellPlus.y++;
		ZonePoint zp1 = convert(cellPlus);
		if (zp1.y-zp.y > standardY)
			return true;
		return false;
	}

	private ZonePoint getZonePoint(ZonePoint vertex, CellPoint cell) {
		return getZonePoint(vertex, cell, false);
	}
	
	private ZonePoint getZonePoint(ZonePoint vertex, CellPoint cell, boolean roundUp) {
		ZonePoint origin = convert(new CellPoint(0,0));
		ZonePoint zp = convert(cell);
		return new ZonePoint(vertex.y + zp.x - origin.x + getOffsetX(), vertex.x + zp.y - origin.y + getOffsetY() + (roundUp?1:0));
	}
	
	@Override
	public ZonePoint getNearestVertex(ZonePoint point) {
		// Hack to return vertex or centre point
		// Probably a much better way to do this mathematically :(
		//
		//System.out.println("--"+ point.x+ " "+point.y);
		//for (ZonePoint z : getVertex()) {
		//	System.out.println(z.x + " " + z.y);
		//}
		ZonePoint zp0 = getVertex().get(0);
		ZonePoint zp1 = getVertex().get(1);
		ZonePoint zp2 = getVertex().get(2);
		ZonePoint zp3 = getVertex().get(3);
		CellPoint cp = convert(point);
		ZonePoint zp = convert(cp);
		int t = (int) getURadius() / 2;
		int e = (int)getEdgeLength() / 2;
		int diffX = point.x - zp.x;
		int diffY = point.y - zp.y;
		boolean oddrow = (cp.y % 2 == 1);
		if (Math.abs(diffY) > t) {
			if (diffY<0) {
				// 0 if odd 2
				if (oddrow) {
					cp.y--;
					cp.x++;
					return getZonePoint(zp2, cp, roundUp(cp));
				} else {
					return getZonePoint(zp0, cp);
				}
			} else {
				// 3 if odd 1
				if (oddrow) {
					cp.y++;
					cp.x++;
					return getZonePoint(zp1, cp);
				} else {
					return getZonePoint(zp3, cp, roundUp(cp));
				}
			}
		}
		if (Math.abs(diffX) > e) {
			if (diffY<0) {
				if (diffX<0) {
					// 1 if odd 3
					if (oddrow) {
						cp.y--;
						return getZonePoint(zp3, cp, roundUp(cp));
					} else {
						return getZonePoint(zp1, cp);
					}
				} else {
					// 5 - if even 1 if odd 3
					if (oddrow) {
						cp.y--;
						cp.x++;
						return getZonePoint(zp3, cp, roundUp(cp));
					} else {
						cp.x++;
						return getZonePoint(zp1, cp);
					}
				}
			} else {
				if (diffX < 0) {
					// 2 if odd 0
					if (oddrow) {
						cp.y++;
						return getZonePoint(zp0, cp);
					} else {
						return getZonePoint(zp2, cp, roundUp(cp));
					}
				} else {
					// 4 if even 2 if odd 0
					if (oddrow) {
						cp.y++;
						cp.x++;
						return getZonePoint(zp0, cp);
					} else {
						cp.x++;
						return getZonePoint(zp2, cp, roundUp(cp));
					}
				}
			}
		}
		// Centre
		return zp;
	}
}
