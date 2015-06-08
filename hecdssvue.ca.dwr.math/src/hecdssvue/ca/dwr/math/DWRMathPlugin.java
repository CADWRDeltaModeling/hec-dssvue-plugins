package hecdssvue.ca.dwr.math;

import hec.dssgui.ListSelection;
import hec.heclib.dss.DSSPathname;
import hec.io.TimeSeriesContainer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.Vector;

import javax.swing.JMenuItem;

public class DWRMathPlugin {
	public static void main(Object[] args) {
		final ListSelection listSelection = (ListSelection) args[0];
		JMenuItem menuItem = new JMenuItem("Godin Filter");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				@SuppressWarnings("rawtypes")
				List[] selectedDataContainers = listSelection
						.getSelectedDataContainers();
				if (selectedDataContainers[0] != null) { // only the first element contains time series :)
					Vector<TimeSeriesContainer> resultContainer = new Vector<TimeSeriesContainer>();
					for (Object container : selectedDataContainers[0]) {
						if (container instanceof TimeSeriesContainer) {
							try {
								TimeSeriesContainer tsc = (TimeSeriesContainer) container;
								TimeSeriesContainer godinFiltered = DWRMath
										.godinFilter(tsc);
								DSSPathname pathname = new DSSPathname(
										godinFiltered.fullName);
								pathname.setFPart(pathname.getFPart()
										+ "-GODIN-FILTERED");
								godinFiltered.fullName = pathname.pathname();
								resultContainer.add(godinFiltered);
							} catch (Exception ex) {
								ex.printStackTrace();
							}
						}
					}
					listSelection.save(resultContainer);
					listSelection.refreshCatalog();
				}
			}
		});
		listSelection.registerPlugin(ListSelection.TOOLS, menuItem);
	}
}
