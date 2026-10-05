import java.io.IOException;
import java.util.Locale;

/**
 * GIVEN -- the measuring harness for the report. You do not write or change it.
 *
 *     java Bench <your barcode>
 *
 * It runs every measurement once as a warm-up and then takes the best of three,
 * and it adds every result into one number it prints at the end, so that the JIT
 * cannot decide the timed code has no effect and delete it. Those two tricks are
 * what separates a measurement from a random number; you are asked about them at
 * the defense, so read the two methods at the bottom.
 *
 * FifoQueueRoom is not in these tables: every one of its operations is O(1), so
 * there is nothing to compare and nothing interesting to time. The tables are
 * about the three structures whose add/next cost actually differs: array, list,
 * heap.
 *
 * Copy the two tables it prints straight into your report, then explain them.
 * The exact numbers depend on the machine that runs them -- what matters is the
 * shape (does the time grow the way the complexity analysis says it should), not
 * matching anyone else's milliseconds.
 */
public class Bench {

    static final int REPEATS = 3;
    static long sink;                 // keeps the timed work alive, see the note above

    public static void main(String[] args) throws IOException {
        if (args.length < 1) { System.out.println("usage: java Bench <your barcode>"); return; }
        long barcode = 251449;
        Patient[] all = Shift.load(barcode);

        System.out.println("Bench for barcode " + barcode + "   (best of " + REPEATS + ", one warm-up first)");
        System.out.println();

        boolean[] on = { true, written(new SortedListRoom(), all[0]), written(new HeapRoom(), all[0]) };
        if (!on[1] || !on[2]) {
            System.out.println("Not written yet: " + (!on[1] ? "SortedListRoom " : "") + (!on[2] ? "HeapRoom" : ""));
            System.out.println("Those columns will show a dash. Finish Task 2 and Task 3 for the full tables.");
            System.out.println();
        }

        // ---------------- A - one whole shift ----------------
        System.out.println("A - one whole shift, milliseconds");
        System.out.printf(Locale.ROOT, "%8s %10s %10s %10s%n", "n", "array", "list", "heap");
        int[] sizes = {1_000, 5_000, 20_000};
        double[][] t = new double[sizes.length][3];
        for (int r = 0; r < sizes.length; r++) {
            Patient[] ps = java.util.Arrays.copyOf(all, sizes[r]);
            t[r][0] = best(() -> shift(new ArrayRoom(), ps));
            t[r][1] = on[1] ? best(() -> shift(new SortedListRoom(), ps)) : Double.NaN;
            t[r][2] = on[2] ? best(() -> shift(new HeapRoom(), ps))       : Double.NaN;
            System.out.printf(Locale.ROOT, "%8d %10s %10s %10s%n", sizes[r],
                    ms(t[r][0]), ms(t[r][1]), ms(t[r][2]));
        }
        System.out.printf(Locale.ROOT, "  from n=%d to n=%d (4x the patients):   array %s   list %s   heap %s%n",
                sizes[1], sizes[2], fx(t[2][0] / t[1][0]), fx(t[2][1] / t[1][1]), fx(t[2][2] / t[1][2]));
        System.out.println("  (the ratio is what you explain -- not the milliseconds themselves, which are");
        System.out.println("   specific to this machine and will differ from anyone else's run.)");
        System.out.println();

        // ---------------- B - the two operations apart ----------------
        final int FILL = 19_000, BATCH = 1_000;
        System.out.println("B - the two operations on their own, in a room already holding " + FILL + " patients");
        System.out.printf(Locale.ROOT, "%8s %18s %18s%n", "room", BATCH + " adds, ms", BATCH + " calls, ms");
        String[] names = {"array", "list", "heap"};
        for (int which = 0; which < 3; which++) {
            final int w = which;
            if (!on[which]) { System.out.printf(Locale.ROOT, "%8s %18s %18s%n", names[which], "-", "-"); continue; }
            double bestAdd = Double.MAX_VALUE, bestCall = Double.MAX_VALUE;
            for (int rep = 0; rep <= REPEATS; rep++) {          // rep 0 is the warm-up
                WaitingRoom room = make(w);
                for (int i = 0; i < FILL; i++) room.add(all[i]);  // fill with adds only, not timed

                long t0 = System.nanoTime();
                for (int i = FILL; i < FILL + BATCH; i++) room.add(all[i]);
                long t1 = System.nanoTime();
                for (int i = 0; i < BATCH; i++) sink += room.next().id();
                long t2 = System.nanoTime();

                if (rep > 0) {
                    bestAdd  = Math.min(bestAdd,  (t1 - t0) / 1e6);
                    bestCall = Math.min(bestCall, (t2 - t1) / 1e6);
                }
            }
            System.out.printf(Locale.ROOT, "%8s %18.3f %18.3f%n", names[which], bestAdd, bestCall);
        }

        System.out.println();
        System.out.println("(work guard: " + sink + " - ignore the value, it only keeps the timed code alive)");
    }

    static String ms(double v) { return Double.isNaN(v) ? "-" : String.format(Locale.ROOT, "%.2f", v); }
    static String fx(double v) { return Double.isNaN(v) ? "-" : String.format(Locale.ROOT, "x%.1f", v); }

    static boolean written(WaitingRoom room, Patient p) {
        try { room.add(p); room.next(); return true; }
        catch (UnsupportedOperationException e) { return false; }
    }

    static WaitingRoom make(int which) {
        return which == 0 ? new ArrayRoom() : which == 1 ? new SortedListRoom() : new HeapRoom();
    }

    /** One shift, exactly like Shift.run, with the result folded into the guard. No CallLog needed here. */
    static void shift(WaitingRoom room, Patient[] ps) {
        Shift.Result r = Shift.run(room, null, ps);
        sink += r.checksum;
    }

    /**
     * Run it once to let the JIT compile it, then three more times and keep the
     * fastest. The fastest run is the one least disturbed by the rest of the machine.
     */
    static double best(Runnable job) {
        job.run();                                   // warm-up, not measured
        double ms = Double.MAX_VALUE;
        for (int i = 0; i < REPEATS; i++) {
            long t0 = System.nanoTime();
            job.run();
            ms = Math.min(ms, (System.nanoTime() - t0) / 1e6);
        }
        return ms;
    }
}
