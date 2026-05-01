package UniSpace.model.research;

import UniSpace.exception.NonResearcherJoinException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ResearchProject implements Serializable {

    private String topic;
    private List<ResearchPaper> publishedPapers;
    private List<Researcher> participants;

    public ResearchProject() {
        this.publishedPapers = new ArrayList<>();
        this.participants = new ArrayList<>();
    }

    public ResearchProject(String topic) {
        this.topic = topic;
        this.publishedPapers = new ArrayList<>();
        this.participants = new ArrayList<>();
    }

    public void joinProject(Object person) throws NonResearcherJoinException {
        if (!(person instanceof Researcher)) {
            throw new NonResearcherJoinException("Only researchers can join research project.");
        }

        Researcher researcher = (Researcher) person;

        if (!participants.contains(researcher)) {
            participants.add(researcher);
        }
    }

    public void addPublishedPaper(ResearchPaper paper) {
        if (!publishedPapers.contains(paper)) {
            publishedPapers.add(paper);
        }
    }

    public String getTopic() {
        return topic;
    }

    public List<ResearchPaper> getPublishedPapers() {
        return Collections.unmodifiableList(publishedPapers);
    }

    public List<Researcher> getParticipants() {
        return Collections.unmodifiableList(participants);
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    @Override
    public String toString() {
        return "ResearchProject{" +
                "topic='" + topic + '\'' +
                ", papers=" + publishedPapers.size() +
                ", participants=" + participants.size() +
                '}';
    }
}