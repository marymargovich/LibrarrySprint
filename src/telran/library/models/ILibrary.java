package telran.library.models;


import java.io.Serializable;


import telran.library.entites.Book;
import telran.library.entites.Reader;
import telran.library.entites.enums.BooksReturnCode;


public interface ILibrary extends Serializable {
    //sprint 1

    BooksReturnCode addBookItem(Book book );
    BooksReturnCode addReader(Reader reader );
    BooksReturnCode addBookExemplars(long isbn, int amount );
    Reader getReader(int readerId);
    Book getBookItem(long isbn);

}


