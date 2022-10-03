package com.edu.ulab.app.repository;

import com.edu.ulab.app.config.SystemJpaTest;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;


import java.util.List;
import java.util.function.Consumer;

import static com.vladmihalcea.sql.SQLStatementCountValidator.*;
import static org.assertj.core.api.Assertions.*;

/**
 * Тесты репозитория {@link BookRepository}.
 */
@SystemJpaTest
public class BookRepositoryTest {
    @Autowired
    BookRepository bookRepository;
    @Autowired
    UserRepository userRepository;



    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();
    }


    @DisplayName("Сохранить книгу и автора." +
            " число insert должно равняться 2")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void insertPersonThenHisBook_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader1");
        person.setFullName("Test Test");
        person.setRating(2);

        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());

        //When
        Book result = bookRepository.save(book);

        bookRepository.flush();

        //Then
        assertThat(result.getPageCount()).isEqualTo(1000);
        assertThat(result.getTitle()).isEqualTo("test");
        assertSelectCount(0);
        assertInsertCount(2);
        assertUpdateCount(0);
        assertDeleteCount(0);


    }

    // update
    @DisplayName("Обновление полей книги. Число Select должно быть равно 2." +
            " Число Insert должно быть равно 2." +
            " Число Update должно быть равно 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void updateBook_thenAssertDmlCount() {
        // Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader1");
        person.setFullName("Test Test");
        person.setRating(2);

        Person savedPerson = userRepository.saveAndFlush(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());

        Book bookForUpdate = bookRepository.save(book);

        bookForUpdate.setPageCount(1002);
        bookForUpdate.setAuthor("Updated Author");

        //When
        Book result = bookRepository.save(bookForUpdate);

        bookRepository.flush();

        //Then
        assertThat(result.getPageCount()).isEqualTo(1002);
        assertThat(result.getAuthor()).isEqualTo("Updated Author");
        assertSelectCount(2);
        assertInsertCount(2);
        assertUpdateCount(1);
        assertDeleteCount(0);


    }

    // get
    @DisplayName("Получение книги из БД. Число Select должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void getBook_thenAssertDmlCount() {
        // Given
        // Книга уже сохранена в бд с помощью файла 3_insert_book_data.sql
        Book savedBook = new Book();
        savedBook.setId(2002L);
        savedBook.setAuthor("author");
        savedBook.setTitle("default book");
        savedBook.setPageCount(5500);
        savedBook.setUserId(1001L);

        Long savedBookId = savedBook.getId();

        //When
        Book retrievedBook = bookRepository.findById(savedBookId).get();

        //Then
        assertThat(savedBook).isEqualTo(retrievedBook);
        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    // get all
    @DisplayName("Получение всех книг из БД. Число Select должно быть равно 1. " +
            "Число Insert должны быть равно 2.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void getAllBooks_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader1");
        person.setFullName("Test Test");
        person.setRating(2);

        Person savedPerson = userRepository.save(person);

        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);
        book.setUserId(savedPerson.getId());

        Book savedBook = bookRepository.save(book);

        //When
        List<Book> bookList =  bookRepository.findAll();

        //Then
        assertThat(bookList.size()).isEqualTo(3);
        assertSelectCount(1);
        assertInsertCount(2);
        assertUpdateCount(0);
        assertDeleteCount(0);

    }

    // delete
    @DisplayName("Удаление книги из БД. Число Select должно быть равно 2. " +
            "Число Delete должны быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void deleteBook_thenAssertDmlCount() {
        // Given
        // 2 книги сохранены с помощью 3_insert_book_data.sql
        Long bookIdForDelete = 2002L;

        //When
        bookRepository.deleteById(bookIdForDelete);
        long count = bookRepository.count();

        //Then
        assertThat(count).isEqualTo(1);
        assertSelectCount(2);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(1);

    }

    // * failed
    @DisplayName("Сохранение книги с Null полем. Должно выброситься исключение.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void saveBookWithNullField_thenAssertError() {
        //Given
        Book book = new Book();
        book.setAuthor("Test Author");
        book.setTitle("test");
        book.setPageCount(1000);

        //When
        Throwable thrown = catchThrowable(() -> {
            Book savedBook = bookRepository.save(book);
        });

        //Then
        assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);

    }


}
