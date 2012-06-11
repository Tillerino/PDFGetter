package de.tiller.PDFGetter;
import java.awt.GridBagLayout;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import java.awt.GridBagConstraints;

import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.UIManager;

import java.awt.Insets;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.prefs.*;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;

import de.tiller.PDFGetter.Page.Base64Encoder;

import com.sun.org.apache.xml.internal.security.utils.Base64;


public class PDFGetterSwingGUI extends JPanel {

	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane1 = null;
	private JPanel jPanel = null;
	private JLabel jLabel1 = null;
	private JLabel jLabel2 = null;
	private JTextField jTextField1 = null;
	private JScrollPane jScrollPane2 = null;
	private JTextArea jTextArea = null;
	private JLabel jLabel3 = null;
	private JLabel jLabel4 = null;
	private JLabel jLabel5 = null;
	private JButton jButton = null;
	private JPanel jPanel1 = null;
	private JButton jButton1 = null;
	private JScrollPane jScrollPane = null;
	private JTable jTable = null;
	private JButton jButton3 = null;
	private JLabel jLabel6 = null;
	private String settingsLocation;
	private List<Settings> settings = new ArrayList<Settings>();  //  @jve:decl-index=0:
	private JButton jButton4 = null;
	
	private Page currentPage = null;
	private JButton jButton5 = null;
	private JButton jButton6 = null;
	private JLabel jLabel = null;
	private JCheckBox jCheckBox = null;
	private JLabel jLabel7 = null;
	private JLabel jLabel8 = null;
	private JTextField jTextField = null;
	/**
	 * This is the default constructor
	 */
	public PDFGetterSwingGUI() {
		super();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch(Exception e) {
			// whatever
		}
		Page.encoder = new Base64Encoder() {
			public String encode(String in) {
				return Base64.encode(in.getBytes());
			}
		};
		initialize();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		settingsLocation = prefs.get("settingsLocation", null);
		loadSettings();
		undisplay();
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(1000000);
	}

	private void loadSettings() {
		if(settingsLocation == null)
			return;
		jLabel5.setText(settingsLocation);
		Preferences.userNodeForPackage(getClass()).put("settingsLocation", settingsLocation);
		SettingsFile file;
		try {
			file = (SettingsFile) new ObjectInputStream(new FileInputStream(new File(settingsLocation))).readObject();
			
			settings = file.settings;
			jCheckBox.setSelected(file.update);
		} catch (FileNotFoundException e) {
			// eat
		} catch (IOException e) {
			exception(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		jTable.updateUI();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridx = 1;
		gridBagConstraints12.anchor = GridBagConstraints.WEST;
		gridBagConstraints12.gridy = 2;
		GridBagConstraints gridBagConstraints111 = new GridBagConstraints();
		gridBagConstraints111.gridx = 0;
		gridBagConstraints111.anchor = GridBagConstraints.EAST;
		gridBagConstraints111.gridy = 2;
		jLabel = new JLabel();
		jLabel.setText("Update all Files");
		GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
		gridBagConstraints81.gridx = 2;
		gridBagConstraints81.gridwidth = 1;
		gridBagConstraints81.insets = new Insets(0, 5, 5, 5);
		gridBagConstraints81.gridy = 2;
		GridBagConstraints gridBagConstraints71 = new GridBagConstraints();
		gridBagConstraints71.gridx = 3;
		gridBagConstraints71.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints71.gridy = 1;
		GridBagConstraints gridBagConstraints61 = new GridBagConstraints();
		gridBagConstraints61.gridx = 1;
		gridBagConstraints61.anchor = GridBagConstraints.NORTHWEST;
		gridBagConstraints61.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints61.gridy = 1;
		GridBagConstraints gridBagConstraints51 = new GridBagConstraints();
		gridBagConstraints51.gridx = 2;
		gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints51.anchor = GridBagConstraints.WEST;
		gridBagConstraints51.ipady = 0;
		gridBagConstraints51.ipadx = 5;
		gridBagConstraints51.weightx = 1.0;
		gridBagConstraints51.gridy = 1;
		jLabel5 = new JLabel();
		jLabel5.setText("");
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridy = 1;
		gridBagConstraints41.ipadx = 0;
		gridBagConstraints41.ipady = 0;
		gridBagConstraints41.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints41.anchor = GridBagConstraints.WEST;
		jLabel4 = new JLabel();
		jLabel4.setText("Settings Location");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridwidth = 5;
		gridBagConstraints.gridx = 0;
		this.setSize(515, 299);
		this.setLayout(new GridBagLayout());
		this.add(getJSplitPane(), gridBagConstraints);
		this.add(jLabel4, gridBagConstraints41);
		this.add(jLabel5, gridBagConstraints51);
		this.add(getJButton(), gridBagConstraints61);
		this.add(getJButton4(), gridBagConstraints71);
		this.add(getJButton6(), gridBagConstraints81);
		this.add(jLabel, gridBagConstraints111);
		this.add(getJCheckBox(), gridBagConstraints12);
	}

	/**
	 * This method initializes jSplitPane	
	 * 	
	 * @return javax.swing.JSplitPane	
	 */
	private JSplitPane getJSplitPane() {
		if (jSplitPane == null) {
			jSplitPane = new JSplitPane();
			jSplitPane.setDividerLocation(170);
			jSplitPane.setResizeWeight(.3);
			jSplitPane.setRightComponent(getJScrollPane1());
			jSplitPane.setLeftComponent(getJPanel1());
		}
		return jSplitPane;
	}

	/**
	 * This method initializes jScrollPane1	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane1() {
		if (jScrollPane1 == null) {
			jScrollPane1 = new JScrollPane();
			jScrollPane1.setViewportView(getJPanel());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jPanel	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel() {
		if (jPanel == null) {
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints14.gridy = 3;
			gridBagConstraints14.weightx = 1.0;
			gridBagConstraints14.gridwidth = 2;
			gridBagConstraints14.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints14.gridx = 1;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 0;
			gridBagConstraints13.gridy = 3;
			jLabel8 = new JLabel();
			jLabel8.setText("Search Pattern");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.gridy = 4;
			jLabel6 = new JLabel();
			jLabel6.setText("");
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints9.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints9.gridy = 4;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.ipadx = 0;
			gridBagConstraints7.ipady = 0;
			gridBagConstraints7.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints7.weighty = 1.0;
			gridBagConstraints7.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints7.gridy = 4;
			jLabel3 = new JLabel();
			jLabel3.setText("Target Folder");
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 2;
			gridBagConstraints6.weightx = 0.0;
			gridBagConstraints6.weighty = 0.0;
			gridBagConstraints6.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints6.gridheight = 1;
			gridBagConstraints6.gridwidth = 2;
			gridBagConstraints6.gridx = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.weighty = 0.0;
			gridBagConstraints5.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints5.gridx = 1;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 0;
			gridBagConstraints4.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints4.ipadx = 0;
			gridBagConstraints4.ipady = 0;
			gridBagConstraints4.weighty = 0.0;
			gridBagConstraints4.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints4.gridy = 2;
			jLabel2 = new JLabel();
			jLabel2.setText("Password(s)");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints3.gridy = 1;
			jLabel1 = new JLabel();
			jLabel1.setText("URL");
			jPanel = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			jPanel.setLayout(gridBagLayout);
			jPanel.add(jLabel1, gridBagConstraints3);
			jPanel.add(jLabel2, gridBagConstraints4);
			jPanel.add(getJTextField1(), gridBagConstraints5);
			jPanel.add(getJScrollPane2(), gridBagConstraints6);
			jPanel.add(jLabel3, gridBagConstraints7);
			jPanel.add(getJButton3(), gridBagConstraints9);
			jPanel.add(jLabel6, gridBagConstraints11);
			jPanel.add(jLabel8, gridBagConstraints13);
			jPanel.add(getJTextField(), gridBagConstraints14);
		}
		return jPanel;
	}

	/**
	 * This method initializes jTextField1	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField1() {
		if (jTextField1 == null) {
			jTextField1 = new JTextField();
			jTextField1.setToolTipText("Enter the Address of the Website that links to the PDFs you'd like to download.");
			jTextField1.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					if(currentPage == null ||jTextField1.getText().equals(""))
						return;
					try {
						currentPage.URL = new URL(jTextField1.getText());
					} catch (MalformedURLException e1) {
						JOptionPane.showMessageDialog(me(), "The URL you entered seems to be malformatted and will not be saved.");
					}
				}
			});
		}
		return jTextField1;
	}

	/**
	 * This method initializes jScrollPane2	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getJTextArea());
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes jTextArea	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JTextArea getJTextArea() {
		if (jTextArea == null) {
			jTextArea = new JTextArea();
			jTextArea.setPreferredSize(new Dimension(0, 50));
			jTextArea.setToolTipText("Please enter user/password pairs in the form user:password that I am supposed to try on this page, one per line.");
			jTextArea.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					if(currentPage == null)
						return;
					
					currentPage.userpwd = jTextArea.getText();
				}
			});
		}
		return jTextArea;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Browse");
			jButton.setToolTipText("Click to select a settings file. If the file already exists, the settings stored in it will be loaded. I will remember the location and load the settings from the selected file the next time I'm started.");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					JFileChooser c = settingsLocation != null ? new JFileChooser(settingsLocation) : new JFileChooser();
					if(c.showSaveDialog(me()) != JFileChooser.APPROVE_OPTION)
						return;
					settingsLocation = c.getSelectedFile().getAbsolutePath();
					loadSettings();
				}
			});
		}
		return jButton;
	}

	/**
	 * This method initializes jPanel1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getJPanel1() {
		if (jPanel1 == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.gridy = 0;
			jLabel7 = new JLabel();
			jLabel7.setText("[HELP]");
			jLabel7.setToolTipText("Manage your Pages here. Select a Page to edit, double click to rename it. Disable the 'work' Checkbox of a Page to skip it when downloading. Tooltips will explain the details.");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints2.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints2.gridwidth = 2;
			gridBagConstraints2.gridy = 2;
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints1.insets = new Insets(5, 5, 5, 5);
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = GridBagConstraints.BOTH;
			gridBagConstraints8.gridy = 1;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 1.0;
			gridBagConstraints8.insets = new Insets(0, 5, 0, 5);
			gridBagConstraints8.gridwidth = 2;
			gridBagConstraints8.gridx = 0;
			jPanel1 = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			jPanel1.setLayout(gridBagLayout);
			jPanel1.add(getJButton1(), gridBagConstraints1);
			jPanel1.add(getJScrollPane(), gridBagConstraints8);
			jPanel1.add(getJButton5(), gridBagConstraints2);
			jPanel1.add(jLabel7, gridBagConstraints10);
		}
		return jPanel1;
	}

	/**
	 * This method initializes jButton1	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton1() {
		if (jButton1 == null) {
			jButton1 = new JButton();
			jButton1.setText("New Page");
			jButton1.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					settings.add(new Settings());
					settings.get(settings.size() - 1).download = false;
					settings.get(settings.size() - 1).name = "New Page";
					jTable.updateUI();
				}
			});
		}
		return jButton1;
	}

	/**
	 * This method initializes jScrollPane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getJScrollPane() {
		if (jScrollPane == null) {
			jScrollPane = new JScrollPane();
			jScrollPane.setViewportView(getJTable());
		}
		return jScrollPane;
	}

	/**
	 * This method initializes jTable	
	 * 	
	 * @return javax.swing.JTable	
	 */
	private JTable getJTable() {
		if (jTable == null) {
			jTable = new JTable(new AbstractTableModel() {
				private static final long serialVersionUID = 1L;
				public Class<?> getColumnClass(int arg0) {
					return arg0 == 0 ? Boolean.class : String.class;
				}
				public int getColumnCount() {
					return 2;
				}
				public String getColumnName(int arg0) {
					if(arg0 == 0)
						return "work";
					else
						return "Page name";
				}
				public int getRowCount() {
					return settings.size();
				}
				public Object getValueAt(int arg0, int arg1) {
					return arg1 == 0 ? settings.get(arg0).download : settings.get(arg0).name;
				}
				public boolean isCellEditable(int arg0, int arg1) {
					return true;
				}
				public void setValueAt(Object arg0, int arg1, int arg2) {
					if(arg2 == 0)
						settings.get(arg1).download = (Boolean) arg0;
					else
						settings.get(arg1).name = (String) arg0;
				}
			});
			jTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			jTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
				public void valueChanged(ListSelectionEvent arg0) {
					if(arg0.getValueIsAdjusting())
						return;
					if(jTable.getSelectedRow() >= 0)
						display(jTable.getSelectedRow());
				}
			});
			jTable.getColumnModel().getColumn(0).setPreferredWidth(40);
			jTable.getColumnModel().getColumn(1).setPreferredWidth(110);
		}
		return jTable;
	}

	protected void display(int firstIndex) {
		System.out.println("display(" + firstIndex + ")");
		if(settings.get(firstIndex).page == null)
			settings.get(firstIndex).page = new Page(null, null, null);
		
		jTextField1.setText(settings.get(firstIndex).page.URL != null ? settings.get(firstIndex).page.URL.toString() : "");
		jTextArea.setText(settings.get(firstIndex).page.userpwd != null ? settings.get(firstIndex).page.userpwd : "");
		jLabel6.setText(settings.get(firstIndex).page.targetLocation != null ? settings.get(firstIndex).page.targetLocation.getAbsolutePath() : "");
		jTextField.setText("");
		if(settings.get(firstIndex).page.pattern != null)
			jTextField.setText(settings.get(firstIndex).page.pattern.toString());
		
		jTextField1.setEditable(true);
		jTextArea.setEditable(true);
		jTextField.setEditable(true);
		
		currentPage = settings.get(firstIndex).page;
	}

	Component me() {
		return this;
	}
	
	void exception(Exception e) {
		StringWriter sw = new StringWriter();
		
		e.printStackTrace(new PrintWriter(sw));
		JOptionPane.showMessageDialog(me(), e.getMessage(), "Exception", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This method initializes jButton3	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton3() {
		if (jButton3 == null) {
			jButton3 = new JButton();
			jButton3.setText("Browse...");
			jButton3.setToolTipText("browse for a folder to save the PDF Files from this page to");
			jButton3.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(currentPage == null)
						return;
					JFileChooser c = currentPage.targetLocation == null ? new JFileChooser() : new JFileChooser(currentPage.targetLocation);
					
					c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					
					if(c.showOpenDialog(me()) != JFileChooser.APPROVE_OPTION)
						return;
					
					currentPage.targetLocation = c.getSelectedFile();
					jLabel6.setText(c.getSelectedFile().getAbsolutePath());
				}
			});
		}
		return jButton3;
	}

	/**
	 * This method initializes jButton4	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton4() {
		if (jButton4 == null) {
			jButton4 = new JButton();
			jButton4.setText("Save Settings");
			jButton4.setToolTipText("click to save your settings to the selected file");
			jButton4.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(settingsLocation == null || settingsLocation.equals("") || new File(settingsLocation).isDirectory())
						return;
					
					try {
						System.out.println("Serializing");
						ObjectOutputStream out;
						out = new ObjectOutputStream(new FileOutputStream(new File(settingsLocation)));
						out.writeObject(new SettingsFile(settings, jCheckBox.isSelected()));
					} catch(Exception e1) {
						// eat
					}
				}
			});
		}
		return jButton4;
	}

	/**
	 * This method initializes jButton5	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton5() {
		if (jButton5 == null) {
			jButton5 = new JButton();
			jButton5.setText("Remove Page");
			jButton5.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					if(jTable.getSelectedRowCount() != 1)
						return;
					int selected = jTable.getSelectedRow();
					jTable.getSelectionModel().removeSelectionInterval(selected, selected);
					settings.remove(selected);
					if(selected > 0)
						jTable.getSelectionModel().setSelectionInterval(selected - 1, selected - 1);
					jTable.updateUI();
					undisplay();
					
					if(jTable.getSelectedRow() != -1)
						display(jTable.getSelectedRow());
				}
			});
		}
		return jButton5;
	}

	protected void undisplay() {
		jTextField1.setEditable(false);
		jTextField1.setText("");
		jTextField1.updateUI();
		
		jTextField.setEditable(false);
		jTextField.setText("");
		jTextField.updateUI();
		
		jTextArea.setEditable(false);
		jTextArea.setText("");
		jTextArea.updateUI();
	}

	/**
	 * This method initializes jButton6	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton6() {
		if (jButton6 == null) {
			jButton6 = new JButton();
			jButton6.setText("DOWNLOAD PDFs");
			jButton6.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					long time = System.currentTimeMillis();
					
					Vector<Page> pages = new Vector<Page>();
					
					for(Settings s : settings) {
						if(s == null || s.download == false || s.page == null || s.page.URL == null || s.page.targetLocation == null)
							continue;
						
						pages.add(s.page);
					}
					
					if(pages.size() <= 0) {
						JOptionPane.showMessageDialog(me(), "Please choose some pages to work on!");
						return;
					}
					
					Page.updateAllFiles = jCheckBox.isSelected();
					
					Throwable[] thrown = Page.execute(pages.toArray(new Page[pages.size()]), true).thrown;
					
					String message = "Downloaded " + Page.downloadedFiles[0] + " files (" + Page.downloadedBytes[0] + " Bytes)\n" +
							"in " + (System.currentTimeMillis() - time) + " milliseconds";
					
					if(thrown.length > 0)
						message += "\n" + thrown.length + " Error(s) occured:\n";
					
					for(Throwable t : thrown)
						message += "\n\t" + t.getMessage();
					
					JOptionPane.showMessageDialog(me(), message);
				}
			});
		}
		return jButton6;
	}

	/**
	 * This method initializes jCheckBox	
	 * 	
	 * @return javax.swing.JCheckBox	
	 */
	private JCheckBox getJCheckBox() {
		if (jCheckBox == null) {
			jCheckBox = new JCheckBox();
			jCheckBox.setToolTipText("If checked, files will be downloaded and overwritten even if they already exist. Note that I will also check subfolders for existing files.");
		}
		return jCheckBox;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (jTextField == null) {
			jTextField = new JTextField();
			jTextField.setToolTipText("I will only download Files that match this Regular Expression. If you have no Idea what a Regular Expression is, type .*\\.pdf or any file extension instead of pdf to match files with that extension.");
			jTextField.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					try {
						currentPage.pattern = Pattern.compile(jTextField.getText(), Pattern.CASE_INSENSITIVE);
					} catch (Exception e1) {
						JOptionPane.showMessageDialog(me(), "Invalid Regular Expression: " + e1.getMessage() + "\n\nTry again!");
					}
				}
			});
		}
		return jTextField;
	}
	
}  //  @jve:decl-index=0:visual-constraint="10,10"

