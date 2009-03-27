
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

package viewer.common;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import drawable.StateBorder;


public class PreferencePanel extends JPanel
{
    private static int                    VERTICAL_GAP_HEIGHT = 10;

    // Options: Zoomable window reinitialization (requires window restart)
    private        LabeledTextField       fld_Y_AXIS_ROOT_LABEL;
    private        LabeledTextField       fld_INIT_SLOG2_LEVEL_READ;
    private        LabeledComboBox        lst_AUTO_WINDOWS_LOCATION;
    private        LabeledFloatSlider     sdr_SCREEN_HEIGHT_RATIO;
    private        LabeledFloatSlider     sdr_TIME_SCROLL_UNIT_RATIO;

    // Options: All zoomable windows
    private        LabeledComboBox        lst_Y_AXIS_ROOT_VISIBLE;
    private        LabeledComboBox        lst_ACTIVE_REFRESH;
    private        LabeledComboBox        lst_BACKGROUND_COLOR;
    // private        LabeledTextField       fld_Y_AXIS_ROW_HEIGHT;

    private        LabeledFloatSlider     sdr_STATE_HEIGHT_FACTOR;
    private        LabeledFloatSlider     sdr_NESTING_HEIGHT_FACTOR;
    private        LabeledComboBox        lst_ARROW_ANTIALIASING;
    private        LabeledTextField       fld_MIN_WIDTH_TO_DRAG;
    private        LabeledComboBox        lst_LEFTCLICK_INSTANT_ZOOM;

    // Options: Timeline zoomable window
    private        LabeledComboBox        lst_STATE_BORDER;
    private        LabeledTextField       fld_ARROW_HEAD_LENGTH;
    private        LabeledTextField       fld_ARROW_HEAD_WIDTH;
    private        LabeledTextField       fld_EVENT_BASE_WIDTH;

    private        LabeledTextField       fld_SEARCH_ARROW_LENGTH;
    private        LabeledTextField       fld_SEARCH_FRAME_THICKNESS;
    private        LabeledComboBox        lst_SEARCHED_OBJECT_ON_TOP;

    // Options: Histogram zoomable window
    private        LabeledComboBox        lst_HISTOGRAM_ZERO_ORIGIN;
    private        LabeledComboBox        lst_SUMMARY_STATE_BORDER;
    private        LabeledTextField       fld_SUMMARY_ARROW_LOG_BASE;


    public PreferencePanel()
    {
        super();
        super.setLayout( new BoxLayout( this, BoxLayout.Y_AXIS ) );

        JPanel label_panel;
        JLabel label;

        /*  Options: Zoomable window reinitialization             */

        label_panel = new JPanel();
        label_panel.setLayout( new BoxLayout( label_panel, BoxLayout.X_AXIS ) );
            label = new JLabel( "Zoomable window reinitialization" );
            label.setToolTipText( "Options become effective after "
                                + "the zoomable window is restarted" );
        label_panel.add( Box.createHorizontalStrut( Const.LABEL_INDENTATION ) );
        label_panel.add( label );
        label_panel.add( Box.createHorizontalGlue() );
        label_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        super.add( label_panel );

        fld_Y_AXIS_ROOT_LABEL = new LabeledTextField( true,
                                    "Y_AXIS_ROOT_LABEL",
                                    Const.STRING_FORMAT );
        fld_Y_AXIS_ROOT_LABEL.setToolTipText(
        "Label for the root node of the Y-axis tree label in the left panel" );
        fld_Y_AXIS_ROOT_LABEL.setHorizontalAlignment( JTextField.CENTER );
        fld_Y_AXIS_ROOT_LABEL.addSelfDocumentListener();
        fld_Y_AXIS_ROOT_LABEL.setEditable( true );
        super.add( fld_Y_AXIS_ROOT_LABEL );

        fld_INIT_SLOG2_LEVEL_READ = new LabeledTextField( true,
                                        "INIT_SLOG2_LEVEL_READ",
                                        Const.SHORT_FORMAT );
        fld_INIT_SLOG2_LEVEL_READ.setToolTipText(
          "The number of SLOG-2 levels being read into memory when "
        + "timeline window is initialized, the number affects the "
        + "zooming and scrolling performance exponentially (in a "
        + "asymptotical sense)." );
        fld_INIT_SLOG2_LEVEL_READ.setHorizontalAlignment( JTextField.CENTER );
        fld_INIT_SLOG2_LEVEL_READ.addSelfDocumentListener();
        fld_INIT_SLOG2_LEVEL_READ.setEditable( true );
        super.add( fld_INIT_SLOG2_LEVEL_READ );

        lst_AUTO_WINDOWS_LOCATION = new LabeledComboBox(
                                        "AUTO_WINDOWS_LOCATION" );
        lst_AUTO_WINDOWS_LOCATION.addItem( Boolean.TRUE );
        lst_AUTO_WINDOWS_LOCATION.addItem( Boolean.FALSE );
        lst_AUTO_WINDOWS_LOCATION.setToolTipText(
        "Whether to let Jumpshot-4 automatically set windows placement." );
        super.add( lst_AUTO_WINDOWS_LOCATION );

        super.add( Box.createVerticalStrut( VERTICAL_GAP_HEIGHT ) );

        sdr_SCREEN_HEIGHT_RATIO = new LabeledFloatSlider(
                                      "SCREEN_HEIGHT_RATIO",
                                      0.0f, 1.0f );
        sdr_SCREEN_HEIGHT_RATIO.setToolTipText(
        "Ratio of the initial timeline canvas height to the screen height.");
        sdr_SCREEN_HEIGHT_RATIO.setHorizontalAlignment( JTextField.CENTER );
        sdr_SCREEN_HEIGHT_RATIO.setEditable( true );
        super.add( sdr_SCREEN_HEIGHT_RATIO );

        sdr_TIME_SCROLL_UNIT_RATIO = new LabeledFloatSlider(
                                         "TIME_SCROLL_UNIT_RATIO",
                                         0.0f, 1.0f );
        sdr_TIME_SCROLL_UNIT_RATIO.setToolTipText(
          "Unit increment of the horizontal scrollbar in the fraction of "
        + "timeline canvas's width." );
        sdr_TIME_SCROLL_UNIT_RATIO.setHorizontalAlignment( JTextField.CENTER );
        sdr_TIME_SCROLL_UNIT_RATIO.setEditable( true );
        super.add( sdr_TIME_SCROLL_UNIT_RATIO );

        super.add( Box.createVerticalStrut( 2 * VERTICAL_GAP_HEIGHT ) );

        /*  Options: All zoomable windows                         */

        label_panel = new JPanel();
        label_panel.setLayout( new BoxLayout( label_panel, BoxLayout.X_AXIS ) );
            label = new JLabel( "All zoomable windows" );
            label.setToolTipText( "Options become effective after return "
                                + "and the zoomable window is redrawn" );
        label_panel.add( Box.createHorizontalStrut( Const.LABEL_INDENTATION ) );
        label_panel.add( label );
        label_panel.add( Box.createHorizontalGlue() );
        label_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        super.add( label_panel );

        lst_Y_AXIS_ROOT_VISIBLE = new LabeledComboBox( "Y_AXIS_ROOT_VISIBLE" );
        lst_Y_AXIS_ROOT_VISIBLE.addItem( Boolean.TRUE );
        lst_Y_AXIS_ROOT_VISIBLE.addItem( Boolean.FALSE );
        lst_Y_AXIS_ROOT_VISIBLE.setToolTipText(
        "Whether to show the top of the Y-axis tree-styled directory label." );
        super.add( lst_Y_AXIS_ROOT_VISIBLE );

        //  Temporary disable it as this field is not being used
        lst_ACTIVE_REFRESH = new LabeledComboBox( "ACTIVE_REFRESH" );
        lst_ACTIVE_REFRESH.addItem( Boolean.TRUE );
        lst_ACTIVE_REFRESH.addItem( Boolean.FALSE );
        lst_ACTIVE_REFRESH.setToolTipText(
        "Whether to let Jumpshot-4 actively update the timeline canvas." );
        super.add( lst_ACTIVE_REFRESH );
        lst_ACTIVE_REFRESH.setEnabled( false );

        lst_BACKGROUND_COLOR = new LabeledComboBox( "BACKGROUND_COLOR" );
        lst_BACKGROUND_COLOR.addItem( Const.COLOR_BLACK );
        lst_BACKGROUND_COLOR.addItem( Const.COLOR_DARKGRAY );
        lst_BACKGROUND_COLOR.addItem( Const.COLOR_GRAY );
        lst_BACKGROUND_COLOR.addItem( Const.COLOR_LIGHTGRAY );
        lst_BACKGROUND_COLOR.addItem( Const.COLOR_WHITE );
        lst_BACKGROUND_COLOR.setToolTipText(
        "Background color of the timeline canvas" );
        super.add( lst_BACKGROUND_COLOR );

        /*
        fld_Y_AXIS_ROW_HEIGHT = new LabeledTextField( true,
                                    "Y_AXIS_ROW_HEIGHT",
                                    Const.INTEGER_FORMAT );
        fld_Y_AXIS_ROW_HEIGHT.setToolTipText(
        "Row height of Y-axis tree in pixel, i.e. height for each timeline." );
        fld_Y_AXIS_ROW_HEIGHT.setHorizontalAlignment( JTextField.CENTER );;
        fld_Y_AXIS_ROW_HEIGHT.addSelfDocumentListener();
        fld_Y_AXIS_ROW_HEIGHT.setEditable( true );
        super.add( fld_Y_AXIS_ROW_HEIGHT );
        */

        super.add( Box.createVerticalStrut( VERTICAL_GAP_HEIGHT ) );

        sdr_STATE_HEIGHT_FACTOR = new LabeledFloatSlider(
                                      "STATE_HEIGHT_FACTOR",
                                      0.0f, 1.0f );
        sdr_STATE_HEIGHT_FACTOR.setToolTipText(
          "Ratio of the outermost rectangle height to the row height. The "
        + "larger the factor is, the larger the outermost rectangle will "
        + "be with respect to the row height." );
        sdr_STATE_HEIGHT_FACTOR.setHorizontalAlignment( JTextField.CENTER );
        sdr_STATE_HEIGHT_FACTOR.setEditable( true );
        super.add( sdr_STATE_HEIGHT_FACTOR );

        sdr_NESTING_HEIGHT_FACTOR = new LabeledFloatSlider(
                                        "NESTING_HEIGHT_FACTOR",
                                        0.0f, 1.0f );
        sdr_NESTING_HEIGHT_FACTOR.setToolTipText(
          "The gap ratio between successive nesting rectangles. The "
        + "larger the factor is, the smaller the gap will be." );
        sdr_NESTING_HEIGHT_FACTOR.setHorizontalAlignment( JTextField.CENTER );
        sdr_NESTING_HEIGHT_FACTOR.setEditable( true );
        super.add( sdr_NESTING_HEIGHT_FACTOR );

        lst_ARROW_ANTIALIASING = new LabeledComboBox( "ARROW_ANTIALIASING" );
        lst_ARROW_ANTIALIASING.addItem( Const.ANTIALIAS_DEFAULT );
        lst_ARROW_ANTIALIASING.addItem( Const.ANTIALIAS_OFF );
        lst_ARROW_ANTIALIASING.addItem( Const.ANTIALIAS_ON );
        lst_ARROW_ANTIALIASING.setToolTipText(
          "Whether to draw arrow with antialiasing lines. Turning this on "
        + "will slow down the canvas drawing by a factor of ~3" );
        super.add( lst_ARROW_ANTIALIASING );

        fld_MIN_WIDTH_TO_DRAG = new LabeledTextField( true,
                                    "MIN_WIDTH_TO_DRAG",
                                    Const.INTEGER_FORMAT );
        fld_MIN_WIDTH_TO_DRAG.setToolTipText(
        "Minimum width in pixel to be considered a dragged operation." );
        fld_MIN_WIDTH_TO_DRAG.setHorizontalAlignment( JTextField.CENTER );
        fld_MIN_WIDTH_TO_DRAG.addSelfDocumentListener();
        fld_MIN_WIDTH_TO_DRAG.setEditable( true );
        super.add( fld_MIN_WIDTH_TO_DRAG );

        lst_LEFTCLICK_INSTANT_ZOOM = new LabeledComboBox(
                                         "LEFTCLICK_INSTANT_ZOOM" );
        lst_LEFTCLICK_INSTANT_ZOOM.addItem( Boolean.TRUE );
        lst_LEFTCLICK_INSTANT_ZOOM.addItem( Boolean.FALSE );
        lst_LEFTCLICK_INSTANT_ZOOM.setToolTipText(
        "Whether to zoom in immediately after left mouse click on canvas." );
        super.add( lst_LEFTCLICK_INSTANT_ZOOM );

        super.add( Box.createVerticalStrut( 2 * VERTICAL_GAP_HEIGHT ) );

        /*  Options: Timeline zoomable window                     */

        label_panel = new JPanel();
        label_panel.setLayout( new BoxLayout( label_panel, BoxLayout.X_AXIS ) );
            label = new JLabel( "Timeline zoomable window" );
            label.setToolTipText( "Options become effective after return "
                                + "and the Timeline window is redrawn" );
        label_panel.add( Box.createHorizontalStrut( Const.LABEL_INDENTATION ) );
        label_panel.add( label );
        label_panel.add( Box.createHorizontalGlue() );
        label_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        super.add( label_panel );

        lst_STATE_BORDER = new LabeledComboBox( "STATE_BORDER" );
        lst_STATE_BORDER.addItem( StateBorder.COLOR_RAISED_BORDER );
        lst_STATE_BORDER.addItem( StateBorder.COLOR_LOWERED_BORDER );
        lst_STATE_BORDER.addItem( StateBorder.WHITE_RAISED_BORDER );
        lst_STATE_BORDER.addItem( StateBorder.WHITE_LOWERED_BORDER );
        lst_STATE_BORDER.addItem( StateBorder.WHITE_PLAIN_BORDER );
        lst_STATE_BORDER.addItem( StateBorder.EMPTY_BORDER );
        lst_STATE_BORDER.setToolTipText( "Border style of real states" );
        super.add( lst_STATE_BORDER );

        fld_ARROW_HEAD_LENGTH = new LabeledTextField( true,
                                    "ARROW_HEAD_LENGTH",
                                    Const.INTEGER_FORMAT );
        fld_ARROW_HEAD_LENGTH.setToolTipText(
        "Length of the arrow head in pixel." );
        fld_ARROW_HEAD_LENGTH.setHorizontalAlignment( JTextField.CENTER );
        fld_ARROW_HEAD_LENGTH.addSelfDocumentListener();
        fld_ARROW_HEAD_LENGTH.setEditable( true );
        super.add( fld_ARROW_HEAD_LENGTH );

        fld_ARROW_HEAD_WIDTH = new LabeledTextField( true,
                                   "ARROW_HEAD_WIDTH",
                                   Const.INTEGER_FORMAT );
        fld_ARROW_HEAD_WIDTH.setToolTipText(
        "Width of the arrow head's base in pixel(Even number)." );
        fld_ARROW_HEAD_WIDTH.setHorizontalAlignment( JTextField.CENTER );
        fld_ARROW_HEAD_WIDTH.addSelfDocumentListener();
        fld_ARROW_HEAD_WIDTH.setEditable( true );
        super.add( fld_ARROW_HEAD_WIDTH );

        fld_EVENT_BASE_WIDTH = new LabeledTextField( true,
                                   "EVENT_BASE_WIDTH",
                                   Const.INTEGER_FORMAT );
        fld_EVENT_BASE_WIDTH.setToolTipText(
        "Width of the event triangle's base in pixel(Even number)." );
        fld_EVENT_BASE_WIDTH.setHorizontalAlignment( JTextField.CENTER );
        fld_EVENT_BASE_WIDTH.addSelfDocumentListener();
        fld_EVENT_BASE_WIDTH.setEditable( true );
        super.add( fld_EVENT_BASE_WIDTH );

        super.add( Box.createVerticalStrut( VERTICAL_GAP_HEIGHT ) );
        
        super.add( Box.createVerticalStrut( VERTICAL_GAP_HEIGHT ) );

        fld_SEARCH_ARROW_LENGTH = new LabeledTextField( true,
                                      "SEARCH_ARROW_LENGTH",
                                      Const.INTEGER_FORMAT );
        fld_SEARCH_ARROW_LENGTH.setToolTipText(
        "Length of the search marker's arrow in pixel" );
        fld_SEARCH_ARROW_LENGTH.setHorizontalAlignment( JTextField.CENTER );
        fld_SEARCH_ARROW_LENGTH.addSelfDocumentListener();
        fld_SEARCH_ARROW_LENGTH.setEditable( true );
        super.add( fld_SEARCH_ARROW_LENGTH );

        fld_SEARCH_FRAME_THICKNESS = new LabeledTextField( true,
                                         "SEARCH_FRAME_THICKNESS",
                                         Const.INTEGER_FORMAT );
        fld_SEARCH_FRAME_THICKNESS.setToolTipText(
          "Thickness in pixel of the popup frame that hightlights "
        + "the searched drawable" );
        fld_SEARCH_FRAME_THICKNESS.setHorizontalAlignment( JTextField.CENTER );
        fld_SEARCH_FRAME_THICKNESS.addSelfDocumentListener();
        fld_SEARCH_FRAME_THICKNESS.setEditable( true );
        super.add( fld_SEARCH_FRAME_THICKNESS );

        lst_SEARCHED_OBJECT_ON_TOP = new LabeledComboBox(
                                         "SEARCHED_OBJECT_ON_TOP" );
        lst_SEARCHED_OBJECT_ON_TOP.addItem( Boolean.TRUE );
        lst_SEARCHED_OBJECT_ON_TOP.addItem( Boolean.FALSE );
        lst_SEARCHED_OBJECT_ON_TOP.setToolTipText(
        "Whether to display the searched object on top of the search frame." );
        super.add( lst_SEARCHED_OBJECT_ON_TOP );

        super.add( Box.createVerticalStrut( 2 * VERTICAL_GAP_HEIGHT ) );

        /*  Options: Histogram zoomable window                    */

        label_panel = new JPanel();
        label_panel.setLayout( new BoxLayout( label_panel, BoxLayout.X_AXIS ) );
            label = new JLabel( "Histogram zoomable window" );
            label.setToolTipText( "Options become effective after return "
                                + "and the Histogram window is redrawn" );
        label_panel.add( Box.createHorizontalStrut( Const.LABEL_INDENTATION ) );
        label_panel.add( label );
        label_panel.add( Box.createHorizontalGlue() );
        label_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        super.add( label_panel );

        lst_HISTOGRAM_ZERO_ORIGIN = new LabeledComboBox(
                                        "HISTOGRAM_ZERO_ORIGIN" );
        lst_HISTOGRAM_ZERO_ORIGIN.addItem( Boolean.TRUE );
        lst_HISTOGRAM_ZERO_ORIGIN.addItem( Boolean.FALSE );
        lst_HISTOGRAM_ZERO_ORIGIN.setToolTipText(
        "Whether to the time ruler is in duration, i.e. starts with 0.0." );
        super.add( lst_HISTOGRAM_ZERO_ORIGIN );

        lst_SUMMARY_STATE_BORDER = new LabeledComboBox(
                                       "SUMMARY_STATE_BORDER" );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.COLOR_XOR_BORDER );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.COLOR_RAISED_BORDER );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.COLOR_LOWERED_BORDER );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.WHITE_RAISED_BORDER );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.WHITE_LOWERED_BORDER );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.WHITE_PLAIN_BORDER );
        lst_SUMMARY_STATE_BORDER.addItem( StateBorder.EMPTY_BORDER );
        lst_SUMMARY_STATE_BORDER.setToolTipText(
        "Border style of the Summary state in the histogram window." );
        super.add( lst_SUMMARY_STATE_BORDER );

        fld_SUMMARY_ARROW_LOG_BASE = new LabeledTextField( true,
                                         "SUMMARY_ARROW_LOG_BASE",
                                         Const.INTEGER_FORMAT );
        fld_SUMMARY_ARROW_LOG_BASE.setToolTipText(
          "The logarithmic base of the number of arrows in Summary arrow.\n"
        + "This determines the Summary arrow's width." );
        fld_SUMMARY_ARROW_LOG_BASE.setHorizontalAlignment( JTextField.CENTER );
        fld_SUMMARY_ARROW_LOG_BASE.addSelfDocumentListener();
        fld_SUMMARY_ARROW_LOG_BASE.setEditable( true );
        super.add( fld_SUMMARY_ARROW_LOG_BASE );

        super.add( Box.createVerticalStrut( 2 * VERTICAL_GAP_HEIGHT ) );

        /*  Options: Legend window                                */

        label_panel = new JPanel();
        label_panel.setLayout( new BoxLayout( label_panel, BoxLayout.X_AXIS ) );
            label = new JLabel( "Legend window" );
            label.setToolTipText( "Options become effective after return "
                                + "and the Legend window is redrawn" );
        label_panel.add( Box.createHorizontalStrut( Const.LABEL_INDENTATION ) );
        label_panel.add( label );
        label_panel.add( Box.createHorizontalGlue() );
        label_panel.setAlignmentX( Component.LEFT_ALIGNMENT );
        super.add( label_panel );

        super.add( Box.createVerticalStrut( VERTICAL_GAP_HEIGHT ) );

        super.setBorder( BorderFactory.createEtchedBorder() );
    }

    public void updateAllFieldsFromParameters()
    {
        // Options: Zoomable window reinitialization (requires window restart)
        fld_Y_AXIS_ROOT_LABEL.setText( Parameters.Y_AXIS_ROOT_LABEL );
        fld_INIT_SLOG2_LEVEL_READ.setShort( Parameters.INIT_SLOG2_LEVEL_READ );
        lst_AUTO_WINDOWS_LOCATION.setSelectedBooleanItem(
                                  Parameters.AUTO_WINDOWS_LOCATION );
        sdr_SCREEN_HEIGHT_RATIO.setFloat( Parameters.SCREEN_HEIGHT_RATIO );
        sdr_TIME_SCROLL_UNIT_RATIO.setFloat(
                                   Parameters.TIME_SCROLL_UNIT_RATIO );

        // Options: All zoomable windows
        lst_Y_AXIS_ROOT_VISIBLE.setSelectedBooleanItem(
                                Parameters.Y_AXIS_ROOT_VISIBLE );
        lst_ACTIVE_REFRESH.setSelectedBooleanItem( Parameters.ACTIVE_REFRESH );
        lst_BACKGROUND_COLOR.setSelectedItem( Parameters.BACKGROUND_COLOR );
        // fld_Y_AXIS_ROW_HEIGHT.setInteger( Parameters.Y_AXIS_ROW_HEIGHT );

        sdr_STATE_HEIGHT_FACTOR.setFloat( Parameters.STATE_HEIGHT_FACTOR );
        sdr_NESTING_HEIGHT_FACTOR.setFloat( Parameters.NESTING_HEIGHT_FACTOR );
        lst_ARROW_ANTIALIASING.setSelectedItem( Parameters.ARROW_ANTIALIASING );
        fld_MIN_WIDTH_TO_DRAG.setInteger( Parameters.MIN_WIDTH_TO_DRAG );
        lst_LEFTCLICK_INSTANT_ZOOM.setSelectedBooleanItem(
                                   Parameters.LEFTCLICK_INSTANT_ZOOM );

        // Options: Timeline zoomable window
        lst_STATE_BORDER.setSelectedItem( Parameters.STATE_BORDER );
        fld_ARROW_HEAD_LENGTH.setInteger( Parameters.ARROW_HEAD_LENGTH );
        fld_ARROW_HEAD_WIDTH.setInteger( Parameters.ARROW_HEAD_WIDTH );
        fld_EVENT_BASE_WIDTH.setInteger( Parameters.EVENT_BASE_WIDTH );

        fld_SEARCH_ARROW_LENGTH.setInteger( Parameters.SEARCH_ARROW_LENGTH );
        fld_SEARCH_FRAME_THICKNESS.setInteger(
                                   Parameters.SEARCH_FRAME_THICKNESS );
        lst_SEARCHED_OBJECT_ON_TOP.setSelectedBooleanItem(
                                   Parameters.SEARCHED_OBJECT_ON_TOP );

        // Options: Histogram zoomable window
        lst_HISTOGRAM_ZERO_ORIGIN.setSelectedBooleanItem(
                                  Parameters.HISTOGRAM_ZERO_ORIGIN );
        lst_SUMMARY_STATE_BORDER.setSelectedItem(
                                 Parameters.SUMMARY_STATE_BORDER );
        fld_SUMMARY_ARROW_LOG_BASE.setInteger(
                                   Parameters.SUMMARY_ARROW_LOG_BASE );

    }

    public void updateAllParametersFromFields()
    {
        // Options: Zoomable window reinitialization (requires window restart)
        Parameters.Y_AXIS_ROOT_LABEL
                  = fld_Y_AXIS_ROOT_LABEL.getText();
        Parameters.INIT_SLOG2_LEVEL_READ
                  = fld_INIT_SLOG2_LEVEL_READ.getShort();
        Parameters.AUTO_WINDOWS_LOCATION
                  = lst_AUTO_WINDOWS_LOCATION.getSelectedBooleanItem();
        Parameters.SCREEN_HEIGHT_RATIO
                  = sdr_SCREEN_HEIGHT_RATIO.getFloat();
        Parameters.TIME_SCROLL_UNIT_RATIO
                  = sdr_TIME_SCROLL_UNIT_RATIO.getFloat();

        // Options: All zoomable windows
        Parameters.Y_AXIS_ROOT_VISIBLE
                  = lst_Y_AXIS_ROOT_VISIBLE.getSelectedBooleanItem();
        Parameters.ACTIVE_REFRESH
                  = lst_ACTIVE_REFRESH.getSelectedBooleanItem();
        Parameters.BACKGROUND_COLOR
                  = (Alias) lst_BACKGROUND_COLOR.getSelectedItem();
        // Parameters.Y_AXIS_ROW_HEIGHT
        //           = fld_Y_AXIS_ROW_HEIGHT.getInteger();

        Parameters.STATE_HEIGHT_FACTOR
                  = sdr_STATE_HEIGHT_FACTOR.getFloat();
        Parameters.NESTING_HEIGHT_FACTOR
                  = sdr_NESTING_HEIGHT_FACTOR.getFloat();
        Parameters.ARROW_ANTIALIASING
                  = (Alias) lst_ARROW_ANTIALIASING.getSelectedItem();
        Parameters.MIN_WIDTH_TO_DRAG
                  = fld_MIN_WIDTH_TO_DRAG.getInteger();
        Parameters.LEFTCLICK_INSTANT_ZOOM
                  = lst_LEFTCLICK_INSTANT_ZOOM.getSelectedBooleanItem();

        // Options: Timeline zoomable window
        Parameters.STATE_BORDER
                  = (StateBorder) lst_STATE_BORDER.getSelectedItem();
        Parameters.ARROW_HEAD_LENGTH
                  = fld_ARROW_HEAD_LENGTH.getInteger();
        Parameters.ARROW_HEAD_WIDTH
                  = fld_ARROW_HEAD_WIDTH.getInteger();
        Parameters.EVENT_BASE_WIDTH
                  = fld_EVENT_BASE_WIDTH.getInteger();

        Parameters.SEARCH_ARROW_LENGTH
                  = fld_SEARCH_ARROW_LENGTH.getInteger();
        Parameters.SEARCH_FRAME_THICKNESS
                  = fld_SEARCH_FRAME_THICKNESS.getInteger();
        Parameters.SEARCHED_OBJECT_ON_TOP
                  = lst_SEARCHED_OBJECT_ON_TOP.getSelectedBooleanItem();

        // Options: Histogram zoomable window
        Parameters.HISTOGRAM_ZERO_ORIGIN
                  = lst_HISTOGRAM_ZERO_ORIGIN.getSelectedBooleanItem();
        Parameters.SUMMARY_STATE_BORDER
                  = (StateBorder) lst_SUMMARY_STATE_BORDER.getSelectedItem();
        Parameters.SUMMARY_ARROW_LOG_BASE
                  = fld_SUMMARY_ARROW_LOG_BASE.getInteger();
    }
}
