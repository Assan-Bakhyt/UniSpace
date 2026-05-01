package UniSpace.model.research;

import UniSpace.enums.Faculty;

import java.util.*;

public interface Researcher {

    /** Display name of this researcher (delegates to User.getFullName()). */
    String getName();

    /** Faculty this researcher belongs to. */
    Faculty getSchool();

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