package telran.library.models;

import telran.library.entites.Book;
import telran.library.entites.Reader;
import telran.library.entites.enums.BooksReturnCode;
import telran.library.entites.enums.BooksReturnCode.*;

import java.util.HashMap;
import java.util.Map;

import static telran.library.entites.enums.BooksReturnCode.*;

public class LibraryMaps extends AbstractLibrary{

    Map<Long, Book> books = new HashMap<>();
    Map<Integer, Reader> readers = new HashMap<>();


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
        if(book == null)
            return NO_BOOK_ITEM;
        book.setAmount(book.getAmount() + amount);
        return OK ;

    }

    @Override
    public Reader getReader(int readerId) {
        return readers.get(readerId);
    }

    @Override
    public Book getBookItem(long isbn) {
        return books.get(isbn);
    }
}
