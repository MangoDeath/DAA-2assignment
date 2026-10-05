/**
 * TASK 3. You write this one. About 45 lines. The hardest of the four --
 * do it after SortedListRoom is passing.
 *
 * A binary max-heap stored in a plain array -- no node objects, no pointers.
 * For index i: parent is (i-1)/2, children are 2i+1 and 2i+2.
 * "Max" here means "should be called next", under the same rule as
 * SortedListRoom: Patient.before(a, b) decides that, same as before -- never
 * compare severities yourself.
 *
 * WHAT THIS ROOM PROMISES
 *   The root (index 0) is always the patient who should be called next.
 *   add() puts a new patient in and restores that promise by sifting up;
 *   next() removes the root and restores the promise by sifting down.
 *
 * THE PART THAT DECIDES YOUR MARK
 *   siftDown must compare BOTH children, not just one -- pick whichever
 *   child is "more before" than the current node (if either), swap with
 *   that one, and keep going. Comparing only the left child is a bug that a
 *   short hand-trace will often NOT catch -- it only shows up once the heap
 *   is several levels deep, which a full shift (java Main, not just
 *   `trace`) will expose.
 *
 * No ready-made collection -- your own Patient[] array, resized by doubling
 * when full, exactly like ArrayRoom does.
 */
public class HeapRoom implements WaitingRoom {

    private Patient[] a = new Patient[4];
    private int size;

    public void add(Patient p) {
        // TODO 1: if the array is full, double it (same idea as
        //         ArrayRoom.add -- new array, System.arraycopy the old
        //         contents in).
        // TODO 2: place p at index `size`.
        // TODO 3: sift it up from there (see siftUp below), then size++.
        if (size == a.length) {
            Patient[] bigger = new Patient[a.length * 2];
            System.arraycopy(a, 0, bigger, 0, size);
            a = bigger;
        }
        a[size] = p;
        siftUp(size);
        size++;
    }

    public Patient next() {
        // TODO 4: empty room -> throw new IllegalStateException("empty room")
        // TODO 5: the root (index 0) is the answer to return.
        // TODO 6: size--; move the LAST element into the root's old spot
        //         (index 0); clear the now-unused last slot.
        // TODO 7: if the room isn't empty, sift the new root down (see
        //         siftDown below).
        if (size == 0) throw new IllegalStateException("empty room");
        Patient result = a[0];
        size--;
        a[0] = a[size];
        a[size] = null;
        if (size > 0) siftDown(0);
        return result;
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (!Patient.before(a[i], a[parent])) break;
            swap(i, parent);
            i = parent;
        }
        // TODO 8: while i has a parent and p at i should come before its
        //         parent (Patient.before), swap them and move i to the
        //         parent's index. Stop when it shouldn't move any further.
    }

    private void siftDown(int i) {
        while (true) {
            int best = i;
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            if (left < size && Patient.before(a[left], a[best])) best = left;
            if (right < size && Patient.before(a[right], a[best])) best = right;
            if (best == i) break;
            swap(i, best);
            i = best;
        }
        // TODO 9: repeatedly find which of {i, left child, right child}
        //         should come first (Patient.before, checking BOTH
        //         children -- this is the step everyone gets wrong the
        //         first time). If it's i, stop. Otherwise swap i with
        //         whichever child won and continue from there.
    }

    private void swap(int i, int j) { Patient t = a[i]; a[i] = a[j]; a[j] = t; }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    /** GIVEN -- the heap array from index 0, used by the trace. Do not change. */
    public String layout() {
        StringBuilder s = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            if (i > 0) s.append(' ');
            s.append('#').append(a[i].id()).append('s').append(a[i].severity());
        }
        return s.append(']').toString();
    }
}
