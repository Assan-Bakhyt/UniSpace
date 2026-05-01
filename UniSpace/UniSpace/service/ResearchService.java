package UniSpace.service;

import UniSpace.enums.Faculty;
import UniSpace.model.research.ResearchPaper;
import UniSpace.model.research.Researcher;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ResearchService {

    private static ResearchService instance;

    public static ResearchService getInstance() {
        if (instance == null) instance = new ResearchService();
        return instance;
    }

    private final List<Researcher> researchers = new ArrayList<>();

    private ResearchService() {}

    // ── Registry ──────────────────────────────────────────────────────────────

    public void addResearcher(Researcher researcher) {
        if (!researchers.contains(researcher)) researchers.add(researcher);
    }

    public List<Researcher> getResearchers() {
        return researchers;
    }

    // ── Paper queries ─────────────────────────────────────────────────────────

    public void printAllPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> allPapers = new ArrayList<>();
        for (Researcher r : researchers) allPapers.addAll(r.getResearchPapers());
        allPapers.sort(comparator);
        for (ResearchPaper paper : allPapers) System.out.println(paper);
    }

    // ── Researcher queries ────────────────────────────────────────────────────

    public Researcher getTopCitedResearcherBySchool(Faculty school) {
        Researcher top = null;
        for (Researcher r : researchers) {
            if (r.getSchool() == school) {
                if (top == null || r.getTotalCitations() > top.getTotalCitations()) top = r;
            }
        }
        return top;
    }

    public Researcher getTopCitedResearcherByYear(int year) {
        Researcher top = null;
        int maxCitations = -1;
        for (Researcher r : researchers) {
            int citations = 0;
            for (ResearchPaper p : r.getResearchPapers()) {
                if (p.getDate() != null && p.getDate().getYear() == year)
                    citations += p.getCitations();
            }
            if (citations > maxCitations) {
                maxCitations = citations;
                top = r;
            }
        }
        return top;
    }
}
