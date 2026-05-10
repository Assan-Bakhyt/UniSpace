package UniSpace.ui;

import UniSpace.enums.Faculty;
import UniSpace.model.research.Researcher;
import UniSpace.service.ResearchService;
import UniSpace.util.PaperCitationsComparator;
import UniSpace.util.PaperDateComparator;
import UniSpace.util.PaperLengthComparator;

import java.util.Scanner;

public class ResearcherMenu {

    private final Researcher      currentResearcher;
    private final Faculty         ownerFaculty;
    private final ResearchService researchService;
    private final Scanner         scanner;

    public ResearcherMenu(Researcher currentResearcher, Faculty ownerFaculty,
                          ResearchService researchService) {
        this.currentResearcher = currentResearcher;
        this.ownerFaculty      = ownerFaculty;
        this.researchService   = researchService;
        this.scanner           = new Scanner(System.in);
    }

    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1"  -> printAllPapersByDate();
                case "2"  -> printAllPapersByCitations();
                case "3"  -> printAllPapersByLength();
                case "4"  -> findTopCitedResearcherByFaculty();
                case "5"  -> findTopCitedResearcherByYear();
                case "6"  -> addPaper();
                case "7"  -> createProject();
                case "8"  -> joinProject();
                case "9"  -> viewMyPapers();
                case "10" -> viewHIndex();
                case "0"  -> running = false;
                default   -> System.out.println("  Invalid option. Try again.");
            }
        }
    }

    // ── Существующие действия ─────────────────────────────────────────────────

    private void printAllPapersByDate() {
        System.out.println("\n  ── Research Papers by Date ──");
        researchService.printAllPapers(new PaperDateComparator());
    }

    private void printAllPapersByCitations() {
        System.out.println("\n  ── Research Papers by Citations ──");
        researchService.printAllPapers(new PaperCitationsComparator());
    }

    private void printAllPapersByLength() {
        System.out.println("\n  ── Research Papers by Length ──");
        researchService.printAllPapers(new PaperLengthComparator());
    }

    private void findTopCitedResearcherByFaculty() {
        Faculty faculty = pickFaculty();
        if (faculty == null) return;
        Researcher top = researchService.getTopCitedResearcherBySchool(faculty);
        if (top == null) { System.out.println("  No researcher found for faculty: " + faculty); return; }
        System.out.println("\n  ── Top Cited Researcher — " + faculty + " ──");
        System.out.println("  " + top);
        System.out.println("  Total citations: " + top.getTotalCitations());
    }

    private void findTopCitedResearcherByYear() {
        System.out.print("  Year: ");
        try {
            int year = Integer.parseInt(scanner.nextLine().trim());
            Researcher top = researchService.getTopCitedResearcherByYear(year);
            if (top == null) { System.out.println("  No researcher found for year " + year); return; }
            System.out.println("\n  ── Top Cited Researcher — " + year + " ──");
            System.out.println("  " + top);
            System.out.println("  Total citations: " + top.getTotalCitations());
        } catch (NumberFormatException e) {
            System.out.println("  [ERROR] Year must be a number.");
        }
    }

    // ── Новые действия ────────────────────────────────────────────────────────

    private void addPaper() {
        System.out.println("\n  ── Add Research Paper ──");
        System.out.print("  Title: ");   String title   = scanner.nextLine().trim();
        System.out.print("  Journal: "); String journal = scanner.nextLine().trim();
        System.out.print("  DOI: ");     String doi     = scanner.nextLine().trim();

        // БЫЛО: String pages = scanner.nextLine().trim();
        // СТАЛО: парсинг через ConsoleHelper — возвращает int
        int pages = UniSpace.util.ConsoleHelper.readInt(scanner, "  Number of pages: ");
        int year  = UniSpace.util.ConsoleHelper.readInt(scanner, "  Year published: ");

        researchService.addPaperToResearcher(currentResearcher, title, journal, doi, pages, year);
        System.out.println("  Paper added successfully.");
    }

    private void createProject() {
        System.out.println("\n  ── Create Research Project ──");
        System.out.print("  Project topic: ");
        String topic = scanner.nextLine().trim();
        researchService.createProject(currentResearcher, topic);
        System.out.println("  Project created successfully.");
    }

    private void joinProject() {
        System.out.println("\n  ── Join Research Project ──");
        researchService.printAllProjects();
        System.out.print("  Enter project topic to join: ");
        String topic = scanner.nextLine().trim();
        try {
            researchService.joinProject(currentResearcher, topic);
            System.out.println("  Successfully joined the project.");
        } catch (Exception e) {
            System.out.println("  [ERROR] " + e.getMessage());
        }
    }

    private void viewMyPapers() {
        System.out.println("\n  ── My Papers ──");
        System.out.println("  Sort by: 1. Date  2. Citations  3. Length");
        System.out.print("  Choice: ");
        switch (scanner.nextLine().trim()) {
            case "1" -> currentResearcher.printPapers(new PaperDateComparator());
            case "2" -> currentResearcher.printPapers(new PaperCitationsComparator());
            case "3" -> currentResearcher.printPapers(new PaperLengthComparator());
            default  -> System.out.println("  [ERROR] Invalid choice.");
        }
    }

    private void viewHIndex() {
        System.out.printf("%n  ── H-Index ──%n  Your current h-index: %d%n",
                currentResearcher.getHIndex());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private Faculty pickFaculty() {
        Faculty[] values = Faculty.values();
        System.out.println("  Select faculty:");
        for (int i = 0; i < values.length; i++)
            System.out.printf("    %d. %s%n", i + 1, values[i]);
        System.out.print("  Choice: ");
        try {
            int idx = Integer.parseInt(scanner.nextLine().trim()) - 1;
            if (idx >= 0 && idx < values.length) return values[idx];
        } catch (NumberFormatException ignored) {}
        System.out.println("  [ERROR] Invalid selection.");
        return null;
    }

    private void printMenu() {
        System.out.println("\n  ══════════════════════════════════════");
        System.out.println("   Researcher Menu — " + ownerFaculty);
        System.out.println("  ══════════════════════════════════════");
        System.out.println("   1.  Print all papers by date");
        System.out.println("   2.  Print all papers by citations");
        System.out.println("   3.  Print all papers by length");
        System.out.println("   4.  Find top cited researcher by faculty");
        System.out.println("   5.  Find top cited researcher by year");
        System.out.println("   6.  Add research paper");
        System.out.println("   7.  Create research project");
        System.out.println("   8.  Join research project");
        System.out.println("   9.  View my papers");
        System.out.println("   10. View my h-index");
        System.out.println("   0.  Back");
        System.out.print("  Choice: ");
    }
}