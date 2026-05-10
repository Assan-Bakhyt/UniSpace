package UniSpace.service;

import UniSpace.enums.Faculty;
import UniSpace.model.research.ResearchPaper;
import UniSpace.model.research.ResearchProject;
import UniSpace.model.research.Researcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ResearchService {

    private static ResearchService instance;

    public static ResearchService getInstance() {
        if (instance == null) instance = new ResearchService();
        return instance;
    }

    private final List<Researcher>      researchers = new ArrayList<>();
    private final List<ResearchProject> projects    = new ArrayList<>(); // ← НОВОЕ

    private ResearchService() {}

    // ── Researcher registry ───────────────────────────────────────────────────

    public void addResearcher(Researcher researcher) {
        if (!researchers.contains(researcher)) researchers.add(researcher);
    }

    public List<Researcher> getResearchers() {
        return Collections.unmodifiableList(researchers);
    }

    // ── Project registry ──────────────────────────────────────────────────────

    /** Регистрирует проект в общем списке. Идемпотентен. */
    public void addProject(ResearchProject project) {
        if (project != null && !projects.contains(project))
            projects.add(project);
    }

    public List<ResearchProject> getAllProjects() {
        return Collections.unmodifiableList(projects);
    }

    // ── Project search ────────────────────────────────────────────────────────

    /** Поиск проекта по точному topic (без учёта регистра). */
    public Optional<ResearchProject> findProjectByTopic(String topic) {
        return projects.stream()
                .filter(p -> p.getTopic().equalsIgnoreCase(topic))
                .findFirst();
    }

    /** Поиск проекта по id. */
    public Optional<ResearchProject> findProjectById(String projectId) {
        return projects.stream()
                .filter(p -> p.getProjectId().equals(projectId))
                .findFirst();
    }

    /** Все проекты в которых участвует данный researcher. */
    public List<ResearchProject> getProjectsByResearcher(Researcher researcher) {
        List<ResearchProject> result = new ArrayList<>();
        for (ResearchProject p : projects) {
            if (p.getParticipants().contains(researcher)) result.add(p);
        }
        return Collections.unmodifiableList(result);
    }

    // ── Paper queries ─────────────────────────────────────────────────────────

    public void printAllPapers(Comparator<ResearchPaper> comparator) {
        List<ResearchPaper> allPapers = new ArrayList<>();
        for (Researcher r : researchers) allPapers.addAll(r.getResearchPapers());
        allPapers.sort(comparator);
        if (allPapers.isEmpty()) { System.out.println("  No papers found."); return; }
        for (ResearchPaper paper : allPapers) System.out.println("  " + paper);
    }

    // ── Researcher queries ────────────────────────────────────────────────────

    public Researcher getTopCitedResearcherBySchool(Faculty school) {
        return researchers.stream()
                .filter(r -> r.getSchool() == school)
                .max(Comparator.comparingInt(Researcher::getTotalCitations))
                .orElse(null);
    }

    public Researcher getTopCitedResearcherByYear(int year) {
        Researcher top        = null;
        int        maxCit     = -1;
        for (Researcher r : researchers) {
            int citations = r.getResearchPapers().stream()
                    .filter(p -> p.getDate() != null && p.getDate().getYear() == year)
                    .mapToInt(ResearchPaper::getCitations)
                    .sum();
            if (citations > maxCit) { maxCit = citations; top = r; }
        }
        return top;
    }

    // ── Paper management ──────────────────────────────────────────────────────

    /**
     * Создаёт ResearchPaper и добавляет его researcher-у.
     * Парсинг pages вынесен в вызывающий UI-слой — сюда приходит уже int.
     */
    public void addPaperToResearcher(Researcher researcher, String title, String journal,
                                     String doi, int pages, int year) {
        ResearchPaper paper = new ResearchPaper();
        paper.setName(title);
        paper.setJournal(journal);
        paper.setDoi(doi);
        paper.setPages(pages);
        paper.setDate(java.time.LocalDate.of(year, 1, 1));
        researcher.addResearchPaper(paper);
    }

    // ── Project management ────────────────────────────────────────────────────

    /**
     * Создаёт новый проект, добавляет researcher как первого участника,
     * регистрирует проект в общем списке и в профиле researcher-а.
     */
    public void createProject(Researcher researcher, String topic) {
        ResearchProject project = new ResearchProject(topic);
        try {
            project.joinProject(researcher);
        } catch (Exception e) {
            System.out.println("  [ERROR] " + e.getMessage());
            return;
        }
        researcher.addResearchProject(project);
        addProject(project); // ← регистрируем в общем списке
        System.out.println("  Project '" + topic + "' created successfully.");
    }

    /**
     * Добавляет researcher в существующий проект (поиск по topic).
     * @throws Exception если проект не найден
     */
    public void joinProject(Researcher researcher, String topic) throws Exception {
        ResearchProject project = findProjectByTopic(topic)
                .orElseThrow(() -> new Exception("Project '" + topic + "' not found."));

        project.joinProject(researcher);       // бросит NonResearcherJoinException если нужно
        researcher.addResearchProject(project);
    }

    /**
     * Выводит все проекты в консоль.
     */
    public void printAllProjects() {
        System.out.println("\n  ── All Research Projects ──");
        if (projects.isEmpty()) { System.out.println("  No projects found."); return; }
        for (int i = 0; i < projects.size(); i++) {
            ResearchProject p = projects.get(i);
            System.out.printf("  %d. %-30s (participants: %d, papers: %d)%n",
                    i + 1, p.getTopic(),
                    p.getParticipants().size(),
                    p.getPublishedPapers().size());
        }
    }
}