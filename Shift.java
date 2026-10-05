import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** GIVEN -- reads your patients and runs one simulated shift. You do not need to change anything here. */
public class Shift {

    public static final int ARRIVALS   = 20_000;  // simulated arrival events
    public static final int ROSTER_SIZE = 60_000;  // rows in patients.csv

    /**
     * Your own window of 20 000 patients out of the 60 000-row roster, chosen by your
     * barcode. offset ranges over EVERY window that fits, including the one that ends
     * on the very last row of the file.
     */
    public static Patient[] load(long barcode) throws IOException {
        int windows = ROSTER_SIZE - ARRIVALS + 1;              // 40 001 possible windows
        int offset = (int) (barcode % windows);
        Patient[] ps = new Patient[ARRIVALS];
        try (BufferedReader in = Files.newBufferedReader(Path.of("patients.csv"))) {
            in.readLine();                                  // skip the header line
            for (int i = 0; i < offset; i++) in.readLine(); // skip to your window
            for (int i = 0; i < ARRIVALS; i++) {
                String[] cell = in.readLine().split(",");
                ps[i] = new Patient(i + 1, cell[0], Integer.parseInt(cell[1]));
            }
        }
        return ps;
    }

    /** What one simulated shift produced. */
    public static class Result {
        public long served, checksum, criticalServed, criticalWait;
        /** stillWaiting[s] = patients of severity s who were never called. Index 1..5. */
        public final long[] stillWaiting = new long[6];
        /** the exact sequence of ids in the order they were called -- ground truth for Main. */
        public int[] calledOrder;
    }

    /**
     * One simulated shift: at event m patient m arrives, and after every second arrival
     * the doctor is free and calls one patient in -- so 20 000 arrive and 10 000 are seen.
     * The k-th patient called adds k * their id to the checksum, and is pushed onto log.
     */
    public static Result run(WaitingRoom room, CallLog log, Patient[] ps) {
        Result r = new Result();
        int[] order = new int[ps.length / 2];
        boolean[] called = new boolean[ps.length + 1];       // called[id]
        for (int minute = 1; minute <= ps.length; minute++) {
            room.add(ps[minute - 1]);

            if (minute % 2 == 0) {
                Patient p = room.next();
                called[p.id()] = true;
                order[(int) r.served] = p.id();
                r.served++;
                r.checksum += r.served * p.id();
                if (log != null) log.push(new CallLog.Entry(p, minute));
                if (p.severity() == 5) {
                    r.criticalServed++;
                    r.criticalWait += minute - p.id();
                }
            }
        }
        r.calledOrder = order;
        for (Patient p : ps) if (!called[p.id()]) r.stillWaiting[p.severity()]++;
        return r;
    }
}
