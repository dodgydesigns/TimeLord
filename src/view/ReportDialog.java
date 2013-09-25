package view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import widgets.ColourCellRenderer;
import widgets.ComboBoxPopup;
import widgets.JCalendarButton;
import widgets.JCalendarPopup;
import widgets.MultiLineCellRenderer;
import controller.Controller;

public class ReportDialog extends JDialog implements ActionListener, PropertyChangeListener
{
	/**
	 * ReportDialog
	 */
	private static final long serialVersionUID = -3227961033713711510L;
	private Controller controller;
	private JPanel dataPanel;
	private JPanel buttonPanel;
	private JCalendarButton fromDatePicker;
	private JCalendarButton toDatePicker;
	private JRadioButton timePeriodRadioButton;
	private JRadioButton taskRadioButton;
	private JPanel bottomPanel;
	private JButton printButton;
	private JButton publishButton;
	private JButton closeButton;
	private JTable taskTable;
	private ComboBoxPopup jiraComboBox;
	private JLabel fromLabel;
	private JLabel toLabel;

	public ReportDialog( Controller controller )
	{
		super();

		this.controller = controller;

		initComponents();
		layoutComponents();
	}

	private void initComponents()
	{
		addWindowListener( new WindowAdapter()
		{

			@Override
			public void windowClosing( WindowEvent e )
			{
				controller.getView().showDialog();
			}
		} );

		setLocation( new Point( 50, 50 ) );
		setLayout( new GridBagLayout() );

		drawTaskTable();
		setJiraComboBox();

		// Panels
		dataPanel = new JPanel();
		dataPanel.setPreferredSize( new Dimension( 650, 100 ) );
		dataPanel.setBorder( BorderFactory.createTitledBorder( "Data" ) );
		dataPanel.add( taskTable );

		buttonPanel = new JPanel();
		buttonPanel.setLayout( new GridBagLayout() );
		buttonPanel.setBorder( BorderFactory.createTitledBorder( "Selection" ) );

		bottomPanel = new JPanel();
		bottomPanel.setBorder( BorderFactory.createTitledBorder( "Action" ) );

		// Radio buttons
		timePeriodRadioButton = new JRadioButton( "Time Period" );
		timePeriodRadioButton.addActionListener( this );

		taskRadioButton = new JRadioButton( "Task" );
		taskRadioButton.addActionListener( this );

		// Labels
		fromLabel = new JLabel( "From" );
		toLabel = new JLabel( "To" );

		// Buttons
		fromDatePicker = new JCalendarButton();
		fromDatePicker.addPropertyChangeListener( this );
		toDatePicker = new JCalendarButton();
		toDatePicker.addPropertyChangeListener( this );


		printButton = new JButton( "Print" );
		printButton.addActionListener( this );
		publishButton = new JButton( "Publish" );
		publishButton.addActionListener( this );
		closeButton = new JButton( "Close" );
		closeButton.addActionListener( this );
	}

	private void layoutComponents()
	{
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		constraints.insets = new Insets( 0, 0, 0, 0 );
		buttonPanel.add( timePeriodRadioButton, constraints );
		constraints.insets = new Insets( 0, 250, 0, 0 );
		buttonPanel.add( taskRadioButton, constraints );

		constraints = new GridBagConstraints();
		constraints.gridy = 1;
		constraints.anchor = GridBagConstraints.WEST;
		constraints.insets = new Insets( 0, 0, 10, 0 );
		buttonPanel.add( fromDatePicker, constraints );
		constraints.insets = new Insets( 0, -60, 10, 0 );
		buttonPanel.add( fromLabel, constraints );
		constraints.insets = new Insets( 0, -260, 10, 0 );
		buttonPanel.add( toDatePicker, constraints );
		constraints.insets = new Insets( 0, -215, 10, 0 );
		buttonPanel.add( toLabel, constraints );
		constraints.insets = new Insets( 0, -65, 10, 0 );
		buttonPanel.add( jiraComboBox, constraints );

		constraints = new GridBagConstraints();
		constraints.anchor = GridBagConstraints.EAST;
		constraints.insets = new Insets( 10, 200, 10, 10 );
		bottomPanel.add( printButton, constraints );
		bottomPanel.add( publishButton, constraints );
		bottomPanel.add( closeButton, constraints );

		constraints = new GridBagConstraints();
		constraints.gridy = 0;
		constraints.fill = GridBagConstraints.HORIZONTAL;
		add( dataPanel, constraints );
		constraints.gridy = 1;
		add( buttonPanel, constraints );
		constraints.gridy = 2;
		add( bottomPanel, constraints );
	}

	/**
	 * Set the model and appearance of the task data table and set initial values.
	 */
	protected void drawTaskTable()
	{
		taskTable = new JTable( controller.getTaskTableModel() );

		taskTable.setAutoCreateRowSorter( true );
		taskTable.setSelectionMode( ListSelectionModel.MULTIPLE_INTERVAL_SELECTION );

		JTableHeader taskTableHeader = taskTable.getTableHeader();
		taskTableHeader.setBorder( BorderFactory.createLineBorder( Color.GRAY ) );
		taskTableHeader.setBackground( new Color( 85, 91, 106 ) );
		taskTableHeader.setForeground( Color.WHITE );

		// Disable auto resizing
		taskTable.setAutoResizeMode( JTable.AUTO_RESIZE_OFF );

		setupColumns();
	}

	/**
	 * Setup the column widths, colours and behaviours.
	 */
	public void setupColumns()
	{
		TableColumnModel colModel = taskTable.getColumnModel();
		// Start
		TableColumn col0 = colModel.getColumn( 0 );
		col0.setPreferredWidth( 70 );
		col0.setCellRenderer( new ColourCellRenderer( Color.WHITE, new Color( 10, 150, 10 ) ) );

		// Stop
		TableColumn col1 = colModel.getColumn( 1 );
		col1.setPreferredWidth( 70 );
		col1.setCellRenderer( new ColourCellRenderer( Color.WHITE, new Color( 150, 10, 10 ) ) );

		// Delta
		TableColumn col2 = colModel.getColumn( 2 );
		col2.setMinWidth( 105 );
		col2.setPreferredWidth( 110 );
		col2.setCellRenderer( new ColourCellRenderer( Color.WHITE, Color.BLACK ) );

		// JIRA
		TableColumn col3 = colModel.getColumn( 3 );
		col3.setPreferredWidth( 90 );
		col3.setCellRenderer( new ColourCellRenderer( Color.WHITE, Color.BLUE ) );

		// Description
		final TableColumn col4 = colModel.getColumn( 4 );
		col4.setPreferredWidth( 300 );
		col4.setCellRenderer( new MultiLineCellRenderer() );
	}

	/**
	 * @param issues
	 */
	public void setJiraComboBox()
	{
		// If the ComboPopupBox hasn't been created yet, do it ready to fill from setModel(...).
		jiraComboBox = new ComboBoxPopup( new String[]{ "" }, new String[]{ "" } );

		// Get the issues from the Jira server
		ArrayList<String[]> issues = controller.getJiraIssues();

		if( issues != null )
		{
			String[][] jiraData = new String[2][issues.size()];

			int i = 0;
			for( String[] entries : issues )
			{
				jiraData[0][i] = entries[0];
				jiraData[1][i] = entries[2];
				i++;
			}
			jiraComboBox.setPopupComboModel( jiraData[0], jiraData[1] );
		}
		else
			jiraComboBox.setPopupComboModel( new String[1], new String[1] );
	}

	public void showDialog()
	{
		SwingUtilities.invokeLater( new Runnable()
		{

			@Override
			public void run()
			{
				pack();
				setVisible( true );
				controller.getView().setVisible( false );
			}
		} );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	//////////////////////////////////// Action Handlers ///////////////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	@Override
	public void actionPerformed( ActionEvent e )
	{
		String actionCommand = e.getActionCommand();
		System.out.println( actionCommand );
		if( actionCommand.equals( "from" ) )
		{

		}
		else if( actionCommand.equals( "to" ) )
		{
			JCalendarPopup.createCalendarPopup( new Date(), toDatePicker );
		}

		else if( actionCommand.equals( "Close" ) )
		{
			dispose();
			controller.getView().showDialog();
		}
	}

	@Override
	public void propertyChange( PropertyChangeEvent e )
	{
		SimpleDateFormat dateFormat = new SimpleDateFormat( "E d/M/yyyy" );
		
		if( e.getSource() == fromDatePicker )
		{
			if (e.getNewValue() instanceof Date)
				fromLabel.setText( dateFormat.format( (Date)e.getNewValue()).toString() );
		}
		else if( e.getSource() == toDatePicker )
		{
			if (e.getNewValue() instanceof Date)
				toLabel.setText( dateFormat.format( (Date)e.getNewValue()).toString() );
		}
	}
}
