package telran.library.entites;

import java.time.LocalDate;
import java.util.Objects;

public class PickRecord {
    long   isbn;
    int   readerId;
    LocalDate pickDate;
    LocalDate returnDate;
    int  delayDays;

    public PickRecord(LocalDate pickDate, long isbn, int readerId) {
        this.pickDate = pickDate;
        this.isbn = isbn;
        this.readerId = readerId;
    }

    public int getDelayDays() {
        return delayDays;
    }

    public long getIsbn() {
        return isbn;
    }

    public LocalDate getPickDate() {
        return pickDate;
    }

    public int getReaderId() {
        return readerId;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    public void setDelayDays(int delayDays) {
        this.delayDays = delayDays;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PickRecord that)) return false;
        return isbn == that.isbn && readerId == that.readerId && Objects.equals(pickDate, that.pickDate);
    }


    @Override
    public int hashCode() {
        return Objects.hash(isbn, readerId, pickDate);
    }

    @Override
    public String toString() {
        final String RESET = "\u001B[0m";
        final String GREEN = "\u001B[32m";

        return "PickRecord:\n" +
                GREEN + "  isbn        = " + RESET + getIsbn()  +
                GREEN + "  readerId    = " + RESET + getReaderId() +
                GREEN + "  pickDate    = " + RESET + getPickDate()  +
                GREEN + "  returnDate  = " + RESET + getReturnDate()  +
                GREEN + "  delayDays   = " + RESET + getDelayDays();
    }
}
