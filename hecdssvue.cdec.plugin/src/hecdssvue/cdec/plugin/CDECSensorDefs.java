package hecdssvue.cdec.plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class CDECSensorDefs {
	public static final String CACHE_FILE = "station-defs.json";
	private List<CDECSensorDef> defs;
	private HashMap<String, CDECSensorDef> nameToDefMap;
	private HashMap<String, CDECSensorDef> descriptionToDefMap;
	private static CDECSensorDefs instance;
	/**
	 * get singleton instance
	 * @return
	 */
	public static CDECSensorDefs get(){
		if (instance==null){ // not purely thread safe but constructors are so slightly inefficient but safe.
			try {
				instance = new CDECSensorDefs();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return instance;
	}

	private CDECSensorDefs() throws Exception{
		try {
			initCache();
			loadFromCache();
		} catch (IOException ex) {
			System.out
					.println("No station type definitions found. Loading from CDEC website.");
			if (defs == null || defs.size() == 0) {
				defs = new CDECStationWebService().retrieveSensorDefs();
				saveToCache();
			}
		}
		nameToDefMap = new HashMap<String, CDECSensorDef>();
		for(CDECSensorDef def:  defs){
			nameToDefMap.put(def.getSensorName(), def);
		}
		descriptionToDefMap = new HashMap<String, CDECSensorDef>();
		for(CDECSensorDef def:  defs){
			descriptionToDefMap.put(def.getSensorDescription()+", "+def.getUnits().toLowerCase(), def);
		}
	}

	public void initCache() throws Exception {
		File cacheFile = new File(getAbsolutePathToCacheFile());
		if (cacheFile.exists()){
			return;
		}
		Utils.copyInputStreamToFile(this.getClass().getResourceAsStream(CACHE_FILE), cacheFile);
	}

	protected String getAbsolutePathToCacheFile(){
		return Utils.getAppDataDir() + File.separator + CACHE_FILE;
	}
	

	private void saveToCache() throws IOException {
		// write out to file
		Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping()
				.create();
		PrintWriter wr = new PrintWriter(new FileWriter(getAbsolutePathToCacheFile()));
		wr.print(gson.toJson(defs));
		wr.close();
	}

	private void loadFromCache() throws IOException {
		Gson gson = new Gson();
		defs = gson.fromJson(new FileReader(getAbsolutePathToCacheFile()),
				new TypeToken<List<CDECSensorDef>>() {
				}.getType());
	}
	
	public List<CDECSensorDef> getDefs(){
		return defs;
	}
	
	public CDECSensorDef getDefForName(String name){
		return nameToDefMap.get(name);
	}
	
	public CDECSensorDef getDefFromDescription(String description){
		return descriptionToDefMap.get(description);
	}

}
