package hecdssvue.cdec.plugin;

public class CDECSensor {
	private String id;
	private String sensorNumber;
	private String type;
	private String subType;
	private String duration;
	private String units;
	private String plot;
	private String dataCollection;
	private String dataAvailable;
	private String stationId;
	private String moleculeType;

	public CDECSensor(String id){
		setId(id);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSensorNumber() {
		return sensorNumber;
	}

	public void setSensorNumber(String sensorNumber) {
		this.sensorNumber = sensorNumber;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSubType() {
		return subType;
	}

	public void setSubType(String subType) {
		this.subType = subType;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public String getPlot() {
		return plot;
	}

	public void setPlot(String plot) {
		this.plot = plot;
	}

	public String getDataCollection() {
		return dataCollection;
	}

	public void setDataCollection(String dataCollection) {
		this.dataCollection = dataCollection;
	}

	public String getDataAvailable() {
		return dataAvailable;
	}

	public void setDataAvailable(String dataAvailable) {
		this.dataAvailable = dataAvailable;
	}

	public void setStationId(String stationId) {
		this.stationId = stationId;
	}
	
	public String getStationId(){
		return stationId;
	}
	
	public boolean equals(Object obj){
		if (obj != null && obj instanceof CDECSensor){
			CDECSensor other = (CDECSensor) obj;
			return other.stationId.equals(stationId) && other.id.equals(id) && other.type.equals(type) && other.subType.equals(subType) && other.units.equals(units) && other.duration.equals(duration);
		} 
		return false;
	}

	@Override
	public int hashCode() {
		return stationId.hashCode() + id.hashCode() + type.hashCode() + subType.hashCode() + units.hashCode() + duration.hashCode();
	}

	public void setMoleculeType(String moleculeType) {
		this.moleculeType = moleculeType;
	}

	public String getMoleculeType() {
		return moleculeType;
	}
}
