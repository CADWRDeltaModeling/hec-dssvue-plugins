package hecdssvue.cdec.plugin;

import java.io.FileWriter;
import java.io.PrintWriter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Reads in station cache file and writes it out in a format that conforms to a stable schema
 * E.g. the station cache file has implied schema of <Name of Station> : <Station Information in json object>. This is not stable as <Name of Station>
 * keeps changing. A stable schema would be of the form 
 * "Name": <Name of Station>
 * "StationInfo": <Station Information in json object>
 * 
 * @author psandhu
 *
 */
public class CDECTransformJSONCache {
	public static class CDECStationAlternate{
		public String stationId="";
		public CDECStation station;
	}

	public static void main(String[] args) throws Exception{
		String stationCacheFile = CDECStationCache.STATIONS_CACHE_FILE;
		if (args.length == 1){
			stationCacheFile = args[0];
		}
		CDECStationCache cache = new CDECStationCache();
		//cache.recache(); // uncomment if you want to retrieve the list again -- could take upto 15 minutes
		// write out to file
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		PrintWriter wr = new PrintWriter(new FileWriter(stationCacheFile+"-alternate.json"));
		wr.print(gson.toJson(cache.getStations()));
		wr.close();
	}
}
