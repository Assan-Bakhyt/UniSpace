package UniSpace.patterns;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Strategy interface for sorting any list of items.
 * Pattern: Strategy (GoF).
 *
 * Concrete strategies: PaperDateComparator, PaperCitationsComparator,
 * PaperLengthComparator (implement both Comparator<T> and SortStrategy<T>).
 */
public interface SortStrategy<T> {

    /** Returns the comparator that defines the sort order for this strategy. */
    Comparator<T> getComparator();

    /** Returns a new sorted list; the original list is not modified. */
    default List<T> sort(List<T> items) {
        List<T> result = new ArrayList<>(items);
        result.sort(getComparator());
        return result;
    }
}
