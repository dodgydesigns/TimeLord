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
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JLabel;
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
import view.View;

public class Controller
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------

    public static final String VERSION = "2.0";
    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    private View view;
    private SqlInterface database;
    private DefaultTableModel taskTableModel;
    private Preferences preferences;
    private JiraInterface jiraInterface;
    
	private boolean recording;
	private int taskCount;
	private DateTime currentStartTime;
	private Period dayTally;
	protected JLabel dayTallyValueLabel;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public Controller( View view )
    {
        this.view = view;
        view.addListener( this );
        
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
        	Semaphore semaphore = new Semaphore( 1 );
            preferences.saveToDisk();
            
            try
            {
    	        semaphore.acquire();
            }
            catch( InterruptedException e1 )
            {
    	        e1.printStackTrace();
            }
            JDialog configDialog = new Configuration( this, semaphore );
            
            configDialog.setVisible( true );
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
        // Get the issues from the Jira server
		ArrayList<String[]> issues = getJiraIssues();
        view.setJiraComboBox( issues );
        view.initComponents();
    
		setDateLabel();
		
        // Set the tally for today on the dayTallyLabel
		dayTally = database.getTodayTally();
        view.getBottomPanel().setBorder( createTallyBorder() );
        
		SwingUtilities.invokeLater( new Runnable()
		{
			
			@Override
			public void run()
			{
		        view.setVisible( true );				
			}
		} );
    }
    
    /**
     * Set current date for the date label and time for the time label.  
     * This is updated every second to ensure the values are kept current.
     * 
     * If the clock reaches Friday 4pm, the beer alarm goes off.
     * 
     * @return Today's date formatted for the dataLabel in the GUI.
     */
    public void setDateLabel()
    {      
		TimerTask dateUpdater = new TimerTask()
		{

			@Override
			public void run()
			{
				DateTime dateTime = new DateTime(); // reference to current date/time
				DateTime.Property dayOfWeek = dateTime.dayOfWeek();
				String minutes = (dateTime.getMinuteOfHour() < 10 ? "0" + dateTime.getMinuteOfHour()
				                                                  : String.valueOf(dateTime.getMinuteOfHour()) );
				String hours = (String)(dateTime.getHourOfDay() < 10 ? "0" + dateTime.getHourOfDay()
					                                                 : String.valueOf(dateTime.getHourOfDay()) );
				
				// Set date label
				view.setDate( "<html><div align='center' font color='white'>" + 
							  "<font size='6'>" +
				              dayOfWeek.getAsText() + 
				              "</font>" +
				              "<br>" + 
							  "<font size='4'>" +
				              dateTime.getDayOfMonth() + 
				              "/" +
				              dateTime.getMonthOfYear() +
				              "</font></div></html>" );
				
				// Set clock with a blinking ':'.
				String colonText = "";
				if( dateTime.getSecondOfMinute() % 2 == 0 )
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
				setBeerAlarm( dateTime );
			}
		};
		Timer timer = new Timer();
		timer.scheduleAtFixedRate( dateUpdater, 0, 1000 );
	}

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

        }
    }
	
    /**
     * @return
     */
    public ArrayList<String[]> getJiraIssues()
    {

        int i = 0;
        ArrayList<String[]> issues = new ArrayList<String[]>();

        if( jiraInterface.getToken() != null )
        {
            try
            {
            	issues = jiraInterface.getIssues();
            	preferences.setIssuesForProject( issues );
            }
            catch( XmlRpcException e )
            {
    	        e.printStackTrace();
            }
        }
        else
        {
        	issues = preferences.getIssuesForProject();
        }

        // Can't get issues from preferences or the server, get user to check configuration.
        while( issues == null || issues.isEmpty() )
        {
        	int selection = JOptionPane.showConfirmDialog( view, 
                        	                               "Could not connect to Jira server.  Please" +
                        	                               " check your login details and try again.",
                        	                               "Jira Connection", 
                        	                               JOptionPane.OK_CANCEL_OPTION );
        	if( selection == JOptionPane.OK_OPTION )
        	{
				Semaphore semaphore = new Semaphore( 1 );
				Configuration configuration = new Configuration( this, semaphore );
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
        
        // We can still start without any issues but can't perform any of the following.
        if( issues != null )
    	{
        	preferences.setIssuesForProject( issues );
    
            String[][] jiraData = new String[2][issues.size()];
    
            for ( String[] entries : issues )
            {
                jiraData[0][i] = entries[0];
                jiraData[1][i] = entries[2];
                i++;
            }
    	}
        
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
     * 
     * @param table - task table showing today's task statistics.
     * @param time - the stop time.
     * @param dayTallyLabel - the label showing the total time worked today.
     * @param weekTallyLabel - the label showing the total time worked this
     *            week.
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
                view.getBottomPanel().setBorder( createTallyBorder() );
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
     * 
     * @return String - total amount of time worked on the current day.
     */
    public String calculateDayTally()
    {
        Period tally = database.getTodayTally();

        return Time.displayDelta( tally );
    }
    
    /**
     * Read the database to determine how many hours:minutes that have been
     * complete on the current day.
     * 
     * A week starts on Sunday and ends on Saturday.
     * 
     * @return String - total amount of time worked this week.
     */
    public String calculateWeekTally()
    {
        // What is today's index in the week
        int today = new DateTime().getDayOfWeek();
        DateTime todayDate = new DateTime();
        DateTime startOfWeek = new DateTime();
        startOfWeek = todayDate.minusDays( today );

        Period tally = database.getWeekTally( startOfWeek, todayDate );

        return Time.displayDelta( tally );
    }
    
    /**
     * Get the time worked per day and week and set them as the title for a border.
     * 
     * @return A Border with the total time worked per day and week.
     */
    private TitledBorder createTallyBorder()
    {   
        TitledBorder border = BorderFactory.createTitledBorder( null, 
                                                                "Week: " 
                                                                + calculateWeekTally() + 
                                                                "   Day: " + 
                                                                Time.displayDelta( dayTally ), 
                                                                TitledBorder.LEFT, 
                                                                TitledBorder.TOP, 
                                                                new Font( "Lucida Grande", 1, 12 ), 
                                                                Color.WHITE );
        
        return border;
    }
    
    /**
     * This method takes in the dateLabel from the GUI and advances or
     * decrements it by one day for each time it is clicked.
     * 
     * @param label - the dateLabel from the GUI that is to be changed.
     * @param direction - increment or decrement the date.
     */
    public void incrememntDecrementDayButtonActionPerformed( final JLabel label, String direction )
    {
//        if ( direction.equals( INCREMENT_DAY ) )
//        {
//            dayOffset++;
//        }
//        else if ( direction.equals( DECREMENT_DAY ) )
//        {
//            dayOffset--;
//        }
//
//        SwingUtilities.invokeLater( new Runnable()
//        {
//
//            public void run()
//            {
//                DateTime dateTime = new DateTime(); // reference to current
//                // date/time
//                label.setText( dateTime.plusDays( dayOffset ).getDayOfMonth()
//                        + "/" + dateTime.getMonthOfYear() + "/"
//                        + dateTime.getYear() % 100 );
//            }
//        } );
    }

	/**
	 * Stop recording and exit.
	 * @return 
	 */
	public int exitTimeLord()
    {
    	stopRecording();
    	System.out.println("close");
		System.exit( 0 );
    	
		// Amazing, we can never get to here
    	return 0;
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

    public View getView()
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
	
	
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------



}
