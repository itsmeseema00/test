package org.rbfcu.projectview.util;

import java.util.Comparator;

import org.rbfcu.projectview.bean.Ticket;

public class TicketStatusComparator implements Comparator<Ticket> {

	@Override
	public int compare(Ticket ticket1, Ticket ticket2) {
		return ticket1.getStatus().compareTo(ticket2.getStatus());
	}
}
