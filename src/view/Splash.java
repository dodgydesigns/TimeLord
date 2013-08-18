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

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Graphics2D;
import java.awt.SplashScreen;

import controller.Controller;

/**
 * This class uses the new Java SpashScreen to display an image before the JVM
 * has even started. When the JVM is up, it then displays the version of
 * TimeLord and then shows the status of the JIRA connection.
 * 
 * @author mullsy
 */
public class Splash
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public Splash( Frame parent )
    {
        super();

        final SplashScreen splash = SplashScreen.getSplashScreen();

        if ( splash != null )
        {
            Graphics2D graphicArea = splash.createGraphics();
            if ( graphicArea != null )
            {
                renderVersion( graphicArea );

                for ( int i = 0; i < 100; i++ )
                {
                    renderLoading( graphicArea, i );
                    splash.update();
                    try
                    {
                        Thread.sleep( 90 );
                    }
                    catch ( InterruptedException e )
                    {
                    }
                }
                splash.close();
                parent.setVisible( true );
                parent.toFront();
            }
            else
            {
                System.out.println( "graphicArea is null" );
            }
        }
    }

    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    /**
     * This draws the version on the splash screen as soon as the JVM is
     * available.
     * 
     * @param graphicsAreag The graphics object the text is drawn on.
     * @param frame A count of 1 to 100 to animate the splash screen.
     */
    /**
     * @param graphicArea
     */
    static void renderVersion( Graphics2D graphicArea )
    {
        graphicArea.setComposite( AlphaComposite.Clear );
        graphicArea.fillRect( 120, 140, 200, 40 );
        graphicArea.setPaintMode();
        graphicArea.setColor( new Color( 20, 20, 110 ) );
        graphicArea.drawString( "v. " + Controller.VERSION, 10, 287 );
    }

    /**
     * This writes the JIRA connection status on the splash screen a moment
     * after the JVM is available. It uses frame to fade the message out as the
     * connection is established.
     * 
     * @param graphicArea The graphics object the text is drawn on.
     * @param frame A count of 1 to 100 to animate the splash screen.
     */
    static void renderLoading( Graphics2D graphicArea, int frame )
    {
        final String[] comps = { "Connecting to JIRA", "Connecting to JIRA",
                "Connecting to JIRA" };

        if ( frame > 30 )
        {
            graphicArea.setComposite( AlphaComposite.Clear );
            graphicArea.fillRect( 120, 140, 200, 40 );
            graphicArea.setPaintMode();
            graphicArea.setColor( new Color( 2 * frame + 30, 2 * frame + 30, 2 * frame + 30 ) );
            graphicArea.drawString( comps[( frame / 5 ) % 3], 80, 287 );
        }
    }
    
    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}