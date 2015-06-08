package hecdssvue.cdec.plugin;

import hec.heclib.util.HecTime;
import hec.hecmath.TimeSeriesMath;
import hec.io.TimeSeriesContainer;
import hecdssvue.cdec.plugin.CDECStationWebService.CDECSensorDataDownloadTask.MergedData;

import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import rma.util.DoubleArray;
import rma.util.IntArray;

public class CDECStationWebService {
	public static final String CDEC_BASE_URL = "http://cdec.water.ca.gov";
	public static final HashMap<String, String> DURATION_MAP = new HashMap<String, String>();
	public static final HashMap<String, String> DSS_INTERVAL = new HashMap<String, String>();
	static {
		DURATION_MAP.put("daily", "D");
		DURATION_MAP.put("hourly", "H");
		DURATION_MAP.put("monthly", "M");
		DURATION_MAP.put("event", "E");
		//
		DSS_INTERVAL.put("monthly", "IR-DECADE");
		DSS_INTERVAL.put("daily", "IR-YEAR");
		DSS_INTERVAL.put("hourly", "IR-MONTH");
		DSS_INTERVAL.put("event", "IR-DAY");
	}

	protected List<CDECStation> fetchDailyStations() throws Exception {
		String data = Utils.fetchDataInUrl(CDEC_BASE_URL
				+ "/misc/dailyStations.html");
		return findStations(data);
	}

	protected List<CDECStation> fetchRealtimeStations() throws Exception {
		String data = Utils.fetchDataInUrl(CDEC_BASE_URL
				+ "/misc/realStations.html");
		return findStations(data);
	}

	protected List<CDECStation> findStations(String data) {
		ArrayList<CDECStation> stations = new ArrayList<CDECStation>();
		String tableContents = Utils.getTagContents("table", data)[0];
		String[] rows = Utils.getTagContents("tr", tableContents);
		for (int i = 1; i < rows.length; i++) {
			String[] cols = Utils.getTagContents("td", rows[i]);
			String name = Utils.getTagContents("a", cols[0])[0].trim();
			String id = Utils.getTagContents("b", cols[1])[0].trim();
			CDECStation s = new CDECStation(id);
			s.setName(name);
			s.setElevation(cols[2].trim());
			s.setLat(cols[3].trim());
			s.setLng(cols[4].trim());
			s.setCounty(cols[5]);
			s.setRiverBasin(cols[6]);
			stations.add(s);
		}
		return stations;
	}

	public List<CDECSensorDef> retrieveSensorDefs() throws Exception {
		List<CDECSensorDef> defs = new ArrayList<CDECSensorDef>();
		String data = Utils.fetchDataInUrl(CDEC_BASE_URL
				+ "/misc/senslist.html");
		String table = Utils.getTagContents("table", data)[0];
		String[] rows = Utils.getTagContents("tr", table);

		for (int i = 1; i < rows.length; i++) {
			String[] cols = Utils.getTagContents("td", rows[i]);
			CDECSensorDef def = new CDECSensorDef();
			def.setSensorNumber(Integer.parseInt(cols[0].trim()));
			def.setSensorName(cols[1].trim());
			def.setSensorDescription(cols[3].trim());
			def.setUnits(cols[4].trim());
			defs.add(def);
		}

		return defs;
	}

	/**
	 * Retrieves a stations metadata as long as the id for the station is
	 * populated
	 * 
	 * @param station
	 * @throws Exception
	 */
	public CDECStation retrieveStationMetadata(CDECStation station)
			throws Exception {
		String data = Utils.fetchDataInUrl(CDEC_BASE_URL
				+ "/cgi-progs/stationInfo?station_id=" + station.getId());
		String table = Utils.getTagContents("table", data)[0];
		String[] rows = Utils.getTagContents("tr", table);
		// check station id

		station.setHydrologicArea(Utils.getTagContents("td", rows[2])[1].trim());
		station.setNearbyCity(Utils.getTagContents("td", rows[2])[3].trim());
		station.setOperator(Utils.getTagContents("td", rows[4])[1].trim());
		station.setDataCollection(Utils.getTagContents("td", rows[4])[3].trim());

		String sensorTable = Utils.getTagContents("table", data)[1];
		// check for additional peak of record table... & skip it for now
		if (sensorTable.contains("Peak of Record")) {
			sensorTable = Utils.getTagContents("table", data)[2];
		}
		String[] sensorRows = Utils.getTagContents("tr", sensorTable);
		ArrayList<CDECSensor> sensors = new ArrayList<CDECSensor>();
		for (String sensorRow : sensorRows) {
			String[] sensorCols = Utils.getTagContents("td", sensorRow);
			String sensorId = Utils.getAllMatches("sensor_no=(\\d+)",
					sensorCols[2])[0];
			CDECSensor sensor = new CDECSensor(sensorId);
			sensors.add(sensor);
			String[] fields = sensorCols[0].replaceAll("<b>", "")
					.replaceAll("</b>", "").split(",");

			sensor.setType(fields.length >= 1 ? fields[0].trim() : "");
			if (fields.length == 2) {
				sensor.setSubType("");
				sensor.setUnits(fields[1].trim());
			} else if (fields.length == 3) {
				sensor.setSubType(fields[1].trim());
				sensor.setUnits(fields[2].trim());
			} else {
				sensor.setSubType("");
				sensor.setUnits("");
			}
			sensor.setDuration(Utils.getTagContents("a", sensorCols[1])[0]
					.trim());
			sensor.setPlot(sensorCols[2].trim());
			sensor.setDataCollection(sensorCols[3].trim());
			sensor.setDataAvailable(sensorCols[4].trim());
		}
		station.setSensors(sensors);
		return station;
	}

	public static final SimpleDateFormat cdecDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	public static final SimpleDateFormat cdecQueryDateFormat = new SimpleDateFormat(
			"yyyyMMdd");
	public static final SimpleDateFormat hecDateFormat = new SimpleDateFormat("ddMMMyyyy");

	public HecTime createDateTime(String date, String time) {
		try {
			return new HecTime(hecDateFormat.format(cdecQueryDateFormat
					.parse(date)), time);
		} catch (Exception ex) {
			ex.printStackTrace();
			return new HecTime();
		}
	}

	public TimeSeriesContainer retrieveData(CDECSensor sensor,
			String startDate, String endDate) throws Exception {
		// duration code always E (event). All other data seems to be processed
		// from E duration e.g. hourly, daily etc.
		String duration = sensor.getDuration();
		String durationCode = DURATION_MAP.get(duration);
		CDECSensorDef sensorDef = CDECSensorDefs.get().getDefFromDescription(
				sensor.getType()
						+ (sensor.getSubType().equals("") ? "" : ", "
								+ sensor.getSubType()) + ", "
						+ sensor.getUnits().toLowerCase());
		if (sensorDef == null) {
			System.out.println("Could not find sensor definition for "
					+ sensor.getType()
					+ (sensor.getSubType().equals("") ? "" : ", "
							+ sensor.getSubType()) + ", "
					+ sensor.getUnits().toLowerCase());
		}
		String url = "/cgi-progs/queryCSV?station_id=" + sensor.getStationId()
				+ "&dur_code=" + durationCode + "&sensor_num="
				+ sensorDef.getSensorNumber() + "&start_date=" + startDate
				+ "&end_date=" + endDate;
		String dataInUrl = Utils.fetchDataInUrl(CDEC_BASE_URL + url);
		StringReader reader = new StringReader(dataInUrl);
		CSVFormat format = CSVFormat.newFormat(',').withQuote('\'');
		CSVParser parser = new CSVParser(reader, format);
		Iterator<CSVRecord> iterator = parser.iterator();
		CSVRecord record = iterator.next(); // skip first line
		if (!iterator.hasNext()) {
			System.err.println("No data found for " + sensor.getType() + "@"
					+ sensor.getStationId() + "->[" + startDate + " - "
					+ endDate + "]");
			parser.close();
			reader.close();
			return null;
		}
		record = iterator.next(); // second line
		int sensorId = Integer.parseInt(record.get(0));
		// String timezoneStr = record.get(1);
		String[] dataTypeFields = record.get(2).split("\\(");
		String dataType = dataTypeFields[0].trim();
		String units = dataTypeFields[1].split("\\)")[0].trim();
		IntArray times = new IntArray(100);
		DoubleArray values = new DoubleArray(100);
		int i = 0;
		while (iterator.hasNext()) {
			record = iterator.next();
			HecTime time = createDateTime(record.get(0), record.get(1));
			times.insertElementAt(time.value(), i);
			double value = record.get(2).equals("m") ? -901.0 : Double
					.parseDouble(record.get(2));
			values.insertElementAt(value, i);
			i++;
		}
		reader.close();
		parser.close();
		times.trimToSize();
		values.trimToSize();
		//
		TimeSeriesContainer tsc = new TimeSeriesContainer();
		tsc.numberValues = times.size();
		tsc.startTime = times.firstElement();
		tsc.endTime = times.lastElement();
		tsc.times = times.toArray();
		tsc.values = values.toArray();
		tsc.interval = times.elementAt(1) - times.elementAt(0);
		tsc.location = sensor.getStationId();
		tsc.type = "INST-VAL"; // FIXME: is that right?
		tsc.units = units;
		tsc.parameter = dataType;
		String epart = DSS_INTERVAL.get(duration);
		tsc.fullName = "/CDEC/" + tsc.location + "/" + tsc.parameter + "//"
				+ epart + "/" + sensor.getStationId() + "-"
				+ sensorDef.getSensorName() + "-" + sensorId + "/";

		return tsc;
	}

	/**
	 * Downloads data for sensor...
	 * 
	 * @param sensor
	 * @return
	 * @throws Exception
	 */
	public TimeSeriesContainer retrieveSensorData(CDECSensor sensor, String timeWindow, Progress progress)
			throws Exception {
		if (timeWindow == null){
			timeWindow = sensor.getDataAvailable();
		}
		String startDateStr = timeWindow.split("to")[0].trim();
		Date startDate = cdecDateFormat.parse(startDateStr);
		String endDateStr = timeWindow.split("to")[1].trim();
		Date endDate = null;
		if (endDateStr.equals("present.")) {
			endDate = new Date();
		} else {
			endDate = cdecDateFormat.parse(endDateStr);
		}
		// download a year at a time... except if hourly or event data then
		// download monthly
		int incrementField = Calendar.YEAR;
		if (sensor.getDuration().equals("hourly")
				|| sensor.getDuration().equals("event")) {
			incrementField = Calendar.MONTH;
		}
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(startDate);
		Date currentStartDate = startDate;
		BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(50);
		ThreadPoolExecutor executorService = new ThreadPoolExecutor(20, 45, 0, TimeUnit.SECONDS, workQueue);
		MergedData dataContainer = new MergedData();
		// estimate total number of tasks
		long timeInMillis = endDate.getTime()-startDate.getTime();
		int totalNumberOfTasks = (int) (timeInMillis/(30*24*60*60*1000L));
		if (incrementField == Calendar.YEAR){
			totalNumberOfTasks = (int) (timeInMillis/(365*24*60*60*1000L));
		}
		progress.setTotalNumberOfTasks(totalNumberOfTasks);
		progress.resetProgress();
		while (true) {
			calendar.add(incrementField, 1);
			Date currentEndDate = calendar.getTime();
			TimeSeriesContainer currentData = null;
			if (currentEndDate.after(endDate)) {
				currentEndDate = endDate;
			}
			System.out.println("Queuing task to download: "+currentStartDate+" -> "+currentEndDate);
			// wait for tasks to finish
			while (workQueue.remainingCapacity() == 0){
				Thread.sleep(1000);
				System.out.println("Work Queue is full! Waiting");
			}
			executorService.execute(new CDECSensorDataDownloadTask(sensor,
					cdecDateFormat.format(currentStartDate), cdecDateFormat
							.format(currentEndDate), dataContainer, progress));

			currentStartDate = currentEndDate;
			if (currentEndDate.equals(endDate)) {
				break;
			}
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(30, TimeUnit.MINUTES);
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
		progress.setPercentProgress(100);
		TimeSeriesContainer data = dataContainer.data;
		data.fullName = dataContainer.fullName; // restore the fullname esp.
												// after all those
		// merges
		return data;
	}

	public static class CDECSensorDataDownloadTask implements Runnable {
		public static class MergedData {
			public TimeSeriesContainer data;
			public String fullName;
		}

		private CDECSensor sensor;
		private String startDate;
		private String endDate;
		private MergedData mergedData;
		private Progress progress;

		public CDECSensorDataDownloadTask(CDECSensor sensor, String startDate,
				String endDate, MergedData mergedData, Progress progress) {
			this.sensor = sensor;
			this.startDate = startDate;
			this.endDate = endDate;
			this.mergedData = mergedData;
			this.progress = progress;
		}

		public void run() {
			try {
				TimeSeriesContainer currentData = new CDECStationWebService()
						.retrieveData(sensor, startDate, endDate);
				progress.setMessage("Downloading data for "
						+ sensor.getStationId() + ", " + sensor.getType()
						+ ", [" + startDate + " -> " + endDate + "]");
				progress.incrementProgress();
				synchronized (mergedData) {
					if (mergedData.data == null) {
						mergedData.data = currentData;
						mergedData.fullName = currentData.fullName;
					} else {
						new TimeSeriesMath(currentData).mergeTimeSeries(
								new TimeSeriesMath(mergedData.data)).getData(
								mergedData.data);
					}
				}
			} catch (Exception ex) {
				System.err.println("Failure to download data from "
						+ sensor.getStationId() + ", " + sensor.getType()
						+ ", [" + startDate + " -> " + endDate + "]");
				ex.printStackTrace();
			}
		}
	}
}
