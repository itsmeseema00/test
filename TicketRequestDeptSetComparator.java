package org.rbfcu.projectview.util;

import java.util.Comparator;

import org.rbfcu.projectview.bean.Ticket;

public class TicketRequestDeptSetComparator implements Comparator<Ticket>{

	@Override
	public int compare(Ticket ticket1, Ticket ticket2) {
		
		String dept1 = ticket1.getRequestDept() == null ? "" : ticket1.getRequestDept(); 
		String dept2 = ticket2.getRequestDept() == null ? "" : ticket2.getRequestDept(); 
		
		return  dept1.compareTo(dept2) ;
	}

}
