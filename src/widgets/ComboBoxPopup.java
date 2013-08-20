package widgets;

import java.awt.Component;

import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.plaf.basic.BasicComboBoxRenderer;

/**
 * This class is responsible for propagating a JComboBox with data from the JIRA server including
 * task names and details. The details are displayed as a tooltip when a cell has focus.
 * 
 * @author mullsy
 * 
 */
public class ComboBoxPopup extends JComboBox<String>
{
	/**
	 * ComboBoxPopup
	 */
	private static final long serialVersionUID = 7402002995279508860L;
	private String[] tooltips;


	/**
	 * Constructor
	 * 
	 * @param items JIRA tasks for the user account in use.
	 * @param tooltips The description for each task.
	 */
	public ComboBoxPopup( String[] items, String[] tooltips )
	{
		super( items );
		this.tooltips = tooltips;

		setRenderer( new ComboBoxPopupRenderer() );
	}

	/**
	 * This class provides the renderer to put the JIRA task in the JComboBox and the task
	 * description in as a tooltip.
	 * 
	 * @author mullsy
	 * 
	 */
	class ComboBoxPopupRenderer extends BasicComboBoxRenderer
	{
		/**
		 * ComboBoxPopupRenderer
		 */
		private static final long serialVersionUID = 3176097106064685430L;


		public Component getListCellRendererComponent( JList list, Object value, int index,
		                                               boolean isSelected, boolean cellHasFocus )
		{
			if( list != null )
			{
				if( isSelected )
				{
					setBackground( list.getSelectionBackground() );
					setForeground( list.getSelectionForeground() );
					if( index >= 0 )
						list.setToolTipText( tooltips[index] );
				}
				else
				{
					setBackground( list.getBackground() );
					setForeground( list.getForeground() );
				}
				setFont( list.getFont() );
				setText( (value == null) ? "" : value.toString() );
			}
			return this;
		}
	}
}
