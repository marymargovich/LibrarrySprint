package telran.library.models;


import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;


import telran.library.entites.*;
import telran.library.entites.enums.BooksReturnCode;


public interface ILibrary extends Serializable {
    //sprint 1

    BooksReturnCode addBookItem(Book book );
    BooksReturnCode addReader(Reader reader );
    BooksReturnCode addBookExemplars(long isbn, int amount );
    Reader getReader(int readerId);
    Book getBookItem(long isbn);


    // Sprint2 (добавилось)
    BooksReturnCode pickBook(long isbn, int readerId, LocalDate pickDate);
    List<Book> getBooksPickedByReader(int readerId);
    List<Reader> getReadersPickedBook(long isbn);
    List<Book> getBooksAuthor(String authorName);
    List<PickRecord> getPickedRecordsAtDates(LocalDate from, LocalDate to);

    //Sprint3 (добавилось)
    RemovedBookData removeBook(long isbn);
    List<RemovedBookData> removeAuthor(String author);
    RemovedBookData returnBook(long isbn, int readerId, LocalDate returnDate);

    //Sprint 4 (добавилось)
    List<ReaderDelay> getReadersDelayingBooks(LocalDate currentDate);
    List<ReaderDelay> getReadersDelayedBooks();
    List<Book>        getMostPopularBooks(LocalDate fromDate, LocalDate toDate, int fromAge, int toAge);
    List<String>      getMostPopularAuthors();
    List<Reader>      getMostActiveReaders(LocalDate fromDate, LocalDate toDate);


}


