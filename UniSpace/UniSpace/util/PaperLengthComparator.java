package UniSpace.util;

import UniSpace.model.research.ResearchPaper;
import UniSpace.patterns.SortStrategy;

import java.util.Comparator;

/** Sorts papers by page count descending. Implements Strategy pattern via SortStrategy. */
public class PaperLengthComparator implements Comparator<ResearchPaper>, SortStrategy<ResearchPaper> {

    @Override
    public int compare(ResearchPaper p1, ResearchPaper p2) {
        return Integer.compare(p2.getPages(), p1.getPages());
    }

    @Override
    public Comparator<ResearchPaper> getComparator() { return this; }
}
