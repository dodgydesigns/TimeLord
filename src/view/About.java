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
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import widgets.BareBonesBrowserLaunch;
import controller.Controller;

/**
 * This class provides details about the application including version information and a link
 * to the project on GitHub.
 */
public class About extends JDialog
{
	//----------------------------------------------------------
	//                    STATIC VARIABLES
	//----------------------------------------------------------
	/**
	 * About
	 */
    private static final long serialVersionUID = 76185848949981982L;

	//----------------------------------------------------------
	//                   INSTANCE VARIABLES
	//----------------------------------------------------------
	private JLabel iconLabel;
	private JPanel detailsPanel;
	private JLabel detailsLabel;
	private JButton closeButton;
	private JButton linkButton;

	//----------------------------------------------------------
	//                      CONSTRUCTORS
	//----------------------------------------------------------
	public About()
	{
		super();

		initialiseComponents();
		layoutComponents();

		pack();
		setVisible( true );
	}

	//----------------------------------------------------------
	//                    INSTANCE METHODS
	//----------------------------------------------------------
	private void initialiseComponents()
	{
		setSize( 200, 300 );
		setUndecorated( true );
		setBackground( new Color( 0, 0, 0, 0 ) );
		setLocationRelativeTo( getParent() );

		ImageIcon iconImage = new ImageIcon( this.getClass().getResource( "/media/icon.png" ) );
		iconLabel = new JLabel( iconImage );
		detailsPanel = new JPanel();
		detailsPanel.setPreferredSize( new Dimension( 340, 80 ) );
		detailsPanel.setBackground( new Color( 16, 60, 156 ) );

		detailsLabel = new JLabel( "TimeLord v." + Controller.VERSION );
		detailsLabel.setFont( new Font( "Lucida Grande", Font.BOLD, 20 ) );
		detailsLabel.setForeground( Color.WHITE );
		
		linkButton = new JButton();
		linkButton.setBorderPainted( false );
		linkButton.setBackground( new Color( 0, 0, 0, 0 ) );
		linkButton.setIcon( new ImageIcon(this.getClass().getResource("/media/link.png")) );
		linkButton.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				BareBonesBrowserLaunch.openURL( "https://github.com/dodgydesigns/TimeLord" );
			}
		} );


		closeButton = new JButton( "X Close" );
		closeButton.setBorderPainted( false );
		closeButton.addActionListener( new ActionListener()
		{

			@Override
			public void actionPerformed( ActionEvent e )
			{
				dispose();
			}
		} );
	}

	private void layoutComponents()
	{
		setLayout( new GridBagLayout() );
		detailsPanel.setLayout( new GridBagLayout() );
		GridBagConstraints constraints = new GridBagConstraints();

		constraints.gridy = 0;
		detailsPanel.add( detailsLabel );
		constraints.gridy = 1;
		detailsPanel.add( linkButton, constraints );
		constraints.gridy = 2;
		detailsPanel.add( closeButton, constraints );

		constraints.gridy = 0;
		add( iconLabel, constraints );
		constraints.gridy = 1;
		constraints.insets = new Insets( 20, 0, 0, 0 );
		add( detailsPanel, constraints );
	}

	////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
	////////////////////////////////////////////////////////////////////////////////////////////
	
	//----------------------------------------------------------
	//                     INNER CLASSES
	//----------------------------------------------------------
}
