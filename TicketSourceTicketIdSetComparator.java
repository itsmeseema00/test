package org.rbfcu.projectview.util;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;
import org.rbfcu.projectview.bean.Ticket;

public class TicketSourceTicketIdSetComparator implements Comparator<Ticket>{
	@Override
	public int compare(Ticket ticket1, Ticket ticket2) {		 
		final String SEPARATOR = "-";
		Integer key1 = Integer.parseInt(StringUtils.substringAfter(ticket1.getTicketId(), SEPARATOR));
		Integer key2 = Integer.parseInt(StringUtils.substringAfter(ticket2.getTicketId(), SEPARATOR));
		return key2.compareTo(key1);		 	 
	}
}
