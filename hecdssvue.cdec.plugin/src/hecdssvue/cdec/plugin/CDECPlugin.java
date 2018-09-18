package hecdssvue.cdec.plugin;

import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.RowFilter;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.table.TableRowSorter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import hec.dssgui.CombinedDataManager;
import hec.dssgui.ListSelection;
import hec.heclib.util.booleanContainer;
import hec.io.TimeSeriesContainer;

public class CDECPlugin {
	// FIXME: Change this with every release
	public static final String VERSION = "1.2";

	public static void main(Object[] args) {
		final CDECPlugin plugin = new CDECPlugin();
		final ListSelection listSelection = (ListSelection) args[0];
		JMenuItem menuItem = new JMenuItem("DWR CDEC Plugin");
		menuItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				plugin.process(listSelection);
			}
		});
		listSelection.registerImportPlugin(null, menuItem, null);
	}

	public static String chooseFileToOpenOrSave(final ListSelection listSelection, String dialogTitle,
			String extensionName, String extension, boolean open) {
		JFileChooser chooser;
		if (listSelection != null) {
			chooser = new JFileChooser(listSelection.getDirectory());
		} else {
			chooser = new JFileChooser();
		}
		chooser.setAcceptAllFileFilterUsed(false);
		chooser.setDialogTitle(dialogTitle);
		chooser.setFileFilter(new rma.util.RMAFilenameFilter(extensionName, extension));
		if (open) {
			chooser.showOpenDialog(listSelection);
		} else {
			chooser.showSaveDialog(listSelection);
		}
		java.io.File file = chooser.getSelectedFile();
		return file.getAbsolutePath();
	}

	private ListSelection listSelection;
	private CDECStationTableModel stationTableModel;
	private CDECStationCache cache;
	private JProgressBar progressBar;
	private JLabel statusMessage;
	private JTable table;
	private TableRowSorter<CDECStationTableModel> sorter;
	private JButton downloadSelectedSensors;
	private JTextField filterText;
	private Action recacheAction;

	protected void process(final ListSelection listSelection) {
		this.listSelection = listSelection;
		//
		try {
			cache = new CDECStationCache();
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			JOptionPane.showConfirmDialog(listSelection,
					"Creating station cache file for first time use. Use Options menu to recache stations from CDEC to update.");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		final JFrame fr = new JFrame("CDEC Downloader" + " [" + CDECPlugin.VERSION + "]");
		//
		JPanel controlPanel = new JPanel();
		final JToggleButton showSelectedSensors = new JToggleButton("Show Selected Only");
		showSelectedSensors.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (showSelectedSensors.isSelected()) {
					showSelectedSensors.setText("Show All");
				} else {
					showSelectedSensors.setText("Show Selected Only");
				}
				showSelected(showSelectedSensors.isSelected());
			}
		});
		downloadSelectedSensors = new JButton("Download Selected");
		downloadSelectedSensors.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				final Progress progress = new Progress();
				progressBar.setIndeterminate(true);
				progressBar.setMaximum(100);
				progress.setMessage("Initializing download... Please wait.");
				final booleanContainer allDone = new booleanContainer(false);
				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							downloadSelectedSensors.setEnabled(false);
							progressBar.setIndeterminate(false);
							for (CDECSensor sensor : stationTableModel.getSelected()) {
								progress.setMessage("Starting download for : " + sensor.getStationId() + "/"
										+ sensor.getId() + "/" + sensor.getType()
										+ (sensor.getSubType().length() > 0 ? ", " : "") + sensor.getSubType());
								String timeWindow = getTimeWindowStringFromListSelection();
								TimeSeriesContainer tsc = new CDECStationWebService().retrieveSensorData(sensor,
										timeWindow, progress);
								progress.setMessage("Saving " + tsc.fullName + " to dss file");
								progress.setPercentProgress(100);
								CombinedDataManager dataManager = listSelection.getDataManager();
								tsc.fileName = dataManager.DSSFileName();
								dataManager.writeData(tsc);
								// listSelection.save(tsc);

							}
							progress.setMessage("Refreshing catalog...");
							listSelection.refreshCatalog();
							allDone.value = true;
							progressBar.setValue(100);
							statusMessage.setText("All done!");
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						} finally {
							downloadSelectedSensors.setEnabled(true);
						}
					}
				}, "CDEC-Downloader").start();
				new Thread(new Runnable() {

					@Override
					public void run() {
						while (!allDone.value) {
							SwingUtilities.invokeLater(new Runnable() {

								@Override
								public void run() {
									progressBar.setValue(progress.getPercentProgress());
									statusMessage.setText(progress.getMessage());
								}

							});
							try {
								Thread.sleep(2000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}, "CDEC-Progress-Monitor").start();
			}

		});
		controlPanel.add(downloadSelectedSensors);
		controlPanel.add(showSelectedSensors);
		filterText = new JTextField(32);
		filterText.setBorder(BorderFactory.createTitledBorder("Filter to matching"));
		filterText.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				doFilter();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				doFilter();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				doFilter();
			}
		});
		controlPanel.add(filterText);
		//
		table = new JTable();
		table.setAutoCreateColumnsFromModel(true);
		// table.setAutoCreateRowSorter(true);
		table.setFillsViewportHeight(true);
		JPanel tableContainer = new JPanel();
		tableContainer.setLayout(new BorderLayout());
		tableContainer.add(table.getTableHeader(), BorderLayout.PAGE_START);
		tableContainer.add(new JScrollPane(table), BorderLayout.CENTER);
		stationTableModel = new CDECStationTableModel(cache);
		table.setModel(stationTableModel);
		sorter = new TableRowSorter<CDECStationTableModel>(stationTableModel);
		table.setRowSorter(sorter);
		fr.getContentPane().setLayout(new BorderLayout());
		fr.getContentPane().add(controlPanel, BorderLayout.PAGE_START);
		fr.getContentPane().add(tableContainer, BorderLayout.CENTER);
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new GridLayout(2, 1));
		progressBar = new JProgressBar();
		statusMessage = new JLabel();
		bottomPanel.add(progressBar);
		bottomPanel.add(statusMessage);
		fr.getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
		// add menu items to save and load list of selections
		JMenuBar menubar = new JMenuBar();
		JMenu fileMenu = new JMenu("File");
		Action saveSelectionAction = new SaveSelectionAction("Save Selected...", null, "Saves selection to file.",
				new Integer(KeyEvent.CTRL_DOWN_MASK | KeyEvent.VK_S));
		Action loadSelectionAction = new LoadSelectionAction("Load Selected...", null, "Loads selection from file",
				new Integer(KeyEvent.CTRL_DOWN_MASK | KeyEvent.VK_L));
		fileMenu.add(saveSelectionAction);
		fileMenu.add(loadSelectionAction);
		menubar.add(fileMenu);
		JMenu optionsMenu = new JMenu("Options");
		recacheAction = new AbstractAction("Recache Station Sensor List") {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						recacheAction.setEnabled(false);
						Cursor originalCursor = fr.getContentPane().getCursor();
						fr.getContentPane().setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
						try {
							cache.recache();
						} catch (Exception e) {
							e.printStackTrace();
							showErrorDialog(e);
						} finally {
							fr.getContentPane().setCursor(originalCursor);
							recacheAction.setEnabled(true);
						}
					}
				});
				
				
			}
		};
		
		
		optionsMenu.add(recacheAction);
		JCheckBoxMenuItem useInternalSiteItem = new JCheckBoxMenuItem("Use cdec4gov internal site", false);
		useInternalSiteItem.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				AbstractButton abtn = (AbstractButton) e.getSource();
				if (abtn.getModel().isSelected()){
					CDECStationWebService.CDEC_BASE_URL="http://cdec4gov.water.ca.gov";
				} else {
					CDECStationWebService.CDEC_BASE_URL="http://cdec.water.ca.gov";
				}			
			}
		});
		optionsMenu.add(useInternalSiteItem);
		
		menubar.add(optionsMenu);
		fr.setJMenuBar(menubar);
		// fr.setIconImage(image);
		fr.pack();
		fr.setSize(1200, 800);
		fr.setVisible(true);
		if (listSelection == null) { // if running in standalone mode
			fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
	}

	protected void showErrorDialog(Exception e) {
		StringWriter stringWriter = new StringWriter();
		e.printStackTrace(new PrintWriter(stringWriter));
		/*
		JOptionPane.showMessageDialog(listSelection,
				"Error: " + e.getMessage()
						+ stringWriter.getBuffer().substring(0, Math.max(30, stringWriter.getBuffer().length())),
				"Error Message", JOptionPane.ERROR_MESSAGE);
				*/
	}

	protected void doFilter() {
		List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>(2);
		String s = filterText.getText();
		if (s == null || s.length() == 0) {
			sorter.setRowFilter(null);
		} else {
			for (int i = 1; i < stationTableModel.getColumnCount(); i++) { // exclude
																			// 0th
																			// column
				filters.add(RowFilter.regexFilter("(?i)" + s, i));
				// filters.add(RowFilter.regexFilter("(?i)^" + Pattern.quote(s)+
				// "$", i));
			}
			RowFilter<Object, Object> allFilter = RowFilter.orFilter(filters);
			sorter.setRowFilter(allFilter);
		}
	}

	protected void showSelected(boolean selectedOnly) {
		if (selectedOnly) {
			RowFilter<Object, Object> selectedFilter = new RowFilter<Object, Object>() {
				public boolean include(Entry<? extends Object, ? extends Object> entry) {
					if (entry.getValue(0).equals(true)) {
						return true;
					} else {
						return false;
					}
				}
			};
			sorter.setRowFilter(selectedFilter);
		} else {
			sorter.setRowFilter(null);
		}
	}

	protected void saveSelected(String saveToFile) throws Exception {
		// save to file the list of selected items
		List<CDECSensor> selected = stationTableModel.getSelected();
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new FileWriter(saveToFile));
			// write out to file
			Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
			writer.print(gson.toJson(selected));
		} finally {
			if (writer != null)
				writer.close();
		}

	}

	protected void loadSelected(String loadFromFile) throws Exception {
		// load from file list of selected items and have them selected.

		Gson gson = new Gson();
		List<CDECSensor> selectedSensors = gson.fromJson(new FileReader(loadFromFile),
				new TypeToken<List<CDECSensor>>() {
				}.getType());
		stationTableModel.setSelected(selectedSensors);
		stationTableModel.fireTableChanged(new TableModelEvent(stationTableModel));
	}

	public String getTimeWindowStringFromListSelection() {
		String startDate = listSelection.getStartTime().date(-111);
		String endDate = listSelection.getEndTime().date(-111);
		if (startDate.equals("") || endDate.equals("")) {
			return null;
		}
		return startDate + " to " + endDate;
	}

	public class SaveSelectionAction extends AbstractAction {
		public SaveSelectionAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String saveToFile = chooseFileToOpenOrSave(listSelection, "Save selection to file", "sel", "*.sel", false);
			try {
				saveSelected(saveToFile);
			} catch (Exception e1) {
				e1.printStackTrace();
				showErrorDialog(e1);
			}
		}

	}

	public class LoadSelectionAction extends AbstractAction {

		public LoadSelectionAction(String text, ImageIcon icon, String desc, Integer mnemonic) {
			super(text, icon);
			putValue(SHORT_DESCRIPTION, desc);
			putValue(MNEMONIC_KEY, mnemonic);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			String loadFromFile = chooseFileToOpenOrSave(listSelection, "Load selection from file", "sel", "*.sel",
					true);
			try {
				loadSelected(loadFromFile);
			} catch (Exception e1) {
				e1.printStackTrace();
				showErrorDialog(e1);
			}
		}

	}
}