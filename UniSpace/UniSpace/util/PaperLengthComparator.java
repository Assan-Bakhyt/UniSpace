package UniSpace.util;

import UniSpace.model.research.ResearchPaper;

import java.util.Comparator;

public class PaperLengthComparator implements Comparator<ResearchPaper> {

    @Override
    public int compare(ResearchPaper p1, ResearchPaper p2) {
        return Integer.compare(p2.getPages(), p1.getPages());
    }
}