package telran.library.entites;

import java.util.List;
import java.util.Objects;

public class RemovedBookData {
    Book book;
    List <PickRecord> records;


    public RemovedBookData() {
    }

    public RemovedBookData(Book book, List<PickRecord> records) {
        this.book = book;
        this.records = records;
    }

    public Book getBook() {
        return book;
    }

    public List<PickRecord> getRecords() {
        return records;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        RemovedBookData that = (RemovedBookData) o;
        return Objects.equals(book, that.book) && Objects.equals(records, that.records);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, records);
    }

    @Override
    public String toString() {
            String YELLOW = "\u001B[33m";
            String WHITE = "\u001B[37m";
            String RESET = "\u001B[0m";

            return YELLOW + "RemovedBookData " + RESET +
                    YELLOW + "book=" + RESET + WHITE + getBook() + RESET + " " +
                    YELLOW + "records=" + RESET + WHITE + getRecords() + RESET;
        }
}
