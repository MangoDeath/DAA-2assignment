/**
 * One patient. id is the arrival number: patient #7 is the seventh through the door,
 * so a smaller id always means an earlier arrival.
 */
public record Patient(int id, String name, int severity) {

    /** true if a must be seen before b: more severe first, and on equal severity the earlier arrival. */
    public static boolean before(Patient a, Patient b) {
        if (a.severity != b.severity) return a.severity > b.severity;
        return a.id < b.id;
    }

    @Override public String toString() { return "#" + id + " " + name + " s" + severity; }
}
