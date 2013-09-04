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
package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import model.JiraInterface;
import model.Preferences;
import model.SqlInterface;
import net.miginfocom.swing.MigLayout;

import org.joda.time.DateTime;

import controller.Controller;

/**
 * This class provides a UI to set all the parameters required to log into a Jira server.  The
 * data entered here is stored in the preferences and any projects discovered on the Jira server
 * are also stored for off-line use.
 * </p>
 * It is also possible to work with the database here:
 * <ul>
 *   <li>Backup or restore a database.</li>
 *   <li>Delete the current database.</li>
 * </ul>
 */
public class Configuration extends JDialog implements ActionListener
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
    /**
	 * Configuration
	 */
    private static final long serialVersionUID = -1869584080186759637L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private JPanel databasePanel;
	private JPanel beerPanel;
	private JPanel jiraPanel;
    private JTextField urlTextField;
    private JTextField usernaneTextField;
    private JPasswordField passwordPasswordField;
	private JButton tryButton;
	private JButton backupDBButton;
	private JButton restoreDBButton;
	private JButton clearDBButton;
	private JButton cancelButton;
	private JButton okButton;
    private JComboBox<String> projectCombo;
	private JComboBox<String> alarmDay;
	private JComboBox<String> alarmHour;
	private JComboBox<String> alarmMinute;
	private JCheckBox killOnAlarm;
    private JCheckBox connectToJira;
    private JLabel tryJIRASettingsLabel;
	private JLabel logoLabel;

    private view.MainView view;
    private Preferences preferences;
    private SqlInterface database;

    private JiraInterface jiraInterface;
	private boolean connected;
	private Map<String, String> projects;

	private SwingWorker<Boolean,Void> jiraAttemptWorker;
	private SwingWorker<Void,Void> jiraIssuesWorker;
	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    public Configuration( Controller controller, Point locationOnScreen )
    {
        super( controller.getView() );
        
        this.view = controller.getView();
        this.preferences = controller.getPreferences();
        this.database = controller.getDatabase();
        this.jiraInterface = controller.getJiraInterface();
        
        initComponents();
        loadPreferences();
        layoutComponents();
        showConfiguration();
    }

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// User Interface /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Create and layout the UI components.
     */
    private void initComponents()
    {
    	setLocationRelativeTo( getParent() );
    	
		addWindowListener( new WindowAdapter()
		{

			@Override
			public void windowClosing( WindowEvent e )
			{
				returnToMainWindow();
			}
		} );
    	
    	// Hide the main window to reduce clutter
        view.setVisible( false );
        
        setBackground( new Color( 115, 121, 136 ) );
        setLayout( new MigLayout( "", "[][]", "[][][][]") );
    	setModal( true );
        getContentPane().setBackground( Color.LIGHT_GRAY );

        // Panels
        jiraPanel = new JPanel( new MigLayout( "", "[][grow]", "[shrink 0][shrink 0]" ) );
        TitledBorder border = BorderFactory.createTitledBorder( null, 
                                                                "Jira", 
                                                                TitledBorder.LEFT, 
                                                                TitledBorder.TOP, 
                                                                new Font( "Lucida Grande", 1, 12 ), 
                                                                Color.WHITE );
        jiraPanel.setBorder( border );
        jiraPanel.setBackground( new Color( 115, 121, 136 ) );
        
        databasePanel = new JPanel( new MigLayout( "", "[][grow]", "[][shrink 0]" ) );
        border = BorderFactory.createTitledBorder( null, 
                                                   "Database", 
                                                   TitledBorder.LEFT, 
                                                   TitledBorder.TOP, 
                                                   new Font( "Lucida Grande", 1, 12 ), 
                                                   Color.WHITE );
        databasePanel.setBorder( border );
        databasePanel.setBackground( new Color( 115, 121, 136 ) );

        beerPanel = new JPanel( new MigLayout( "", "[]", "[]" ) );
        border = BorderFactory.createTitledBorder( null, 
                                                   "Alarm Settings", 
                                                   TitledBorder.LEFT, 
                                                   TitledBorder.TOP, 
                                                   new Font( "Lucida Grande", 1, 12 ), 
                                                   Color.WHITE );
        beerPanel.setBorder( border );
        beerPanel.setBackground( new Color( 115, 121, 136 ) );
        
        // ComboBoxes
        alarmDay = new JComboBox<String>( new String[]{  "Monday", 
                                                         "Tuesday",
                                                         "Wednesday",
                                                         "Thursday",
                                                         "Friday",
                                                         "Saturday",
                                                         "Sunday"} );
        alarmDay.setSelectedItem( "Friday" );
        
        String[] hours = new String[24];
        for( int i = 0; i < 24; i++ )
        	hours[i] = String.valueOf( i );
        alarmHour = new JComboBox<String>( hours );
        alarmHour.setSelectedItem( "16" );
        
        String[] minutes = new String[60];
        minutes[0] = "00";
        for( int i = 1; i <= 59; i++ )
        	minutes[i] = String.valueOf( i );
    	alarmMinute = new JComboBox<String>( minutes );
    	alarmMinute.setSelectedItem( "00" );
    	
        // Labels
		logoLabel = new JLabel( new ImageIcon( getClass().getResource( "/media/" +
																	   "timelord logo.png" 
																			) ) );
		tryJIRASettingsLabel = new JLabel( "<html><font color='white'>Try to connect using " +
										   "current settings:" );
		logoLabel.setBackground( new Color( 85, 91, 106 ) );
		logoLabel.setPreferredSize( new Dimension( 450, 0 ) );
		logoLabel.setOpaque( true );

        // TextFields
        urlTextField = new JTextField();
        usernaneTextField = new JTextField();
        passwordPasswordField = new JPasswordField();
        
        // Combo for available projects
        Map<String,String> projects = (preferences.getProjects() != null) ? preferences.getProjects() 
                                                                          : new HashMap<String,String>();
        
        projectCombo = new JComboBox<String>( projects.keySet().toArray( new String[projects.size()] ) );
        if( preferences.getCurrentProject() != null )
        	projectCombo.setSelectedItem( preferences.getCurrentProject() );

        // Checkbox
        connectToJira = new JCheckBox();
        connectToJira.addActionListener( this );
        
        killOnAlarm = new JCheckBox( "Exit on Alarm" );
        killOnAlarm.setForeground( Color.WHITE );
        killOnAlarm.addActionListener( this );
        
        // Buttons
        tryButton = new JButton( "Try..." );
        tryButton.setBackground( null );
        tryButton.addActionListener( this );
        backupDBButton = new JButton( "Backup" );
        backupDBButton.addActionListener( this );
        restoreDBButton = new JButton( "Restore" );
        restoreDBButton.addActionListener( this );
        clearDBButton = new JButton( "Clear" );
        clearDBButton.addActionListener( this );
        cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( this );
        okButton = new JButton( "OK" );
        okButton.addActionListener( this );
    }

    /**
     * Layout all the GUI elements on the dialog.
     */
    private void layoutComponents()
    {
        jiraPanel.add( connectToJira );
        jiraPanel.add( new JLabel( "<html><font color='white'>Connect to Jira at startup</font>" ), 
                       "wrap, span, grow" );
        jiraPanel.add( new JLabel( "<html><font color='white'>Jira URL:</font>" ) );
        jiraPanel.add( urlTextField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "<html><font color='white'>Username:</font>" ) );
        jiraPanel.add( usernaneTextField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "<html><font color='white'>Password:</font>" ) );
        jiraPanel.add( passwordPasswordField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "<html><font color='white'>Project:</font>" ) );
        jiraPanel.add( projectCombo, "wrap 15, span, grow" );

        jiraPanel.add( tryJIRASettingsLabel, "span 2" );
        jiraPanel.add( tryButton, "right, width 80:80:80" );

        databasePanel.add( new JLabel( "<html><font color='white'>Clear DB:</font>" ) );
        databasePanel.add( clearDBButton, "gapleft 30, width 80:80:80, wrap" );
        databasePanel.add( new JLabel( "<html><font color='white'>Backup DB:</font>" ) );
        databasePanel.add( backupDBButton, "gapleft 30, width 80:80:80, wrap" );
        databasePanel.add( new JLabel( "<html><font color='white'>Restore DB:</font>" ) );
        databasePanel.add( restoreDBButton, "gapleft 30, width 80:80:80," );

        beerPanel.add( killOnAlarm );
        beerPanel.add( alarmDay );
        beerPanel.add( alarmHour );
        beerPanel.add( alarmMinute );

        add( logoLabel, "span, wrap" );
        add( jiraPanel, "wrap, span, grow" );
        add( databasePanel, "wrap, span, grow" );
		add( beerPanel, "wrap, span, grow" );
        add( cancelButton, "right, width 80:80:80," );
        add( okButton, "right, width 80:80:80," );
		setLocation( 50, 50 );
    }
    
    /**
     * Display the configuration dialog from the event dispatch thread.
     */
    private void showConfiguration()
    {
        SwingUtilities.invokeLater( new Runnable()
		{
			
			@Override
			public void run()
			{
				pack();
	            setVisible( true );	
			}
		} );
        urlTextField.requestFocus();
    }
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Action Handlers ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Get the settings just entered and try to connect to JIRA server.  This could take a while
     * so put it in a worker thread.
     */
    private void retrieveJiraProjects()
    {
    	setPreferenceValues();

    	// This could take ages
    	jiraAttemptWorker = new SwingWorker<Boolean,Void>()
    	{
    		@Override
    		protected Boolean doInBackground() throws Exception
    		{
    			displayScrollWheel( true );
    	    	connected = jiraInterface.connectToJira();

    			return connected;
    		}

			// Can safely update the GUI from this method.
			protected void done()
			{
				if( connected )
				{
					displayScrollWheel( false );
					tryJIRASettingsLabel.setText( "Success!" );
					tryJIRASettingsLabel.setForeground( new Color( 100, 255, 100 ) );

					// Retrieve all the projects and hold in jiraInterface.
					jiraInterface.getProjects();

					projects = new HashMap<String,String>();
					projects.putAll( jiraInterface.getProjectsKeyName() );
					preferences.setProjects( projects );

					String[] projectsNames =
					    projects.values().toArray( new String[projects.size()] );
					DefaultComboBoxModel<String> model =
					    new DefaultComboBoxModel<String>( projectsNames );
					projectCombo.setModel( model );
				}
				else
				{
					tryJIRASettingsLabel.setText( "Sorry, Failed!" );
					tryJIRASettingsLabel.setForeground( new Color( 255, 50, 50 ) );
				}
			}
		};

		jiraAttemptWorker.execute();
    }

	/**
	 * When the ok button is pressed, the Jira issues for the chosen project must be retrieved
	 * which can take some time.  The scroll wheel will be displayed while this happens and once
	 * complete, the dialog is dismissed.
	 */
	private void acceptSettingsAndClose()
	{
    	setPreferenceValues();

    	jiraIssuesWorker = new SwingWorker<Void,Void>()
    	{
    		@Override
    		protected Void doInBackground() throws Exception
    		{
    			displayScrollWheel( true );
    			
    			preferences.setCurrentProject( (String)projectCombo.getSelectedItem() );
    			view.setJiraComboBox();
    		
    			// Don't care about return value
    			return null;
    		}

			// Can safely update the GUI from this method.
			protected void done()
			{
				returnToMainWindow();
			}
		};

		jiraIssuesWorker.execute();
	}
	
	/**
	 * This method drops the database thereby clearing any data contained in it.
	 * 
	 * @param choice Whether the OK or Cancel button was clicked.
	 */
	private void clearDatabase( int choice )
    {
    	// Destroy the database and all data.
        if( choice == JOptionPane.OK_OPTION )
        {
        	try
            {
	            database.dropDataBase();
	            database.createTimeLordDB();
	            view.getTaskTable().setModel( new DefaultTableModel() );
	            view.getStartStopButton().setEnabled( false );
	            view.resetDescriptionTextfield();
            }
            catch( SQLException e )
            {
	            e.printStackTrace();
            }        
        }
    }


	@Override
    public void actionPerformed( ActionEvent e )
    {
		Object source = e.getSource();

		if( source instanceof JButton )
        {   
			String buttonText = ((JButton)source).getText();

			if( buttonText.toLowerCase().equals( "try..." ) )
            {
                retrieveJiraProjects();
            }	
            if( buttonText.toLowerCase().equals( "backup" ) )
            {
                database.backupDB();
            }	
            if( buttonText.toLowerCase().equals( "restore" ) )
            {
                database.restoreDB();
            }	
            if( buttonText.toLowerCase().equals( "clear" ) )
            {
            	int choice = JOptionPane.showConfirmDialog( (JFrame)view,
                                                            "Are you sure you want to clear " +
                                                            "the database?",
                                                            "<html><FONT SIZE=\"5\" " +
                      				   					    "COLOR=\"FF0000\">All records " +
                      									    "will be deleted!</h1></html>", 
                      									    JOptionPane.OK_CANCEL_OPTION,
                      								   	    JOptionPane.WARNING_MESSAGE,
                      									    null );  
            	clearDatabase( choice );            
        	}
            if( buttonText.toLowerCase().equals( "ok" ) )
            {
            	acceptSettingsAndClose();
            }
            if( buttonText.toLowerCase().equals( "cancel" ) )
            {
                // TODO: undo any changes
            	returnToMainWindow();
            }
        }
		else if( source instanceof JCheckBox )
		{			
			if( e.getActionCommand().toLowerCase().equals( "exit on alarm" ))
				preferences.setKillOnBeer( killOnAlarm.isSelected() );
			else
				preferences.setConnectToJiraAtStartup( connectToJira.isSelected() );
		}
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Utility Methods ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Read the preferences from disk and fill the text fields with the
     * appropriate data.
     */
    private void loadPreferences()
    {
    	connectToJira.setSelected( preferences.connectToJiraAtStartup() );
    	urlTextField.setText( preferences.getJiraUrl() );
        passwordPasswordField.setText( preferences.getPassword() );
        usernaneTextField.setText( preferences.getUserName() );
        
        DateTime alarmTime = preferences.getBeerTime();
        alarmDay.setSelectedIndex( alarmTime.getDayOfWeek() - 1 );
        alarmHour.setSelectedItem( String.valueOf(alarmTime.getHourOfDay()) );
        alarmMinute.setSelectedItem( String.valueOf(alarmTime.getMinuteOfHour()) );
        killOnAlarm.setSelected( preferences.getKillOnBeer() );
    }
    
	/**
     * Write all the data entered into various fields to the preferences file.  This will only
     * happen if a successful connection is made to the Jira server.
     */
    private void setPreferenceValues()
    {        
        char[] pw = passwordPasswordField.getPassword();
        preferences.setPassword( String.valueOf( pw ) );
        preferences.setUserName( usernaneTextField.getText() );
        
        String url = urlTextField.getText();
        if( url.toLowerCase().startsWith( "http://" ) || url.toLowerCase().startsWith( "https://" ))
        	preferences.setJiraUrl( urlTextField.getText() );
        else
        	preferences.setJiraUrl( "http://" + urlTextField.getText() );
        
        preferences.setBeerTime( alarmDay.getSelectedIndex() + " " +
        						 alarmHour.getSelectedItem() + " " + 
        						 alarmMinute.getSelectedItem() );
    }
    
    /**
	 * Close this dialog and return to the main window.
	 */
	private void returnToMainWindow()
	{
        dispose();
        
        synchronized( this )
        {
        	this.notify();
        }
        
    	// Show the main window again
        SwingUtilities.invokeLater( new Runnable()
		{
			
			@Override
			public void run()
			{
		        view.setVisible( true );
		        view.pack();				
			}
		} );

	}
	
    /**
     * This method will add an animated scroll wheel to the glass pane of the configuration
     * panel when required.  When the lengthy process is complete, the glasspane is cleared.
     * 
     * @param visible  Whether the glasspane (and thus scroll wheel) should be visible.
     */
    private void displayScrollWheel( final boolean visible )
    {
    	final JPanel glassPane = (JPanel)this.getGlassPane();
		final JLabel spinLabel = new JLabel( new ImageIcon( getClass().getResource( "/media/" +
			  "scrollwheel.gif" 
			) ) );	
		
    	SwingUtilities.invokeLater( new Runnable()
		{
			
			@Override
			public void run()
			{
		    	glassPane.setLayout( new GridBagLayout() );
		    	
		    	if( visible )
		    		glassPane.add( spinLabel );
		    	else
		    		glassPane.removeAll();
		    	
		    	glassPane.setVisible( visible );				
			}
		} );
    }
    
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     INNER CLASSES
	//----------------------------------------------------------
}
