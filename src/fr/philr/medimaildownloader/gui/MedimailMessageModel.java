package fr.philr.medimaildownloader.gui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import fr.philr.medimaildownloader.MedimailMessage;

public class MedimailMessageModel extends AbstractTableModel {

	private static final long serialVersionUID = -8132032261675871941L;

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Boolean.class;
		case 4:
			return OpenedState.class;
		default:
			return String.class;
		}
	}

	@Override
	public int getColumnCount() {
		return entetes.length;
	}

	@Override
	public String getColumnName(int columnIndex) {
		return entetes[columnIndex];
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (columnIndex == 0) {
			download.set(rowIndex, (Boolean) aValue);
		}
	}

	@Override
	public int getRowCount() {
		if (localMessages != null) {
			return localMessages.size();
		}
		return 0;
	}

	private ArrayList<Boolean> download;
	private final String[] entetes = { "Télécharger", "De", "Titre", "Date", "Vu" };
	private ArrayList<MedimailMessage> localMessages = null;

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (localMessages != null) {
			MedimailMessage object = localMessages.get(rowIndex);
			switch (columnIndex) {
			case 0:
				return download.get(rowIndex);
			case 1:
				return object.getSenderEmail();
			case 2:
				return object.getTitle();
			case 3:
				return object.getDate();
			case 4:
				return object.isOpened();
			default:
				break;
			}
		}
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return columnIndex == 0;
	}

	public void feedMessages(ArrayList<MedimailMessage> allMessages) {
		localMessages = allMessages;
		download = new ArrayList<>();
		for (int i = 0; i < localMessages.size(); i++)
			download.add(false);
		fireTableDataChanged();

	}
	
	public Set<MedimailMessage> getAllMessages() {
		Set<MedimailMessage> list = new HashSet<MedimailMessage>();
		list.addAll(localMessages);
		return list;
	}

	public Set<MedimailMessage> getPickedMessages() {
		Set<MedimailMessage> list = new HashSet<MedimailMessage>();
		if (localMessages != null) {
			for (int i = 0; i < localMessages.size(); i++) {
				if (download.get(i)) {
					list.add(localMessages.get((i)));
				}
			}
		}
		return list;
	}
}
