/** What every waiting room must be able to do. Four rooms implement it; you write three of them. */
public interface WaitingRoom {
    void add(Patient p);     // a patient arrives
    Patient next();          // the doctor calls the next patient: remove and return them
    int size();
    boolean isEmpty();
}
