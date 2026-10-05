import java.util.Arrays;

/**
 * ===========================================================================
 *  WORKED EXAMPLE -- read this file before you write anything.
 * ===========================================================================
 *
 * This is one finished waiting room, and it is the only one you are given.
 * It shows you three things you will need for your own three:
 *
 *   1. what implementing WaitingRoom looks like -- four methods, nothing else;
 *   2. how a dynamic array grows (question 2 of the report is about this code);
 *   3. that every ordering decision goes through Patient.before, never
 *      through a severity comparison you write yourself.
 *
 * This room is also a TRUSTED REFERENCE IMPLEMENTATION: it follows the same rule as
 * your SortedListRoom and your HeapRoom, so all three must call patients in exactly
 * the same order. When your two disagree with this one, look for the mistake in
 * your own code first -- but "trusted" is not "infallible by definition": if you
 * genuinely believe you found a bug here, say so at the defense and show why.
 *
 * The idea: pile the patients up in any order and do nothing on arrival.
 * The search happens when the doctor asks, and it looks at everybody.
 */
public class ArrayRoom implements WaitingRoom {

    private Patient[] a = new Patient[4];   // room for four; it grows by doubling
    private int size;                       // how many of those slots are in use

    /** A patient arrives: put them at the end. No order is maintained at all. */
    public void add(Patient p) {
        if (size == a.length) {
            // Full. Make an array twice as long and copy everything across.
            // The copy costs n, but it happens rarely enough that add is still
            // O(1) on average -- that is the sum you work out in the report.
            // (A SINGLE add can still cost Theta(n) in the worst case, the one
            // that happens to trigger the resize -- amortised and worst-case
            // are two different numbers, and the report asks for both.)
            a = Arrays.copyOf(a, a.length * 2);
        }
        a[size] = p;
        size++;
    }

    /** The doctor calls one patient in: scan all of them and take the best. */
    public Patient next() {
        if (size == 0) throw new IllegalStateException("empty room");

        int best = 0;
        for (int i = 1; i < size; i++) {
            // "Does i have to be seen before the best one found so far?"
            // Patient.before knows the whole rule: severity first, and on
            // equal severity the smaller id, because that patient arrived
            // earlier. Ask it; never compare severities here yourself.
            if (Patient.before(a[i], a[best])) best = i;
        }

        Patient chosen = a[best];

        // Remove them without leaving a hole: the LAST patient moves into the
        // gap. That scrambles the array, and it does not matter -- the order in
        // the array never meant anything. Who goes first is decided by
        // Patient.before, not by where somebody happens to sit.
        size--;
        a[best] = a[size];
        a[size] = null;

        return chosen;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }
}
