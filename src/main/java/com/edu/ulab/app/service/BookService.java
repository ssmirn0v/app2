package com.edu.ulab.app.service;


import com.edu.ulab.app.dto.BookDto;

import java.util.List;

public interface BookService {
    BookDto createBook(BookDto bookDto);

    BookDto updateBook(BookDto bookDto);

    BookDto getBookById(Long id);

    void deleteBookById(Long id);

    List<Long> getBooksIdsByUserId(Long userId);

    void deleteBooksByUserId(Long userId);

    List<BookDto> getBooksByUserId(Long userId);
}
