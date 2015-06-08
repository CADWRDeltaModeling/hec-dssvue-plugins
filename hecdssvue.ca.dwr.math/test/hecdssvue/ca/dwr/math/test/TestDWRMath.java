package hecdssvue.ca.dwr.math.test;

import junit.framework.TestCase;
import hec.heclib.util.HecTime;
import hec.io.TimeSeriesContainer;
import hecdssvue.ca.dwr.math.DWRMath;

public class TestDWRMath extends TestCase {
	public TimeSeriesContainer createSinTS(){
		TimeSeriesContainer tsc = new TimeSeriesContainer();
		tsc.startTime = new HecTime("01JAN1990 0100").julian();
		tsc.interval = 15;
		tsc.numberValues = 10000;
		tsc.values = new double[tsc.numberValues];
		tsc.times = new int[tsc.numberValues];
		for(int i=0; i< tsc.numberValues; i++){
			tsc.times[i] = tsc.startTime+i*tsc.interval;
			tsc.values[i] = Math.sin(Math.PI*2.0*i/96); // sin wave of period of 1 day = 96*15 min values
		}
		return tsc;
	}

	
	public void testMovingAverage() throws Exception{
		TimeSeriesContainer movingAverage = DWRMath.movingAverage(createSinTS(), 10, 10);
		assertNotNull(movingAverage);
	}
	
	public void testGodin() throws Exception{
		TimeSeriesContainer godinFiltered = DWRMath.godinFilter(createSinTS());
		assertNotNull(godinFiltered);
	}
}
