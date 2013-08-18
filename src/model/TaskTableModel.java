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

package model;

import java.sql.SQLException;

import javax.swing.table.DefaultTableModel;

import org.joda.time.DateTime;

public class TaskTableModel extends DefaultTableModel
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------
    private static final long serialVersionUID = 6892726129271491905L;

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public TaskTableModel( SqlInterface database )
    {
        super( new String[][] { { "", "", "", "", "" } }, 
               new String[] { "Start", 
                              "Stop", 
                              "Delta", 
                              "JIRA", 
                              "Description" } );
        try
        {
            String[][] startData = database.getTodaysEntries( Time.getReferableDate( new DateTime() ) );
            new DefaultTableModel( startData, 
                                   new String[] { "Start", 
                                                  "Stop", 
                                                  "Delta", 
                                                  "JIRA", 
                                                  "Description" } );
//
//            controller.setTaskCount( database.getTodayTaskCount() );
//
//            dayValueLabel.setText( controller.calculateDayTally() );
//            weekValueLabel.setText( controller.calculateWeekTally() );
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }
    }
    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
    
    //----------------------------------------------------------
    //                     INNER CLASSES
    //----------------------------------------------------------
}
