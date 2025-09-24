package telran.library.models;

import telran.library.entites.*;
import telran.library.entites.enums.BooksReturnCode;
import telran.library.utils.Persistable;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static telran.library.entites.enums.BooksReturnCode.*;

public class LibraryMaps extends AbstractLibrary implements ILibrary, Persistable {

    Map<Long, Book> books = new HashMap<>();
    Map<Integer, Reader> readers = new HashMap<>();


    // Sprint2 — новые индексы выдач (private)
    Map<Integer, List<PickRecord>> readersRecords = new HashMap<>(); // readerId → выдачи
    Map<Long, List<PickRecord>> booksRecords = new HashMap<>();   // isbn → выдачи
    NavigableMap<LocalDate, List<PickRecord>> records = new TreeMap<>();       // дата выдачи → выдачи

    Map<String, List<Book>> authorBooks = new HashMap<>();


    @Override
    public BooksReturnCode addBookItem(Book book) {
        if (book.getPickPeriod() < minPickPeriod)
            return PICK_PERIOD_LESS_MIN;
        if (book.getPickPeriod() > maxPickPeriod)
            return PICK_PERIOD_GREATER_MAX;
//       BooksReturnCode res = books
//                .putIfAbsent(book.getIsbn(), book) ==
//                null ? OK : BOOK_ITEM_EXISTS;
        if (books.putIfAbsent(book.getIsbn(), book) != null)
            return BOOK_ITEM_EXISTS;
        addAuthorBooks(book);
        return OK;

    }

    private void addAuthorBooks(Book book) {
        String key = book.getAuthor();
        List<Book> list = authorBooks.computeIfAbsent(key, k -> new ArrayList<Book>());
        boolean exists = list.stream().anyMatch(b -> b.getIsbn() == book.getIsbn());
        if (exists)
            list.add(book);

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
        if (book == null) return NO_BOOK_ITEM;
        if (book.isFlRemove()) return BOOK_REMOVED;
        if (book.getAmountInUse() >= book.getAmount()) return NO_BOOK_EXEMPLARS;
        if (!readers.containsKey(readerId)) return NO_READER;


        List<PickRecord> pickRecords = readersRecords.get(readerId);
        if (pickRecords != null && pickRecords.stream()
                .anyMatch(r -> r.getIsbn() == isbn && r.getReturnDate() == null))
            return READER_READS_IT;

        PickRecord record = new PickRecord(pickDate, isbn, readerId);
        addToReadersRecords(record);
        addToBooksRecords(record);
        addToRecords(record);
        book.setAmountInUse(book.getAmountInUse() + 1);
        addToMap(booksRecords, record.getIsbn(), record);
        addToMap(readersRecords, record.getReaderId(), record);
        addToMap(records, record.getPickDate(), record);
        return OK;
    }

    private <K, V> void addToMap(Map<K, List<V>> map, K key, V value) {
        map.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
    }

    private void addToRecords(PickRecord record) {
        records.computeIfAbsent(record.getPickDate(), r -> new ArrayList<>()).add(record);
    }

    private void addToBooksRecords(PickRecord record) {
        booksRecords.computeIfAbsent(record.getIsbn(), r -> new ArrayList<>()).add(record);

    }

    private void addToReadersRecords(PickRecord record) {
        readersRecords.computeIfAbsent(record.getReaderId(), r -> new ArrayList<>()).add(record);

    }

    @Override
    public List<Book> getBooksPickedByReader(int readerId) {
        List<PickRecord> listRecords = readersRecords.getOrDefault(readerId, new ArrayList<>());

        return listRecords.stream()
                .map(r -> getBookItem(r.getIsbn()))
                .distinct()
                .toList();
    }

    @Override
    public List<Reader> getReadersPickedBook(long isbn) {
        List<PickRecord> listRecords = booksRecords.getOrDefault(isbn, new ArrayList<>());

        return listRecords.stream()
                .map(r -> getReader(r.getReaderId()))
                .distinct()
                .toList();
    }


    @Override
    public List<Book> getBooksAuthor(String authorName) {
        if (authorName == null || authorName.isBlank())
            return new ArrayList<>();
        List<Book> list = authorBooks.getOrDefault(authorName, new ArrayList<>());

        return list.stream()
                .filter(book -> book.getAmount() - book.getAmountInUse() > 0)
                .toList();
    }

    @Override
    public List<PickRecord> getPickedRecordsAtDates(LocalDate from, LocalDate to) {
        Collection<List<PickRecord>> res =
                records.subMap(from, to).values();
        return res == null ? new ArrayList<>()
                : res.stream()
                .flatMap(Collection::stream)
                .toList();
    }

    @Override
    public RemovedBookData removeBook(long isbn) {
        Book book = getBookItem(isbn);
        if (book == null || book.isFlRemove())
            return null;
        boolean inUse = booksRecords.getOrDefault(isbn, new ArrayList<>())
                .stream()
                .anyMatch(r -> r.getReturnDate() == null);

        return inUse ? new RemovedBookData(book, null)
                : actualBookRemove(book);
    }

    private RemovedBookData actualBookRemove(Book book) {
        Long isbn = book.getIsbn();
        List<PickRecord> removedRecords = booksRecords.get(isbn);
        removeFromAuthorRecords(removedRecords);
        removeFromRecords(removedRecords);
        books.remove(isbn);
        booksRecords.remove(isbn);
        List<Book> authorList = authorBooks.getOrDefault(book.getAuthor(), new ArrayList<>());
        authorList.removeIf(b -> b.getIsbn() == isbn);
        return new RemovedBookData(book, removedRecords);

    }

    private void removeFromRecords(List<PickRecord> removedRecords) {
        if (removedRecords == null) return;
        removedRecords.forEach(r -> {
            List<PickRecord> daylist = records.get(r.getPickDate());
            if (daylist != null) daylist.remove(r);

        });

    }

    private void removeFromAuthorRecords(List<PickRecord> removeRecords) {
        if (removeRecords == null) return;
        removeRecords.forEach(r -> {
            List<PickRecord> readerList = readersRecords.get(r.getReaderId());
            if (readerList != null) readerList.remove(r);

        });
    }

    @Override
    public List<RemovedBookData> removeAuthor(String author) {
        List<Book> bookAuthor = authorBooks.getOrDefault(author, new ArrayList<>());
        return bookAuthor.stream()
                .map(b -> removeBook(b.getIsbn()))
                .filter(Objects::nonNull)
                .toList();
    }


    @Override
    public RemovedBookData returnBook(long isbn, int readerId, LocalDate returnDate) {
        List<PickRecord> bookRecord = booksRecords.getOrDefault(isbn, new ArrayList<>());
        PickRecord record = bookRecord.stream()
                .filter(r -> r.getReaderId() == readerId && r.getReturnDate() == null)
                .findFirst()
                .orElse(null);
        if (bookRecord == null)
            return new RemovedBookData(null, null);

        if (record != null) {
            record.setReturnDate(returnDate);
        }

        Book book = getBookItem(isbn);
        if (book != null)
            book.setAmountInUse(book.getAmountInUse() - 1);

        for (PickRecord r : bookRecord) {
            if (r.getReturnDate() == null) {
                return new RemovedBookData(book, List.of(record));
            }
        }
        if (book != null && book.isFlRemove()) {
            return actualBookRemove(book);
        }
        return new RemovedBookData(book, List.of(record));
    }

    @Override
    public List<ReaderDelay> getReadersDelayingBooks(LocalDate currentDate) {
        List <ReaderDelay> recordsDelay= new ArrayList<>();

        readersRecords.forEach((readerId, records)-> {
            Reader reader = getReader(readerId);

            records.stream()
                    .filter(record -> getDelayDays(record, currentDate) > 0)
                    .forEach(record -> {
                        Book book = getBookItem(record.getIsbn());
                        if (book != null) {
                            int delay = getDelayDays(record, currentDate);
                            recordsDelay.add(new ReaderDelay(reader, delay));
                        }
                    });
        });
        return  recordsDelay;

    }

    @Override
    public List<ReaderDelay> getReadersDelayedBooks() {
        List<ReaderDelay> recordsDelay = new ArrayList<>();

        readersRecords.forEach((readerId, records) -> {
            Reader reader = getReader(readerId);

            records.stream()
                    .filter(record -> getDelayDays(record,
                            record.getReturnDate() != null ? record.getReturnDate() : LocalDate.now()) > 0)
                    .forEach(record -> {
                        Book book = getBookItem(record.getIsbn());
                        if (book != null) {
                            int delay = getDelayDays(record,
                                    record.getReturnDate() != null ? record.getReturnDate() : LocalDate.now());
                            recordsDelay.add(new ReaderDelay(reader, delay));
                        }
                    });
        });

        return recordsDelay;

    }

    private int getDelayDays(PickRecord record, LocalDate currentDate) {
        LocalDate endDate = record.getReturnDate() != null ? record.getReturnDate() : currentDate;
        long actualDays = ChronoUnit.DAYS.between(record.getPickDate(), endDate);
        int delta = (int) (actualDays - record.getDelayDays()); // <-- здесь число дней, а не LocalDate
        return Math.max(delta, 0);

    }

    @Override
    public List<Book> getMostPopularBooks(LocalDate fromDate, LocalDate toDate, int fromAge, int toAge) {
        List < PickRecord> recordsFromTo = getPickedRecordsAtDates(fromDate, toDate);
        if( recordsFromTo.isEmpty()) return new ArrayList<>();


        Map <Long, Long> mapTemp = recordsFromTo.stream()
                .collect(Collectors.groupingBy(PickRecord::getIsbn, Collectors.counting()));

        if( mapTemp.isEmpty()) return  new ArrayList<>();


        long maxValue = Collections.max(mapTemp.values());

        List<Book> res = new ArrayList<>();
        mapTemp.forEach((isbn, count)-> {
            if (count == maxValue){
                Book book = getBookItem(isbn);
                if( book!=null) res.add(book);
            }

        });

        return res;
    }

    @Override
    public List<String> getMostPopularAuthors() {
        List< PickRecord> allRecords =
                readersRecords.values().stream()
                        .flatMap(List::stream)
                        .toList();
        if( allRecords.isEmpty()) return new ArrayList<>();

        //выдачи по авторам
        Map <String , Long> authorCount = allRecords.stream()
                .map( r->{
                    Book book = getBookItem(r.getIsbn());
                    if (book == null || book.getAuthor() == null)
                        return null;

                    //нормализация имен?
                    String author = book.getAuthor();
                    author = author.trim().toLowerCase();
                    int lastSpace = author.lastIndexOf(" ");
                    if (lastSpace >= 0) {
                        author = author.substring(lastSpace + 1);
                    }
                    return author;

                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(a-> a, Collectors.counting()));

        if( authorCount.isEmpty()) return new ArrayList<>();

        //максимум
        long maxValue = Collections.max(authorCount.values());

        //авторы с максимумом
        List<String > res = new ArrayList<>();
        authorCount.forEach((author, count)->{
            if(count == maxValue) res.add(author);
        });

        return res;
    }

    @Override
    public List<Reader> getMostActiveReaders(LocalDate fromDate, LocalDate toDate) {
        long maxCount = 0;
        for (List<PickRecord> records : readersRecords.values()) {
            long countInRange = records.stream()
                    .filter(r -> !r.getPickDate().isBefore(fromDate)
                            && !r.getPickDate().isAfter(toDate))
                    .count();
            if (countInRange > maxCount)
                maxCount = countInRange;
        }

        long max = maxCount;
        List<Reader> res = new ArrayList<>();
        readersRecords.forEach((readerId, record)->{
            long countInRange = record.stream()
                        .filter(r -> !r.getPickDate().isBefore(fromDate)
                        && !r.getPickDate().isAfter(toDate))
                        .count();
            if( countInRange == max && max>0 ){
                res.add(readers.get(readerId));
                }
            });
        return res;
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
