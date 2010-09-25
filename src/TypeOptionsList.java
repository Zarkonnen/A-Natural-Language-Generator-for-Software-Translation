import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Arrays;

public class TypeOptionsList extends JList {

	private MeaningRepresentation mr;
	private MRPanel displayPanel;
	private int selectedIndex;
	private JTextField literalInputField;
	//private ArrayList options;
	private String[] optionNames;
	private JLabel infoLabel;
	private JTextArea fieldInfoArea;
	private JTextArea nodeInfoArea;
	
	
	public TypeOptionsList(MeaningRepresentation mr, MRPanel displayPanel, JTextField literalInputField, JLabel infoLabel, JTextArea fieldInfoArea, JTextArea nodeInfoArea) {
		super();
		this.mr = mr;
		this.displayPanel = displayPanel;
		this.literalInputField = literalInputField;
		this.infoLabel = infoLabel;
		this.fieldInfoArea = fieldInfoArea;
		this.nodeInfoArea = nodeInfoArea;
		selectedIndex = -1;
	}
	
	public void clicked() {
		if (getSelectedIndex() == selectedIndex) {
			return;
		}
		
		selectedIndex = getSelectedIndex();
		
		//Root changing.
		if ((displayPanel.currentField == displayPanel.ROOT_FIELD) || (((displayPanel.currentNode.getParent() == null) && (displayPanel.currentField == null)))) {
			//MRNode root = new MRNode((VocabularyMeaning) options.get(selectedIndex), null);
			MRNode root = new MRNode(optionNames[selectedIndex], displayPanel.getDisplayLanguage(), null, "");
			mr.setRootNode(root);
			displayPanel.currentNode = root;
			displayPanel.currentField = null;
			displayPanel.repaint();
			return;
		}
		
		//Root attaching.
		/*if (displayPanel.currentNode.getParent() == null) {
			displayPanel.currentNode = displayPanel.currentNode.addChild(displayPanel.currentField.getName("en"), ((VocabularyMeaning) options.get(selectedIndex)).getName("en"), "en", "");
			displayPanel.repaint();
			return;
		}*/
		
		//Either fill in a new field or change an old one.
		if (displayPanel.currentField == null) {
			//Change an old one.
			MeaningField nodeField = displayPanel.currentNode.getParentField();
			MRNode parent = displayPanel.currentNode.getParent();
			parent.deleteChild(nodeField);
			displayPanel.currentNode = parent.addChild(nodeField.getName(displayPanel.getDisplayLanguage()), optionNames[selectedIndex], displayPanel.getDisplayLanguage(), "");
			//displayPanel.currentNode = parent.addChild(nodeField.getName("en"), ((VocabularyMeaning) options.get(selectedIndex)).getName("en"), "en", "");
			displayPanel.repaint();
		} else {
			//Fill in a new one.
			displayPanel.currentNode = displayPanel.currentNode.addChild(displayPanel.currentField.getName(displayPanel.getDisplayLanguage()), optionNames[selectedIndex], displayPanel.getDisplayLanguage(), "");
			//displayPanel.currentNode = displayPanel.currentNode.addChild(displayPanel.currentField.getName("en"), ((VocabularyMeaning) options.get(selectedIndex)).getName("en"), "en", "");
			displayPanel.currentField = null;
			displayPanel.repaint();
		}
	}
	
	public void deleteClicked() {
		if ((displayPanel.currentField == null) && (displayPanel.currentNode != null)) {
			literalInputField.setText("");
			setEnabled(true);
			literalInputField.setEnabled(false);
			
			MeaningField nodeField = displayPanel.currentNode.getParentField();
			MRNode parent = displayPanel.currentNode.getParent();
			if (parent == null) {
				//deleting root
				mr.clearRootNode();
				displayPanel.currentNode = null;
				displayPanel.currentField = displayPanel.ROOT_FIELD;
			} else {
				parent.deleteChild(nodeField);
				displayPanel.currentNode = parent;
			}
			displayPanel.repaint();
		}
	}
	
	/**
	 * This method is called when the user clicks on the main area.
	*/
	public void newPanelSelection() {
		selectedIndex = -1;
		setSelectedIndex(-1);
		
		//field info area
		if (displayPanel.currentNode != null) {
			if (displayPanel.currentField != null) {
				if (displayPanel.currentField.isLiteral()) {
					infoLabel.setText("enter a value");
					fieldInfoArea.setText("");
				} else {
					if (displayPanel.currentField.isAny()) {
						infoLabel.setText("select a new root node");
						fieldInfoArea.setText("The root node determines what kind of information you wish to convey. For example, selecting a \"statement\" root node allows you to make statements like \"This is where you configure the web browser.\".");
					} else {
						infoLabel.setText("select a " + displayPanel.currentField.getType().getName(displayPanel.getDisplayLanguage()));
						fieldInfoArea.setText(displayPanel.currentField.getInfoString(displayPanel.getDisplayLanguage()));
					}
				}
			} else {
				if (displayPanel.currentNode.getParent() == null) {
					infoLabel.setText("select a root node");
					fieldInfoArea.setText("The root node determines what kind of information you wish to convey. For example, selecting a \"statement\" root node allows you to make statements like \"This is where you configure the web browser.\".");
				} else {
					if (displayPanel.currentNode.isLiteral()) {
						infoLabel.setText("enter a value");
						fieldInfoArea.setText("");
					} else {
						if (displayPanel.currentNode.getParentField().isAny()) {
							infoLabel.setText("select a new root node");
							fieldInfoArea.setText("The root node determines what kind of information you wish to convey. For example, selecting a \"statement\" root node allows you to make statements like \"This is where you configure the web browser.\".");
						} else {
							infoLabel.setText("select a " + displayPanel.currentNode.getParentField().getType().getName(displayPanel.getDisplayLanguage()));
							fieldInfoArea.setText(displayPanel.currentNode.getParentField().getInfoString(displayPanel.getDisplayLanguage()));
						}
					}
				}
			}
		} else {
			if (displayPanel.currentField == displayPanel.ROOT_FIELD) {
					infoLabel.setText("select a root node");
					fieldInfoArea.setText("The root node determines what kind of information you wish to convey. For example, selecting a \"statement\" root node allows you to make statements like \"This is where you configure the web browser.\".");
			} else {
				infoLabel.setText("");
				fieldInfoArea.setText("");
			}
		}
		
		//node info area
		if ((displayPanel.currentField == null) && (displayPanel.currentNode != null)) {
			if (displayPanel.currentNode.isLiteral()) {
				nodeInfoArea.setText("enter a literal value");
			} else {
				nodeInfoArea.setText(displayPanel.currentNode.getType().getInfoString(displayPanel.getDisplayLanguage()));
			}
		} else {
			nodeInfoArea.setText("");
		}
		
		//selected uninstantiated literal
		if ((displayPanel.currentNode != null) && (displayPanel.currentField != null) && (displayPanel.currentField.isLiteral())) {
			setEnabled(false);
			literalInputField.setText("");
			literalInputField.setEnabled(true);
			String[] noVals = {};
			setListData(noVals);
			optionNames = noVals;
			return;
		}
		
		//selected instantiated literal
		if ((displayPanel.currentNode != null) && (displayPanel.currentNode.isLiteral())) {
			setEnabled(false);
			literalInputField.setText(displayPanel.currentNode.literalValue());
			literalInputField.setEnabled(true);
			String[] noVals = {};
			setListData(noVals);
			optionNames = noVals;
			return;
		}
		
		literalInputField.setText("");
		setEnabled(true);
		literalInputField.setEnabled(false);
	
		if ((displayPanel.currentNode == null) && !(displayPanel.currentField == displayPanel.ROOT_FIELD)) {
			//nothing selected
			String[] noVals = {};
			setListData(noVals);
			optionNames = noVals;
			return;
		}
		
		
		MeaningField field = displayPanel.currentField;
		if (displayPanel.currentField == null) {
			field = displayPanel.currentNode.getParentField();
		}

		ArrayList options = null;
		if ((field == null) || (field == displayPanel.ROOT_FIELD) || (field.isAny())) {
			//root selected
			options = VocabularyServer.getRootTypes();
		} else {
			options = VocabularyServer.getAllSubtypes(field.getType());
		}
		
		//convert options to name strings
		optionNames = new String[options.size()];
		for (int i = 0; i < optionNames.length; i++) {
			VocabularyMeaning option = (VocabularyMeaning) options.get(i);
			optionNames[i] = option.getName(displayPanel.getDisplayLanguage());
		}
		Arrays.sort(optionNames);
		setListData(optionNames);
		
		//If this is an already instantiated node, we select its type from the list.
		if ((displayPanel.currentField == null) && (displayPanel.currentNode != null)) {
			String currentTypeName = displayPanel.currentNode.getType().getName(displayPanel.getDisplayLanguage());
			for (int i = 0; i < optionNames.length; i++) {
				if (optionNames[i] == currentTypeName) {
					selectedIndex = i;
				}
			}
			setSelectedIndex(selectedIndex);
		} else {
			selectedIndex = -1;
			setSelectedIndex(-1);
		}
	}
	
	public void handleLitInpFieldChange() {
		//instantiating a new one?
		if ((displayPanel.currentNode != null) && (displayPanel.currentField != null) && (displayPanel.currentField.isLiteral())) {
			if (displayPanel.currentField.isInt()) {
				displayPanel.currentNode = displayPanel.currentNode.addChild(displayPanel.currentField.getName(displayPanel.getDisplayLanguage()), literalInputField.getText(), displayPanel.getDisplayLanguage(), "");
			} else {
				displayPanel.currentNode = displayPanel.currentNode.addChild(displayPanel.currentField.getName(displayPanel.getDisplayLanguage()), "\"" + literalInputField.getText() + "\"", displayPanel.getDisplayLanguage(), "");
			}
			displayPanel.currentField = null;
			displayPanel.repaint();
			return;
		}
	
		if ((displayPanel.currentNode != null) && (displayPanel.currentNode.isLiteral())) {
			displayPanel.currentNode.setLiteralValue(literalInputField.getText());
			displayPanel.repaint();
		}
	}
	
	public void setMR(MeaningRepresentation newMR) {
		mr = newMR;
	}	
}
