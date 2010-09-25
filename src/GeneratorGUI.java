import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.event.*;
import java.util.*;
import java.io.File;

public class GeneratorGUI extends JFrame {

	//The main panel of the app.
	private MRPanel vMPanel;
	
	//The list of options.
	private TypeOptionsList typeList;
	
	//The output.
	private OutputPanel outputPanel;
	
	//The MR we're manipulating.
	public MeaningRepresentation mr;
	
	/**
	 * Constructor for empty MR.
	*/
	public GeneratorGUI() {
		this(new MeaningRepresentation(false));
	}
	
	/**
	 * Constructor.
	*/
	public GeneratorGUI(MeaningRepresentation mr) {
		super("Natural Language Generator");
				
        // Respond to the user clicking 'close' on the frame
        this.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent evt) {
               System.exit(0);
            }
        });
		
		Container contentPane = getContentPane();
		
		this.mr = mr;
		
		//Create the GUI. First the main panel:
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setPreferredSize(new Dimension(800, 600));
		contentPane.add(mainPanel, BorderLayout.CENTER);
		
		//Now the meaning representation display panel.
		vMPanel = new MRPanel(mr);
		vMPanel.setPreferredSize(new Dimension(800, 450));
		//Clicking on the display panel:
		vMPanel.addMouseListener(
			new MouseAdapter() {
				public void mouseClicked(MouseEvent evt) {
					//System.out.println(evt.getX() + "/" + evt.getY());
					//vMPanel.handleMouseClick(evt.getX(), evt.getY());
					handlePanelMouseClick(evt.getX(), evt.getY());
				}
			}
		);
		//Add display panel to the main panel:
		mainPanel.add(vMPanel, BorderLayout.CENTER);
		
		//The info panel below the display panel.
		JPanel infoPanel = new JPanel();
		infoPanel.setLayout(new BorderLayout());
		infoPanel.setPreferredSize(new Dimension(800, 200));
		mainPanel.add(infoPanel, BorderLayout.SOUTH);
		
		//Field info area
		JTextArea fieldInfoArea = new JTextArea("");
		fieldInfoArea.setBorder(BorderFactory.createLineBorder(Color.black));
		fieldInfoArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
		fieldInfoArea.setPreferredSize(new Dimension(400, 150));
		fieldInfoArea.setLineWrap(true);
		fieldInfoArea.setEditable(false);
		fieldInfoArea.setWrapStyleWord(true);
		
		infoPanel.add(fieldInfoArea, BorderLayout.WEST);
		
		//Node info area
		JTextArea nodeInfoArea = new JTextArea("");
		nodeInfoArea.setBorder(BorderFactory.createLineBorder(Color.black));
		nodeInfoArea.setFont(new Font("Helvetica", Font.PLAIN, 12));
		nodeInfoArea.setPreferredSize(new Dimension(400, 150));
		nodeInfoArea.setLineWrap(true);
		nodeInfoArea.setEditable(false);
		nodeInfoArea.setWrapStyleWord(true);
		
		infoPanel.add(nodeInfoArea, BorderLayout.CENTER);
		
		//Now, the side panel.
		JPanel sideBar = new JPanel();
		sideBar.setPreferredSize(new Dimension(180, 600));
		sideBar.setLayout(new BorderLayout());
			//Info Label
			JLabel infoLabel = new JLabel("");
			infoLabel.setPreferredSize(new Dimension(170, 22));
		
			//Literals input field.
			JTextField litInpField = new JTextField();
			litInpField.getDocument().addDocumentListener(
				new DocumentListener() {
					public void insertUpdate(DocumentEvent e) {
						handleLitInpFieldChange();
					}
					public void removeUpdate(DocumentEvent e) {
						handleLitInpFieldChange();
					}
					public void changedUpdate(DocumentEvent e) {
						handleLitInpFieldChange();
					}
				}
			);
			
			litInpField.setEnabled(false);
			litInpField.setPreferredSize(new Dimension(170, 22));
		
		
			//Options List
			typeList = new TypeOptionsList(mr, vMPanel, litInpField, infoLabel, fieldInfoArea, nodeInfoArea);
			JScrollPane typeSP = new JScrollPane(typeList);
			typeSP.setPreferredSize(new Dimension(170, 500));
			//Clicking on the list:
			typeList.addMouseListener(
				new MouseAdapter() {
					public void mouseClicked(MouseEvent evt) {
						//System.out.println(typeList.getSelectedIndex());
						handleSideBarMouseClick();
					}
				}
			);
		
	
			
			//Delete Button
			JButton deleteButton = new JButton("Delete");
			deleteButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						deleteButtonPressed();
					}
				}
			);
			
			sideBar.add(infoLabel, BorderLayout.NORTH);
			sideBar.add(typeSP, BorderLayout.CENTER);
			
			//a small panel for handling the delete button and lit input field
			JPanel lowerSidebarPanel = new JPanel();
			lowerSidebarPanel.setLayout(new BorderLayout());
			sideBar.add(lowerSidebarPanel, BorderLayout.SOUTH);
			lowerSidebarPanel.add(litInpField, BorderLayout.CENTER);
			lowerSidebarPanel.add(deleteButton, BorderLayout.SOUTH);
			
		contentPane.add(sideBar, BorderLayout.WEST);
		
		//Now the output panel.
		outputPanel = new OutputPanel(mr);
		outputPanel.setPreferredSize(new Dimension(10, 40));
		outputPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
		mainPanel.add(outputPanel, BorderLayout.NORTH);
		//contentPane.add(outputPanel, BorderLayout.NORTH);
		
        // Set up the menu structure.  This skeleton code just defines
        // a "File" menu with "Print" and "Quit" options on it.
        JMenuBar menu_bar = new JMenuBar();
        JMenu file_menu = new JMenu("File");
		
		JMenuItem load_file_menu_item = new JMenuItem ("Load Meaning Representation");
		load_file_menu_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        load_file_menu_item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                loadFile();
            }
        });
		
		JMenuItem save_file_menu_item = new JMenuItem ("Save Meaning Representation");
		save_file_menu_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        save_file_menu_item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                saveFile();
            }
        });
		
		JMenuItem exit_menu_item = new JMenuItem ("Quit");
		exit_menu_item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
        exit_menu_item.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                System.exit(0);
            }
        });

        file_menu.add(load_file_menu_item);
        file_menu.add(save_file_menu_item);
        file_menu.add(exit_menu_item);
        menu_bar.add(file_menu);
        setJMenuBar(menu_bar);
		
		//Pack up the GUI.
		pack();
	}
	
	/*
	Action handlers:
	*/
	
	private void handlePanelMouseClick(int x, int y) {
		vMPanel.handleMouseClick(x, y);
		typeList.newPanelSelection();
	}
	
	private void handleSideBarMouseClick() {
		typeList.clicked();
		outputPanel.repaint();
	}
	
	private void deleteButtonPressed() {
		typeList.deleteClicked();
		outputPanel.repaint();
	}
	
	private void handleLitInpFieldChange() {
		typeList.handleLitInpFieldChange();
		outputPanel.repaint();
	}
	
	private void loadFile() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File f = chooser.getSelectedFile();
			try {
				MeaningRepresentation newMR = new MeaningRepresentation(f.getAbsolutePath());
				setMRGlobally(newMR);
			}
			catch (Exception e) {
				System.err.println("Could not read file; " + e.toString());
			}
		}
	}
	
	private void saveFile() {
		JFileChooser chooser = new JFileChooser();
		if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
			mr.saveTo(chooser.getSelectedFile());
		}
	}
	
	private void setMRGlobally(MeaningRepresentation newMR) {
		mr = newMR;
		outputPanel.setMR(newMR);
		typeList.setMR(newMR);
		vMPanel.setMR(newMR);
		vMPanel.repaint();
		outputPanel.repaint();
		typeList.newPanelSelection();
	}
	

}
