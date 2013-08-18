package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.ComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import net.miginfocom.swing.MigLayout;
import widgets.ComboBoxPopup;
import control.ControllerOld;

/**
 * This class provides a generic warning dialog with one or two messages and two
 * response buttons. Each component is configurable to suit the calling method.
 * 
 * @author mullsy
 */
public class WarningDialog extends JDialog
{
    /**
     * WarningDialog
     */
    private static final long serialVersionUID = -7039363573780481046L;
    // --------------------------------------------------------------------------
    // CLASS CONSTANTS
    // --------------------------------------------------------------------------
    public static final String CLEAR_DB = "clear.db";
    public static final String JIRA_URL = "jira.url";

    // -------------------------------------------------------------------------
    // MEMBER VARIABLES
    // -------------------------------------------------------------------------
    private JLabel pictureLabel;
    private JLabel warningTitleLabel;
    private JLabel messageLabel;

    private String caller;

    private JButton affirmativeButton;
    private JButton negativeButton;

    // --------------------------------------------------------------------------
    // CONSTRUCTOR
    // --------------------------------------------------------------------------
    public WarningDialog( JFrame parent, 
                          String caller, 
                          String warning,
                          String message, 
                          String affirmative, 
                          String negative )
    {
        super( parent, true );

        MigLayout layout = new MigLayout( "", // Layout Constraints
                "[100][300]", // Column constraints
                "[][]" ); // Row constraint

        setLayout( layout );

        this.caller = caller;

        initComponents( warning, message, negative, affirmative );
        layoutComponents();
        setLocationRelativeTo( parent );
    }

    // --------------------------------------------------------------------------
    // PRIVATE METHODS
    // --------------------------------------------------------------------------
    private void initComponents( String warning, String message,
            String affirmative, String negative )
    {
        getContentPane().setBackground( Color.WHITE );

        setPreferredSize( new Dimension( 500, 210 ) );
        setSize( new Dimension( 500, 210 ) );

        pictureLabel = new JLabel( new ImageIcon( getClass().getResource(
                "/media/warning.png" ) ) );
        warningTitleLabel = new JLabel( warning );
        messageLabel = new JLabel( message );

        affirmativeButton = new JButton( affirmative );
        affirmativeButton.addActionListener( new ActionListener()
        {

            @Override
            public void actionPerformed( ActionEvent arg0 )
            {
                affirmativeButtonActionPerformed();
            }
        } );

        negativeButton = new JButton( negative );
        negativeButton.addActionListener( new ActionListener()
        {

            @Override
            public void actionPerformed( ActionEvent e )
            {
                negativeButtonActionPerformed();
            }
        } );
    }

    private void layoutComponents()
    {
        add( pictureLabel, "span 1 2" );
        add( warningTitleLabel, "center, span, wrap" );
        add( messageLabel, "center, span" );
        add( negativeButton, "cell 2 2, center, split" );
        add( affirmativeButton, "wrap 30" );
    }

    /*
     * Depending on the calling method, perform the appropriate action for a
     * positive response.
     */
    protected void negativeButtonActionPerformed()
    {
        if ( caller.equals( JIRA_URL ) )
        {
            ControllerOld.getInstance().usingJIRA = false;
            MainFrame.getInstance().enableJIRAPanel( false );
            dispose();
        }
        else if ( caller.equals( CLEAR_DB ) )
        {
        }
        dispose();
    }

    /*
     * Depending on the calling method, perform the appropriate action for a
     * positive response.
     */
    protected void affirmativeButtonActionPerformed()
    {
        if ( caller.equals( JIRA_URL ) )
        {
            ComboBoxPopup result = ControllerOld.getInstance()
                    .generateJIRAData();
            if ( result.getItemCount() > 0 )
            {
                ControllerOld.getInstance()
                        .getJiraItemsCombo()
                        .setModel( (ComboBoxModel) result );
            }

            else
            {
                String[] messageString = { "Not Available" };
                String[] toolTipString = { "Could not connect to JIRA server to retrieve tasks." };
                result = new ComboBoxPopup( messageString, toolTipString );
            }
            dispose();
        }
        else if ( caller.equals( CLEAR_DB ) )
        {
            try
            {
                ControllerOld.getInstance().getDB().dropDataBase();
                ControllerOld.getInstance().getDB().createTimeLordDB();
            }
            catch ( SQLException e )
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            SwingUtilities.invokeLater( new Runnable()
            {

                @Override
                public void run()
                {
                    ( (DefaultTableModel) MainFrame.getInstance()
                            .getTaskTable()
                            .getModel() ).setRowCount( 0 );
                    MainFrame.getInstance()
                            .getTaskTable()
                            .setModel(
                                    MainFrame.getInstance()
                                            .setupTaskTableModel() );
                    MainFrame.getInstance().setupColumns();

                    dispose();
                }
            } );
        }
    }

    // /**
    // * Only for testing
    // * @param args
    // */
    // public static void main(String args[])
    // {
    // JFrame frame = new JFrame();
    // WarningDialog warning = new WarningDialog(frame, WarningDialog.CLEAR_DB,
    // "Are you sure you want to clear the database?",
    // "<html><FONT SIZE=\"5\" COLOR=\"FF0000\">All records will be deleted!</h1></html>",
    // "Go Ahead", "Cancel");
    // warning.setVisible(true);
    // }
}
