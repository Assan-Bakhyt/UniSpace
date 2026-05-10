package UniSpace.enums;

public enum Faculty {
    CS("Computer Science"),
    MATH("Mathematics"),
    PHYSICS("Physics"),
    FIT("Faculty of Information Technology"),
    BS("Business School"),
    KMA("Kazakh Maritime Academy"),
    ISE("International School of Economics");

    private final String displayName;

    Faculty(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
