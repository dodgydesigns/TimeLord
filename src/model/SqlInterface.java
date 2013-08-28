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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.SwingUtilities;

import org.joda.time.DateTime;
import org.joda.time.MutableDateTime;
import org.joda.time.Period;

import view.View;

/**
 * This provides a wrapper for the SQLite database that forms the data
 * repository for TimeLord.
 * 
 * @author mullsy
 * 
 */
public class SqlInterface
{
    //----------------------------------------------------------
    //                    STATIC VARIABLES
    //----------------------------------------------------------

    //----------------------------------------------------------
    //                   INSTANCE VARIABLES
    //----------------------------------------------------------
    private Connection dbConnection;
    private Statement statementHandler;
    private PreparedStatement prep;
    private int taskCount;
    private String dbPath;
    private View view;

    //----------------------------------------------------------
    //                      CONSTRUCTORS
    //----------------------------------------------------------
    public SqlInterface( View view ) throws Exception
    {
        this.view = view;

        // Put the database somewhere suitable
        dbPath = System.getProperty( "user.home" ) + "/" + ".TimeLord.db";

        Class.forName( "org.sqlite.JDBC" );
        dbConnection = DriverManager.getConnection( "jdbc:sqlite:" + dbPath );
        statementHandler = dbConnection.createStatement();
        
        // Create an empty database if it doesn't already exist.
        createTimeLordDB();
    }
    
    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------
    /**
     * Create the database if it doesn't exist or return a handle to it if it
     * does.
     * 
     * @throws SQLException
     */
    public void createTimeLordDB() throws SQLException
    {
        statementHandler.execute( "create table if not exists timelord (/*1*/date, " +
        		                  "/*2*/start, /*3*/stop, /*4*/delta, /*5*/jira, " +
        		                  "/*6*/description, /*7*/dayTally);" );
    }
    

    /**
     * The controller records data to the database as it is generated i.e. when
     * the start and stop buttons are pressed.
     * 
     * @param date Today's date.
     * @param start The start time for the work being recorded.
     * @param jira The Jira issue code.
     * @param description The description of the work being done.
     * 
     * @return Prepared statement that can be used to access the database data.
     * 
     * @throws SQLException
     */
    public PreparedStatement setStartParameters( String date, 
                                                 DateTime start,
                                                 String jira, 
                                                 String description ) throws SQLException
    {
        prep = dbConnection.prepareStatement( "insert into timelord values (?, ?, ?, ?, ? ,?, ?);" );
        prep.setString( 1, date );
        prep.setObject( 2, start );
        prep.setString( 5, jira );
        prep.setString( 6, description );

        return prep;
    }

    /**
     * The controller records data to the database as it is generated i.e. when
     * the start and stop buttons are pressed.
     * 
     * @param stop The time work ceased on the task.
     * @param delta The time the work took.
     * @param dayTally The total time worked today.
     * 
     * @throws SQLException
     */
    public void setStopParametersAndCommit( DateTime stop, 
                                            Period delta,
                                            Period dayTally ) throws SQLException
    {
        prep.setObject( 3, stop );
        prep.setObject( 4, delta );
        prep.setObject( 7, dayTally );
        prep.addBatch();

        commit();
    }

    /**
     * When an entry is complete, write it to the database.
     *    
     * @throws SQLException
     */
    private void commit() throws SQLException
    {
        dbConnection.setAutoCommit( false );
        prep.executeBatch();
        dbConnection.setAutoCommit( true );
    }

    /**
     * Used to destroy any data in the database. The actual database file is
     * left in place.
     * 
     * @throws SQLException
     */
    public void dropDataBase() throws SQLException
    {
        statementHandler.executeUpdate( "drop table if exists timelord;" );
    }

    /**
     * @return The column headers for the TimeLord task table.
     */
    public String[] getColumnHeaders()
    {
    	return new String[] {"Start", "Stop", "Delta", "Jira", "Description"};
    }
    
    /**
     * Get every entry in the database.
     * 
     * @return All lines from the database.
     * 
     * @throws SQLException
     */
    public ResultSet getAllEntries() throws SQLException
    {
        System.out.println( "Getting all entries" );
        ResultSet rs = statementHandler.executeQuery( "select * from timelord;" );

        while ( rs.next() )
        {
            System.out.println( "date = " + rs.getString( "date" ) );
            System.out.println( "start = " + rs.getString( "start" ) );
            System.out.println( "stop = " + rs.getString( "stop" ) );
            System.out.println( "delta = " + rs.getString( "delta" ) );
            System.out.println( "jira = " + rs.getString( "jira" ) );
            System.out.println( "description = " + rs.getString( "description" ) );
            System.out.println( "dayTally = " + rs.getString( "dayTally" ) );
            System.out.println( "weekTally = " + rs.getString( "weekTally" ) );
        }

        rs.close();

        return rs;
    }
    
    public String[][] getTodaysEntries() throws SQLException
    {
    	String todayDate = Time.getReferableDate( new DateTime() );
    	return getEntriesByDate( todayDate );
    }

    /**
     * Return all entries filtered by the date of interest.
     * 
     * @param date The date to get entries for.
     * 
     * @return Array of strings containing all the TimeLord entries for 'date'.
     * 
     * @throws SQLException
     */
    public String[][] getEntriesByDate( String date ) throws SQLException
    {
        taskCount = 0;

        ResultSet rs = statementHandler.executeQuery( "select * from timelord where date = '"
                + date + "';" );

        int tmpCounter = 0;
        while ( rs.next() )
        {
        	tmpCounter++;
        }

        String[][] tableData = new String[tmpCounter][5];

        rs = statementHandler.executeQuery( "select * from timelord where date = '" + date + "';" );
        
        while ( rs.next() )
        {
            DateTime start = new DateTime( rs.getObject("start") );
            tableData[taskCount][0] = Time.getFormattedTime( start );
            
            DateTime stop = new DateTime( rs.getObject("stop") );
            tableData[taskCount][1] = Time.getFormattedTime( stop );
            
            Period period = new Period( rs.getObject("delta") );
            tableData[taskCount][2] = Time.displayDelta( period );
            
            tableData[taskCount][3] = rs.getString( "jira" );
            tableData[taskCount][4] = rs.getString( "description" );

            taskCount++;
        }

        rs.close();

        return tableData;
    }

    /**
     * Used to generate the current tally of time spent today and this week.
     * 
     * @param daySubTotal The current subtotal for today and this week.
     * 
     * @return Time object containing the total time for today and this week.
     */
    public DateTime getDayTimeTotal( DateTime daySubTotal )
    {
        String day = "";
        String week = "";
        DateTime date = new DateTime();
        String dateString = "" + date.getYear() + date.getMonthOfYear() + date.getDayOfMonth();
        ResultSet rs;

        try
        {
            rs = statementHandler.executeQuery( "select * from timelord where date = '" + 
                                                dateString + "';" );

            while ( rs.next() )
            {
                day = rs.getString( "dayTally" );
                week = rs.getString( "weekTally" );
                System.out.println( "SQL: " + day + " " + week );
                System.out.println( "dayTally = " + rs.getString( "dayTally" ) );
                System.out.println( "weekTally = " + rs.getString( "weekTally" ) );
                System.out.println( "-----------------------------------------" );
            }
            
            rs.close();
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }

        DateTime time = new DateTime( 0, 1, 1, 0, 0, 0, 0 );
        try
        {
            time.plusHours( 1 );
        }
        catch ( Exception e )
        {
            e.printStackTrace();
        }

        return time;
    }

    /**
     * Number of entries made today. Required to keep the table row index
     * correct.
     * 
     * @return The total number of task entries entered.
     */
    public int getTodayTaskCount()
    {
        return taskCount;
    }

    /**
     * Keep a running total of the time spent on tasks today. This can also then
     * be used to calculate the time spent on tasks this week.
     * 
     * @return The total time spent on tasks today.
     */
    public Period getTodayTally()
    {
    	return getTallyForDate( new DateTime() );
    }
    
    /**
     * Keep a running total of the time spent on tasks on a specific date. 
     * This can also then be used to calculate the time spent on tasks this week.
     * 
     * @param date Today's date
     * 
     * @return The total time spent on tasks today.
     */
    public Period getTallyForDate( DateTime date )
    {
        Period tally = new Period();
        ResultSet rs;

        try
        {
            rs = statementHandler.executeQuery( "select * from timelord where date = '" + 
                                                Time.getReferableDate( date ) + "';" );
            while ( rs.next() )
            {
                tally = new Period( tally ).plus( new Period( rs.getObject( 4 ) ) );
            }
            
            rs.close();
        }
        catch ( SQLException e )
        {
            e.printStackTrace();
        }

        return tally;
    }

    /**
     * Using today's date and the date of the start of the week, determine how
     * much time has been spent on tasks so far this week.
     * 
     * @param startDate The start date of the period.
     * @param endDate The end date of the period.
     * 
     * @return The total time spent on tasks this within the given period.
     */
    public Period getWeekTally( DateTime startDate, DateTime endDate )
    {
        Period tally = new Period();
        // Need mutable to perform arithmetic
        MutableDateTime start = startDate.toMutableDateTime();
        // Need mutable to perform arithmetic
        MutableDateTime end = endDate.toMutableDateTime();
        // Only have 'isBefore' so add one day to make it equivalent to
        // 'isBeforeOrOnThisDay'
        end.addDays( 1 );

        ResultSet rs;

        while ( start.isBefore( end ) )
        {
            try
            {
                rs = statementHandler.executeQuery( "select * from timelord where date = '"
                        + Time.getReferableDate( start.toDateTime() ) + "';" );
                while ( rs.next() )
                {
                    // There should only be one for day and 7 for week
                    tally = new Period( tally ).plus( new Period(
                            rs.getObject( 4 ) ) );
                }
                
                rs.close();
            }
            catch ( SQLException e )
            {
                e.printStackTrace();
            }
            start.addDays( 1 );
        }

        return tally;
    }

    /**
     * Use the date and start time to return a particular entry in the database.
     * 
     * @param date The date of the entry.
     * @param start The start time of the entry.
     * 
     * @return Entry for date at time start.
     * 
     * @throws SQLException
     */
    public ResultSet getValue( String date, String start ) throws SQLException
    {
        ResultSet rs = statementHandler.executeQuery( "select * from timelord where date = '"
                + date + "' and start = '" + start + "';" );

        while ( rs.next() )
        {
            System.out.println( "date = " + rs.getString( 3 ) );
        }

        rs.close();
        
        return rs;
    }

    
    
    
    // TODO: this needs serious revision - doesn't make sense.
    /**
     * Use this method to get all the values from the database corresponding to
     * the given JIRA project for the time period specified.
     * 
     * @param startDate The start date of the period to report on.
     * @param interval The length of time from the start date to report on.
     * @param jiraCode The Jira issue to report on.
     * 
     * @return ArrayList where each entry is an array containing the
     *         contents of each row from the database that was found: date,
     *         start, stop, delta, jira, description, dayTally.
     */
    public Object[][] generateReport( DateTime startDate, Period interval, String jiraCode )
    {
        int taskCount = 0;
        String[][] tableData = null;

        MutableDateTime start = startDate.toMutableDateTime();
        // Need mutable to perform arithmetic
        MutableDateTime end = startDate.plus( interval ).toMutableDateTime();
        // Only have 'isBefore' so add one day to make it equivalent to
        // 'isBeforeOrOnThisDay'
        end.addDays( 1 );

        ResultSet rs;
        try
        {
            rs = statementHandler.executeQuery( "select * from timelord where date = '" + 
                                                Time.getReferableDate( start.toDateTime() ) + 
                                                "' and jira = '" + 
                                                jiraCode + "';" );

            while ( rs.next() )
            {
                taskCount++;
            }
            rs = statementHandler.executeQuery( "select * from timelord where date = '" + 
                                                Time.getReferableDate( start.toDateTime() ) + 
                                                "' and jira = '" + 
                                                jiraCode + "';" );

            tableData = new String[taskCount][6];

            taskCount = 0;
            while ( start.isBefore( end ) )
            {
                while ( rs.next() )
                {
                    tableData[taskCount][0] = Time.getFormattedDate( new DateTime(
                            rs.getObject( "start" ) ) );
                    tableData[taskCount][1] = Time.getFormattedTime( new DateTime(
                            rs.getObject( "start" ) ) );
                    tableData[taskCount][2] = Time.getFormattedTime( new DateTime(
                            rs.getObject( "stop" ) ) );
                    tableData[taskCount][3] = Time.displayDelta( new Period(
                            rs.getObject( "delta" ) ) );
                    tableData[taskCount][4] = rs.getString( "jira" );
                    tableData[taskCount][5] = rs.getString( "description" );

                    taskCount++;
                }
                start.addDays( 1 );
            }
        }
        catch ( SQLException e1 )
        {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        return tableData;
    }

    /**
     * Close the database connection.
     * 
     * @throws SQLException
     */
    public void closeDBConnection() throws SQLException
    {
        dbConnection.close();
    }

    /**
     * Read in the database file and make a copy of it at a chosen location.
     */
    public void backupDB()
    {
        SwingUtilities.invokeLater( new Runnable()
        {
            @Override
            public void run()
            {
                JFileChooser fc = new JFileChooser();
                fc.setCurrentDirectory( new File( dbPath ) );
                fc.setSelectedFile( new File( "TimeLordDB.backup" ) );
                fc.showSaveDialog( view );

                File dbCopy = fc.getSelectedFile();

                try
                {
                    copyFile( new File( dbPath ), dbCopy );
                }
                catch ( IOException e )
                {
                    Logger.getAnonymousLogger( "TimeLord" ).info( "Could not backup database: " + 
                                                                  e );
                    e.printStackTrace();
                }
            }
        } );
    }

    /**
     * Present a dialog so user can choose a database file to restore from.
     */
    public void restoreDB()
    {
        SwingUtilities.invokeLater( new Runnable()
        {

            @Override
            public void run()
            {
                JFileChooser fc = new JFileChooser();
                int returnVal = fc.showOpenDialog( view );

                if ( returnVal == JFileChooser.APPROVE_OPTION )
                {
                    File backup = fc.getSelectedFile();

                    try
                    {
                        copyFile( backup, new File( dbPath ) );
                    }
                    catch ( IOException e )
                    {
                        Logger.getAnonymousLogger( "TimeLord" ).info( "Could not restore database: "
                                                                      + e );
                        e.printStackTrace();
                    }
                }
            }
        } );
    }

    /**
     * Use this method to copy the database file to a backup file.
     * 
     * @param src The file to be copied.
     * @param dst The file to backup to.
     * 
     * @throws IOException - any kind of IO problem. Pass on to caller.
     */
    void copyFile( File src, File dst ) throws IOException
    {
        InputStream in = new FileInputStream( src );
        OutputStream out = new FileOutputStream( dst );

        // Transfer bytes from in to out
        byte[] buf = new byte[1024];
        int len;
        while ( ( len = in.read( buf ) ) > 0 )
        {
            out.write( buf, 0, len );
        }
        in.close();
        out.close();
    }
    
    //----------------------------------------------------------
    //                    INSTANCE METHODS
    //----------------------------------------------------------

    ////////////////////////////////////////////////////////////////////////////////////////////
    /////////////////////////////// Accessor and Mutator Methods ///////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////
}
