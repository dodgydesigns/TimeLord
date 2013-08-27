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
package model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.DateTime;

/**
 * Store the user configuration details and preferences as key/value pair in a
 * hash map that is stored on disk.
 * 
 * If the preference file is not found, create a dummy set of valid settings.
 * 
 */
public class Preferences implements Serializable
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
    /**
     * Preferences
     */
    private static final long serialVersionUID = 5995511796426365665L;

    private static final String JIRA_USERNAME = "jira.user";
    private static final String JIRA_PASSWORD = "jira.password";
    private static final String JIRA_URL = "jira.url";
    private static final String JIRA_PROJECTS = "jira.project";
    private static final String JIRA_CURRENT_PROJECT = "jira.current.project";
    private static final String JIRA_ISSUES = "jira.issues";
	private static final String CONNECT_TO_JIRA = "jira.connect";
    private static final String JIRA_KILL_FOR_BEER = "jira.kill";
    private static final String JIRA_BEER_TIME = "jira.beer.time";

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    private static String prefsPath;
    private static HashMap<String, Object> preferences;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public Preferences()
    {
        // Sort out location and name of preferences file
        prefsPath = System.getProperty( "user.home" ) + "/" + ".TimeLord.prefs";
        
        preferences = new HashMap<String, Object>();
    }

    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    /**
     * Write the map containing all the preferences to disk.
     */
    public void saveToDisk()
    {
        try
        {
            FileOutputStream fileOut = new FileOutputStream( prefsPath );
            ObjectOutputStream out = new ObjectOutputStream( fileOut );

            out.writeObject( preferences );

            out.close();
            fileOut.close();

        }
        catch ( FileNotFoundException e )
        {
            e.printStackTrace();
        }
        catch ( IOException e )
        {
            e.printStackTrace();
        }
    }

    /**
     * Read the preferences map from disk
     */
    @SuppressWarnings( "unchecked" )
    public boolean readExistingPrefsFromDisk()
    {
        boolean preferencesExist = false;
        
        // Path path = Paths.get(m_prefsPath);
        File prefs = new File( prefsPath );
        
        // If the preferences file doesn't exist, write a new one to disk.
        preferencesExist = prefs.exists();
        
        if( preferencesExist )
        {
            try
            {
                FileInputStream fileIn = new FileInputStream( prefs );
                ObjectInputStream in = new ObjectInputStream( fileIn );
    
                preferences = (HashMap<String,Object>)in.readObject();
    
                in.close();
                fileIn.close();
    
            }
            catch ( ClassNotFoundException e )
            {
                System.out.println( "Prefs: couldn't class in prefs file." );
            }
            catch ( FileNotFoundException e )
            {
                System.out.println( "Prefs: couldn't find prefs file." );
            }
            catch ( IOException e )
            {
                System.out.println( "Prefs: IO error." );
            }
        }
        
        return preferencesExist;
    }

    /**
     * @return The Jira username used for logging into Jira.
     */
    public String getUserName()
    {
        String result = (String)preferences.get( JIRA_USERNAME );
        if ( result == null )
        {
            result = "";
        }

        return result;
    }

    /**
     * @param The Jira username used for logging into Jira.
     */
    public void setUserName( String user )
    {
        preferences.put( JIRA_USERNAME, user );
        saveToDisk();
    }

    /**
     * @return The Jira password used for logging into Jira.
     */
    public String getPassword()
    {
        readExistingPrefsFromDisk();
        String result = (String)preferences.get( JIRA_PASSWORD );
        if ( result == null )
        {
            result = "";
        }

        return result;
    }

    /**
     * @param The Jira password used for logging into Jira.
     */
    public void setPassword( String password )
    {
        preferences.put( JIRA_PASSWORD, password );
        saveToDisk();
    }

    /**
     * @return The address of the Jira server.
     */
    public String getJiraUrl()
    {
        String result = (String)preferences.get( JIRA_URL );
        if ( result == null )
        {
            result = "";
        }

        return result;
    }

    /**
     * @param The address of the Jira server.
     */
    public void setJiraUrl( String url )
    {
        preferences.put( JIRA_URL, url );
        saveToDisk();
    }

	/**
	 * @param selected
	 */
	public void setConnectToJiraAtStartup( boolean selected )
    {
		preferences.put( CONNECT_TO_JIRA, selected );
		saveToDisk();
    }
	
	/**
	 * @return
	 */
	public boolean connectToJiraAtStartup()
	{
		boolean connect = preferences.get(CONNECT_TO_JIRA) != null ? (Boolean)preferences.get( CONNECT_TO_JIRA )
		                                                           : false;
		return connect;
	}
	
    /**
     * @return The project the user is logging time against.
     */
    @SuppressWarnings("unchecked")
    public Map<String,String> getProjects()
    {
    	return (Map<String,String>)preferences.get( JIRA_PROJECTS );
    }
    
    /**
     * @param The project the user is logging time against.
     */
    public void setProjects( Map<String,String> projects )
    {
        	preferences.put( JIRA_PROJECTS, projects );
        	saveToDisk();
    }
    
    /**
     * @param issues
     */
    public void setIssuesForProject( ArrayList<String[]> issues )
    {
		preferences.put( JIRA_ISSUES, issues );
    	saveToDisk();
    }
    
    /**
     * @return
     */
    @SuppressWarnings("unchecked")
    public ArrayList<String[]> getIssuesForProject()
    {
    	return (ArrayList<String[]>)preferences.get( JIRA_ISSUES );
    }
    
    /**
     * @return The project the user is logging time against.
     */
    public String getCurrentProject()
    {
    	return (String)preferences.get( JIRA_CURRENT_PROJECT );
    }
    
    /**
     * @param The project the user is logging time against.
     */
    public void setCurrentProject( String project )
    {
        preferences.put( JIRA_CURRENT_PROJECT, project );
        saveToDisk();
    }

	/**
	 * @param kill Whether the application should exit.
	 */
	public void setKillOnBeer( Boolean kill )
    {
	    preferences.put( JIRA_KILL_FOR_BEER, kill );
    }
	
	/**
	 * @return Whether the application should exit at beer o'clock time.
	 */
	public boolean getKillOnBeer()
    {
	    return (Boolean)preferences.get( JIRA_KILL_FOR_BEER );
    }
    
	/**
	 * @return The day and time the alarm should go off.
	 */
	public DateTime getBeerTime()
	{
		String dateString = (String)preferences.get( JIRA_BEER_TIME );
		
		if( dateString == null )
			dateString = "5 15 59";
		
		String when[] = dateString.split( " " );

		DateTime whenDateTime = new DateTime( 1, 
		                                      1, 
		                                      Integer.valueOf(when[0]) + 1, 
		                                      Integer.valueOf(when[1]), 
		                                      Integer.valueOf(when[2]) );
		
	    return whenDateTime;
	}
	
	/**
	 * @param When the alarm should go off.

	 */
	public void setBeerTime( String beerTime )
	{
		preferences.put( JIRA_BEER_TIME, beerTime );	
	}
	
    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}
