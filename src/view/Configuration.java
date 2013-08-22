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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
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

import model.JiraInterface;
import model.Preferences;
import model.SqlInterface;
import net.miginfocom.swing.MigLayout;
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

    private JTextField urlTextField;
    private JTextField usernaneTextField;
    private JPasswordField passwordPasswordField;
    private JComboBox<String> projectCombo;
    private JCheckBox connectToJira;
    private JLabel tryJIRASettingsLabel;
    
    private JFrame view;
    private Preferences preferences;
    private SqlInterface database;

    private JiraInterface jiraInterface;
	private Semaphore semaphore;
	private boolean connected;
	private Map<String, String> projects;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
    public Configuration( Controller controller, Semaphore semaphore )
    {
        super( controller.getView() );
        
        this.semaphore = semaphore;
        this.view = (JFrame) controller.getView();
        this.preferences = controller.getPreferences();
        this.database = controller.getDatabase();
        this.jiraInterface = controller.getJiraInterface();

        initComponents();
        loadPreferences();
        setLocationRelativeTo( view );
    }

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
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
    }

    /**
     * Create and layout the UI components.
     */
    private void initComponents()
    {
        setBackground( new Color( 255, 255, 255 ) );
        setLayout( new MigLayout("", "[][grow]", "[][shrink 0]") );
    	setModal( true );
        getContentPane().setBackground( Color.WHITE );

        // Panels
        JPanel jiraPanel = new JPanel( new MigLayout( "", "[][grow]", "[shrink 0][shrink 0]" ) );
        jiraPanel.setBorder( BorderFactory.createTitledBorder( "JIRA" ) );
        jiraPanel.setBackground( new java.awt.Color( 245, 245, 255 ) );
        
        JPanel databasePanel = new JPanel( new MigLayout( "", "[][grow]", "[][shrink 0]" ) );
        databasePanel.setBorder( BorderFactory.createTitledBorder( "Database" ) );
        databasePanel.setBackground( new java.awt.Color( 245, 245, 255 ) );

        // Labels
		JLabel logoLabel =
		    new JLabel(
		                new javax.swing.ImageIcon(
		                                           getClass().getResource(
		                                                                   "/media/timelord logo.png" ) ) );
		tryJIRASettingsLabel = new JLabel( "Try to connect using current settings:" );
		logoLabel.setBackground( Color.WHITE );
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
        
        // Buttons
        JButton tryButton = new JButton( "Try..." );
        tryButton.addActionListener( this );
        JButton backupDBButton = new JButton( "Backup" );
        backupDBButton.addActionListener( this );
        JButton restroreDBButton = new JButton( "Restore" );
        restroreDBButton.addActionListener( this );
        JButton clearDBButton = new JButton( "Clear" );
        clearDBButton.addActionListener( this );
        JButton cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( this );
        JButton okButton = new JButton( "OK" );
        okButton.addActionListener( this );
        
        add( logoLabel, "span, wrap 15" );
        add( jiraPanel, "wrap, span, grow" );
        add( databasePanel, "wrap 15, span, grow" );

        jiraPanel.add( connectToJira );
        jiraPanel.add( new JLabel( "Connect to Jira at startup" ), "wrap, span, grow" );
        jiraPanel.add( new JLabel( "JIRA URL:" ) );
        jiraPanel.add( urlTextField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "Username:" ) );
        jiraPanel.add( usernaneTextField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "Password:" ) );
        jiraPanel.add( passwordPasswordField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "Project:" ) );
        jiraPanel.add( projectCombo, "wrap 15, span, grow" );

        jiraPanel.add( tryJIRASettingsLabel, "span 2" );
        jiraPanel.add( tryButton, "right, width 80:80:80" );

        databasePanel.add( new JLabel( "Clear DB:" ) );
        databasePanel.add( clearDBButton, "gapleft 30, width 80:80:80, wrap" );
        databasePanel.add( new JLabel( "Backup DB:" ) );
        databasePanel.add( backupDBButton, "gapleft 30, width 80:80:80, wrap" );
        databasePanel.add( new JLabel( "Restore DB:" ) );
        databasePanel.add( restroreDBButton, "gapleft 30, width 80:80:80," );

        add( cancelButton, "right, width 80:80:80," );
        add( okButton, "right, width 80:80:80," );

        pack();
        urlTextField.requestFocus();
    }

    /**
     * Get the settings just entered and try to connect to JIRA server.
     */
    private void tryButtonActionPerformed()
    {
    	setPreferenceValues();
    	
    	connected = jiraInterface.connectToJira();
        if( connected )
        {
            tryJIRASettingsLabel.setText( "Success!" );
            
            // Retrieve all the projects and hold in jiraInterface.
            jiraInterface.getProjects();

            projects = new HashMap<String,String>();
            projects.putAll( jiraInterface.getProjectsKeyName() );
            preferences.setProjects( projects );

			DefaultComboBoxModel<String> model =
			    new DefaultComboBoxModel<String>(
			                                      projects.values().toArray(
			                                                                 new String[projects.size()] ) );
            projectCombo.setModel( model );
        }
        else
        {
            tryJIRASettingsLabel.setText( "Sorry, failed!" );
        }
    }

	/**
	 * This method drops the database thereby clearing any data contained in it.
	 * 
	 * @param choice Whether the OK or Cancel button was clicked.
	 */
	private void clearButtonActionPerformed( int choice )
    {
    	// Destroy the database and all data.
        if( choice == JOptionPane.OK_OPTION )
        {
        	try
            {
	            database.dropDataBase();
            }
            catch( SQLException e )
            {
	            e.printStackTrace();
            }        
        }
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
                tryButtonActionPerformed();
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
            	clearButtonActionPerformed( choice );            
        	}
            if( buttonText.toLowerCase().equals( "ok" ) )
            {
            	setPreferenceValues();
            	// Set this now that they've selected a project
                preferences.setCurrentProject( (String)projectCombo.getSelectedItem() );
                semaphore.release();
                dispose();
            }
            if( buttonText.toLowerCase().equals( "cancel" ) )
            {
                // TODO: undo any changes
            	semaphore.release();
                dispose();            
            }
        }
		else if( source instanceof JCheckBox )
		{
			preferences.setConnectToJiraAtStartup( connectToJira.isSelected() );
		}
    }

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     INNER CLASSES
	//----------------------------------------------------------
}
