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

import java.awt.Frame;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JDialog;
import javax.swing.table.TableModel;

import model.JiraInterface;
import model.Preferences;
import model.SqlInterface;
import model.TaskTableModel;

import org.apache.xmlrpc.XmlRpcException;
import org.joda.time.DateTime;

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
    private TaskTableModel taskTableModel;
    private Preferences preferences;
    private JiraInterface jiraInterface;

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
        taskTableModel = new TaskTableModel( database );

        startupTimeLord();
    }
    
    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    private void startupTimeLord()
    {
    	// Get the configuration dialog ready in case there is a problem starting up e.g.
    	// there is no preferences file or the Jira connection failed.
    	Semaphore semaphore = new Semaphore( 1 );
        
        // If a preferences file does not already exist, create the file and display the 
        // configuration dialog.
        if( !preferences.readExistingPrefsFromDisk() || !jiraInterface.connectedToJira() )
        {
            preferences.saveToDisk();
            
            try
            {
    	        semaphore.acquire();
            }
            catch( InterruptedException e1 )
            {
    	        // TODO Auto-generated catch block
    	        e1.printStackTrace();
            }
            JDialog configDialog = new Configuration( this, semaphore );
            
            configDialog.setVisible( true );
        }
     
        // Start drawing the GUI
		ArrayList<String[]> issues = getJiraIssues();
        view.setJiraComboBox( issues );
        view.initComponents();
    
		setDateLabel();
		System.out.println(preferences.getCurrentProject());
        view.setVisible( true );
    }
    
    /**
     * Set current date for the date label.  This is updated every hour to make sure it remains
     * current.
     * 
     * @return Today's date formatted for the dataLabel in the GUI
     */
    public void setDateLabel()
    {      
        TimerTask dateUpdater = new TimerTask()
        {
            
            @Override
            public void run()
            {
                DateTime dateTime = new DateTime();             // reference to current date/time
                DateTime.Property dayOfWeek = dateTime.dayOfWeek();
               
                view.setDate( "<html>" +
                              "<div align='center' font color='white'>" +
                              dayOfWeek.getAsText() +
                              "<br>" +
                              dateTime.getDayOfMonth() + 
                              "/" + 
                              dateTime.getMonthOfYear() + 
                              "/" + 
                              dateTime.getYear() % 100 +
                              "</div>" +
                              "</html>" );  
                System.out.println("update date");
            }
        };
        Timer timer = new Timer();
        timer.scheduleAtFixedRate( dateUpdater, 0, 60*60*1000 );
    }

    /**
     * @return
     */
    public int getDayOfWeek()
    {
        DateTime dt = new DateTime();
        
        return dt.getDayOfWeek();
    }

    /**
     * @return
     */
    public ArrayList<String[]> getJiraIssues()
    {

        int i = 0;
        ArrayList<String[]> issues = new ArrayList<String[]>();

        try
        {
        	issues = jiraInterface.getIssues();
        }
        catch( XmlRpcException e )
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        String[][] jiraData = new String[2][issues.size()];

        for ( String[] entries : issues )
        {
            jiraData[0][i] = entries[0];
            jiraData[1][i] = entries[2];
            i++;
        }

        return issues;
    }
    
//    /**
//     * @param project
//     * @return
//     * @throws InterruptedException
//     * @throws ExecutionException
//     */
//    @SuppressWarnings( "unchecked" )
//    private ArrayList<String[]> getJiraIssues( final String project )
//            throws InterruptedException, ExecutionException
//    {
//        final SwingWorker<?, ?> jiraWorker = new SwingWorker<ArrayList<String[]>, Void>()
//        {
//
//            @Override
//            protected ArrayList<String[]> doInBackground() throws Exception
//            {
//                ArrayList<String[]> jiraIssues = new ArrayList<String[]>();
//                try
//                {
//                    String token = jiraInterface.getToken();
//
//                    if ( token != null )
//                    {
//                        jiraIssues = jiraInterface.getIssues( preferences.getUserName(), project );
//                        jiraInterface.logout();
//                    }
//                    else
//                    {
//                        // new Warning(m_tmpController, "Get JIRA Issues", true,
//                        // "There was a major probelm connecting to the JIRA server.",
//                        // "OK");
//                    }
//                }
//                catch ( XmlRpcException e )
//                {
//                    e.printStackTrace();
//                }
//                return jiraIssues;
//            }
//        };
//        jiraWorker.execute();
//
//        return (ArrayList<String[]>) jiraWorker.get();
//    }
    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    public SqlInterface getDatabase()
    {
        return database;
    }
    
    public TableModel getTaskTableModel()
    {
        return taskTableModel;
    }
    
    public Preferences getPreferences()
    {
        return preferences;
    }

    public Frame getView()
    {
        return view;
    }
    
    public JiraInterface getJiraInterface()
    {
        return jiraInterface;
    }
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}
