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

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

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
    private XmlRpcClient rpcClient;
	private Map<String,String> projects;
	private Preferences preferences;

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
    	this.preferences = controller.getPreferences();
    	
        jiraURL = preferences.getJiraUrl();
        jiraUsername = preferences.getUserName();
        jiraPassword = preferences.getPassword();
        
        createRpcClient();
    }

    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------

	private boolean createRpcClient()
    {
		boolean success = false;
		 XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();

	        try
	        {
	            // Create a trust manager that does not validate certificate chains
	            TrustManager[] trustAllCerts = new TrustManager[] {

	            new X509TrustManager()
	            {
	                public X509Certificate[] getAcceptedIssuers()
	                {
	                    return null;
	                }

	                public void checkClientTrusted( X509Certificate[] certs,
	                        String authType )
	                {
	                    // Trust always
	                }

	                public void checkServerTrusted( X509Certificate[] certs,
	                        String authType )
	                {
	                    // Trust always
	                }
	            } };

	            // Install the all-trusting trust manager
	            SSLContext sc = SSLContext.getInstance( "SSL" );
	            // Create empty HostnameVerifier
	            HostnameVerifier hv = new HostnameVerifier()
	            {
	                public boolean verify( String arg0, SSLSession arg1 )
	                {
	                    return true;
	                }
	            };

	            sc.init( null, trustAllCerts, new java.security.SecureRandom() );
	            HttpsURLConnection.setDefaultSSLSocketFactory( sc.getSocketFactory() );
	            HttpsURLConnection.setDefaultHostnameVerifier( hv );

	            rpcClient = new XmlRpcClient();
	            config.setServerURL( new URL( jiraURL + RPC_PATH ) );
	            rpcClient.setConfig( config );
	            
	            success = true;
	        }
	        catch ( MalformedURLException e )
	        {
	            System.out.println( "Malformed URL Exception" );
	        }
	        catch ( NoSuchAlgorithmException e )
	        {
	            e.printStackTrace();
	        }
	        catch ( KeyManagementException e )
	        {
	            e.printStackTrace();
	        }
	        
	        return success;
    }

	/**
	 * This method attempts to connect to the provided Jira URL using the supplied credentials.
	 * If the service can be contacted, a list of the Jira projects will be fetched which indicates
	 * a successful connection.
	 * 
	 * @return Whether a successful connection was possible using the supplied connection details.
	 */
	public boolean connectToJira()
    {		
        jiraURL = preferences.getJiraUrl();
        jiraUsername = preferences.getUserName();
        jiraPassword = preferences.getPassword();
        
		if( rpcClient != null &&
			!jiraUsername.isEmpty() &&
			!jiraPassword.isEmpty() )
		{
    		try
            {
    	        login();
            }
            catch( XmlRpcException e )
            {
            }
		}
		
        return loginToken != null;
    }
	
    /**
     * Get all the projects on the JIRA server.
     * 
     */
    public void getProjects()
    {
        // Retrieve projects
        Vector<String> loginTokenVector = new Vector<String>( 1 );
        loginTokenVector.add( loginToken );

        Object[] projectsKeyName = null;
        try
        {
	        projectsKeyName = (Object[]) rpcClient.execute( "jira1.getProjectsNoSchemes", 
	                                                 loginTokenVector );
        }
        catch( XmlRpcException e )
        {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }

        projects = new HashMap<String,String>();
        // Print projects
        for ( int i = 0; i < projectsKeyName.length; i++ )
        {
            Map<?, ?> project = (Map<?, ?>) projectsKeyName[i];
            
            projects.put( project.get( "key" ).toString(), project.get( "name" ).toString() );
        }        
    }

    /**
     * Get the issues in a project for a given user from the JIRA server.
     * 
     * @param user - the user assigned each issue
     * @param project - the project the issues belong to
     * 
     * @return Array of strings containing the issue, a description and date.
     * 
     * @throws XmlRpcException
     */
    public ArrayList<String[]> getIssues() throws XmlRpcException
    {
        ArrayList<String[]> returnArray = new ArrayList<String[]>();

        Vector<String> parameters = new Vector<String>( 2 );
        parameters.add( loginToken );
        parameters.add( preferences.getCurrentProject() );

        if ( loginToken != null )
        {
            Object[] issues = (Object[]) rpcClient.execute( "jira1.getIssuesFromTextSearch", parameters );

            // Add matched results to returnArray and print them out.
            for ( int i = 0; i < issues.length; i++ )
            {
                Map<?, ?> issue = (Map<?, ?>) issues[i];

                if ( issue.get( "assignee" ) != null && 
                	 issue.get( "assignee" ).equals( preferences.getUserName() ) )
                {
                    if ( !issue.get( "status" ).equals( STATUS_CLOSED )
                            && !issue.get( "status" ).equals( STATUS_RESOLVED ) )
                    {
                        String[] details = new String[3];
                        details[0] = (String) issue.get( "key" );
                        details[1] = (String) issue.get( "created" );
                        details[2] = (String) issue.get( "summary" );
                        returnArray.add( details );
                    }
                }
            }
        }

        return returnArray;
    }

    /**
     * Use the login credentials to try and log into the JIRA server.
     * 
     * @return - a token representing the current login
     * 
     * @throws XmlRpcException
     */
    public void login() throws XmlRpcException
    {
    	createRpcClient();
    	
        // Login and retrieve login token
        Vector<String> loginParams = new Vector<String>( 2 );
        loginParams.add( jiraUsername );
        loginParams.add( jiraPassword );

        try
        {
            loginToken = (String) rpcClient.execute( "jira1.login", loginParams );
        }
        catch ( org.apache.xmlrpc.XmlRpcException e )
        {
            SwingUtilities.invokeLater( new Runnable()
            {
                public void run()
                {
                    JOptionPane.showMessageDialog( null, "Could not connect to server: \n" + 
							 							 jiraURL + "\n\n" +
							 							 "Please try again later.", "Jira Connection",
						   								 JOptionPane.WARNING_MESSAGE );
                }
            } );
        }
    }

    /**
     * Logout of JIRA server.
     * 
     * @throws XmlRpcException
     */
    public void logout() throws XmlRpcException
    {
        Vector<String> loginTokenVector = new Vector<String>( 1 );
        loginTokenVector.add( loginToken );

        if ( loginToken != null )
        {
            if ( (Boolean) rpcClient.execute( "jira1.logout", loginTokenVector ) )
            {
                Logger.getLogger( "TimeLord" ).info(
                        "Could not log out of JIRA" );
            }

        }
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
            return false;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    public String getToken()
    {
    	return loginToken;
    }
    
    /**
     * @return
     */
    public Map<String,String> getProjectsKeyName()
    {
    	return projects;
    }
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}
