package de.tiller.PDFGetter;
import javax.swing.SwingUtilities;
import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.GridBagConstraints;
import java.awt.Insets;

public class GuiMain extends JFrame {

	private static final long serialVersionUID = 1L;
	private PDFGetterSwingGUI PDFGetterGUI = null;
	private JButton jButton = null;
	/**
	 * This method initializes PDFGetterSwingGUI	
	 * 	
	 * @return de.tiller.PDFGetter.PDFGetterSwingGUI	
	 */
	private PDFGetterSwingGUI getPDFGetterGUI() {
		if (PDFGetterGUI == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 3;
			gridBagConstraints.insets = new Insets(0, 0, 5, 5);
			gridBagConstraints.anchor = GridBagConstraints.SOUTHEAST;
			gridBagConstraints.gridy = 2;
			PDFGetterGUI = new PDFGetterSwingGUI();
			PDFGetterGUI.add(getJButton(), gridBagConstraints);
		}
		return PDFGetterGUI;
	}

	/**
	 * This method initializes jButton	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getJButton() {
		if (jButton == null) {
			jButton = new JButton();
			jButton.setText("Exit");
			jButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					dispose();
				}
			});
		}
		return jButton;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				GuiMain thisClass = new GuiMain();
				thisClass.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
				thisClass.setVisible(true);
			}
		});
	}

	/**
	 * This is the default constructor
	 */
	public GuiMain() {
		super();
		initialize();
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setSize(522, 314);
		this.setContentPane(getPDFGetterGUI());
		this.setTitle("PDFGetter");
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
