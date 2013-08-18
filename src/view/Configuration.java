package view;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import controller.Controller;

import model.JiraInterface;
import model.Preferences;
import model.SqlInterface;
import net.miginfocom.swing.MigLayout;

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
    private JTextField projectTextField;

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

    // -------------------------------------------------------------------------
    // CONSTRUCTOR
    // -------------------------------------------------------------------------
    public Configuration( Controller controller )
    {
        super( controller.getView() );
        
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
        urlTextField.setText( preferences.getJiraUrl() );
        passwordPasswordField.setText( preferences.getPassword() );
        usernaneTextField.setText( preferences.getUserName() );
        projectTextField.setText( preferences.getProject() );
    }

    private void initComponents()
    {
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
        projectTextField = new JTextField();

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
                final WarningDialog warning = new WarningDialog(
                        view,
                        WarningDialog.CLEAR_DB,
                        "Are you sure you want to clear the database?",
                        "<html><FONT SIZE=\"5\" COLOR=\"FF0000\">All records will be deleted!</h1></html>",
                        "Cancel", "Go Ahead" );

                SwingUtilities.invokeLater( new Runnable()
                {
                    @Override
                    public void run()
                    {
                        warning.setVisible( true );
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
                dispose();
            }
        } );

        okButton = new JButton( "OK" );
        okButton.addActionListener( new ActionListener()
        {
            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                preferences.setProject( projectTextField.getText() );
                char[] pw = passwordPasswordField.getPassword();
                preferences.setPassword( String.valueOf( pw ) );
                preferences.setUserName( usernaneTextField.getText() );
                preferences.setJIRAURL( urlTextField.getText() );

                dispose();
            }
        } );
    }

    private void layoutComponents()
    {
        add( logoLabel, "span, wrap 15" );
        add( jiraPanel, "wrap, span, grow" );
        add( databasePanel, "wrap 15, span, grow" );

        jiraPanel.add( new JLabel( "JIRA URL:" ) );
        jiraPanel.add( urlTextField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "Username:" ) );
        jiraPanel.add( usernaneTextField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "Password:" ) );
        jiraPanel.add( passwordPasswordField, "wrap, span, grow" );
        jiraPanel.add( new JLabel( "Project:" ) );
        jiraPanel.add( projectTextField, "wrap 15, span, grow" );

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
        preferences.setProject( projectTextField.getText() );
        char[] pw = passwordPasswordField.getPassword();
        preferences.setPassword( String.valueOf( pw ) );
        preferences.setUserName( usernaneTextField.getText() );
        preferences.setJIRAURL( urlTextField.getText() );

        if( jiraInterface.attemptConnection() )
        {
            tryJIRASettingsLabel.setText( "Success!" );
        }
        else
        {
            tryJIRASettingsLabel.setText( "Sorry, failed!" );
        }
        System.out.println("1");
        jiraInterface.getProjects();
//        ComboBoxPopup result = controller.generateJIRAData();
//        if ( result != null )
//        {
//            tryJIRASettingsLabel.setText( "Success!" );
//            view.enableJIRAPanel( true );
//            view.getJiraComboBox()
//                    .setModel( result.getModel() );
//            ControllerOld.getInstance().usingJIRA = true;
//        }
//        else
//        {
//            tryJIRASettingsLabel.setText( "Sorry, failed!" );
//        }
    }

    /*
     * Just for testing
     */
    // public static void main(String args[])
    // {
    // JFrame frame = new JFrame();
    // Configuration config = new Configuration(frame);
    // config.setVisible(true);
    // }
}
