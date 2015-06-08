package hecdssvue.ca.dwr.math;

import hec.hecmath.TimeSeriesMath;
import hec.io.TimeSeriesContainer;

public class DWRMath {
	/**
	 * Godin filter best done on 15 min data. (tidal day = 24hrs and 45 mins
	 * which is exactly 99 15min intervals)
	 * 
	 * This function accepts irregular or regular time series and first converts
	 * it via interpolation to 15min regular time series Then applies a
	 * 
	 * @author psandhu
	 * 
	 */
	public static TimeSeriesContainer godinFilter(TimeSeriesContainer original)
			throws Exception {
		TimeSeriesContainer result = null;
		String timeIntervalString = "15MIN";
		String timeOffsetString = null; // null implies no offset
		result = ((TimeSeriesMath) new TimeSeriesMath(original)
				.interpolateDataAtRegularInterval(timeIntervalString,
						timeOffsetString)).getContainer();
		result = movingAverage(result, 49, 49); // centered 99 values
		result = movingAverage(result, 48, 49); // forward centered 96 values
		result = movingAverage(result, 49, 48); // back centered 96 values
		return result;
	}

	/**
	 * Does a moving average with number of values chose from previous and
	 * forward values.
	 * 
	 * @param tsc
	 * @param nbackward
	 * @param nforward
	 * @return
	 */
	public static TimeSeriesContainer movingAverage(TimeSeriesContainer tsc,
			int nbackward, int nforward) throws Exception {
		TimeSeriesContainer result = (TimeSeriesContainer) tsc.clone();
		for (int i = 0; i < result.numberValues - nforward; i++) {
			if (i > nbackward) { // first time we have potential for calculating
									// a value
				double sum = 0;
				boolean missing = false;
				for (int j = i - nbackward - 1; j < i + nforward; j++) { // loop
																			// over
																			// array
																			// and
																			// try
																			// to
																			// calculate
																			// average
					if (tsc.values[j] != hec.lang.Const.UNDEFINED_DOUBLE) {
						sum += tsc.values[j];
					} else { // even one missing causes a missing value for
								// now...
						missing = true;
						break;
					}
				}
				if (missing) {
					result.values[i] = hec.lang.Const.UNDEFINED_DOUBLE;
				} else {
					result.values[i] = sum / (nbackward + nforward + 1);
				}

			} else {
				result.values[i] = hec.lang.Const.UNDEFINED_DOUBLE;
			}
		}
		for (int i = result.numberValues - nforward; i < result.numberValues; i++) { // fill
																						// in
																						// the
																						// rest
																						// to
																						// the
																						// end.
			result.values[i] = hec.lang.Const.UNDEFINED_DOUBLE;
		}
		return result;
	}
}
