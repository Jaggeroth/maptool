package net.rptools.maptool.model;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import net.rptools.maptool.client.tool.PointerTool;

public class GridMovementAction extends AbstractAction {
	private static final long serialVersionUID = -2937112931742781263L;
	private final PointerTool tool;
	private final int keyEvent;
	
	public GridMovementAction(PointerTool callback, int keyEvent) {
		this.tool = callback;
		this.keyEvent = keyEvent;
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		tool.handleKeyMove(keyEvent);		
	}

}
