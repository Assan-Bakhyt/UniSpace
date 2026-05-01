package UniSpace.util;

import UniSpace.model.research.ResearchPaper;
import UniSpace.patterns.SortStrategy;

import java.util.Comparator;

/** Sorts papers by citations descending. Implements Strategy pattern via SortStrategy. */
public class PaperCitationsComparator implements Comparator<ResearchPaper>, SortStrategy<ResearchPaper> {

    @Override
    public int compare(ResearchPaper p1, ResearchPaper p2) {
        return Integer.compare(p2.getCitations(), p1.getCitations());
    }

    @Override
    public Comparator<ResearchPaper> getComparator() { return this; }
}
