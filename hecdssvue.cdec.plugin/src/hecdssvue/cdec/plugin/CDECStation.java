package hecdssvue.cdec.plugin;

import java.util.ArrayList;
import java.util.List;

public class CDECStation {
	private List<CDECSensor> sensors;
	private String id;
	private String name;
	private String elevation;
	private String riverBasin;
	private String county;
	private String hydrologicArea;
	private String nearbyCity;
	private String lat;
	private String lng;
	private String operator;
	private String dataCollection;

	public CDECStation(String id) {
		this.id = id;
		sensors = new ArrayList<CDECSensor>();
	}

	public List<CDECSensor> getSensors() {
		return sensors;
	}

	public void setSensors(List<CDECSensor> sensors) {
		for(CDECSensor s: sensors){
			s.setStationId(id);
		}
		this.sensors = sensors;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getElevation() {
		return elevation;
	}

	public void setElevation(String elevation) {
		this.elevation = elevation;
	}

	public String getRiverBasin() {
		return riverBasin;
	}

	public void setRiverBasin(String riverBasin) {
		this.riverBasin = riverBasin;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getHydrologicArea() {
		return hydrologicArea;
	}

	public void setHydrologicArea(String hydrologicArea) {
		this.hydrologicArea = hydrologicArea;
	}

	public String getNearbyCity() {
		return nearbyCity;
	}

	public void setNearbyCity(String nearbyCity) {
		this.nearbyCity = nearbyCity;
	}

	public String getLat() {
		return lat;
	}

	public void setLat(String lat) {
		this.lat = lat;
	}

	public String getLng() {
		return lng;
	}

	public void setLng(String lng) {
		this.lng = lng;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getDataCollection() {
		return dataCollection;
	}

	public void setDataCollection(String dataCollection) {
		this.dataCollection = dataCollection;
	}

}
