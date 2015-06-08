package hecdssvue.cdec.plugin;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

public class CDECStationTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<CDECSensor> sensors;
	private String[] columnNames = { "SELECTED", "ID", "NAME", "SENSOR TYPE",
			"DURATION", "TIME WINDOW", "UNITS", "ELEVATION", "COUNTY",
			"RIVER BASIN" };
	private Boolean[] selectedRows;
	private CDECStationCache stationCache;

	public CDECStationTableModel(CDECStationCache stationCache) {
		this.stationCache = stationCache;
		sensors = new ArrayList<CDECSensor>();
		for (CDECStation station : stationCache.getStations()) {
			for (CDECSensor sensor : station.getSensors()) {
				sensors.add(sensor);
			}
		}
		setSensors(sensors);
	}

	public void setSensors(List<CDECSensor> sensors) {
		this.sensors = sensors;
		selectedRows = new Boolean[sensors.size()];
		for (int i = 0; i < selectedRows.length; i++) {
			selectedRows[i] = new Boolean(false);
		}
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return columnNames[column];
	}

	@Override
	public int getRowCount() {
		return sensors.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (column == 0) {
			return selectedRows[row];
		} else {
			CDECSensor sensor = sensors.get(row);
			CDECStation station = stationCache.getStationWithId(sensor
					.getStationId());
			switch (column) {
			case 1:
				return station.getId();
			case 2:
				return station.getName();
			case 3:
				return sensor.getType()
						+ (sensor.getSubType().equals("") ? "" : ", "
								+ sensor.getSubType()) + ", ";
			case 4:
				return sensor.getDuration().toUpperCase();
			case 5:
				return sensor.getDataAvailable();
			case 6:
				return sensor.getUnits().toUpperCase();
			case 7:
				return station.getElevation();
			case 8:
				return station.getCounty();
			case 9:
				return station.getRiverBasin();
			default:
				return "???";
			}
		}
	}

	public boolean isCellEditable(int row, int column) {
		return column == 0;
	}

	public void setValueAt(Object value, int row, int column) {
		if (column == 0) {
			selectedRows[row] = (Boolean) value;
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 0) {
			return Boolean.class;
		} else {
			return super.getColumnClass(columnIndex);
		}
	}

	public List<CDECSensor> getSelected() {
		ArrayList<CDECSensor> selected = new ArrayList<CDECSensor>();
		for (int row = 0; row < selectedRows.length; row++) {
			if (selectedRows[row].booleanValue()) {
				selected.add(sensors.get(row));
			}
		}
		return selected;
	}

	public void setSelected(List<CDECSensor> selectedSensors){
		for(int row=0; row < selectedRows.length; row++){
			CDECSensor cdecSensor = sensors.get(row);
			if (selectedSensors.contains(cdecSensor)){
				selectedRows[row]=true;
			} else {
				selectedRows[row]=false;
			}
		}
	}
}
