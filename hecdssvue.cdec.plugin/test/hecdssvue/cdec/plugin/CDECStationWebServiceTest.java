package hecdssvue.cdec.plugin;

import hec.heclib.dss.HecDss;
import hec.io.TimeSeriesContainer;

import java.util.List;

import junit.framework.TestCase;

public class CDECStationWebServiceTest extends TestCase {
	public void xtestRetrieveSensorDefs() throws Exception {
		List<CDECSensorDef> sensorDefs = new CDECStationWebService()
				.retrieveSensorDefs();
		assertNotNull(sensorDefs);
		assertTrue(sensorDefs.size() > 0);
		assertEquals("RIV STG", sensorDefs.get(0).getSensorName());
	}

	public void xtestRetrieveSensorData() throws Exception {
		CDECStationCache cache = new CDECStationCache();
		CDECStation fptStation = cache.getStationWithId("FPT");
		CDECSensor fptSensor0 = fptStation.getSensors().get(0);
		String startDate = "01/01/2015";
		String endDate = "01/02/2015";
		TimeSeriesContainer data = new CDECStationWebService().retrieveData(
				fptSensor0, startDate, endDate);
		assertNotNull(data);
		HecDss dssFile = HecDss.open("test.dss", false);
		dssFile.put(data);
		dssFile.close();
	}

	public void testRetrieveSensorDataAll() throws Exception {
		CDECStationCache cache = new CDECStationCache();
		CDECStation fptStation = cache.getStationWithId("FPT");
		Progress progress = new Progress();
		for (CDECSensor sensor : fptStation.getSensors().subList(3, 4)) {
			TimeSeriesContainer data = new CDECStationWebService()
					.retrieveSensorData(sensor, null, progress);
			assertNotNull(data);
			HecDss dssFile = HecDss.open("test.dss", false);
			dssFile.put(data);
			dssFile.close();
		}

	}

	public void testRetrieveStationMetaDataBCR() throws Exception {
		CDECStationWebService service = new CDECStationWebService();
		CDECStation horStation = new CDECStation("BCR");
		CDECStation horStationWithMeta = service.retrieveStationMetadata(horStation);
		for(CDECSensor sensor: horStationWithMeta.getSensors()){
			System.out.println(sensor.getType()+"::"+sensor.getSubType()+"::"+sensor.getUnits());
		}
	}

	public void testRetrieveStationMetaData() throws Exception {
		CDECStationWebService service = new CDECStationWebService();
		CDECStation horStation = new CDECStation("HRO");
		CDECStation horStationWithMeta = service.retrieveStationMetadata(horStation);
		for(CDECSensor sensor: horStationWithMeta.getSensors()){
			System.out.println(sensor.getType()+"::"+sensor.getSubType()+"::"+sensor.getUnits());
			
		}
		
		
	}
}
