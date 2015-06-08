package hecdssvue.cdec.plugin;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTable;

/**
 * Displays Station and sensor information in a view
 * @author psandhu
 *
 */
public class StationInfoPanel extends JPanel{
	JPanel metaInfoPanel;
	JTable sensorTable;
	private CDECStation station;
	public StationInfoPanel(){
		setLayout(new BorderLayout());
		metaInfoPanel = new JPanel();
		metaInfoPanel.setLayout(new GridLayout(5, 4));
		
		add(metaInfoPanel, BorderLayout.PAGE_START);
		add(sensorTable, BorderLayout.CENTER);
	}
	
	public void setStation(CDECStation station){
		this.station = station;
		refreshView();
	}

	public void refreshView() {
		
	}
}
