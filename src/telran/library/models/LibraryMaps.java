package telran.library.models;

import telran.library.entites.Book;
import telran.library.entites.PickRecord;
import telran.library.entites.Reader;
import telran.library.entites.enums.BooksReturnCode;
import telran.library.entites.enums.BooksReturnCode.*;
import telran.library.utils.Persistable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.util.*;

import static telran.library.entites.enums.BooksReturnCode.*;

public class LibraryMaps extends AbstractLibrary implements ILibrary, Persistable {

    Map<Long, Book> books = new HashMap<>();
    Map<Integer, Reader> readers = new HashMap<>();


    // Sprint2 — новые индексы выдач (private)
    Map<Integer, List<PickRecord>>  readersRecords; // readerId → выдачи
    Map<Long, List<PickRecord>>     booksRecords;   // isbn → выдачи
    Map<LocalDate, List<PickRecord>> records;       // дата выдачи → выдачи




    @Override
    public BooksReturnCode addBookItem(Book book) {
        if (book.getPickPeriod() < minPickPeriod)
            return PICK_PERIOD_LESS_MIN;
        if (book.getPickPeriod() > maxPickPeriod)
            return PICK_PERIOD_GREATER_MAX;
        BooksReturnCode res = books
                .putIfAbsent(book.getIsbn(), book) ==
                null ? OK : BOOK_ITEM_EXISTS;


        return res;

    }

    @Override
    public BooksReturnCode addReader(Reader reader) {
        if (readers.putIfAbsent(reader.getReaderId(), reader) == null) {
            return OK;
        }
        return READER_EXISTS;
    }

    @Override
    public BooksReturnCode addBookExemplars(long isbn, int amount) {
        Book book = books.get(isbn);
        if (book == null)
            return NO_BOOK_ITEM;
        book.setAmount(book.getAmount() + amount);
        return OK;

    }

    @Override
    public Reader getReader(int readerId) {
        return readers.get(readerId);
    }

    @Override
    public Book getBookItem(long isbn) {
        return books.get(isbn);
    }

    @Override
    public BooksReturnCode pickBook(long isbn,
                                    int readerId,
                                    LocalDate pickDate) {
        Book book = getBookItem(isbn);
        if(book == null) return NO_BOOK_ITEM;
        if(book.isFlRemove()) return BOOK_REMOVED;
        if (book.getAmountInUse() >= book.getAmount()) return NO_BOOK_EXEMPLARS;
        if(!readers.containsKey(readerId)) return NO_READER;

        PickRecord record = new PickRecord(pickDate, isbn, readerId);
        addToReadersRecords(record);
        addToBooksRecords(record);
        addToRecords(record);
        return OK;
    }

    private void addToRecords(PickRecord record) {
        records.computeIfAbsent(record.getPickDate(), r-> new ArrayList<>()).add(record);
    }

    private void addToBooksRecords(PickRecord record) {
        booksRecords.computeIfAbsent(record.getIsbn(), r-> new ArrayList<>()).add(record);

    }

    private void addToReadersRecords(PickRecord record) {
        readersRecords.computeIfAbsent(record.getReaderId(), r-> new ArrayList<>()).add(record);

    }

    @Override
    public List<Book> getBooksPickedByReader(int readerId) {
        List<PickRecord> listRecords = readersRecords.getOrDefault(readerId, new ArrayList<>());

        return listRecords.stream()
                .map(r-> getBookItem(r.getIsbn()))
                .distinct()
                .toList();
    }

    @Override
    public List<Reader> getReadersPickedBook(long isbn) {
        List<PickRecord> listRecords = booksRecords.getOrDefault(isbn, new ArrayList<>());

        return listRecords.stream()
                .map(r-> getReader(r.getReaderId()))
                .distinct()
                .toList();
    }

    @Override
    public List<Book> getBooksAuthor(String authorName) {
        return books.values().stream()
                .filter(book -> book.getAuthor().equals(authorName))
                .toList();
    }

    @Override
    public List<PickRecord> getPickedRecordsAtDates(LocalDate from, LocalDate to) {
        Collection <List<PickRecord>> res =
                ((TreeMap<LocalDate, List<PickRecord>>)records).subMap(from, to).values();
        return res.stream()
                .flatMap(i-> i.stream())
                .toList();
    }

    @Override
    public void save(String fileName) {
        try (ObjectOutputStream outputStream =
                     new ObjectOutputStream(
                             new FileOutputStream(fileName))) {
            outputStream.writeObject(this);
        } catch (IOException e) {
            System.out.println("Error in method save " + e.getMessage());
        }
    }


}
