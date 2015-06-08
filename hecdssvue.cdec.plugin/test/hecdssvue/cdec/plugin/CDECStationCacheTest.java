package hecdssvue.cdec.plugin;

import java.util.List;

import junit.framework.TestCase;

public class CDECStationCacheTest extends TestCase{
	public void testCache() throws Exception{
		CDECStationCache cache = new CDECStationCache();
		cache.recache();
		List<CDECStation> stations = cache.getStations();
		assertTrue(stations.size() > 0);
		cache.emptyCache();
		List<CDECStation> emptyStations = cache.getStations();
		assertEquals(0, emptyStations.size());
		cache.loadCache();
		List<CDECStation> cachedStations =cache.getStations();
		assertEquals(stations.size(), cachedStations.size());
	}
}
