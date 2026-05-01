package UniSpace.model.research;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public class ResearchPaper implements Serializable {

    private String name;
    private List<String> authors;
    private String journal;
    private int pages;
    private int citations;
    private LocalDate date;
    private String doi;

    public ResearchPaper() {}

    public ResearchPaper(String name, List<String> authors,
                         String journal, int pages,
                         int citations, LocalDate date, String doi) {
        this.name = name;
        this.authors = authors;
        this.journal = journal;
        this.pages = pages;
        this.citations = citations;
        this.date = date;
        this.doi = doi;
    }

    public String getName() { return name; }
    public List<String> getAuthors() { return authors; }
    public String getJournal() { return journal; }
    public int getPages() { return pages; }
    public int getCitations() { return citations; }
    public LocalDate getDate() { return date; }
    public String getDoi() { return doi; }

    public void setName(String name) { this.name = name; }
    public void setAuthors(List<String> authors) { this.authors = authors; }
    public void setJournal(String journal) { this.journal = journal; }
    public void setPages(int pages) { this.pages = pages; }
    public void setCitations(int citations) { this.citations = citations; }
    public void setDate(LocalDate date) { this.date = date; }
    public void setDoi(String doi) { this.doi = doi; }

    @Override
    public String toString() {
        return String.format(
                "%s | %s | Citations: %d | Pages: %d | Date: %s",
                name, journal, citations, pages, date
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResearchPaper)) return false;
        ResearchPaper that = (ResearchPaper) o;
        return Objects.equals(doi, that.doi);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doi);
    }
}