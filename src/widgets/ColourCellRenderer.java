package widgets;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * Applied background and foreground color to single column of a JTable in order
 * to distinguish it apart from other columns.
 */
public class ColourCellRenderer extends JLabel implements TableCellRenderer
{
	/**
	 * ColorCellRenderer
	 */
	private static final long serialVersionUID = 775511371958729017L;
	private static final String START = "<HTML><FONT FACE=arial><TABLE cellspacing=3 width=250><tr><td width=90>";
	@SuppressWarnings("unused")
	private static final String MIDDLE = "</td><td align:left>";
	private static final String END = "</TD></tr></table></FONT></html>";

	Color bkgndColor, fgndColor;

	public ColourCellRenderer(Color bkgnd, Color foregnd)
	{
		super();
		bkgndColor = bkgnd;
		fgndColor = foregnd;
		setVerticalAlignment(TOP);
	}

	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column)
	{
		if (value == null)
		{
			value = "N/A";
		}

		if (value instanceof JLabel)
		{
			this.setBackground(bkgndColor);
			this.setForeground(fgndColor);
			this.setText(START + ((JLabel) value).getText() + END);
		}
		else
		{
			super.setBackground(bkgndColor);
			super.setForeground(fgndColor);
			super.setText(START + (String) value + END);
		}

		return this;
	}
}
