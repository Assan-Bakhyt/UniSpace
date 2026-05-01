package UniSpace.service;

import UniSpace.model.research.ResearchPaper;
import UniSpace.model.research.Researcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResearchService {

    private List<Researcher> researchers;

    public ResearchService() {
        this.researchers = new ArrayList<>();
    }

    public void addResearcher(Researcher researcher) {
        if (!researchers.contains(researcher)) {
            researchers.add(researcher);
        }
    }

    public List<Researcher> getResearchers() {
        return researchers;
    }

    public void printAllPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> allPapers = new ArrayList<>();

        for (Researcher researcher : researchers) {
            allPapers.addAll(researcher.getResearchPapers());
        }

        allPapers.sort(comparator);

        for (ResearchPaper paper : allPapers) {
            System.out.println(paper);
        }
    }

    public Researcher getTopCitedResearcherBySchool(String school) {
        Researcher topResearcher = null;

        for (Researcher researcher : researchers) {
            if (researcher.getSchool().equalsIgnoreCase(school)) {
                if (topResearcher == null ||
                        researcher.getTotalCitations() > topResearcher.getTotalCitations()) {
                    topResearcher = researcher;
                }
            }
        }

        return topResearcher;
    }

    public Researcher getTopCitedResearcherByYear(int year) {
        Researcher topResearcher = null;
        int maxCitations = -1;

        for (Researcher researcher : researchers) {
            int citationsInYear = 0;

            for (ResearchPaper paper : researcher.getResearchPapers()) {
                if (paper.getDate().getYear() == year) {
                    citationsInYear += paper.getCitations();
                }
            }

            if (citationsInYear > maxCitations) {
                maxCitations = citationsInYear;
                topResearcher = researcher;
            }
        }

        return topResearcher;
    }
}