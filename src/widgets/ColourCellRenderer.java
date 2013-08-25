
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
package widgets;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

/**
 * This class applies background and foreground colours to single column of a JTable in order to 
 * distinguish it apart from other columns.
 * 
 * And it looks pretty.
 */
public class ColourCellRenderer extends JLabel implements TableCellRenderer
{

	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * ColorCellRenderer
	 */
	private static final long serialVersionUID = 775511371958729017L;

	private static final String START = "<html>" + "<font face=arial size='20'>"
	                                    + "<table cellspacing=3 width=250>" 
	                                    + "<tr><td width=90>";
	@SuppressWarnings("unused")
	private static final String MIDDLE = "</td><td align:left>";
	private static final String END = "</td></tr></table></font></html>";


	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	Color bkgndColor, fgndColor;

	public ColourCellRenderer( Color bkgnd, Color foregnd )
	{
		super();
		bkgndColor = bkgnd;
		fgndColor = foregnd;
	}

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	public Component getTableCellRendererComponent( JTable table, 
	                                                Object value, 
	                                                boolean isSelected,
	                                                boolean hasFocus, 
	                                                int row, 
	                                                int column )
	{
		if( value == null )
		{
			value = "N/A";
		}

		if( value instanceof JLabel )
		{
			this.setBackground( bkgndColor );
			this.setForeground( fgndColor );
			this.setText( START + ((JLabel)value).getText() + END );
		}
		else
		{
			super.setBackground( bkgndColor );
			super.setForeground( fgndColor );
			super.setText( START + (String)value + END );
		}

		return this;
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     INNER CLASSES
	//----------------------------------------------------------
}
