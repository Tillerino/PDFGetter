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
import javax.swing.text.JTextComponent;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionListener;
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
import java.util.prefs.*;
import java.util.regex.Pattern;

import javax.swing.JCheckBox;

import de.tiller.PDFGetter.Page.Base64Encoder;
import de.tiller.PDFGetter.Page.MagicResults;
import de.tiller.PDFGetter.Page.Update;

import com.sun.org.apache.xml.internal.security.utils.Base64;

public class PDFGetterSwingGUI extends JPanel {
	private class AllPagesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		public Class<?> getColumnClass(int arg0) {
			return arg0 == 0 ? Boolean.class : String.class;
		}

		public int getColumnCount() {
			return 2;
		}

		public String getColumnName(int arg0) {
			if (arg0 == 0)
				return "work";
			else
				return "Page name";
		}

		public int getRowCount() {
			return settings.size();
		}

		public Object getValueAt(int arg0, int arg1) {
			return arg1 == 0 ? settings.get(arg0).download
					: settings.get(arg0).name;
		}

		public boolean isCellEditable(int arg0, int arg1) {
			return true;
		}

		public void setValueAt(Object arg0, int arg1, int arg2) {
			Settings page = settings.get(arg1);
			if (arg2 == 0) {
				boolean value = (Boolean) arg0;
				if (value) {
					try {
						page.page.validate();
					} catch (Exception e) {
						return;
					}
				}
				page.download = value;
			} else
				page.name = (String) arg0;
		}
	}

	private class DownloadButtonListener implements ActionListener {
		public void actionPerformed(java.awt.event.ActionEvent e) {
			long time = System.currentTimeMillis();

			List<Page> pages = new ArrayList<Page>();

			for (Settings s : settings) {
				if (s == null || s.download == false || s.page == null
						|| s.page.getURL() == null
						|| s.page.getTargetLocation() == null)
					continue;

				pages.add(s.page);
			}

			if (pages.size() <= 0) {
				JOptionPane.showMessageDialog(me(),
						"Please choose some pages to work on!");
				return;
			}

			Page.updateAllFiles = getJCheckBoxDownloadAll().isSelected();
			Page.updateChangedFiles = getChckbxUpdatefiles().isSelected();

			final MagicResults result = Page.execute(
					pages.toArray(new Page[pages.size()]), true);
			
			String message = "Downloaded " + Page.downloadedFiles.get()
					+ " files (" + Page.downloadedBytes.get() + " Bytes)\n"
					+ "in " + (System.currentTimeMillis() - time)
					+ " milliseconds\n";
			
			for (Update update : result.updates) {
				message += "\n" + update.type + ": " + update.file;
			}
			
			message += "\n";

			Throwable[] thrown = result.thrown;
			
			if (thrown.length > 0)
				message += "\n" + thrown.length + " Error(s) occured:\n";

			for (Throwable t : thrown)
				message += "\n\t" + t.getMessage();
			
			System.out.println(message);

			JOptionPane.showMessageDialog(me(), message);
		}
	}

	private static final long serialVersionUID = 1L;
	private JSplitPane jSplitPane = null;
	private JScrollPane jScrollPane1 = null;
	private JPanel jPanelPageDetails = null;
	private JTextField jTextFieldURL = null;
	private JScrollPane jScrollPane2 = null;
	private JTextArea jTextAreaUserPwd = null;
	private JLabel jLabelSettingsLocationLabel = null;
	private JLabel jLabelSettingsLocationContent = null;
	private JButton jButtonChooseSettingsFile = null;
	private JPanel jPanelAllPages = null;
	private JButton jButtonNewPage = null;
	private JScrollPane jScrollPaneForAllPages = null;
	private JTable jTableAllPages = null;
	private JButton jButtonChooseTargetLocation = null;
	private JLabel jLabelTargetLocationContent = null;
	private String settingsLocation;
	private List<Settings> settings = new ArrayList<Settings>(); // @jve:decl-index=0:
	private JButton jButtonSaveSettings = null;

	private Page currentPage = null;
	private JButton jButtonRemovePage = null;
	private JButton jButtonDownload = null;
	private JCheckBox jCheckBoxDownloadAll = null;
	private JTextField jTextFieldPattern = null;
	private JCheckBox chckbxUpdatefiles;

	/**
	 * This is the default constructor
	 */
	public PDFGetterSwingGUI() {
		super();
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			// whatever
		}
		Page.setEncoder(new Base64Encoder() {
			public String encode(String in) {
				return Base64.encode(in.getBytes());
			}
		});
		initialize();
		Preferences prefs = Preferences.userNodeForPackage(getClass());
		settingsLocation = prefs.get("settingsLocation", null);
		loadSettings();
		undisplay();
		ToolTipManager.sharedInstance().setInitialDelay(0);
		ToolTipManager.sharedInstance().setDismissDelay(1000000);
	}

	private void loadSettings() {
		if (settingsLocation == null)
			return;
		jLabelSettingsLocationContent.setText(settingsLocation);
		Preferences.userNodeForPackage(getClass()).put("settingsLocation",
				settingsLocation);
		SettingsFile file;
		try {
			file = (SettingsFile) new ObjectInputStream(new FileInputStream(
					new File(settingsLocation))).readObject();

			settings = file.settings;
			getJCheckBoxDownloadAll().setSelected(file.update);
			getChckbxUpdatefiles().setSelected(file.updateChanged);
		} catch (FileNotFoundException e) {
			// eat
		} catch (IOException e) {
			exception(e);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		jTableAllPages.updateUI();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints81 = new GridBagConstraints();
		gridBagConstraints81.gridx = 2;
		gridBagConstraints81.gridwidth = 1;
		gridBagConstraints81.insets = new Insets(0, 5, 0, 5);
		gridBagConstraints81.gridy = 3;
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
		gridBagConstraints51.insets = new Insets(0, 0, 5, 5);
		gridBagConstraints51.gridx = 2;
		gridBagConstraints51.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints51.anchor = GridBagConstraints.WEST;
		gridBagConstraints51.ipady = 0;
		gridBagConstraints51.ipadx = 5;
		gridBagConstraints51.weightx = 1.0;
		gridBagConstraints51.gridy = 1;
		jLabelSettingsLocationContent = new JLabel("");
		GridBagConstraints gridBagConstraints41 = new GridBagConstraints();
		gridBagConstraints41.gridx = 0;
		gridBagConstraints41.gridy = 1;
		gridBagConstraints41.ipadx = 0;
		gridBagConstraints41.ipady = 0;
		gridBagConstraints41.insets = new Insets(5, 5, 5, 5);
		gridBagConstraints41.anchor = GridBagConstraints.WEST;
		jLabelSettingsLocationLabel = new JLabel("Settings Location");
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.insets = new Insets(0, 0, 5, 0);
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 1.0;
		gridBagConstraints.weighty = 1.0;
		gridBagConstraints.gridwidth = 5;
		gridBagConstraints.gridx = 0;
		this.setSize(515, 299);
		this.setLayout(new GridBagLayout());
		this.add(getJSplitPane(), gridBagConstraints);
		this.add(jLabelSettingsLocationLabel, gridBagConstraints41);
		this.add(jLabelSettingsLocationContent, gridBagConstraints51);
		this.add(getJButtonChooseSettingsFile(), gridBagConstraints61);
		this.add(getJButtonSaveSettings(), gridBagConstraints71);
		GridBagConstraints gbc_chckbxUpdatefiles = new GridBagConstraints();
		gbc_chckbxUpdatefiles.anchor = GridBagConstraints.WEST;
		gbc_chckbxUpdatefiles.gridwidth = 2;
		gbc_chckbxUpdatefiles.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxUpdatefiles.gridx = 0;
		gbc_chckbxUpdatefiles.gridy = 2;
		add(getChckbxUpdatefiles(), gbc_chckbxUpdatefiles);
		GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
		gridBagConstraints12.gridwidth = 2;
		gridBagConstraints12.insets = new Insets(0, 0, 0, 5);
		gridBagConstraints12.gridx = 0;
		gridBagConstraints12.anchor = GridBagConstraints.WEST;
		gridBagConstraints12.gridy = 3;
		this.add(getJCheckBoxDownloadAll(), gridBagConstraints12);
		this.add(getJButtonDownload(), gridBagConstraints81);
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
			jSplitPane.setLeftComponent(getJPanelAllPages());
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
			jScrollPane1.setViewportView(getJPanelPageDetails());
		}
		return jScrollPane1;
	}

	/**
	 * This method initializes jPanelPageDetails
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelPageDetails() {
		if (jPanelPageDetails == null) {
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
			JLabel jLabel8 = new JLabel("Search Pattern");
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints11.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.gridy = 4;
			jLabelTargetLocationContent = new JLabel("");
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
			JLabel jLabelTargetFolder = new JLabel("Target Folder");
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
			JLabel jLabel2 = new JLabel("Password(s)");
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 0;
			gridBagConstraints3.anchor = GridBagConstraints.NORTHWEST;
			gridBagConstraints3.ipadx = 0;
			gridBagConstraints3.ipady = 0;
			gridBagConstraints3.insets = new Insets(5, 5, 5, 5);
			gridBagConstraints3.gridy = 1;
			JLabel jLabel1 = new JLabel("URL");
			jPanelPageDetails = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			jPanelPageDetails.setLayout(gridBagLayout);
			jPanelPageDetails.add(jLabel1, gridBagConstraints3);
			jPanelPageDetails.add(jLabel2, gridBagConstraints4);
			jPanelPageDetails.add(getJTextFieldURL(), gridBagConstraints5);
			jPanelPageDetails.add(getJScrollPane2(), gridBagConstraints6);
			jPanelPageDetails.add(jLabelTargetFolder, gridBagConstraints7);
			jPanelPageDetails.add(getJButtonChooseTargetLocation(),
					gridBagConstraints9);
			jPanelPageDetails.add(jLabelTargetLocationContent,
					gridBagConstraints11);
			jPanelPageDetails.add(jLabel8, gridBagConstraints13);
			jPanelPageDetails.add(getJTextFieldPattern(), gridBagConstraints14);
		}
		return jPanelPageDetails;
	}

	/**
	 * This method initializes jTextFieldURL
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldURL() {
		if (jTextFieldURL == null) {
			jTextFieldURL = new JTextField();
			jTextFieldURL
					.setToolTipText("Enter the Address of the Website that links to the PDFs you'd like to download.");
			jTextFieldURL.addFocusListener(new java.awt.event.FocusAdapter() {
				public void focusLost(java.awt.event.FocusEvent e) {
					if (currentPage == null
							|| getJTextFieldURL().getText().equals(""))
						return;
					try {
						currentPage
								.setURL(new URL(getJTextFieldURL().getText()));
					} catch (MalformedURLException e1) {
						JOptionPane
								.showMessageDialog(me(),
										"The URL you entered seems to be malformatted and will not be saved.");
					}
				}
			});
		}
		return jTextFieldURL;
	}

	/**
	 * This method initializes jScrollPane2
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPane2() {
		if (jScrollPane2 == null) {
			jScrollPane2 = new JScrollPane();
			jScrollPane2.setViewportView(getJTextAreaUserPwd());
		}
		return jScrollPane2;
	}

	/**
	 * This method initializes jTextAreaUserPwd
	 * 
	 * @return javax.swing.JTextArea
	 */
	private JTextArea getJTextAreaUserPwd() {
		if (jTextAreaUserPwd == null) {
			jTextAreaUserPwd = new JTextArea();
			jTextAreaUserPwd.setPreferredSize(new Dimension(0, 50));
			jTextAreaUserPwd
					.setToolTipText("Please enter user/password pairs in the form user:password that I am supposed to try on this page, one per line.");
			jTextAreaUserPwd
					.addFocusListener(new java.awt.event.FocusAdapter() {
						public void focusLost(java.awt.event.FocusEvent e) {
							if (currentPage == null)
								return;

							currentPage.setUserpwd(jTextAreaUserPwd.getText());
						}
					});
		}
		return jTextAreaUserPwd;
	}

	/**
	 * This method initializes jButtonChooseSettingsFile
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonChooseSettingsFile() {
		if (jButtonChooseSettingsFile == null) {
			jButtonChooseSettingsFile = new JButton("Browse");
			jButtonChooseSettingsFile
					.setToolTipText("Click to select a settings file. If the file already exists, the settings stored in it will be loaded. I will remember the location and load the settings from the selected file the next time I'm started.");
			jButtonChooseSettingsFile
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							JFileChooser c = settingsLocation != null ? new JFileChooser(
									settingsLocation) : new JFileChooser();
							if (c.showSaveDialog(me()) != JFileChooser.APPROVE_OPTION)
								return;
							settingsLocation = c.getSelectedFile()
									.getAbsolutePath();
							loadSettings();
						}
					});
		}
		return jButtonChooseSettingsFile;
	}

	/**
	 * This method initializes jPanelAllPages
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJPanelAllPages() {
		if (jPanelAllPages == null) {
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.gridy = 0;
			JLabel jLabel7 = new JLabel("[HELP]");
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
			jPanelAllPages = new JPanel();
			GridBagLayout gridBagLayout = new GridBagLayout();
			jPanelAllPages.setLayout(gridBagLayout);
			jPanelAllPages.add(getJButtonNewPage(), gridBagConstraints1);
			jPanelAllPages
					.add(getJScrollPaneForAllPages(), gridBagConstraints8);
			jPanelAllPages.add(getJButtonRemovePage(), gridBagConstraints2);
			jPanelAllPages.add(jLabel7, gridBagConstraints10);
		}
		return jPanelAllPages;
	}

	/**
	 * This method initializes jButtonNewPage
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonNewPage() {
		if (jButtonNewPage == null) {
			jButtonNewPage = new JButton("New Page");
			jButtonNewPage
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							settings.add(new Settings());
							jTableAllPages.updateUI();
						}
					});
		}
		return jButtonNewPage;
	}

	/**
	 * This method initializes jScrollPaneForAllPages
	 * 
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getJScrollPaneForAllPages() {
		if (jScrollPaneForAllPages == null) {
			jScrollPaneForAllPages = new JScrollPane();
			jScrollPaneForAllPages.setViewportView(getJTableAllPages());
		}
		return jScrollPaneForAllPages;
	}

	/**
	 * This method initializes jTableAllPages
	 * 
	 * @return javax.swing.JTable
	 */
	private JTable getJTableAllPages() {
		if (jTableAllPages == null) {
			jTableAllPages = new JTable(new AllPagesTableModel());
			jTableAllPages
					.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			jTableAllPages.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			jTableAllPages.getSelectionModel().addListSelectionListener(
					new ListSelectionListener() {
						public void valueChanged(ListSelectionEvent arg0) {
							if (arg0.getValueIsAdjusting())
								return;
							if (jTableAllPages.getSelectedRow() >= 0)
								displayPage(jTableAllPages.getSelectedRow());
						}
					});
			jTableAllPages.getColumnModel().getColumn(0).setPreferredWidth(40);
			jTableAllPages.getColumnModel().getColumn(1).setPreferredWidth(110);
		}
		return jTableAllPages;
	}
	
	void setText(JLabel comp, Object object) {
		if(object == null)
			comp.setText("");
		else
			comp.setText(object.toString());
	}

	void setText(JTextComponent comp, Object object) {
		if(object == null)
			comp.setText("");
		else
			comp.setText(object.toString());
	}

	protected void displayPage(int firstIndex) {
		System.out.println("display(" + firstIndex + ")");
		if (settings.get(firstIndex).page == null)
			settings.get(firstIndex).page = new Page(null, null, null);

		final Page page = settings.get(firstIndex).page;
		
		setText(jTextFieldURL, page.getURL());
		setText(jTextAreaUserPwd, page.getUserpwd());
		setText(jLabelTargetLocationContent, page.getTargetLocation());
		setText(jTextFieldPattern, page.getPattern().toString());

		jTextFieldURL.setEditable(true);
		jTextAreaUserPwd.setEditable(true);
		jTextFieldPattern.setEditable(true);

		currentPage = settings.get(firstIndex).page;
	}

	Component me() {
		return this;
	}

	void exception(Exception e) {
		StringWriter sw = new StringWriter();

		e.printStackTrace(new PrintWriter(sw));
		JOptionPane.showMessageDialog(me(),
				e.getMessage() + "\n\n" + sw.toString(), "Exception",
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * This method initializes jButtonChooseTargetLocation
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonChooseTargetLocation() {
		if (jButtonChooseTargetLocation == null) {
			jButtonChooseTargetLocation = new JButton("Browse...");
			jButtonChooseTargetLocation
					.setToolTipText("browse for a folder to save the PDF Files from this page to");
			jButtonChooseTargetLocation
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							if (currentPage == null)
								return;
							JFileChooser c = currentPage.getTargetLocation() == null ? new JFileChooser()
									: new JFileChooser(currentPage
											.getTargetLocation());

							c.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

							if (c.showOpenDialog(me()) != JFileChooser.APPROVE_OPTION)
								return;

							currentPage.setTargetLocation(c.getSelectedFile());
							jLabelTargetLocationContent.setText(c
									.getSelectedFile().getAbsolutePath());
						}
					});
		}
		return jButtonChooseTargetLocation;
	}

	/**
	 * This method initializes jButtonSaveSettings
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonSaveSettings() {
		if (jButtonSaveSettings == null) {
			jButtonSaveSettings = new JButton("Save Settings");
			jButtonSaveSettings
					.setToolTipText("click to save your settings to the selected file");
			jButtonSaveSettings
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							if (settingsLocation == null
									|| settingsLocation.equals("")
									|| new File(settingsLocation).isDirectory())
								return;

							try {
								System.out.println("Serializing");
								ObjectOutputStream out;
								out = new ObjectOutputStream(
										new FileOutputStream(new File(
												settingsLocation)));
								out.writeObject(new SettingsFile(settings,
										getJCheckBoxDownloadAll().isSelected(),
										getChckbxUpdatefiles().isSelected()));
							} catch (Exception e1) {
								// eat
							}
						}
					});
		}
		return jButtonSaveSettings;
	}

	/**
	 * This method initializes jButtonRemovePage
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonRemovePage() {
		if (jButtonRemovePage == null) {
			jButtonRemovePage = new JButton("Remove Page");
			jButtonRemovePage
					.addActionListener(new java.awt.event.ActionListener() {
						public void actionPerformed(java.awt.event.ActionEvent e) {
							if (jTableAllPages.getSelectedRowCount() != 1)
								return;
							int selected = jTableAllPages.getSelectedRow();
							jTableAllPages
									.getSelectionModel()
									.removeSelectionInterval(selected, selected);
							settings.remove(selected);
							if (selected > 0)
								jTableAllPages.getSelectionModel()
										.setSelectionInterval(selected - 1,
												selected - 1);
							jTableAllPages.updateUI();
							undisplay();

							if (jTableAllPages.getSelectedRow() != -1)
								displayPage(jTableAllPages.getSelectedRow());
						}
					});
		}
		return jButtonRemovePage;
	}

	protected void undisplay() {
		jTextFieldURL.setEditable(false);
		jTextFieldURL.setText("");
		jTextFieldURL.updateUI();

		jTextFieldPattern.setEditable(false);
		jTextFieldPattern.setText("");
		jTextFieldPattern.updateUI();

		jTextAreaUserPwd.setEditable(false);
		jTextAreaUserPwd.setText("");
		jTextAreaUserPwd.updateUI();
	}

	/**
	 * This method initializes jButtonDownload
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getJButtonDownload() {
		if (jButtonDownload == null) {
			jButtonDownload = new JButton("DOWNLOAD PDFs");
			jButtonDownload.addActionListener(new DownloadButtonListener());
		}
		return jButtonDownload;
	}

	/**
	 * This method initializes jCheckBoxDownloadAll
	 * 
	 * @return javax.swing.JCheckBox
	 */
	private JCheckBox getJCheckBoxDownloadAll() {
		if (jCheckBoxDownloadAll == null) {
			jCheckBoxDownloadAll = new JCheckBox();
			jCheckBoxDownloadAll.setText("Update all files");
			jCheckBoxDownloadAll
					.setToolTipText("If checked, files will be downloaded and overwritten even if they already exist. Note that I will also check subfolders for existing files.");
		}
		return jCheckBoxDownloadAll;
	}

	/**
	 * This method initializes jTextFieldPattern
	 * 
	 * @return javax.swing.JTextField
	 */
	private JTextField getJTextFieldPattern() {
		if (jTextFieldPattern == null) {
			jTextFieldPattern = new JTextField();
			jTextFieldPattern
					.setToolTipText("I will only download Files that match this Regular Expression. If you have no Idea what a Regular Expression is, type .*\\.pdf or any file extension instead of pdf to match files with that extension.");
			jTextFieldPattern
					.addFocusListener(new java.awt.event.FocusAdapter() {
						public void focusLost(java.awt.event.FocusEvent e) {
							try {
								currentPage.setPattern(Pattern.compile(
										jTextFieldPattern.getText(),
										Pattern.CASE_INSENSITIVE));
							} catch (Exception e1) {
								JOptionPane.showMessageDialog(
										me(),
										"Invalid Regular Expression: "
												+ e1.getMessage()
												+ "\n\nTry again!");
							}
						}
					});
		}
		return jTextFieldPattern;
	}
	private JCheckBox getChckbxUpdatefiles() {
		if (chckbxUpdatefiles == null) {
			chckbxUpdatefiles = new JCheckBox("Update changed Files");
		}
		return chckbxUpdatefiles;
	}
} // @jve:decl-index=0:visual-constraint="10,10"

