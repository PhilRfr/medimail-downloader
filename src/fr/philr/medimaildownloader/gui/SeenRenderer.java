package fr.philr.medimaildownloader.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class SeenRenderer extends DefaultTableCellRenderer {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1396819988541559089L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus,	row, column);

		boolean view = (Boolean) value;
		if (view) {
			setText("VU");
			setBackground(Color.GREEN);
		}else {
			setText("À LIRE");
			setBackground(Color.RED);
		}
		
		return this;
	}
}