package UniSpace.util;

import UniSpace.model.research.ResearchPaper;
import UniSpace.patterns.SortStrategy;

import java.util.Comparator;

/** Sorts papers newest-first. Implements Strategy pattern via SortStrategy. */
public class PaperDateComparator implements Comparator<ResearchPaper>, SortStrategy<ResearchPaper> {

    @Override
    public int compare(ResearchPaper p1, ResearchPaper p2) {
        if (p1.getDate() == null && p2.getDate() == null) return 0;
        if (p1.getDate() == null) return 1;
        if (p2.getDate() == null) return -1;
        return p2.getDate().compareTo(p1.getDate());
    }

    @Override
    public Comparator<ResearchPaper> getComparator() { return this; }
}
