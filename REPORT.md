# DAA - Assignment 2 report - Who Goes Next

Name: Nauryzbay Kinayatov
Group: SE-2536
Barcode: 251449

## 1. Correctness

Output of `java Main 251449`:

```text
barcode,251449
room,checksum,critical_served,critical_wait
fifo,333383335000,1010,5002649
array,660498361860,2013,1250
list,660498361860,2013,1250
heap,660498361860,2013,1250

OK   FifoQueueRoom matches the fixed no-triage checksum for any barcode.
OK   SortedListRoom calls the patients in exactly the same order as the given ArrayRoom.
OK   HeapRoom calls the patients in exactly the same order as the given ArrayRoom.
OK   FifoQueueRoom throws IllegalStateException on an empty room.
OK   SortedListRoom throws IllegalStateException on an empty room.
OK   HeapRoom throws IllegalStateException on an empty room.
OK   CallLog records every call and recent(3) is non-destructive and most-recent-first.

never called by the end of the shift, by severity:
  no triage (fifo)   s5 1003     s4 1459     s3 2506     s2 2459     s1 2573
  with triage        s5 0        s4 1        s3 61       s2 4947     s1 4991
```

All checks show OK. List and heap match ArrayRoom's call order, FIFO has the expected checksum, and recent(3) does not change CallLog.

## 2. Complexity

Here, n is the number of patients already waiting.

| Method | Worst case | Amortised bound |
|---|---|---|
| SortedListRoom.add | Theta(n) | Theta(n) |
| HeapRoom.add | Theta(n) | O(log n) |

SortedListRoom.add may walk through the whole list to insert a patient at the end. Repeated additions can require long walks each time, so amortisation does not improve the bound.

HeapRoom.add normally sifts up in O(log n), but doubling the array copies n patients, making that single call Theta(n). Over many additions, doubling gives O(1) amortised copying per addition. Including sifting, the amortised bound is O(log n).

## 3. Benchmark

Results of `java Bench 251449`, in milliseconds (best of three runs after warm-up).

**Table A - whole shift**

| Patients | Array | List | Heap |
|---|---:|---:|---:|
| 1,000 | 0.66 | 0.34 | 0.19 |
| 5,000 | 3.77 | 8.09 | 0.69 |
| 20,000 | 75.94 | 143.63 | 1.74 |
| Growth from 5,000 to 20,000 | 20.2x | 17.8x | 2.5x |

The complexity-based prediction is roughly 16x growth for array and list with 4x more patients, because a shift can take quadratic time. For heap, O(n log n) suggests slightly more than 4x. Array and list grew by 20.2x and 17.8x, roughly matching the prediction. Heap grew by only 2.5x, so its measured ratio does not closely match the prediction, although it stayed much faster. Short timings are affected by JVM optimisation and measurement noise.

**Table B - operations with 19,000 patients already waiting**

| Room | 1,000 adds | 1,000 calls |
|---|---:|---:|
| Array | 0.007 | 34.286 |
| List | 54.581 | 0.071 |
| Heap | 0.035 | 0.191 |

Array's expensive operation is next(), which searches for the best patient. List's expensive operation is add(), which searches for an insertion position; next() only removes the head. Heap keeps both operations fast by moving along the tree height. Exact timings differ between students because of the barcode, machine, background load and JVM warm-up.

## 4. Triage effect

With FIFO, 1,003 severity-5 patients were never called. With triage, that number was 0, so all 2,013 of the most severe patients were called during the shift.

For severity-1 patients, the number never called increased from 2,573 with FIFO to 4,991 with triage. Triage gives priority to the most severe patients, but leaves more of the least severe patients waiting at the end.
