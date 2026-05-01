package UniSpace.ui;

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

    private final String researchSchool;
    private final ResearchService researchService;
    private final Scanner scanner;

    public ResearcherMenu(String researchSchool, ResearchService researchService) {
        this.researchSchool = researchSchool;
        this.researchService = researchService;
        this.scanner = new Scanner(System.in);
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
                case "4" -> findTopCitedResearcherBySchool();
                case "5" -> findTopCitedResearcherByYear();
                case "0" -> running = false;
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    // ── Menu actions ─────────────────────────────────────────────────────────

    private void printAllPapersByDate() {
        System.out.println("\n── Research Papers by Date ──");
        researchService.printAllPapers(new PaperDateComparator());
    }

    private void printAllPapersByCitations() {
        System.out.println("\n── Research Papers by Citations ──");
        researchService.printAllPapers(new PaperCitationsComparator());
    }

    private void printAllPapersByLength() {
        System.out.println("\n── Research Papers by Length ──");
        researchService.printAllPapers(new PaperLengthComparator());
    }

    private void findTopCitedResearcherBySchool() {
        System.out.print("Enter school/department/major: ");
        String school = scanner.nextLine().trim();

        Researcher topResearcher = researchService.getTopCitedResearcherBySchool(school);

        if (topResearcher == null) {
            System.out.println("No researcher found for this school.");
            return;
        }

        System.out.println("\n── Top Cited Researcher by School ──");
        System.out.println(topResearcher);
        System.out.println("Total citations: " + topResearcher.getTotalCitations());
    }

    private void findTopCitedResearcherByYear() {
        System.out.print("Enter year: ");
        String input = scanner.nextLine().trim();

        try {
            int year = Integer.parseInt(input);

            Researcher topResearcher = researchService.getTopCitedResearcherByYear(year);

            if (topResearcher == null) {
                System.out.println("No researcher found for this year.");
                return;
            }

            System.out.println("\n── Top Cited Researcher by Year ──");
            System.out.println(topResearcher);
            System.out.println("Total citations: " + topResearcher.getTotalCitations());

        } catch (NumberFormatException e) {
            System.out.println("[ERROR] Year must be a number.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void printMenu() {
        System.out.println("\n══════════════════════════════");
        System.out.println(" Researcher Menu");
        System.out.println(" School: " + researchSchool);
        System.out.println("══════════════════════════════");
        System.out.println(" 1. Print all papers by date");
        System.out.println(" 2. Print all papers by citations");
        System.out.println(" 3. Print all papers by length");
        System.out.println(" 4. Find top cited researcher by school");
        System.out.println(" 5. Find top cited researcher by year");
        System.out.println(" 0. Exit");
        System.out.print("Choice: ");
    }
}