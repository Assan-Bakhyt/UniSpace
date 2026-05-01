package UniSpace.enums;

public enum Faculty {
    CS("Computer Science"),
    MATH("Mathematics"),
    PHYSICS("Physics");

    private final String displayName;

    Faculty(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
