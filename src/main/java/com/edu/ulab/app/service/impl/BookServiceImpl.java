package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.repository.BookRepository;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@Qualifier("BookServiceImpl")
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    private final BookMapper bookMapper;

    public BookServiceImpl(BookRepository bookRepository,
                           BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        Book book = bookMapper.bookDtoToBook(bookDto);
        log.info("Mapped book: {}", book);
        Book savedBook = bookRepository.save(book);
        log.info("Saved book: {}", savedBook);
        return bookMapper.bookToBookDto(savedBook);
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        Long id = bookDto.getId();
        Book update = bookMapper.bookDtoToBook(bookDto);
        log.info("Mapped book: {}", update);
        Book book = bookRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("Book with id: " + id + " was not found"));
        bookMapper.updateBook(update, book);
        Book updatedBook = bookRepository.save(book);
        log.info("Updated book: {}", updatedBook);
        return bookMapper.bookToBookDto(updatedBook);
    }

    @Override
    public BookDto getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Book with id: " + id + " was not found"));

        log.info("Retrieved book: {}", book);
        return bookMapper.bookToBookDto(book);
    }

    @Override
    public void deleteBookById(Long id) {
        bookRepository.deleteById(id);
        log.info("Book id:{} deleted", id);
    }

    @Override
    public List<Long> getBooksIdsByUserId(Long userId) {
        return bookRepository.findBooksByUserId(userId).stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(Book::getId)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDto> getBooksByUserId(Long userId) {
        return bookRepository.findBooksByUserId(userId).stream()
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(bookMapper::bookToBookDto)
                .toList();
    }

    @Override
    public void deleteBooksByUserId(Long userId) {
        bookRepository.deleteBooksByUserId(userId);
        log.info("Deleted books with user id:{}", userId);
    }
}
