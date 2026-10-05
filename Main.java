import java.io.IOException;

/**
 * GIVEN -- prints everything you are graded on. You do not change anything here.
 *
 *   java Main <your barcode>            your five rows, and OK / WRONG for your three classes
 *   java Main <your barcode> trace 6    your list and your heap, side by side
 *
 * A room you have not written yet is simply reported as such, so both commands are
 * useful while you are half way through.
 */
public class Main {

    public static void main(String[] args) throws IOException {
        if (args.length < 1) { System.out.println("usage: java Main <your barcode> [trace <k>]"); return; }
        long barcode = 251449;
        if (args.length >= 3 && args[1].equals("trace")) { trace(barcode, Integer.parseInt(args[2])); return; }
        shift(barcode);
    }

    /** The fixed, dataset-independent checksum a correct FifoQueueRoom must produce: sum of k^2, k=1..10000. */
    private static long fifoChecksum(int served) {
        long s = 0;
        for (long k = 1; k <= served; k++) s += k * k;
        return s;
    }

    /** java Main <barcode> */
    static void shift(long barcode) throws IOException {
        Patient[] ps = Shift.load(barcode);
        String[] names = {"fifo", "array", "list", "heap"};
        Shift.Result[] r = new Shift.Result[4];
        String[] contractViolation = new String[4];

        System.out.println("barcode," + barcode);
        System.out.println("room,checksum,critical_served,critical_wait");
        for (int i = 0; i < names.length; i++) {
            try {
                ContractRoom room = new ContractRoom(make(i));
                // no CallLog here on purpose: an unwritten Task 4 must never make Task 1-3's
                // own rooms (especially the given ArrayRoom) look unwritten too. CallLog gets
                // its own separate run below, against a fresh ArrayRoom, so the two tasks
                // can never contaminate each other's verdict.
                r[i] = Shift.run(room, null, ps);
                contractViolation[i] = room.violation;
                System.out.println(names[i] + "," + r[i].checksum + "," + r[i].criticalServed + "," + r[i].criticalWait);
            } catch (UnsupportedOperationException e) {
                System.out.println(names[i] + ",not written yet");
            }
        }

        System.out.println();

        // fifo: dataset-independent, exact expected value -- no reference needed
        if (r[0] != null) {
            long expected = fifoChecksum((int) r[0].served);
            boolean fifoOk = r[0].checksum == expected && contractViolation[0] == null;
            System.out.println(fifoOk
                ? "OK   FifoQueueRoom matches the fixed no-triage checksum for any barcode."
                : "WRONG FifoQueueRoom checksum should always be " + expected + " regardless of barcode"
                  + (contractViolation[0] != null ? " (" + contractViolation[0] + ")" : "") + ".");
        }

        // list and heap: compared against the ACTUAL SEQUENCE OF IDS called by the given ArrayRoom,
        // not just a checksum -- two different sequences can share a checksum by coincidence.
        verdict("SortedListRoom", r[2], r[1], contractViolation[2]);
        verdict("HeapRoom",       r[3], r[1], contractViolation[3]);

        // whether an empty room is handled, which is part of every task
        emptyCheck("FifoQueueRoom",   new FifoQueueRoom());
        emptyCheck("SortedListRoom",  new SortedListRoom());
        emptyCheck("HeapRoom",        new HeapRoom());

        // CallLog: its own isolated run, against a fresh ArrayRoom, so a CallLog that isn't
        // written yet (or is buggy) can never affect the array/list/heap results above.
        callLogCheck(ps);

        // who never gets seen at all: the last question of the report.
        if (r[0] != null && r[1] != null) {
            System.out.println();
            System.out.println("never called by the end of the shift, by severity:");
            waiting("  no triage (fifo)", r[0]);
            waiting("  with triage     ", r[1]);
        }
    }

    /** next() on an empty room must throw IllegalStateException -- checked, not trusted. */
    private static void emptyCheck(String yours, WaitingRoom room) {
        try {
            room.next();
            System.out.println("WRONG " + yours + ".next() on an empty room returned instead of throwing"
                             + " IllegalStateException.");
        } catch (IllegalStateException ok) {
            System.out.println("OK   " + yours + " throws IllegalStateException on an empty room.");
        } catch (UnsupportedOperationException notYet) {
            // the class is not written yet; already reported above
        } catch (RuntimeException other) {
            System.out.println("WRONG " + yours + ".next() on an empty room threw "
                             + other.getClass().getSimpleName() + " instead of IllegalStateException.");
        }
    }

    /**
     * CallLog gets its own private shift, against a fresh ArrayRoom, entirely separate from
     * the graded array/list/heap results above -- so whether Task 4 is written, or buggy, can
     * never change what Main reports for Tasks 1-3.
     */
    private static void callLogCheck(Patient[] ps) {
        try {
            CallLog log = new CallLog();
            Shift.Result r = Shift.run(new ArrayRoom(), log, ps);
            int served = (int) r.served;
            boolean sizeOk = log.size() == served;
            var a = log.recent(3);
            var b = log.recent(3);                     // calling it twice must give the same answer
            boolean nonDestructive = log.size() == served && a.size() == b.size();
            boolean orderOk = true;
            for (int i = 0; i < a.size() && orderOk; i++) {
                int expectedId = r.calledOrder[served - 1 - i];   // most recent first
                if (a.get(i).patient.id() != expectedId || b.get(i).patient.id() != expectedId) orderOk = false;
            }
            boolean ok = sizeOk && nonDestructive && orderOk;
            System.out.println(ok
                ? "OK   CallLog records every call and recent(3) is non-destructive and most-recent-first."
                : "WRONG CallLog disagrees with the actual call order, or recent(n) is not non-destructive.");
        } catch (UnsupportedOperationException e) {
            System.out.println("--   CallLog is not written yet.");
        }
    }

    private static void waiting(String label, Shift.Result r) {
        StringBuilder s = new StringBuilder(label);
        for (int sev = 5; sev >= 1; sev--) s.append(String.format("   s%d %-6d", sev, r.stillWaiting[sev]));
        System.out.println(s);
    }

    private static void verdict(String yours, Shift.Result mine, Shift.Result given, String violation) {
        if (mine == null) { System.out.println("--   " + yours + " is not written yet."); return; }
        boolean sameOrder = java.util.Arrays.equals(mine.calledOrder, given.calledOrder);
        boolean ok = sameOrder && violation == null;
        if (ok) {
            System.out.println("OK   " + yours + " calls the patients in exactly the same order as the given ArrayRoom.");
            return;
        }
        if (!sameOrder) {
            int firstDiff = -1;
            for (int i = 0; i < mine.calledOrder.length; i++)
                if (mine.calledOrder[i] != given.calledOrder[i]) { firstDiff = i; break; }
            System.out.println("WRONG " + yours + " disagrees with the given ArrayRoom, which is trusted --"
                + " so the bug is in " + yours + ". First difference at call #" + (firstDiff + 1)
                + ": you called #" + mine.calledOrder[firstDiff] + ", ArrayRoom called #" + given.calledOrder[firstDiff]
                + ". Run trace 6 and compare the two columns by hand.");
        } else {
            System.out.println("WRONG " + yours + " calls the right patients in the right order, but " + violation
                + " -- fix size()/isEmpty(), the order itself is fine.");
        }
    }

    private static WaitingRoom make(int which) {
        return switch (which) {
            case 0 -> new FifoQueueRoom();
            case 1 -> new ArrayRoom();
            case 2 -> new SortedListRoom();
            default -> new HeapRoom();
        };
    }

    /**
     * java Main <barcode> trace <k>
     * Your list and your heap side by side after each of the first k arrivals, then after
     * one call. Same patients, same patient called, different insides.
     */
    static void trace(long barcode, int k) throws IOException {
        Patient[] ps = Shift.load(barcode);
        boolean listOn = written(new SortedListRoom(), ps[0]);
        boolean heapOn = written(new HeapRoom(), ps[0]);
        if (!listOn && !heapOn) {
            System.out.println("Neither room is written yet - start with Task 2, SortedListRoom.add.");
            return;
        }
        if (!listOn) System.out.println("(SortedListRoom is not written yet - left column empty)");
        if (!heapOn) System.out.println("(HeapRoom is not written yet - right column empty)");

        SortedListRoom l = new SortedListRoom();
        HeapRoom h = new HeapRoom();
        for (int i = 0; i < k; i++) {
            if (listOn) l.add(ps[i]);
            if (heapOn) h.add(ps[i]);
            line("add ", ps[i], listOn ? l.layout() : "-", heapOn ? h.layout() : "-");
        }
        Patient a = listOn ? l.next() : null, b = heapOn ? h.next() : null;
        line("next", a != null ? a : b, listOn ? l.layout() : "-", heapOn ? h.layout() : "-");
        if (a != null && b != null && a.id() != b.id())
            System.out.println("!! the two rooms called different patients: " + a + " and " + b);
    }

    private static boolean written(WaitingRoom room, Patient p) {
        try { room.add(p); room.next(); return true; }
        catch (UnsupportedOperationException e) { return false; }
    }

    private static void line(String what, Patient p, String list, String heap) {
        System.out.printf("%s #%-2d s%d   list %-34s heap %s%n", what, p.id(), p.severity(), list, heap);
    }

    /** Wraps a room and checks size()/isEmpty() stay consistent with what was actually added and removed. */
    private static final class ContractRoom implements WaitingRoom {
        private final WaitingRoom inner;
        private int expected = 0;
        String violation = null;

        ContractRoom(WaitingRoom inner) { this.inner = inner; }

        public void add(Patient p) {
            inner.add(p);
            expected++;
            check();
        }

        public Patient next() {
            Patient p = inner.next();
            expected--;
            check();
            return p;
        }

        private void check() {
            if (violation != null) return;
            if (inner.size() != expected) violation = "size() returned " + inner.size() + ", expected " + expected;
            else if (inner.isEmpty() != (expected == 0)) violation = "isEmpty() disagreed with size()";
        }

        public int size()        { return inner.size(); }
        public boolean isEmpty() { return inner.isEmpty(); }
    }
}
