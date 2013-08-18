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
import java.util.HashMap;

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
    private static final String JIRA_PROJECT = "jira.project";

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    private static String prefsPath;
    private static HashMap<String, String> preferences;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public Preferences()
    {
        // Sort out location and name of preferences file
        prefsPath = System.getProperty( "user.home" ) + 
                                          "/" + 
                                          ".TimeLord.prefs";
        
        preferences = new HashMap<String, String>();
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
    
                preferences = (HashMap<String, String>)in.readObject();
    
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
        String result = preferences.get( JIRA_USERNAME );
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
        String result = preferences.get( JIRA_PASSWORD );
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
        String result = preferences.get( JIRA_URL );
        if ( result == null )
        {
            result = "";
        }

        return result;
    }

    /**
     * @param The address of the Jira server.
     */
    public void setJIRAURL( String url )
    {
        preferences.put( JIRA_URL, url );
        saveToDisk();
    }

    /**
     * @return The project the user is logging time against.
     */
    public String getProject()
    {
        String result = preferences.get( JIRA_PROJECT );
        if ( result == null )
        {
            result = "";
        }

        return result;
    }
    
    /**
     * @param The project the user is logging time against.
     */
    public void setProject( String project )
    {
        preferences.put( JIRA_PROJECT, project );
        saveToDisk();
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}
