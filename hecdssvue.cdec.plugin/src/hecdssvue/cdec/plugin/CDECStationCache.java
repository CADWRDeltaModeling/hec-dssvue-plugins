package hecdssvue.cdec.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * Downloads the CDEC station information and metadata into a local file.
 * 
 * @author psandhu
 * 
 */
public class CDECStationCache {
	public static final String STATIONS_CACHE_FILE = "stations-cache.json";

	public static void main(String[] args) throws Exception {
		CDECStationCache cache = new CDECStationCache();
		cache.recache();
	}

	List<CDECStation> stations;
	private ConcurrentHashMap<String,CDECStation> idToStationMap;
	private CDECStationWebService service;

	public CDECStationCache() throws Exception {
		initCache();
		emptyCache();
		loadCache();
	}

	protected void emptyCache() {
		idToStationMap = new ConcurrentHashMap<String, CDECStation>();
		stations = new ArrayList<CDECStation>();
	}
	
	protected String getAbsolutePathToCacheFile(){
		return Utils.getAppDataDir() + File.separator +STATIONS_CACHE_FILE;
	}
	
	public void initCache() throws Exception {
		File cacheFile = new File(getAbsolutePathToCacheFile());
		if (cacheFile.exists()){
			return;
		}
		Utils.copyInputStreamToFile(this.getClass().getResourceAsStream(STATIONS_CACHE_FILE), cacheFile);
	}

	public void recache() throws Exception {
		emptyCache();
		service = new CDECStationWebService();
		stations.addAll(service.fetchDailyStations());
		stations.addAll(service.fetchRealtimeStations());
		ExecutorService executorService = Executors.newFixedThreadPool(25);
		for (final CDECStation station : stations) {
			executorService.execute(new CDECStationMetadataFetchCommand(station, idToStationMap));
		}
		executorService.shutdown();
		try{
			executorService.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException ex){
			ex.printStackTrace();
		}
		// write out to file
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		PrintWriter wr = new PrintWriter(new FileWriter(getAbsolutePathToCacheFile()));
		wr.print(gson.toJson(idToStationMap));
		wr.close();
	}

	public void loadCache() throws Exception {
		Gson gson = new Gson();
		idToStationMap = gson.fromJson(new FileReader(getAbsolutePathToCacheFile()),
				new TypeToken<ConcurrentHashMap<String, CDECStation>>() {
				}.getType());
		// update station id in sensors -- TEMPORARY MEASURE -- FIXME: 
		for(CDECStation station: idToStationMap.values()){
			for(CDECSensor sensor: station.getSensors()){
				sensor.setStationId(station.getId());
			}
		}

	}

	public List<CDECStation> getStations() {
		return new ArrayList<CDECStation>(idToStationMap.values());
	}

	public static class CDECStationMetadataFetchCommand implements Runnable {
		private CDECStation station;
		private ConcurrentHashMap<String, CDECStation> idToStationMap;

		public CDECStationMetadataFetchCommand(CDECStation station, ConcurrentHashMap<String,CDECStation> idToStationMap) {
			this.station = station;
			this.idToStationMap = idToStationMap;
		}

		@Override
		public void run() {
			try {
				CDECStation stationWithMetaData = new CDECStationWebService().retrieveStationMetadata(station);
				System.out.println("Retreieved station: "+station.getId());
				idToStationMap.put(station.getId(), stationWithMetaData);
			} catch (Exception ex) {
				System.err.println("Exception : " + ex.getMessage()
						+ " when processing station id: " + station.getId());
			}
		}
	}

	public CDECStation getStationWithId(String stationId) {
		return idToStationMap.get(stationId);
	}
}
