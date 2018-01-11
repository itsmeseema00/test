package org.rbfcu.projectview.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.rbfcu.projectview.bean.Ticket;
 
public class TicketChainedComparator implements Comparator<Ticket> {
 
    private List<Comparator<Ticket>> listComparators;
 
    @SafeVarargs
    public TicketChainedComparator(Comparator<Ticket>... comparators) {
        this.listComparators = Arrays.asList(comparators);
    }
 
    @Override
    public int compare(Ticket ticket1, Ticket ticket2) {
        for (Comparator<Ticket> comparator : listComparators) {
            int result = comparator.compare(ticket1, ticket2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
    }
}