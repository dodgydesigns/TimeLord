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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Vector;

import controller.Controller;
/**
 * This class provides an interface to a JIRA server and allows the retrieval of
 * project and issue data.
 * 
 * The credentials used to log into the server and the project details are
 * retrieved from the preferences file. Currently only issues with 'open' status
 * will be returned.
 * 
 * @author mullsy
 * 
 */
//----------------------------------------------------------
//                    STATIC VARIABLES
//----------------------------------------------------------
//----------------------------------------------------------
//                   INSTANCE VARIABLES
//----------------------------------------------------------
//----------------------------------------------------------
//                      CONSTRUCTORS
//----------------------------------------------------------
/**
     * Constructor that logs in to specified server and retrieves a login token.
     * This token can then be used for further interrogation of the server for
     * projects and issues.
     * 
     */
//----------------------------------------------------------
//                    INSTANCE METHODS
//----------------------------------------------------------
/**
     * Get all the projects on the JIRA server.
     * 
     * @throws XmlRpcException
     */
/**
     * Get the issues in a project for a given user from the JIRA server.
     * 
     * @param user - the user assigned each issue
     * @param project - the project the issues belong to
     * 
     * @return - an array of strings containing the issue, a description and
     *         date.
     * 
     * @throws XmlRpcException
     */
/**
     * Use the login credentials to try and log into the JIRA server.
     * 
     * @return - a token representing the current login
     * 
     * @throws XmlRpcException
     */
// Login and retrieve login token
/**
     * Logout of JIRA server.
     * 
     * @throws XmlRpcException
     */
/**
     * This is a helper utility to make sure that a valid URL is used to try and
     * connect to the JIRA server.
     * 
     * @param urlName The URL to test
     * 
     * @return Whether the URL is valid or not
     */
// note : you may also need
// HttpURLConnection.setInstanceFollowRedirects(false)
// return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
////////////////////////////////////////////////////////////////////////////////////////////
/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
////////////////////////////////////////////////////////////////////////////////////////////
//----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
    
/**
 * This class provides an interface to a JIRA server and allows the retrieval of
 * project and issue data.
 * 
 * The credentials used to log into the server and the project details are
 * retrieved from the preferences file. Currently only issues with 'open' status
 * will be returned.
 * 
 * @author mullsy
 * 
 */
public class JiraInterface
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
    private static final String RPC_PATH = "/rpc/xmlrpc";
    @SuppressWarnings( "unused" )
    private static String STATUS_OPEN = "1";
    @SuppressWarnings( "unused" )
    private static String STATUS_IN_PROGRESS = "3";
    @SuppressWarnings( "unused" )
    private static String STATUS_REOPENED = "4";
    private static String STATUS_RESOLVED = "5";
    private static String STATUS_CLOSED = "6";
    @SuppressWarnings( "unused" )
    private static String STATUS_SUBMIT = "10000";
    @SuppressWarnings( "unused" )
    private static String STATUS_REJECTED = "10001";

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    private String jiraURL;
    private String jiraUsername;
    private String jiraPassword;
    private String loginToken;
    
    private Controller controller;
    private String token;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    /**
     * Constructor that logs in to specified server and retrieves a login token.
     * This token can then be used for further interrogation of the server for
     * projects and issues.
     * 
     */
    public JiraInterface( Controller controller )
    {
        this.controller = controller;
        
        jiraURL = controller.getPreferences().getJiraUrl();
        jiraUsername = controller.getPreferences().getUserName();
        jiraPassword = controller.getPreferences().getPassword();
    }

    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    public boolean attemptConnection()
    {
        boolean connected = false;
        
            
        return connected;
    }
    
    /**
     * Get all the projects on the JIRA server.
     * 
     * @throws XmlRpcException
     */
    public void getProjects()
    {

    }

    /**
     * Get the issues in a project for a given user from the JIRA server.
     * 
     * @param user - the user assigned each issue
     * @param project - the project the issues belong to
     * 
     * @return - an array of strings containing the issue, a description and
     *         date.
     * 
     * @throws XmlRpcException
     */
    public ArrayList<String[]> getIssues( String user, String project )
    {
        ArrayList<String[]> issues = new ArrayList<>();
        
        return issues;
    }

    /**
     * Use the login credentials to try and log into the JIRA server.
     * 
     * @return - a token representing the current login
     * 
     * @throws XmlRpcException
     */
    public String login()
    {
        // Login and retrieve login token
        Vector<String> loginParams = new Vector<String>( 2 );
        loginParams.add( jiraUsername );
        loginParams.add( jiraPassword );



        return loginToken;
    }

    /**
     * Logout of JIRA server.
     * 
     * @throws XmlRpcException
     */
    public void logout()
    {

    }

    /**
     * This is a helper utility to make sure that a valid URL is used to try and
     * connect to the JIRA server.
     * 
     * @param urlName The URL to test
     * 
     * @return Whether the URL is valid or not
     */
    public static boolean exists( String urlName )
    {
        try
        {
            HttpURLConnection.setFollowRedirects( false );
            // note : you may also need
            // HttpURLConnection.setInstanceFollowRedirects(false)
            HttpURLConnection con = (HttpURLConnection) new URL( urlName ).openConnection();
            con.setRequestMethod( "HEAD" );
            // return (con.getResponseCode() == HttpURLConnection.HTTP_OK);
            return true;
        }
        catch ( Exception e )
        {
            e.printStackTrace();
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------


}
