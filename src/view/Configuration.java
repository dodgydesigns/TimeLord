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
import javax.swing.SwingUtilities;

import model.JiraInterface;
import model.Preferences;
import model.SqlInterface;
import net.miginfocom.swing.MigLayout;
import controller.Controller;

/**
 * Provide configuration control for JIRA clients. This Swing component uses
 * MigLayout (http://www.miglayout.com/).
 * 
 * @author mullsy
 * 
 */
public class Configuration extends JDialog
{
    /**
	 * 
	 */
    private static final long serialVersionUID = -1869584080186759637L;
    // --------------------------------------------------------------------------
    // MEMBER VARIABLES
    // --------------------------------------------------------------------------

    // -------------------------------------------------------------------------
    // SWING VARIABLES
    // -------------------------------------------------------------------------
    private JPanel jiraPanel;
    private JPanel databasePanel;

    private JLabel logoLabel;
    private JLabel tryJIRASettingsLabel;

    private JTextField urlTextField;
    private JTextField usernaneTextField;
    private JPasswordField passwordPasswordField;
    private JComboBox<String> projectCombo;
    private JCheckBox connectToJira;
    
    private JButton tryButton;
    private JButton clearDBButton;
    private JButton backupDBButton;
    private JButton restroreDBButton;
    private JButton cancelButton;
    private JButton okButton;
    
    private JFrame view;
    private Preferences preferences;
    private SqlInterface database;

    private JiraInterface jiraInterface;

	private Semaphore semaphore;

	private boolean connected;

	private Map<String, String> projects;

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    public Configuration( Controller controller, Semaphore semaphore )
    {
        super( controller.getView() );
        
        this.semaphore = semaphore;
        this.view = (JFrame) controller.getView();
        this.preferences = controller.getPreferences();
        this.database = controller.getDatabase();
        this.jiraInterface = controller.getJiraInterface();

        super.setBackground( new Color( 255, 255, 255 ) );

        setLayout( new MigLayout("", "[][grow]", "[][shrink 0]") );

        initComponents();
        loadPreferences();
        layoutComponents();
        setLocationRelativeTo( view );
    }

    // -------------------------------------------------------------------------
    // PRIVATE METHODS
    // -------------------------------------------------------------------------
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

    private void initComponents()
    {
    	setModal( true );
        getContentPane().setBackground( Color.WHITE );

        // Panels
        jiraPanel = new JPanel( new MigLayout( "", "[][grow]", "[shrink 0][shrink 0]" ) );
        jiraPanel.setBorder( BorderFactory.createTitledBorder( "JIRA" ) );
        jiraPanel.setBackground( new java.awt.Color( 245, 245, 255 ) );
        databasePanel = new JPanel( new MigLayout( "", "[][grow]", "[][shrink 0]" ) );
        databasePanel.setBorder( BorderFactory.createTitledBorder( "Database" ) );
        databasePanel.setBackground( new java.awt.Color( 245, 245, 255 ) );

        // Labels
        logoLabel = new JLabel( new javax.swing.ImageIcon( getClass().getResource( "/media/timelord logo.png" ) ) );
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
        
        // Buttons
        tryButton = new JButton( "Try..." );
        tryButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                tryButtonActionPerformed();
            }
        } );
        
        connectToJira = new JCheckBox();
        connectToJira.addActionListener( new ActionListener()
		{
			
			@Override
			public void actionPerformed( ActionEvent e )
			{
				preferences.setConnectToJiraAtStartup( connectToJira.isSelected() );
			}
		} );
        
        backupDBButton = new JButton( "Backup" );
        backupDBButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                database.backupDB();
            }
        } );

        restroreDBButton = new JButton( "Restore" );
        restroreDBButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                database.restoreDB();
            }
        } );

        clearDBButton = new JButton( "Clear" );
        clearDBButton.addActionListener( new ActionListener()
        {

            @Override
            public void actionPerformed( ActionEvent e )
            {
            	
                SwingUtilities.invokeLater( new Runnable()
                {
                    @Override
                    public void run()
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
                } );
            }
        } );

        cancelButton = new JButton( "Cancel" );
        cancelButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                // TODO: undo any changes
            	semaphore.release();
                dispose();
            }
        } );

        okButton = new JButton( "OK" );
        okButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
            	setPreferenceValues();
            	// Set this now that they've selected a project
                preferences.setCurrentProject( (String)projectCombo.getSelectedItem() );
                semaphore.release();
                dispose();
            }
        } );
    }

    private void layoutComponents()
    {
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
    }

    /*
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
    

	private void clearButtonActionPerformed( int choice )
    {
    	// Destroy the database and all data.
        if( choice == 0 )
        {
        	try
            {
	            database.dropDataBase();
            }
            catch( SQLException e )
            {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
            }        
        }
    }
	
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
}
