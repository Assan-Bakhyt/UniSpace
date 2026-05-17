package UniSpace.util;

public final class Colors {

    public static final String RESET  = "[0m";
    public static final String RED    = "[31m";
    public static final String GREEN  = "[32m";
    public static final String YELLOW = "[33m";
    public static final String GRAY   = "[90m";
    public static final String BOLD   = "[1m";
    public static final String PURPLE = "[35m";
    public static final String CYAN   = "[36m";

    private Colors() {}

    public static String red(String s)    { return RED    + s + RESET; }
    public static String green(String s)  { return GREEN  + s + RESET; }
    public static String yellow(String s) { return YELLOW + s + RESET; }
    public static String gray(String s)   { return GRAY   + s + RESET; }
    public static String bold(String s)   { return BOLD   + s + RESET; }
    public static String purple(String s) { return PURPLE + s + RESET; }
    public static String cyan(String s)   { return CYAN   + s + RESET; }
    public static String accent(String s) { return PURPLE + BOLD + s + RESET; }
}
