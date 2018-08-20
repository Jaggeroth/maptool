package net.rptools.maptool.model;

import java.awt.Rectangle;

import junit.framework.TestCase;

public class TestIsometricGrid extends TestCase {
	/**
	 * Test each corner ZonePoint converts to origin cell.
	 * 
	 * Test converting origin cell to ZonePoint returns the cell centre.
	 * 
	 * Test cell ZonePoint plus cell offset matches the top left of cell bounds
	 * 
	 * @throws Exception
	 */
	public void testConvertZoneConversion() throws Exception {
		IsometricGrid grid = new IsometricGrid();
		CellPoint cp0 = new CellPoint(0, 0);

		ZonePoint zp1 = new ZonePoint(0, 0);
		assertEquals(cp0, grid.convert(zp1));

		ZonePoint zp2 = new ZonePoint(-49, 25);
		assertEquals(cp0, grid.convert(zp2));

		ZonePoint zp3 = new ZonePoint(49, 25);
		assertEquals(cp0, grid.convert(zp3));

		ZonePoint zp4 = new ZonePoint(0, 49);
		assertEquals(cp0, grid.convert(zp4));

		ZonePoint zpc = grid.convert(cp0);
		ZonePoint zp5 = new ZonePoint(0, 25);
		assertEquals(zpc, zp5);

		Rectangle bounds0 = grid.getBounds(cp0);
		ZonePoint b0 = new ZonePoint(bounds0.x, bounds0.y);
		ZonePoint b1 = new ZonePoint(zpc.x + grid.getCellOffset().width, zpc.y + grid.getCellOffset().height);
		assertEquals(b0, b1);
	}
}
