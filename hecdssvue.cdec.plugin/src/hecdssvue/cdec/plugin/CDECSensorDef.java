package hecdssvue.cdec.plugin;

public class CDECSensorDef {
	private int sensorNumber; 
	private String sensorName;
	private String sensorDescription;
	private String units;
	public int getSensorNumber() {
		return sensorNumber;
	}
	public void setSensorNumber(int sensorNumber) {
		this.sensorNumber = sensorNumber;
	}
	public String getSensorName() {
		return sensorName;
	}
	public void setSensorName(String sensorName) {
		this.sensorName = sensorName;
	}
	public String getSensorDescription() {
		return sensorDescription;
	}
	public void setSensorDescription(String sensorDescription) {
		this.sensorDescription = sensorDescription;
	}
	public String getUnits() {
		return units;
	}
	public void setUnits(String units) {
		this.units = units;
	}
	
}
