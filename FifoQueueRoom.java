/**
 * TASK 1 -- the warm-up. You write this one. About 20 lines.
 *
 * A singly linked queue, no triage at all: first come, first served, severity
 * ignored completely. Read ArrayRoom.java first if you haven't -- same
 * interface, already finished, worth 5 minutes before you start this one.
 *
 * WHAT THIS ROOM PROMISES
 *   add() links a new node onto the tail. next() unlinks the head. Nothing
 *   about severity ever enters into it.
 *
 * WHY THIS ONE GRADES ITSELF
 *   Because severity is ignored, a correct FifoQueueRoom produces the exact
 *   same checksum on EVERY barcode: 1^2 + 2^2 + ... + 10000^2. Main checks
 *   this for you automatically -- get that one number right and this task is
 *   done, no comparison against anyone else's code needed.
 *
 * No ready-made collection -- your own Node, a head pointer, a tail pointer,
 * a size counter.
 */
public class FifoQueueRoom implements WaitingRoom {

    private static class Node {
        final Patient p;
        Node next;
        Node(Patient p) { this.p = p; }
    }

    private Node head, tail;
    private int size;

    public void add(Patient p) {
        // TODO 1: make a fresh node for p.
        Node newNode = new Node(p);
        
        // TODO 2: if the room is empty (tail == null), the new node is both
        //         head and tail.

        // TODO 3: otherwise link it on after the current tail, then make it
        //         the new tail.
        if (head == null){
            head = newNode;
        } 
        else {tail.next = newNode;
        }
        // TODO 4: size++

        tail = newNode;
        size++;
        
    }

    public Patient next() {
        // TODO 5: empty room -> throw new IllegalStateException("empty room")

        if (head == null){
            throw new IllegalStateException("empty room");
        }
        // TODO 6: the head is the answer: take its patient, move head one
        //         step on. If that empties the room, remember to fix tail too.

        Patient result = head.p;
        head = head.next;
        size--;

         if (head == null) {
            tail = null;
        }
        return result;
    }

        // TODO 7: size--, return the patient
        
    

    public int size()
        { return size; }

    public boolean isEmpty()
        { return size == 0; }
}
