/*
 * Copyright 2013 Dodgy Designs
 * 
 * 
 * NOTICE: All information contained herein is, and remains the property of
 * Dodgy Designs. The intellectual and technical concepts contained herein are
 * proprietary to Dodgy Designs. Dissemination of this information or
 * reproduction of this material is strictly forbidden unless prior written
 * permission is obtained from Dodgy Designs.
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

// ----------------------------------------------------------
// CONSTRUCTORS
// ----------------------------------------------------------

// ----------------------------------------------------------
// INSTANCE METHODS
// ----------------------------------------------------------

// //////////////////////////////////////////////////////////////////////////////////////////
// ///////////////////////////// Accessor and Mutator Methods
// ///////////////////////////////
// //////////////////////////////////////////////////////////////////////////////////////////

// ----------------------------------------------------------
// INNER CLASSES
// ----------------------------------------------------------import
// gui.MainFrame;


import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import view.Splash;
import view.View;
import controller.Controller;

/**
 * The launch point for TimeLord. The mainframe and controller are both
 * constructed here and made available to all other classes by a static
 * reference.
 * 
 * This is also where the look and feel is set depending on the host OS.
 * 
 * @author mullsy
 * 
 */
public class TimeLord
{
    // ----------------------------------------------------------
    // STATIC VARIABLES
    // ----------------------------------------------------------

    private static View view;

    // ----------------------------------------------------------
    // INSTANCE VARIABLES
    // ----------------------------------------------------------
    /**
     * @param args - There wont be any for now
     */
    public static void main( String[] args )
    {
        view = new View();
        new Controller( view );
        
        // Cobined with a commandline argument:
        // -splash:/Users/mullsy/Documents/workspace/TimeLord/src/media/splash.png
        // This will display a splash screen as the JVM starts and connections are made to Jira.
        // The main TimeLord window will then be displayed.
        new Splash( view );

        String osName = System.getProperty( "os.name" );
        if ( osName.toLowerCase().contains( "mac" ) )
        {
            setMacLAF();
        }
        else if ( osName.toLowerCase().contains( "windows" ) )
        {
            setWindowsLAF();
        }
        else
        {
            setLinuxLAF();
        }
    }

    /**
     * Be sure to take advantage of the most beautiful OS on earth.
     */
    private static void setMacLAF()
    {
        // Take the menu bar off the JFrame
        System.setProperty( "apple.laf.useScreenMenuBar", "true" );

        // set the name of the application menu item
        System.setProperty( "com.apple.mrj.application.apple.menu.about.name",
                            "Time:Lord" );
    }

    private static void setLinuxLAF()
    {

    }

    /**
     * Do what we can about Windows.
     */
    private static void setWindowsLAF()
    {
        view.setIconImage( new javax.swing.ImageIcon(
                View.class.getResource( "/media/icon.png" ) ).getImage() );

        try
        {
            for ( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() )
            {
                if ( ( info.getName().toLowerCase().contains( "nimbus" ) ) )
                {
                    UIManager.setLookAndFeel( info.getClassName() );
                    break;
                }
            }
        }
        catch ( UnsupportedLookAndFeelException e )
        {
            // handle exception
        }
        catch ( ClassNotFoundException e )
        {
            // handle exception
        }
        catch ( InstantiationException e )
        {
            // handle exception
        }
        catch ( IllegalAccessException e )
        {
            // handle exception
        }
    }
}
