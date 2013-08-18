package widgets;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

/**
 * The cell renderer puts a text area in a cell which allows for multiple line
 * cells in the JTable. The JTextArea needs to be put in a JPanel (with
 * MiGLayout) to keep the text in line with the text in other components.
 * 
 * A whole lot of 'random' garbage seems to be required to get the cells to
 * render at the correct height. Any help with this would be greatly
 * appreciated.
 * 
 * @author mullsy
 */
public class MultiLineCellRenderer extends JPanel implements TableCellRenderer
{

    /**
     * MultiLineCellRenderer
     */
    private static final long serialVersionUID = -3137510919777000555L;

    // Make the preferred row height 1 pixel more than expected (don't ask me
    // why!).
    // The normal row height is 16 pixels.
    private static int ROWHEIGHT = 23;

    private JTextArea text;

    /**
     * Constructor
     */
    public MultiLineCellRenderer()
    {
        // Set up JPanel
        super();
        setLayout( new MigLayout( "", "8[fill]", "4[fill]4" ) );
        setOpaque( true );
        setBackground( Color.WHITE );

        // Set up JTextArea
        text = new JTextArea();
        text.setBorder( BorderFactory.createEmptyBorder() );
        text.setColumns( 22 );
        text.setLineWrap( true );
        text.setWrapStyleWord( true );
        // LimitedLinesDocument textDocument = new LimitedLinesDocument(3, 97);
        // text.setDocument(textDocument);
        Font font = new Font( "Helvetica", Font.PLAIN, 14 );
        text.setFont( font );
    }

    @Override
    public Component getTableCellRendererComponent( JTable table, 
                                                    Object value,
                                                    boolean isSelected, 
                                                    boolean hasFocus, 
                                                    int row, 
                                                    int column )
    {
        if( value != null )
        {
            text.setText( ( value == null ) ? "" : value.toString() );
    
            // Based on the preferred width of the Description column,
            // MainFrame.DESCRIPTION_COL_WIDTH, allow characters per
            // line. Then multiply by the preferred row height -1. (again...you tell
            // me-IT WORKS!).
            int descriptionLength = value.toString().length() / 30 + 1;
    
            // Don't set the height every refresh, just when it is first set up
            // which is when the row height will be less
            // than 16 pixels.
            if ( table.getRowHeight() < descriptionLength * ROWHEIGHT )
            {
                int preferredRowHeight = descriptionLength * ROWHEIGHT;
    
                // Set height 1 more than the standard row height (32 pixels)
                add( text, "height " + String.valueOf( preferredRowHeight ) + "::" );
    
                table.setRowHeight( row, preferredRowHeight );
            }
        }
        return this;
    }
}