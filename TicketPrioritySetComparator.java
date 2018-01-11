package org.rbfcu.projectview.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.rbfcu.projectview.bean.Ticket;

public class TicketPrioritySetComparator implements Comparator<Ticket> {
	static List<String> priorityList = new ArrayList<String>();
	static {
		priorityList.add("Urgent");
		priorityList.add("High");
		priorityList.add("Medium");
		priorityList.add("Low");
		priorityList.add("");
	}

	@Override
	public int compare(Ticket ticket1, Ticket ticket2) {
		return priorityList.indexOf((ticket1.getPriority() == null ? "" : ticket1.getPriority()))
				- priorityList.indexOf((ticket2.getPriority() == null ? "" : ticket2.getPriority()));

	}

}
