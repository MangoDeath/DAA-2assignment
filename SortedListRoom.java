/**
 * TASK 2. You write this one. About 25 lines.
 *
 * A singly linked list kept in call order at all times.
 *
 * WHAT THIS ROOM PROMISES
 *   Walking from head to tail gives exactly the order the doctor will call
 *   the patients in. So next() is trivial -- the head is the answer.
 *   Everything is paid for in add().
 *
 * THE PART THAT DECIDES YOUR MARK
 *   Patients of the same severity are not interchangeable: the one who
 *   arrived earlier is called earlier. Patient.before(a, b) already knows
 *   the whole rule -- ask it, and never compare severities yourself. Work
 *   out for yourself where the walk has to stop: stop one node too early and
 *   every tie comes out backwards, and your list row stops matching the
 *   given array row.
 *
 * No ready-made collection -- your own Node, a head pointer and a size
 * counter.
 */
public class SortedListRoom implements WaitingRoom {

    private static class Node {
        final Patient p;
        Node next;
        Node(Patient p) { this.p = p; }
    }

    private Node head;
    private int size;

    public void add(Patient p) {
        // TODO 1: p may belong in front of everybody, including the
        //         empty-list case. Handle that first -- it is the only case
        //         that moves head.
        // TODO 2: otherwise walk from the head until the cursor is standing
        //         just before the place where p belongs, then link p in
        //         after it. Decide carefully what the cursor has to be
        //         looking at to stop.
        // TODO 3: size++
        Node node = new Node(p);
        if (head == null || Patient.before(p, head.p)) {
            node.next = head;
            head = node;
        } else {
            Node cursor = head;
            // Skip everyone who belongs before the new patient.
            while (cursor.next != null && !Patient.before(p, cursor.next.p)) {
                cursor = cursor.next;
            }
            node.next = cursor.next;
            cursor.next = node;
        }
        size++;
    }

    public Patient next() {
        // TODO 4: empty room -> throw new IllegalStateException("empty room")
        // TODO 5: the head is the answer: take its patient, move head one
        //         step on, size--, and return that patient
        if (head == null) throw new IllegalStateException("empty room");
        Patient result = head.p;
        head = head.next;
        size--;
        return result;
    }

    public int size()        { return size; }
    public boolean isEmpty() { return size == 0; }

    /** GIVEN -- the list from head to tail, used by the trace. Do not change. */
    public String layout() {
        StringBuilder s = new StringBuilder("[");
        for (Node c = head; c != null; c = c.next) {
            if (c != head) s.append(' ');
            s.append('#').append(c.p.id()).append('s').append(c.p.severity());
        }
        return s.append(']').toString();
    }
}
