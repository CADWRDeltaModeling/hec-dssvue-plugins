package hecdssvue.cdec.plugin;

import java.util.concurrent.ConcurrentHashMap;

import hecdssvue.cdec.plugin.CDECStationCache.CDECStationMetadataFetchCommand;
import junit.framework.TestCase;

public class CDECStationMetadataFetchCommandTest extends TestCase {
	public void testBCR(){
		ConcurrentHashMap<String,CDECStation> idToStationMap = new ConcurrentHashMap<String, CDECStation>();
		CDECStation station = new CDECStation("BUD");
		CDECStationMetadataFetchCommand cmd = new CDECStationMetadataFetchCommand(station, idToStationMap);
		cmd.run();
		CDECStation station2 = idToStationMap.get(station.getId());
		assertEquals(station.getId(),station2.getId());
	}
}
