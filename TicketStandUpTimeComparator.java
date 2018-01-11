package org.rbfcu.projectview.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.lang3.StringUtils;
import org.rbfcu.projectview.bean.Ticket;

public class TicketStandUpTimeComparator implements Comparator<Ticket> {

	@Override
	public int compare(Ticket ticket1, Ticket ticket2) {
		String time1 = ticket1.getStandUpTime();
		String time2 = ticket2.getStandUpTime();
		return convertStringToDate(time1).compareTo(convertStringToDate(time2));
	}

	private Date convertStringToDate(String standUpTime) {
		DateFormat df = new SimpleDateFormat("HH:mm");
		if (standUpTime == null || standUpTime.trim().length() == 0) {
			return new Date();
		} else {
			try {
				return addDay(df.parse(standUpTime), standUpTime);
			} catch (ParseException e) {
				return new Date();
			}
		}
	}

	private Date addDay(Date date, String standUpTime) {
		int standupTimeHour= Integer.parseInt(StringUtils.substringBefore(standUpTime, ":"));
		GregorianCalendar cal = new GregorianCalendar();
		if (standupTimeHour >= 8 && standupTimeHour < 12) {
			return date;
		} else {
			cal.setTime(date);
			cal.add(Calendar.DATE, 1);
			return cal.getTime();
		}
	}
}
