/*
 *   Copyright 2013 Dodgy Designs
 *
 *
 *   NOTICE:  All information contained herein is, and remains
 *            the property of Dodgy Designs.
 *            The intellectual and technical concepts contained
 *            herein are proprietary to Dodgy Designs.
 *            Dissemination of this information or reproduction of
 *            this material is strictly forbidden unless prior written
 *            permission is obtained from Dodgy Designs.
 *
 *   Unless required by applicable law or agreed to in writing,
 *   software distributed under the License is distributed on an
 *   "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *   KIND, either express or implied.  See the License for the
 *   specific language governing permissions and limitations
 *   under the License.
 */



    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import net.miginfocom.swing.MigLayout;

import org.joda.time.DateTime;

import widgets.BareBonesBrowserLaunch;
import widgets.ColourCellRenderer;
import widgets.ComboBoxPopup;
import widgets.LimitedLinesDocument;
import widgets.MultiLineCellRenderer;
import controller.Controller;

public class View extends JFrame implements ActionListener
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
    private static final long serialVersionUID = -3317570506856865808L;
    public static final int DESCRIPTION_COL_WIDTH = 314;

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
	private JPanel dataDisplayPanel;
    private JLabel dateLabel;
    private JTable taskTable;
    private JTextArea descriptionTextArea;
    private JButton reportButton;
    private JButton configButton;
    private JButton dayBackButton;
    private JButton dayForwardButton;
    private ComboBoxPopup jiraComboBox;
    private JRadioButton notJiraRadioButton;
    private JRadioButton notWorkRadioButton;
    private JButton quitButton;
    private JToggleButton startStopButton;
    private JProgressBar weekProgressBar;
    
    private Controller controller;
	private JLabel beerLabel;
	private JPanel bottomPanel;
	private JLabel timeLabel;
	private JButton clearDescriptionButton;
    
    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public View()
    {
    }
    
    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    public void addListener( Controller controller )
    {
        this.controller = controller;
    }

    /**
     * Instantiate all the GUI components.
     * 
     * This is necessarily a long, laborious method. If anyone knows a better
     * way, I would love to know about it.
     */
    public void initComponents()
    {
        // Set defaults for this GUI element
        // setDefaultCloseOperation(stopRecording());
        setTitle( "TimeLord" );
        setBackground( new Color( 85, 91, 106 ) );
        setPreferredSize( new Dimension(680, 700) );
        setResizable( false );
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Menu
        drawMenu();

        // Labels
        JLabel iconLabel = new JLabel();
        iconLabel.setIcon( new ImageIcon(getClass().getResource("/media/timelord logo.png")) );

        dateLabel = new JLabel();
        dateLabel.setFont( new Font( "Lucida Grande", 1, 20 ) );
        dateLabel.setHorizontalAlignment( SwingConstants.CENTER );

        timeLabel = new JLabel();
        timeLabel.setFont( new Font( "Lucida Grande", 1, 36 ) );

        JLabel taskIconLabel = new JLabel( "JIRA:" );
        taskIconLabel.setIcon( new ImageIcon( getClass().getResource(
                "/media/jiraicon.png" ) ) );

        beerLabel = new JLabel();
        beerLabel.setIcon( new ImageIcon( getClass().getResource( "/media/beer.png" ) ) );

        // Buttons
        dayBackButton = new JButton();
        dayBackButton.setIcon( new ImageIcon(getClass().getResource("/media/bkbutton.png")) );
        dayBackButton.setPressedIcon( new ImageIcon(getClass().getResource("/media/bkbuttonpressed.png")) );
        dayBackButton.setBorderPainted( false );
        dayBackButton.setBackground( null );
        dayBackButton.setBorder( null );
        dayBackButton.addActionListener( this );

        dayForwardButton = new JButton();
        dayForwardButton.setIcon( new ImageIcon(getClass().getResource("/media/fwdbutton.png")) );
        dayForwardButton.setPressedIcon( new ImageIcon(getClass().getResource("/media/fwdbuttonpressed.png")) );
        dayForwardButton.setBorderPainted( false );
        dayForwardButton.setBackground( null );
        dayForwardButton.setBorder( null );
        dayForwardButton.addActionListener( this );
        
        startStopButton = new JToggleButton();
        startStopButton.setEnabled( false );
        startStopButton.setIcon( new ImageIcon(getClass().getResource("/media/startbutton.png")) );
        startStopButton.setSelectedIcon( new ImageIcon(getClass().getResource("/media/stopbutton.png")) );
        startStopButton.setBorderPainted( false );
        startStopButton.setBackground( null );
        startStopButton.setBorder( null );
        startStopButton.setBackground( Color.WHITE );
        startStopButton.addActionListener( this );
        
        notWorkRadioButton = new JRadioButton( "Not Work" );
        notWorkRadioButton.setOpaque( false );
        notWorkRadioButton.addActionListener( this );
        
        notJiraRadioButton = new JRadioButton( "Not JIRA" );
        notJiraRadioButton.setOpaque( false );
        notJiraRadioButton.addActionListener( this );
        
        quitButton = new JButton();
        quitButton.setIcon( new ImageIcon(getClass().getResource("/media/powerbutton.png")) );
        quitButton.setPressedIcon( new ImageIcon(getClass().getResource("/media/powerbuttonpressed.png")) );
        quitButton.setBorderPainted( false );
        quitButton.setBackground( null );
        quitButton.setBorder( null );
        quitButton.addActionListener( this );
        
        configButton = new JButton();
        configButton.setIcon( new ImageIcon( getClass().getResource("/media/configbutton.png")) );
        configButton.setPressedIcon( new ImageIcon(getClass().getResource("/media/configurebuttonpressed.png")) );
        configButton.setBorderPainted( false );
        configButton.setBackground( null );
        configButton.setBorder( null );
        configButton.addActionListener( this );
        
        reportButton = new JButton();
        reportButton.setIcon( new ImageIcon(getClass().getResource("/media/report.png")) );
        reportButton.setPressedIcon( new ImageIcon(getClass().getResource("/media/reportbuttonpressed.png")) );
        reportButton.setBorderPainted( false );
        reportButton.setBackground( null );
        reportButton.setBorder( null );
        reportButton.addActionListener( this );

        clearDescriptionButton = new JButton("X");
        clearDescriptionButton.setForeground( Color.WHITE );
        clearDescriptionButton.setBorder( null );
        clearDescriptionButton.addActionListener( this );

        // TextArea
        setupDescriptionTextArea();
        
        // Table
        drawTaskTable();

        // Progress bar
        weekProgressBar = new JProgressBar();
        weekProgressBar.setMaximum( 5 );
        weekProgressBar.setMinimum( 1 );
        weekProgressBar.setValue( new DateTime().getDayOfWeek() );

        // Now put it all together
        // Panels
        JPanel mainContainer = new JPanel( new MigLayout( "", 
                                                          "5[grow]5",
                                                          "0[20][][300, grow][]0" ) );

        JPanel topPanel = new JPanel( new MigLayout( "", "[]0[][][]", "[]" ) );
        topPanel.add( iconLabel, "" );
        topPanel.add( dayBackButton, "" );
        topPanel.add( dateLabel, "grow" );
        topPanel.add( dayForwardButton, "" );
        topPanel.setBackground( new Color( 85, 91, 106 ) );

        JPanel dataEntryPanel = new JPanel( new MigLayout( "",
                                                  //start clock radiobut combo       
                                                    "10[]20[200]150    []20    []10[grow][]", 
                                                    "2[][grow]2" ) );
        dataEntryPanel.add( startStopButton );
        dataEntryPanel.add( timeLabel, "aligny 45%, hmax 30" );
        dataEntryPanel.add( notWorkRadioButton, "split 2, flowy" );
        dataEntryPanel.add( notJiraRadioButton );
        dataEntryPanel.add( taskIconLabel );
        dataEntryPanel.add( jiraComboBox, "wrap, hmax 28" );
        dataEntryPanel.add( descriptionTextArea, "span 5, grow, hmax 100" );
        dataEntryPanel.add( clearDescriptionButton, "hmax 100" );
        dataEntryPanel.setBackground( new Color( 115, 121, 136 ) );
        TitledBorder border = BorderFactory.createTitledBorder( null, 
                                                                "Task Details", 
                                                                TitledBorder.LEFT, 
                                                                TitledBorder.TOP, 
                                                                new Font( "Lucida Grande", 1, 12 ), 
                                                                Color.WHITE );
        dataEntryPanel.setBorder( border );

        bottomPanel = new JPanel( new MigLayout( "",
                                                 "20[grow]10[]20[]20[]20[]20",
                                                 "2[]0[]10" ) );

        bottomPanel.add( weekProgressBar, "grow, hmax 28" );
        bottomPanel.add( beerLabel );
        bottomPanel.add( reportButton );
        bottomPanel.add( configButton );
        bottomPanel.add( quitButton );
        bottomPanel.setBackground( new Color( 85, 91, 106 ) );

        mainContainer.add( topPanel, "grow, wrap" );
        mainContainer.add( dataEntryPanel, "grow, wrap" );
        mainContainer.add( dataDisplayPanel, "grow, wrap" );
        mainContainer.add( bottomPanel, "grow" );

        add( mainContainer );
        pack();
        setLocationRelativeTo( null );
        descriptionTextArea.requestFocus();
        descriptionTextArea.setCaretPosition( 0 );
    }

	/**
     * Create all the menu headings and menu items available for TimeLord.
     */
    private void drawMenu()
    {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar( menuBar );
    
        // File menu
        JMenu fileMenu = new JMenu( "File" );
        JMenuItem preferencesMenuItem = new JMenuItem( "Preferences" );
        preferencesMenuItem.addActionListener( this );
        fileMenu.add( preferencesMenuItem );
        JMenuItem quitMenuItem = new JMenuItem( "Quit" );
        quitMenuItem.addActionListener( this );
        fileMenu.add( quitMenuItem );

        // Report menu
        JMenu reportMenu = new JMenu( "Report" );
        JMenuItem byTaskMenuItem = new JMenuItem( "By Task..." );
        byTaskMenuItem.addActionListener( this );
        reportMenu.add( byTaskMenuItem );
        JMenuItem byWeekMenuItem = new JMenuItem( "By Week..." );
        byWeekMenuItem.addActionListener( this );
        reportMenu.add( byWeekMenuItem );

        // Help menu
        JMenu helpMenu = new JMenu( "Help" );
        JMenuItem onlineHelpMenuItem = new JMenuItem( "TimeLord Help" );
        onlineHelpMenuItem.addActionListener( this );
        helpMenu.add( onlineHelpMenuItem );
        JMenuItem aboutMenuItem = new JMenuItem( "About Time:Lord" );
        aboutMenuItem.addActionListener( this );
        helpMenu.add( aboutMenuItem );

        menuBar.add( fileMenu );
        menuBar.add( reportMenu );
        menuBar.add( helpMenu );
    }

    /**
     * Set the model and appearance of the task data table and set initial
     * values. 
     */
    protected void drawTaskTable()
    {
        dataDisplayPanel = new JPanel( new MigLayout( "fill", "0[]0", "0[]0" ) );
        TitledBorder border = BorderFactory.createTitledBorder( null, 
                                                                "Data", 
                                                                TitledBorder.LEFT, 
                                                                TitledBorder.TOP, 
                                                                new Font( "Lucida Grande", 1, 12 ), 
                                                                Color.WHITE );
        dataDisplayPanel.setBorder( border );

        dataDisplayPanel.setBackground( new Color( 85, 91, 106 ) );
        
        taskTable = new JTable( controller.getTaskTableModel() );

        taskTable.setAutoCreateRowSorter( true );
        taskTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

        JTableHeader taskTableHeader = taskTable.getTableHeader();
        taskTableHeader.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
        taskTableHeader.setBackground( new Color( 85, 91, 106 ) );
        taskTableHeader.setForeground( Color.WHITE );
//        taskTableHeader.setAlignmentY( CENTER_ALIGNMENT );
        
        // Disable auto resizing
        taskTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

        setupColumns();

        dataDisplayPanel.add( new JScrollPane( taskTable ), "grow" );
    }
    
    /**
     * Setup the column widths, colours and behaviours.
     */
    protected void setupColumns()
    {
    	TableColumnModel colModel = taskTable.getColumnModel();
        // Start
        TableColumn col0 = colModel.getColumn( 0 );
        col0.setPreferredWidth( 70 );
        col0.setCellRenderer( new ColourCellRenderer( Color.WHITE, new Color( 10, 150, 10 ) ) );

        // Stop
        TableColumn col1 = colModel.getColumn( 1 );
        col1.setPreferredWidth( 70 );
        col1.setCellRenderer( new ColourCellRenderer( Color.WHITE, new Color( 150, 10, 10 ) ) );

        // Delta
        TableColumn col2 = colModel.getColumn( 2 );
        col2.setMinWidth( 105 );
        col2.setPreferredWidth( 110 );
        col2.setCellRenderer( new ColourCellRenderer( Color.WHITE, Color.BLACK ) );

        // JIRA
        TableColumn col3 = colModel.getColumn( 3 );
        col3.setPreferredWidth( 90 );
        col3.setCellRenderer( new ColourCellRenderer( Color.WHITE, Color.BLUE ) );

        // Description
        final TableColumn col4 = colModel.getColumn( 4 );
        col4.setPreferredWidth( DESCRIPTION_COL_WIDTH );
        col4.setCellRenderer( new MultiLineCellRenderer() );
    }

	/**
	 * @param issues
	 */
	public void setJiraComboBox( ArrayList<String[]> issues )
    {
		if( issues != null )
		{
            String[][] jiraData = new String[2][issues.size()];
    
            int i = 0;
            for ( String[] entries : issues )
            {
                jiraData[0][i] = entries[0];
                jiraData[1][i] = entries[2];
                i++;
            }
    
            jiraComboBox = new ComboBoxPopup( jiraData[0], jiraData[1] );
		}
		else
			jiraComboBox = new ComboBoxPopup( new String[1], new String[1] );
		
        jiraComboBox.setPreferredSize( new Dimension(150, 28) );
        
//		view.enableJIRAPanel( true );
//		view.getJiraComboBox().setModel( result.getModel() );
//		usingJIRA = true;
    }   
    
    /**
     * This method sets up a key listener on the description text area and removes the
     * hint text when typing starts.
     */
    public void setupDescriptionTextArea()
    {	
        // Limit the maximum number of characters able to be entered to 97.
        LimitedLinesDocument textAreaDocument = new LimitedLinesDocument( 1, 97 );
        descriptionTextArea = new JTextArea();
        descriptionTextArea.setDocument( textAreaDocument );
        descriptionTextArea.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
        descriptionTextArea.setForeground( Color.LIGHT_GRAY );
        descriptionTextArea.setText( "Enter task description..." );
        descriptionTextArea.setBackground( new Color(125, 131, 146) );
        
        descriptionTextArea.addKeyListener( new KeyListener()
        {
            @Override
            public void keyTyped( KeyEvent e )
            {
                if ( !startStopButton.isEnabled() )
                {
                    descriptionTextArea.setText( "" );
                    descriptionTextArea.setCaretPosition( 0 );
                    descriptionTextArea.setForeground( Color.WHITE );
                }
                startStopButton.setEnabled( true );
            }

            @Override
            public void keyReleased( KeyEvent e )
            {
            }

            @Override
            public void keyPressed( KeyEvent arg0 )
            {
            }
        } );	    
    }
    
    /**
     * Used to flash the Beer O' Clock label.
     */
    public void setBeerAlarmLabel()
    {
        System.out.println( "Beer O' Clock!" );
        beerLabel.setText( "<html><div font color='white'>" + "Beer O' Clock!" + "</div></html>" );
        beerLabel.setEnabled( new DateTime().getSecondOfMinute() % 2 == 0 );
    }
    
    public void setDate( String date )
    {
        dateLabel.setText( date );
    }
   
    /**
     * Grab all the GUI events and handle them appropriately.
     */
    @Override
    public void actionPerformed( ActionEvent e )
    {        
    	Object source = e.getSource();
    	
        if( e.getSource() instanceof JButton )
        {   
            if( source == reportButton )
            {
//                new Report( this ).setVisible( true );
            }
            else if( source == dayBackButton )
            {
//                dayBackButtonActionPerformed( e );
            }
            else if( source == dayForwardButton )
            {
//                dayForwardButtonActionPerformed();
            }
            else if( source == clearDescriptionButton )
            {
            	descriptionTextArea.setText( "" );
            	descriptionTextArea.setCaretPosition( 0 );
            	descriptionTextArea.requestFocus();
            }
            else if( source == quitButton )
            {
                if ( controller.isRecording() )
                {
                    controller.stopRecording();
                }
                System.exit( 0 );
            }
            else if( source == configButton )
            {
            	Semaphore semaphore = new Semaphore( 1 );
                JDialog configDialog = new Configuration( controller, semaphore );
                configDialog.setVisible( true );
            }
        }
        
        if( source instanceof JMenuItem )
        {
            if( e.getActionCommand().equalsIgnoreCase( "preferences" ) )
            {
            	Semaphore semaphore = new Semaphore( 1 );
                JDialog configDialog = new Configuration( controller, semaphore );
                configDialog.setVisible( true );
            }
            else if( e.getActionCommand().equalsIgnoreCase( "quit" ) )
            {
                if ( controller.isRecording() )
                {
                    controller.stopRecording();
                }
                System.exit( 0 );
            }
            else if( e.getActionCommand().equalsIgnoreCase( "by task..." ) )
                System.out.println( "Item clicked: " + e.getActionCommand() );
            else if( e.getActionCommand().equalsIgnoreCase( "by week..." ) )
                System.out.println( "Item clicked: " + e.getActionCommand() );
            else if( e.getActionCommand().equalsIgnoreCase( "timelord help" ) )
            {
            	URL resource = getClass().getResource( "/media/help/Welcome to Time.htm" );
            	BareBonesBrowserLaunch.openURL( resource.toString() );
            }
                
            else if( e.getActionCommand().equalsIgnoreCase( "About Time:Lord" ) )
//                System.out.println( "Item clicked: " + e.getActionCommand() );
                new Splash( this );
        }
        
        if( e.getSource() instanceof JToggleButton )
        {
        	if( !e.getActionCommand().toLowerCase().equals("not jira") &&
        		!e.getActionCommand().toLowerCase().equals("not work") )
        	{
            	if( startStopButton.isSelected() )
            		controller.startRecording();
            	else
            		controller.stopRecording();
        	}
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
	public ComboBoxPopup getJiraComboBox()
	{
		return jiraComboBox;
	}

	public void setJiraComboBox( ComboBoxPopup jiraComboBox )
	{
		this.jiraComboBox = jiraComboBox;
	}

	public JRadioButton getNotJiraRadioButton()
	{
		return notJiraRadioButton;
	}

	public void setNotJiraRadioButton( JRadioButton notJiraRadioButton )
	{
		this.notJiraRadioButton = notJiraRadioButton;
	}

	public JRadioButton getNotWorkRadioButton()
	{
		return notWorkRadioButton;
	}

	public void setNotWorkRadioButton( JRadioButton notWorkRadioButton )
	{
		this.notWorkRadioButton = notWorkRadioButton;
	}

	public JTextArea getDescriptionTextArea()
	{
		return descriptionTextArea;
	}

	public void setDescriptionTextArea( JTextArea descriptionTextArea )
	{
		this.descriptionTextArea = descriptionTextArea;
	}

	public JPanel getBottomPanel()
	{
		return bottomPanel;
	}

	public void setBottomPanel( JPanel bottomPanel )
	{
		this.bottomPanel = bottomPanel;
	}

	public JLabel getTimeLabel()
	{
		return timeLabel;
	}

	public void setTimeLabel( JLabel timeLabel )
	{
		this.timeLabel = timeLabel;
	}

	public JTable getTaskTable()
	{
		return taskTable;
	}

    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------

    
    
    
}
