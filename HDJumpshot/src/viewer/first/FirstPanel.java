
 /** Version Control Information $Id$
  * @lastmodified    $Date$
  * @modifiedby      $LastChangedBy$
  * @version         $Revision$ 
  */

//	Copyright (C) 2009 Julian M. Kunkel
//	
//	This file is part of HDJumpshot.
//	
//	HDJumpshot is free software: you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation, either version 3 of the License, or
//	(at your option) any later version.
//	
//	HDJumpshot is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with HDJumpshot.  If not, see <http://www.gnu.org/licenses/>.


/*
 *  (C) 2001 by Argonne National Laboratory
 *      See COPYRIGHT in top-level directory.
 */

/*
 *  @author Anthony Chan (Jumpshot 4), Julian M. Kunkel
 */

package viewer.first;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.event.ListDataListener;

import viewer.common.ActableTextField;
import viewer.common.Const;
import viewer.common.Dialogs;
import viewer.common.Routines;
import viewer.common.TopWindow;

public class FirstPanel extends JPanel {
	private static final long serialVersionUID = 8085293176219056520L;
	
	private static String about_str = "HDJumpshot.\n"	+ "bug-reports/questions:\n" + "            julian.kunkel@gmx.de";
	private static String manual_path = Const.DOC_PATH + "usersguide.html";
	private static String faq_path = Const.DOC_PATH + "faq_index.html";
	private static String js_icon_path = Const.IMG_PATH + "jumpshot.gif";

	private static String open_icon_path = Const.IMG_PATH + "Open24.gif";
	private static String close_icon_path = Const.IMG_PATH + "Stop24.gif";
	private static String legend_icon_path = Const.IMG_PATH	+ "Properties24.gif";
	private static String prefer_icon_path = Const.IMG_PATH	+ "Preferences24.gif";
	private static String manual_icon_path = Const.IMG_PATH + "Help24.gif";
	private static String faq_icon_path = Const.IMG_PATH + "Information24.gif";
	private static String about_icon_path = Const.IMG_PATH + "About24.gif";

	private ActableTextField logname_fld;
	private JComboBox additionalLoadedFilesBox;
	final private Vector<String> loadedFiles = new Vector<String>();

	/* some of these are hidden buttons */
	private JButton file_open_btn;
	private JButton file_add_btn;
	
	private JButton file_convert_btn;
	private JButton file_close_btn;
	private JButton show_timeline_btn;
	private JButton show_legend_btn;
	private JButton edit_prefer_btn;
	private JButton help_manual_btn;
	private JButton help_faq_btn;
	private JButton help_about_btn;

	private HTMLviewer manual_viewer;
	private HTMLviewer faq_viewer;

	private LogFileOperations file_ops;
	private String logfile_name;
	
	public FirstPanel(boolean isApplet, String filename, int view_idx) {
		super();
		super.setLayout(new BorderLayout());

		Border lowered_border, etched_border;
		lowered_border = BorderFactory.createLoweredBevelBorder();
		etched_border = BorderFactory.createEtchedBorder();

		file_ops = new LogFileOperations(isApplet);
		logfile_name = filename;

		Dimension row_pref_sz;
		Dimension lbl_pref_sz;
		Dimension fld_pref_sz;
		row_pref_sz = new Dimension(410, 27);
		lbl_pref_sz = new Dimension(110, 25);
		fld_pref_sz = new Dimension(row_pref_sz.width - lbl_pref_sz.width,
				lbl_pref_sz.height);

		// layout, main panel:
    final GridBagLayout gridbag = new GridBagLayout();
    final GridBagConstraints gConstraints = new GridBagConstraints();
    
		JPanel ctr_panel;
		ctr_panel = new JPanel();
		ctr_panel.setLayout(gridbag);

		JLabel label;
		gConstraints.fill = GridBagConstraints.BOTH;
		gConstraints.weightx = 1.0;
		
		label = new JLabel(" ProjectFile: ");
		Routines.setShortJComponentSizes(label, lbl_pref_sz);
		ctr_panel.add(label);
		logname_fld = new ActableTextField(logfile_name, 40);
		logname_fld.setBorder(BorderFactory.createCompoundBorder(
				lowered_border, etched_border));
		logname_fld.addActionListener(new LogNameTextFieldListener());

		Routines.setShortJComponentSizes(logname_fld, fld_pref_sz);
		ctr_panel.add(logname_fld);
		
		gConstraints.gridwidth = GridBagConstraints.REMAINDER;
		gridbag.setConstraints(logname_fld, gConstraints); 
		
		JLabel label2 = new JLabel(" Loaded projects: ");
		Routines.setShortJComponentSizes(label, lbl_pref_sz);
		ctr_panel.add(label2);
		additionalLoadedFilesBox = new JComboBox(new DefaultComboBoxModel(loadedFiles));		
		ctr_panel.add(additionalLoadedFilesBox);		
		gridbag.setConstraints(additionalLoadedFilesBox,  gConstraints);
		
		ctr_panel.setBorder(etched_border);

		super.add(ctr_panel, BorderLayout.CENTER);

		JToolBar toolbar;
		toolbar = createToolBarAndButtons(JToolBar.HORIZONTAL);
		super.add(toolbar, BorderLayout.SOUTH);

	}

	private JToolBar createToolBarAndButtons(int orientation) {
		JToolBar toolbar;
		toolbar = new JToolBar(orientation);
		toolbar.setFloatable(false);

		Insets btn_insets;
		btn_insets = new Insets(1, 1, 1, 1);

		// test existence of icons:
		if(! new File(open_icon_path).canRead()){
			throw new IllegalArgumentException("Image files seem not to exist (or readable) in path: " + new File(open_icon_path).getAbsolutePath());
		}
		
		
		file_open_btn = new JButton(new ImageIcon(open_icon_path));
		file_open_btn.setToolTipText("Open a new project file");
		file_open_btn.setMargin(btn_insets);
		file_open_btn.addActionListener(new FileSelectButtonListener());
		toolbar.add(file_open_btn);

		file_add_btn = new JButton(new ImageIcon(open_icon_path));
		file_add_btn.setToolTipText("Add another project file to current view");
		// file_select_btn.setBorder( empty_border );
		file_add_btn.setMargin(btn_insets);
		file_add_btn.addActionListener(new FileAddButtonListener());
		toolbar.add(file_add_btn);
		
		toolbar.addSeparator();
		show_legend_btn = new JButton(new ImageIcon(legend_icon_path));
		show_legend_btn.setToolTipText("Display the Legend window");
		// show_legend_btn.setBorder( empty_border );
		show_legend_btn.setMargin(btn_insets);
		show_legend_btn.addActionListener(new ShowLegendButtonListener());
		toolbar.add(show_legend_btn);

		show_timeline_btn = new JButton(new ImageIcon(legend_icon_path));
		show_timeline_btn.setToolTipText("Display the timeline window");
		show_timeline_btn.setMargin(btn_insets);
		show_timeline_btn.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				file_ops.showTimelineWindow();
			}
		});
		toolbar.add(show_timeline_btn);

		toolbar.addSeparator();

		edit_prefer_btn = new JButton(new ImageIcon(prefer_icon_path));
	
		edit_prefer_btn.setToolTipText("Open the Preference window");
		// edit_prefer_btn.setBorder( empty_border );
		edit_prefer_btn.setMargin(btn_insets);
		edit_prefer_btn.addActionListener(new EditPreferButtonListener());
		toolbar.add(edit_prefer_btn);

		toolbar.addSeparator();

		help_manual_btn = new JButton(new ImageIcon(manual_icon_path));

		help_manual_btn.setToolTipText("Open the user's manual window");
		// help_manual_btn.setBorder( empty_border );
		help_manual_btn.setMargin(btn_insets);
		help_manual_btn.addActionListener(new HelpManualButtonListener());
		toolbar.add(help_manual_btn);

		help_faq_btn = new JButton(new ImageIcon(faq_icon_path));
		
		help_faq_btn.setToolTipText("Open the FAQ window");
		// help_faq_btn.setBorder( empty_border );
		help_faq_btn.setMargin(btn_insets);
		help_faq_btn.addActionListener(new HelpFAQsButtonListener());
		toolbar.add(help_faq_btn);

		/* help_about_btn is a hidden button */
		help_about_btn = new JButton(new ImageIcon(about_icon_path));

		help_about_btn.setToolTipText("Open the About-This window");
		// help_about_btn.setBorder( empty_border );
		help_about_btn.setMargin(btn_insets);
		help_about_btn.addActionListener(new HelpAboutButtonListener());

		/* file_close_btn is a hidden button */
		
		file_close_btn = new JButton(new ImageIcon(close_icon_path));
		
		file_close_btn.setToolTipText("Close the logfile");
		// file_close_btn.setBorder( empty_border );
		file_close_btn.setMargin(btn_insets);
		file_close_btn.addActionListener(new FileCloseButtonListener());

		manual_viewer = new HTMLviewer("Manual", help_manual_btn);
		faq_viewer = new HTMLviewer("FAQs", help_faq_btn);

		return toolbar;
	}

	public void init() {
		file_ops.init();
		if (logfile_name != null)
			logname_fld.fireActionPerformed();
	}

	public JButton getLogFileSelectButton() {
		return file_open_btn;
	}
	
	public JButton getFileaddButton() {
		return file_add_btn;
	}

	public JButton getLogFileConvertButton() {
		return file_convert_btn;
	}

	public JButton getLogFileCloseButton() {
		return file_close_btn;
	}

	public JButton getShowLegendButton() {
		return show_legend_btn;
	}

	public JButton getShowTimelineButton() {
		return show_timeline_btn;
	}

	public JButton getEditPreferenceButton() {
		return edit_prefer_btn;
	}

	public JButton getHelpManualButton() {
		return help_manual_btn;
	}

	public JButton getHelpFAQsButton() {
		return help_faq_btn;
	}

	public JButton getHelpAboutButton() {
		return help_about_btn;
	}

	private class FileSelectButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final String filename = file_ops.selectLogFile();
			if (filename != null && filename.length() > 0) {
				file_ops.disposeLogFileAndResources();

				logname_fld.setText(filename);
				logname_fld.fireActionPerformed();		
			}
		}
	}
	
	private class FileAddButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final String filename = file_ops.selectLogFile();
			if (filename != null && filename.length() > 0) {
				if(loadedFiles.contains(filename)){
					Dialogs.info( TopWindow.First.getWindow(), "File is already loaded: " + filename, null);
					return;
				}
				
				try{
					file_ops.addLogFile(filename);
					loadedFiles.add(filename);
					additionalLoadedFilesBox.setSelectedItem(filename);
					
				}catch(Exception e){
					Dialogs.info( TopWindow.First.getWindow(), "Error while loading file: " + filename + "\n" +
							"Cause: " + e.getMessage(), null );
				}
			}
		}
	}

	private class LogNameTextFieldListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			final String filename = logname_fld.getText();
			file_ops.disposeLogFileAndResources();
			loadedFiles.clear();
			file_ops.openLogFile(filename);
			

			loadedFiles.add(filename);
			additionalLoadedFilesBox.setSelectedItem(filename);		
		}
	}

	private class ShowLegendButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			file_ops.showLegendWindow();
		}
	}

	private class EditPreferButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			file_ops.showPreferenceWindow();
		}
	}

	private class HelpManualButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try{
				manual_viewer.init(manual_path);
			
				manual_viewer.setVisible(true);
			}catch(Exception e){
				Dialogs.warn(TopWindow.First.getWindow(), "Cannot locate "
						+ manual_path + ".");
			}
		}
	}

	private class HelpFAQsButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			try{
				manual_viewer.init(faq_path);
			
				manual_viewer.setVisible(true);
			}catch(Exception e){
				Dialogs.warn(TopWindow.First.getWindow(), "Cannot locate "
						+ faq_path + ".");
			}
		}
	}

	private class HelpAboutButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			ImageIcon js_icon = new ImageIcon(js_icon_path);
			Dialogs.info(TopWindow.First.getWindow(), about_str, js_icon);
		}
	}

	private class FileCloseButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent evt) {
			file_ops.disposeLogFileAndResources();
			additionalLoadedFilesBox.removeAllItems();
			loadedFiles.clear();
		}
	}

}
