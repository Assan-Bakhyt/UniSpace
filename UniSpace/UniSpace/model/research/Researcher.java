package UniSpace.model.research;

import UniSpace.exception.HIndexException;

import java.util.*;

public interface Researcher {

    int getHIndex();
    List<ResearchPaper> getResearchPapers();

    default void printPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> papers = new ArrayList<>(getResearchPapers());
        papers.sort(comparator);

        for (ResearchPaper paper : papers) {
            System.out.println(paper);
        }
    }

    default int getTotalCitations() {
        return getResearchPapers()
                .stream()
                .mapToInt(ResearchPaper::getCitations)
                .sum();
    }


}