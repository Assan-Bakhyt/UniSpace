package UniSpace.model.research;

import UniSpace.exception.NonResearcherJoinException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class ResearchProject implements Serializable {

    private static final long serialVersionUID = 1L;

    // БЫЛО: private final String projectId = UUID.randomUUID().toString();
    // СТАЛО: инициализируется в конструкторе — корректно восстанавливается при десериализации
    private final String projectId;
    private String topic;
    private List<ResearchPaper>  publishedPapers;
    private List<Researcher>     participants;

    public ResearchProject() {
        this.projectId       = UUID.randomUUID().toString();
        this.publishedPapers = new ArrayList<>();
        this.participants    = new ArrayList<>();
    }

    public ResearchProject(String topic) {
        this.projectId       = UUID.randomUUID().toString();
        this.topic           = topic;
        this.publishedPapers = new ArrayList<>();
        this.participants    = new ArrayList<>();
    }

    // ── Joining ───────────────────────────────────────────────────────────────

    /**
     * Adds a person to the project.
     * @throws NonResearcherJoinException if the given object is not a Researcher
     */
    public void joinProject(Object person) throws NonResearcherJoinException {
        if (!(person instanceof Researcher)) {
            throw new NonResearcherJoinException(
                    "Only researchers can join a research project. Got: "
                            + (person == null ? "null" : person.getClass().getSimpleName()));
        }
        Researcher researcher = (Researcher) person;
        if (!participants.contains(researcher)) participants.add(researcher);
    }

    // ── Papers ────────────────────────────────────────────────────────────────

    public void addPublishedPaper(ResearchPaper paper) {
        if (paper != null && !publishedPapers.contains(paper))
            publishedPapers.add(paper);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String              getProjectId()     { return projectId; }

    public String              getTopic()         { return topic; }
    public void                setTopic(String t) { this.topic = t; }

    public List<ResearchPaper> getPublishedPapers() {
        return Collections.unmodifiableList(publishedPapers);
    }

    public List<Researcher>    getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    // ── Object overrides ──────────────────────────────────────────────────────

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResearchProject)) return false;
        return Objects.equals(projectId, ((ResearchProject) o).projectId);
    }

    @Override
    public int hashCode() { return Objects.hash(projectId); }

    @Override
    public String toString() {
        return String.format("ResearchProject{id='%s', topic='%s', papers=%d, participants=%d}",
                projectId, topic, publishedPapers.size(), participants.size());
    }
}