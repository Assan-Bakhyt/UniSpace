package UniSpace.model.research;

import UniSpace.enums.Faculty;
import UniSpace.model.user.User;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents the researcher role for a User (Student or Teacher).
 *
 * Not every student/teacher is a researcher — this profile is created
 * only when the user activates the researcher role (activateResearcher()).
 * Pattern: Role Object.
 */
public class ResearcherProfile implements Researcher, Serializable {

    private final User owner;
    private int hIndex;
    private final List<ResearchPaper> papers = new ArrayList<>();
    private final List<ResearchProject> projects = new ArrayList<>();


    public ResearcherProfile(User owner) {
        this.owner = owner;
    }

    // ── Researcher interface ──────────────────────────────────────────────────

    @Override
    public String getName() {
        return owner.getFullName();
    }

    @Override
    public Faculty getSchool() {
        return owner.getFaculty();
    }

    @Override
    public int getHIndex() {
        return hIndex;
    }

    @Override
    public List<ResearchPaper> getResearchPapers() {
        return Collections.unmodifiableList(papers);
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    public void setHIndex(int hIndex) {
        this.hIndex = hIndex;
    }

    @Override
    public void addResearchPaper(ResearchPaper paper) {
        if (!papers.contains(paper)) papers.add(paper);
    }

    @Override
    public void addResearchProject(ResearchProject project) {
        if (!projects.contains(project)) projects.add(project);
    }

    @Override
    public List<ResearchProject> getResearchProjects() {
        return Collections.unmodifiableList(projects);
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public User getOwner() {
        return owner;
    }

    @Override
    public String toString() {
        return String.format("Researcher{name='%s', faculty=%s, hIndex=%d, papers=%d}",
                getName(), getSchool(), hIndex, papers.size());
    }
}
