package model;

import org.joda.time.DateTime;
import org.joda.time.Period;

/**
 * This class will provide an interface between TimeLord and Joda-Time objects. This will
 * hopefully reduce the code clutter needed to produce formatted dates and times and will act as a
 * single point to change the format of either.
 */
public class Time
{
	/**
	 * Get a nicely formatted string of the time provided.
	 * 
	 * @param dateTime - get a string of this time
	 * 
	 * @return - a formatted string of the time provided
	 */
	public static String getFormattedTime( DateTime dateTime )
	{
		String minutes = String.valueOf( dateTime.getMinuteOfHour() );
		if( minutes.equals( "60" ) )
		{
			minutes = "00";
		}

		// Prevent single digit minutes from displaying incorrectly
		if( Integer.valueOf( minutes ) < 10 )
		{
			minutes = "0" + minutes;
		}
		return dateTime.getHourOfDay() + ":" + minutes + " " + getMeridian( dateTime );
	}


	/**
	 * Get the difference in time between two Joda Time objects.
	 * 
	 * @param start - the start Joda Time object
	 * @param stop - the stop Joda Time object
	 * 
	 * @return - a Joda Period object with the difference in time between start and stop
	 */
	public static Period getTimeDifference( DateTime start, DateTime stop )
	{
		// Figure out how long this task took
		Period delta = new Period( start, stop );

		return delta;
	}

	/**
	 * Return a nicely formatted string containing a time delta.
	 * 
	 * @param delta - create a string for this time period
	 * 
	 * @return - a nicely formatted time period
	 */
	public static String displayDelta( Period delta )
	{
		String deltaString = "";
		int hours = 0;
		String hoursLabel = "";
		int minutes = 0;
		String minutesLabel = "";

		if( delta != null )
		{
    		// Determine whether to use plural or not
    		if( delta.getHours() == 1 )
    		{
    			hoursLabel = " Hr";
    		}
    		else
    		{
    			hoursLabel = " Hrs";
    		}
    
    		if( delta.getMinutes() == 1 )
    		{
    			minutesLabel = " Min";
    		}
    		else
    		{
    			minutesLabel = " Mins";
    		}
    
    		// Convert minutes (>=60) to hours and minutes
    		if( delta.getMinutes() >= 60 )
    		{
    			hours = delta.getHours() + (delta.getMinutes() / 60);
    			minutes = delta.getMinutes() % 60;
    		}
    		else
    		{
    			hours = delta.getHours();
    			minutes = delta.getMinutes();
    		}
    
    		deltaString = hours + 
    					  hoursLabel + 
    					  " " + 
    					  minutes + 
    					  minutesLabel;
		}
		
		return deltaString;
	}

//	/**
//	 * Return a nicely formatted string containing a time delta.
//	 * 
//	 * @param delta - create a string for this time period
//	 * 
//	 * @return - a nicely formatted time period
//	 */
//	public static String displayDelta( Period delta )
//	{
//		String deltaString = "";
//		int hours = 0;
//		String hoursLabel = "";
//		int minutes = 0;
//		String minutesLabel = "";
//
//		// Determine whether to use plural or not
//		if( delta.getHours() == 1 )
//		{
//			hoursLabel = " Hr";
//		}
//		else
//		{
//			hoursLabel = " Hrs";
//		}
//
//		if( delta.getMinutes() == 1 )
//		{
//			minutesLabel = " Min";
//		}
//		else
//		{
//			minutesLabel = " Mins";
//		}
//
//		// Convert minutes (>=60) to hours and minutes
//		if( delta.getMinutes() >= 60 )
//		{
//			hours = delta.getHours() + (delta.getMinutes() / 60);
//			minutes = delta.getMinutes() % 60;
//		}
//		else
//		{
//			hours = delta.getHours();
//			minutes = delta.getMinutes();
//		}
//
//		deltaString = "<html><div font color='white'>" + 
//					  hours + 
//					  hoursLabel + 
//					  "<br>" + 
//					  minutes + 
//					  minutesLabel + 
//					  "</div></html>";
//
//		return deltaString;
//	}


	/**
	 * This is a string containing a date that is easy to use as a reference e.g. when referring
	 * to a date in the database.
	 * 
	 * @param dateTime - get a referable version of this date
	 * 
	 * @return - a string containing a referable version of the date
	 */
	public static String getReferableDate( DateTime dateTime )
	{
		return "" + dateTime.getDayOfMonth() + dateTime.getMonthOfYear() + dateTime.getYear();
	}


	/**
	 * This creates a containing the date that looks nice.
	 * 
	 * @param dateTime The date to be formatted.
	 * 
	 * @return A string containing the formatted date.
	 */
	public static String getFormattedDate( DateTime dateTime )
	{
		return "" + dateTime.getDayOfMonth() + "-" + dateTime.getMonthOfYear() + "-" +
		       dateTime.getYear();
	}


	/**
	 * Get the meridian of a time.
	 * 
	 * @param dateTime - get the meridian for this time
	 * 
	 * @return - AM/PM of the time provided
	 */
	private static String getMeridian( DateTime dateTime )
	{
		// I'm sure there is a better way of doing this but...
		// Set the AM/PM text.
		String meridian = "";

		if( dateTime.getHourOfDay() >= 0 && dateTime.getHourOfDay() < 12 )
		{
			meridian = "AM";
		}
		else
		{
			meridian = "PM";
		}

		return meridian;
	}
}
