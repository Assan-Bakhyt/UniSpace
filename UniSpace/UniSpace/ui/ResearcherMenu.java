package UniSpace.ui;

import UniSpace.enums.Faculty;
import UniSpace.model.research.Researcher;
import UniSpace.service.ResearchService;
import UniSpace.util.PaperCitationsComparator;
import UniSpace.util.PaperDateComparator;
import UniSpace.util.PaperLengthComparator;

import java.util.Scanner;

/**
 * Console menu for researchers.
 * Handles: viewing research papers, sorting papers, finding top cited researchers.
 */
public class ResearcherMenu {

    private final Faculty         ownerFaculty;
    private final ResearchService researchService;
    private final Scanner         scanner;

    public ResearcherMenu(Faculty ownerFaculty, ResearchService researchService) {
        this.ownerFaculty    = ownerFaculty;
        this.researchService = researchService;
        this.scanner         = new Scanner(System.in);
    }

    public void show() {
        boolean running = true;
        while (running) {
            printMenu();
            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> printAllPapersByDate();
                case "2" -> printAllPapersByCitations();
                case "3" -> printAllPapersByLength();
                case "4" -> findTopCitedResearcherByFaculty();
                case "5" -> findTopCitedResearcherByYear();
                case "0" -> running = false;
                default  -> System.out.println("  Invalid option. Try again.");
            }
        }
    }

    // ── Menu actions ──────────────────────────────────────────────────────────

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
        if (top == null) {
            System.out.println("  No researcher found for faculty: " + faculty);
            return;
        }
        System.out.println("\n  ── Top Cited Researcher — " + faculty + " ──");
        System.out.println("  " + top);
        System.out.println("  Total citations: " + top.getTotalCitations());
    }

    private void findTopCitedResearcherByYear() {
        System.out.print("  Year: ");
        try {
            int year = Integer.parseInt(scanner.nextLine().trim());
            Researcher top = researchService.getTopCitedResearcherByYear(year);
            if (top == null) {
                System.out.println("  No researcher found for year " + year);
                return;
            }
            System.out.println("\n  ── Top Cited Researcher — " + year + " ──");
            System.out.println("  " + top);
            System.out.println("  Total citations: " + top.getTotalCitations());
        } catch (NumberFormatException e) {
            System.out.println("  [ERROR] Year must be a number.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /** Displays all Faculty options and returns the user's choice, or null on invalid input. */
    private Faculty pickFaculty() {
        Faculty[] values = Faculty.values();
        System.out.println("  Select faculty:");
        for (int i = 0; i < values.length; i++) {
            System.out.printf("    %d. %s%n", i + 1, values[i]);
        }
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
        System.out.println("   1. Print all papers by date");
        System.out.println("   2. Print all papers by citations");
        System.out.println("   3. Print all papers by length");
        System.out.println("   4. Find top cited researcher by faculty");
        System.out.println("   5. Find top cited researcher by year");
        System.out.println("   0. Back");
        System.out.print("  Choice: ");
    }
}
