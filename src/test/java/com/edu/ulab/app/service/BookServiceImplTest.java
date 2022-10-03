package com.edu.ulab.app.service;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.repository.BookRepository;
import com.edu.ulab.app.service.impl.BookServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Тестирование функционала {@link BookServiceImpl}.
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing book functionality.")
public class BookServiceImplTest {
    @InjectMocks
    BookServiceImpl bookService;

    @Mock
    BookRepository bookRepository;

    @Mock
    BookMapper bookMapper;

    @Test
    @DisplayName("Создание книги. Должно пройти успешно.")
    void saveBook_Test() {
        //given
        Person person  = new Person();
        person.setId(1L);

        BookDto bookDto = new BookDto();
        bookDto.setUserId(1L);
        bookDto.setAuthor("test author");
        bookDto.setTitle("test title");
        bookDto.setPageCount(1000);

        BookDto result = new BookDto();
        result.setId(1L);
        result.setUserId(1L);
        result.setAuthor("test author");
        result.setTitle("test title");
        result.setPageCount(1000);

        Book book = new Book();
        book.setPageCount(1000);
        book.setTitle("test title");
        book.setAuthor("test author");
        book.setUserId(person.getId());

        Book savedBook = new Book();
        savedBook.setId(1L);
        savedBook.setPageCount(1000);
        savedBook.setTitle("test title");
        savedBook.setAuthor("test author");
        savedBook.setUserId(person.getId());

        //when

        when(bookMapper.bookDtoToBook(bookDto)).thenReturn(book);
        when(bookRepository.save(book)).thenReturn(savedBook);
        when(bookMapper.bookToBookDto(savedBook)).thenReturn(result);


        //then
        BookDto bookDtoResult = bookService.createBook(bookDto);
        assertEquals(1L, bookDtoResult.getId());
    }


    // update
    @Test
    @DisplayName("Обновление полей книги")
    void updateBook_Test() {
        //Given
        Long id = 2L;

        BookDto bookDtoUpdate = new BookDto();
        bookDtoUpdate.setId(2L);
        bookDtoUpdate.setUserId(1L);
        bookDtoUpdate.setAuthor("Updated author");
        bookDtoUpdate.setTitle("Updated title");
        bookDtoUpdate.setPageCount(1000);

        Book bookUpdate = new Book();
        bookUpdate.setId(2L);
        bookUpdate.setUserId(1L);
        bookUpdate.setAuthor("Updated author");
        bookUpdate.setTitle("Updated title");
        bookUpdate.setPageCount(1000);

        Book bookFromDb = new Book();
        bookFromDb.setId(2L);
        bookFromDb.setUserId(1L);
        bookFromDb.setAuthor("test author");
        bookFromDb.setTitle("test title");
        bookFromDb.setPageCount(1000);

        Book updatedBook = new Book();
        updatedBook.setId(2L);
        updatedBook.setUserId(1L);
        updatedBook.setAuthor("Updated author");
        updatedBook.setTitle("Updated title");
        updatedBook.setPageCount(1000);

        Book savedUpdatedBook = new Book();
        savedUpdatedBook.setId(2L);
        savedUpdatedBook.setUserId(1L);
        savedUpdatedBook.setAuthor("Updated author");
        savedUpdatedBook.setTitle("Updated title");
        savedUpdatedBook.setPageCount(1000);

        BookDto updatedBookDto = new BookDto();
        updatedBookDto.setId(2L);
        updatedBookDto.setUserId(1L);
        updatedBookDto.setAuthor("Updated author");
        updatedBookDto.setTitle("Updated title");
        updatedBookDto.setPageCount(1000);

        //When
        when(bookMapper.bookDtoToBook(bookDtoUpdate)).thenReturn(bookUpdate);
        when(bookRepository.findByIdForUpdate(id)).thenReturn(Optional.of(bookFromDb));
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Book) args[1]).setAuthor("Updated author");
            ((Book) args[1]).setTitle("Updated title");
            return null;
        }).when(bookMapper).updateBook(bookUpdate, bookFromDb);
        when(bookRepository.save(bookFromDb)).thenReturn(savedUpdatedBook);
        when(bookMapper.bookToBookDto(savedUpdatedBook)).thenReturn(updatedBookDto);

        //Then
        BookDto result = bookService.updateBook(bookDtoUpdate);
        assertEquals("Updated author", result.getAuthor());
        assertEquals("Updated title", result.getTitle());


    }

    // get
    @Test
    @DisplayName("Получение книги.")
    void getBookById_Test() {
        //Given
        Long id = 101L;

        Book book = new Book();
        book.setId(101L);
        book.setUserId(202L);
        book.setAuthor("Hugh Jass");
        book.setTitle("Cool book");
        book.setPageCount(444);

        BookDto bookDto = new BookDto();
        bookDto.setId(101L);
        bookDto.setUserId(202L);
        bookDto.setAuthor("Hugh Jass");
        bookDto.setTitle("Cool book");
        bookDto.setPageCount(444);


        //When
        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookMapper.bookToBookDto(book)).thenReturn(bookDto);

        //Then
        BookDto bookDtoResult = bookService.getBookById(id);
        assertEquals(id, bookDtoResult.getId());
        assertEquals("Hugh Jass", bookDtoResult.getAuthor());
    }

    // delete
    @Test
    @DisplayName("Удаление книги.")
    void deleteBook_Test() {
        //Given
        Long id = 101L;

        //When
        doNothing().when(bookRepository).deleteById(id);

        //Then
        bookService.deleteBookById(id);

    }


}
