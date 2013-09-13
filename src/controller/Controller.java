/*
 *   Copyright 2013 Dodgy Designs
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
package controller;

import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import model.JiraInterface;
import model.Preferences;
import model.SqlInterface;
import model.Time;

import org.apache.xmlrpc.XmlRpcException;
import org.joda.time.DateTime;
import org.joda.time.Period;

import view.Configuration;
import view.MainView;

public class Controller
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------

    public static final String VERSION = "2.0";
    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    private MainView view;
    private SqlInterface database;
    private DefaultTableModel taskTableModel;
    private Preferences preferences;
    private JiraInterface jiraInterface;
    
	private boolean recording;
	private int taskCount;
	private DateTime selectedDate = new DateTime(); // reference to current date/time
	private DateTime currentStartTime;
	private int dayOffset;
	private Period dayTally;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public Controller( MainView view )
    {
        this.view = view;
        view.addListener( this );
        
        dayOffset = 0;
        
        // Create a link to the DB
        try
        {
            database = new SqlInterface( view );
        }
        catch ( Exception ex )
        {
            Logger.getLogger( Controller.class.getName() ).log( Level.SEVERE, null, ex );
        }
        
        // Handle preferences
        preferences = new Preferences();
        jiraInterface = new JiraInterface( this );

        startupTimeLord();
    }
    
    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    private void startupTimeLord()
    {   
        // If a preferences file does not already exist, create the file and display the 
        // configuration dialog.
        if( !preferences.readExistingPrefsFromDisk() || 
        	(preferences.connectToJiraAtStartup() && !jiraInterface.connectToJira()) )
        {
        	// Get the configuration dialog ready in case there is a problem starting up e.g.
        	// there is no preferences file or the Jira connection failed.
            preferences.saveToDisk();

            JDialog configDialog = new Configuration( this, null );
            synchronized( configDialog )
            {
            	try
                {
	                configDialog.wait();
                }
                catch( InterruptedException e )
                {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
                }
            }
        }
        else
        {
        	view.setJiraComboBox();
        }
        
        try
        {
        	String[][] todaysEntries = getDatabase().getTodaysEntries();
        	taskCount = todaysEntries.length;
	        taskTableModel =  new DefaultTableModel( todaysEntries, 
	                                                 new String[] { "Start", 
	                                                                "Stop", 
	                                                                "Delta", 
	                                                                "JIRA", 
	                                                       			"Description" } );
        }
        catch( SQLException e )
        {
	        e.printStackTrace();
        }
     
        // Start drawing the GUI
        view.initComponents();
        view.layoutComponents();
        view.showDialog();
        
		setDateLabel( new DateTime() );
		setTimeLabel();
		
        // Set the tally for today on the dayTallyLabel
        view.getBottomPanel().setBorder( setTallyBorder( new DateTime() ) );
        view.showDialog();
    }
 
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// User Interface /////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * Set the date label depending on the date provided.
     * 
     * @param date The date to set the date label to.
     */
    public void setDateLabel( final DateTime date )
    {
		DateTime.Property dayOfWeek = date.dayOfWeek();
		view.setDate( "<html><div align='center' font color='white'>" + 
					  "<font size='6'>" +
		              dayOfWeek.getAsText() + 
		              "</font>" +
		              "<br>" + 
					  "<font size='4'>" +
					  date.getDayOfMonth() + 
		              "/" +
		              date.getMonthOfYear() +
		              "</font></div></html>" );
    }
    /**
     * Set current date for the date label and time for the time label.  
     * This is updated every second to ensure the values are kept current.
     * 
     * If the clock reaches Friday 4pm, the beer alarm goes off.
     */
    //TODO update date/day if 0000ticks over
    public void setTimeLabel()
    {      
		TimerTask dateUpdater = new TimerTask()
		{
			@Override
			public void run()
			{
				DateTime currentTime = new DateTime();
				
				String minutes = (currentTime.getMinuteOfHour() < 10 ? "0" + currentTime.getMinuteOfHour()
				                                                : String.valueOf(currentTime.getMinuteOfHour()) );
				String hours = (String)(currentTime.getHourOfDay() < 10 ? "0" + currentTime.getHourOfDay()
					                                               : String.valueOf(currentTime.getHourOfDay()) );
				
				// Set clock with a blinking ':'.
				String colonText = "";
				if( new DateTime().getSecondOfMinute() % 2 == 0 )
					colonText = "<font color='gray'>";
				else
					colonText = "<font color='white'>";
		
				view.getTimeLabel().setText( "<html><div align='center' font color='white'>" +
				              				 "<font size='50'>" + 
				              				 hours + 
				              				 colonText +
				              				 ":" +
				              				 "</font>" +
				              				 minutes + 
				              				 "</font></div></html>" );
				setBeerAlarm( selectedDate );
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate( dateUpdater, 0, 1000 );
	}

    
    /**
     * Get the time worked per day and week and set them as the title for a border.
     * 
     * @param The date to set the tally for.
     * 
     * @return A Border with the total time worked per day and week.
     */
    private TitledBorder setTallyBorder( DateTime thisDay )
    {   
        TitledBorder border = BorderFactory.createTitledBorder( null, 
                                                                "Week: "  + 
                                                                calculateWeekTally( thisDay ) + 
                                                                "   Day: " + 
                                                                calculateDayTally( thisDay ), 
                                                                TitledBorder.LEFT, 
                                                                TitledBorder.TOP, 
                                                                new Font( "Lucida Grande", 1, 12 ), 
                                                                Color.WHITE );
        
        return border;
    }
    
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Action Handlers ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This method takes in the dateLabel from the GUI and advances or
     * decrements it by one day for each time it is clicked.
     * 
     * @param Increment or decrement the date.
     */
	public void incrementDecrementDay( int direction )
    {
    	dayOffset += direction;
    	DateTime selectedDate = getCurrentDateTime().plusDays( dayOffset );
    	setDateLabel( selectedDate );
    	String selectedDateString = Time.getReferableDate( selectedDate );
    	
    	String[] colHeaders = getDatabase().getColumnHeaders();
        String[][] selectedDateIssues = null;
        try
        {
            selectedDateIssues = database.getEntriesByDate( selectedDateString );
        }
        catch( SQLException e1 )
        {
        	JOptionPane.showConfirmDialog( view, 
        	                               "Could not connect to Jira server.  Please" +
        	                               " check your login details and try again.",
        	                               "Jira Connection", 
        	                               JOptionPane.OK_OPTION );
        }
        
        DefaultTableModel tableModel = new DefaultTableModel( selectedDateIssues,
                                                              colHeaders );
       	   
        // Update the tally border
        view.getBottomPanel().setBorder( setTallyBorder( selectedDate ) );

        getView().getTaskTable().setModel( tableModel );
        view.setupColumns();
        
        if( !recording )
        {
            getView().getStartStopButton().setEnabled( false );
            getView().resetDescriptionTextfield();	
        }
    }

	/**
	 * Stop recording and exit.
	 * @return 
	 */
	public int exitTimeLord()
    {
    	stopRecording();
		System.exit( 0 );
    	
		// Amazing, we can never get to here
    	return 0;
    }
	
	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////// Utility Methods ////////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * This method determines whether it is Beer O' Clock (time to stop working for the week) and
     * if so, sets the alarm off.  If configured, the application will terminate at the chosen 
     * time. 
     * 
     * @param dateTime The current time.
     */
    private void setBeerAlarm( DateTime dateTime )
    {
        // Handle Beer O' Clock
        if ( dateTime.getDayOfWeek() == 5
             && dateTime.getHourOfDay() == 15
             && dateTime.getMinuteOfHour() == 59 )
        {
            view.setBeerAlarmLabel();
        }
        if ( dateTime.getDayOfWeek() == 5
             && dateTime.getHourOfDay() == 16
             && dateTime.getMinuteOfHour() == 00 )
        {
        	if( preferences.getKillOnBeer() )
        		System.exit( 0 );
        }
    }
	
    /**
     * This method gets a day's Jira issues.  It first tries to get them from the preferences
     * and failing that, from the Jira server.
     * 
     * If a problem occurs with both attempts, an error message is displayed.
     * 
     * @return A list of Jira issues.
     */
    public ArrayList<String[]> getJiraIssues()
    {
        ArrayList<String[]> issues = new ArrayList<String[]>();

        if( jiraInterface.getToken() != null )
        {
			try
            {
                issues = jiraInterface.getIssues();
            }
            catch( XmlRpcException e )
            {
            	JOptionPane.showConfirmDialog( view, 
            	                               "Could not connect to Jira server.  Please" +
            	                               " check your login details and try again.",
            	                               "Jira Connection", 
            	                               JOptionPane.OK_OPTION );
            }
        }
        else
        {
        	issues = preferences.getIssuesForProject();
        }

        // Can't get issues from preferences or the server, get user to check configuration.
        while( issues == null )
        {
        	int selection = JOptionPane.showConfirmDialog( view, 
                        	                               "Could not connect to Jira server.  Please" +
                        	                               " check your login details and try again.",
                        	                               "Jira Connection", 
                        	                               JOptionPane.OK_CANCEL_OPTION );
        	if( selection == JOptionPane.OK_OPTION )
        	{
				Configuration configuration = new Configuration( this, null );
				configuration.setVisible( true );
        	}
        	else
        		break;
        	try
            {
	            issues = jiraInterface.getIssues();
            }
            catch( XmlRpcException e )
            {
	            e.printStackTrace();
            }
        }
        
        if( issues != null )
        	preferences.setIssuesForProject( issues );
                
        return issues;
    }
    
    /**
     * When the start recording button is pressed, the current time, selected
     * JIRA task and description are recorded in the database and the task
     * table. If either the notJira or notWork buttons are pressed, 'N/A' is
     * entered for the task reference.
     */
    public void startRecording()
    {
        currentStartTime = new DateTime();

        String jiraKey = (String)view.getJiraComboBox().getSelectedItem();
        if ( view.getNotWorkRadioButton().isSelected() || view.getNotJiraRadioButton().isSelected() )
        	jiraKey = "N/A";
        
        String workDescription = view.getDescriptionTextArea().getText();
        
        final Object[] data = { Time.getFormattedTime( new DateTime() ), 
                                "", 
                                "", 
                                jiraKey, 
                                workDescription };

        // Add to Event Dispatch Thread to avoid blocking the GUI
        SwingUtilities.invokeLater( new Runnable()
        {

            public void run()
            {
                taskTableModel.addRow( data );
            }
        } );

        try
        {
            database.setStartParameters( Time.getReferableDate( currentStartTime ),
                                         currentStartTime, 
                                         jiraKey, 
                                         workDescription );
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }

        taskCount++;
        recording = true;
    }
    
    /**
     * When the recording is stopped, calculate the amount of time spent and
     * display with the stop time. This data is also added to the db and the the
     * day and week tally labels.
     */
    public void stopRecording()
    {
    	if( !recording )
    		return;
    	
        // Save the stop time
        final DateTime currentStopTime = new DateTime();

        // Figure out how long this task took
        final Period delta = Time.getTimeDifference( currentStartTime, currentStopTime );

        dayTally = new Period( dayTally ).plus( new Period( delta ) );

        // Set the time taken for this task.
        SwingUtilities.invokeLater( new Runnable()
        {
            public void run()
            {
                // Set the stop time on the table
            	taskTableModel.setValueAt( Time.getFormattedTime( currentStopTime ),
                                                taskCount - 1, 
                                                1 );
            	
                // Set the delta time on the table                
            	taskTableModel.setValueAt( Time.displayDelta( delta ),
                                                taskCount - 1, 
                                                2 );
            	view.getTaskTable().invalidate();
            	
                // Set the tally for today on the dayTallyLabel
                view.getBottomPanel().setBorder( setTallyBorder( new DateTime() ) );
            }
        } );

        try
        {
            database.setStopParametersAndCommit( currentStopTime, delta, dayTally );
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }

        recording = false;
    }

    /**
     * Read the database to determine how many hours:minutes that have been
     * complete on the current day.
     */
    public String calculateDayTally( DateTime date )
    {
        Period tally = database.getTodayTally( date );

        return Time.displayDelta( tally );
    }
    
    /**
     * Read the database to determine how many hours:minutes that have been
     * complete on the current day.
     * 
     * A week starts on Sunday and ends on Saturday.
     */
    public String calculateWeekTally( DateTime dayInWeek )
    {
        // What is today's index in the week
        int selectedDay = dayInWeek.getDayOfWeek();
        
        DateTime startOfWeek = new DateTime();
        startOfWeek = dayInWeek.minusDays( selectedDay );

        Period tally = database.getWeekTally( startOfWeek, dayInWeek );

        return Time.displayDelta( tally );
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    public SqlInterface getDatabase()
    {
        return database;
    }
    /**
     * The number of tasks listed in the table.
     * 
     * @return
     */
    public void setTaskCount( int count )
    {
        taskCount = count;
    }
    
    public Preferences getPreferences()
    {
        return preferences;
    }

    public MainView getView()
    {
        return view;
    }
    
    public JiraInterface getJiraInterface()
    {
        return jiraInterface;
    }
    
	public TableModel getTaskTableModel()
    {
	    return taskTableModel;
    }
	public void setTaskTableModel( DefaultTableModel newModel )
    {
	    taskTableModel = newModel;
    }
	
	public boolean isRecording()
	{
		return recording;
	}

	public void setRecording( boolean recording )
	{
		this.recording = recording;
	}

	public DateTime getCurrentDateTime()
	{
		return selectedDate;
	}

	public void setCurrentDateTime( DateTime currentDateTime )
	{
		this.selectedDate = currentDateTime;
	}
	
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}
