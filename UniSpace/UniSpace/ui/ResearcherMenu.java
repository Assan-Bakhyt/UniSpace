package UniSpace.ui;

import UniSpace.enums.Faculty;
import UniSpace.model.research.ResearchProject;
import UniSpace.model.research.Researcher;
import UniSpace.service.ResearchService;
import UniSpace.util.Colors;
import UniSpace.util.ConsoleHelper;
import UniSpace.util.Paginator;
import UniSpace.util.PaperCitationsComparator;
import UniSpace.util.PaperDateComparator;
import UniSpace.util.PaperLengthComparator;

import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

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
            printMainMenu();
            String choice = ConsoleHelper.readChoice(scanner);
            switch (choice) {
                case "1"  -> myResearchMenu();
                case "2"  -> universityResearchMenu();
                case "0"  -> running = false;
                default   -> System.out.println(Colors.red("  Invalid option. Try again."));
            }
        }
    }

    // ==========================================================================
    //  1. MY RESEARCH
    // ==========================================================================

    private void myResearchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- My Research --"));
            System.out.println("   1. View my papers");
            System.out.println("   2. Add paper");
            System.out.println("   3. My h-index");
            System.out.println("   4. Create research project");
            System.out.println("   5. Join research project");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> viewMyPapers();
                case "2" -> addPaper();
                case "3" -> viewHIndex();
                case "4" -> createProject();
                case "5" -> joinProject();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option. Try again."));
            }
        }
    }

    private void viewMyPapers() {
        System.out.println(Colors.gray("\n  Sort by:  1. Date   2. Citations   3. Length"));
        System.out.print("  Choice: ");
        switch (ConsoleHelper.readChoice(scanner)) {
            case "1" -> currentResearcher.printPapers(new PaperDateComparator());
            case "2" -> currentResearcher.printPapers(new PaperCitationsComparator());
            case "3" -> currentResearcher.printPapers(new PaperLengthComparator());
            default  -> System.out.println(Colors.red("  Invalid choice."));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void addPaper() {
        System.out.println(Colors.purple("\n  -- Add Research Paper --"));
        System.out.print("  Title: ");   String title   = ConsoleHelper.readChoice(scanner);
        System.out.print("  Journal: "); String journal = ConsoleHelper.readChoice(scanner);
        System.out.println(Colors.gray("  DOI = Digital Object Identifier, e.g. 10.1109/access.2022.3001234"));
        System.out.print("  DOI: ");     String doi     = ConsoleHelper.readChoice(scanner);
        int pages = ConsoleHelper.readInt(scanner, "  Number of pages: ");
        int year  = ConsoleHelper.readInt(scanner, "  Year published: ");
        researchService.addPaperToResearcher(currentResearcher, title, journal, doi, pages, year);
        UniSpace.storage.DataRepository.getInstance().save();
        System.out.println(Colors.green("  Paper added successfully."));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void viewHIndex() {
        System.out.println(Colors.purple("\n  -- H-Index --"));
        System.out.println("  Your current h-index: " + Colors.accent(String.valueOf(currentResearcher.getHIndex())));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void createProject() {
        System.out.println(Colors.purple("\n  -- Create Research Project --"));
        System.out.print("  Project topic: ");
        String topic = ConsoleHelper.readChoice(scanner);
        if (topic.isEmpty()) {
            System.out.println(Colors.red("  [ERROR] Topic cannot be empty."));
            return;
        }
        researchService.createProject(currentResearcher, topic);
        UniSpace.storage.DataRepository.getInstance().save();
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void joinProject() {
        List<ResearchProject> available = researchService.getAllProjects().stream()
                .filter(p -> !p.getParticipants().contains(currentResearcher))
                .collect(Collectors.toList());

        ResearchProject project = Paginator.selectFromList(available, "Join Research Project",
                p -> p.getTopic() + Colors.gray(" (" + p.getParticipants().size() + " participants)"),
                scanner);
        if (project == null) return;

        try {
            researchService.joinProject(currentResearcher, project.getTopic());
            UniSpace.storage.DataRepository.getInstance().save();
            System.out.println(Colors.green("  Successfully joined: " + project.getTopic()));
        } catch (Exception e) {
            System.out.println(Colors.red("  [ERROR] " + e.getMessage()));
        }
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // ==========================================================================
    //  2. UNIVERSITY RESEARCH
    // ==========================================================================

    private void universityResearchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println(Colors.purple("\n  -- University Research --"));
            System.out.println("   1. All papers by date");
            System.out.println("   2. All papers by citations");
            System.out.println("   3. All papers by length");
            System.out.println("   4. Top cited researcher by faculty");
            System.out.println("   5. Top cited researcher by year");
            System.out.println(Colors.gray("   0. <- Back"));
            System.out.print("  Choice: ");

            switch (ConsoleHelper.readChoice(scanner)) {
                case "1" -> printAllPapers(new PaperDateComparator(), "by Date");
                case "2" -> printAllPapers(new PaperCitationsComparator(), "by Citations");
                case "3" -> printAllPapers(new PaperLengthComparator(), "by Length");
                case "4" -> findTopCitedByFaculty();
                case "5" -> findTopCitedByYear();
                case "0" -> back = true;
                default  -> System.out.println(Colors.red("  Invalid option. Try again."));
            }
        }
    }

    private void printAllPapers(java.util.Comparator<?> comparator, String label) {
        System.out.println(Colors.purple("\n  -- All Research Papers " + label + " --"));
        researchService.printAllPapers((java.util.Comparator<UniSpace.model.research.ResearchPaper>) comparator);
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void findTopCitedByFaculty() {
        Faculty faculty = pickFaculty();
        if (faculty == null) return;
        Researcher top = researchService.getTopCitedResearcherBySchool(faculty);
        if (top == null) {
            System.out.println(Colors.yellow("  No researcher found for " + faculty));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        System.out.println(Colors.purple("\n  -- Top Cited Researcher — " + faculty + " --"));
        System.out.println("  " + top);
        System.out.println("  Total citations: " + Colors.accent(String.valueOf(top.getTotalCitations())));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    private void findTopCitedByYear() {
        int year = ConsoleHelper.readInt(scanner, "  Year: ");
        Researcher top = researchService.getTopCitedResearcherByYear(year);
        if (top == null) {
            System.out.println(Colors.yellow("  No researcher with citations found for year " + year));
            ConsoleHelper.pressEnterToContinue(scanner);
            return;
        }
        System.out.println(Colors.purple("\n  -- Top Cited Researcher — " + year + " --"));
        System.out.println("  " + top);
        System.out.println("  Total citations: " + Colors.accent(String.valueOf(top.getTotalCitations())));
        ConsoleHelper.pressEnterToContinue(scanner);
    }

    // -- Helpers ---------------------------------------------------------------

    private Faculty pickFaculty() {
        Faculty[] values = Faculty.values();
        System.out.println(Colors.gray("  Select faculty:"));
        for (int i = 0; i < values.length; i++)
            System.out.printf("    %d. %s%n", i + 1, values[i]);
        System.out.print("  Choice: ");
        try {
            int idx = Integer.parseInt(ConsoleHelper.readChoice(scanner)) - 1;
            if (idx >= 0 && idx < values.length) return values[idx];
        } catch (NumberFormatException ignored) {}
        System.out.println(Colors.red("  [ERROR] Invalid selection."));
        return null;
    }

    private void printMainMenu() {
        System.out.println(Colors.gray("\n  ======================================"));
        System.out.println(Colors.bold("   Researcher Menu - " + ownerFaculty));
        System.out.println(Colors.gray("   h-index: ") + currentResearcher.getHIndex());
        System.out.println(Colors.gray("  ======================================"));
        System.out.println("   1. My Research");
        System.out.println("   2. University Research");
        System.out.println(Colors.gray("   0. <- Back"));
        System.out.print("  Choice: ");
    }
}
