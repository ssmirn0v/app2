package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.service.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class BookServiceImplTemplate implements BookService {

    private final JdbcTemplate jdbcTemplate;

    private final BookMapper bookMapper;

    public BookServiceImplTemplate(JdbcTemplate jdbcTemplate, BookMapper bookMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.bookMapper = bookMapper;
    }

    @Override
    public BookDto createBook(BookDto bookDto) {
        final String INSERT_SQL = "INSERT INTO BOOK(TITLE, AUTHOR, PAGE_COUNT, USER_ID) VALUES (?,?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps =
                            connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                    ps.setString(1, bookDto.getTitle());
                    ps.setString(2, bookDto.getAuthor());
                    ps.setLong(3, bookDto.getPageCount());
                    ps.setLong(4, bookDto.getUserId());
                    return ps;
                },
                keyHolder);

        bookDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return bookDto;
    }

    @Override
    public BookDto updateBook(BookDto bookDto) {
        BookDto bookForUpdate = getBookById(bookDto.getId());
        bookMapper.updateBookDto(bookDto, bookForUpdate);
        log.info("Updated book dto: {}", bookForUpdate);

        final String UPDATE_QUERY = "UPDATE BOOK SET title=?, author=?, title=?, page_count=?, user_id=? WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY);
            ps.setString(1, bookForUpdate.getTitle());
            ps.setString(2, bookForUpdate.getAuthor());
            ps.setLong(3, bookForUpdate.getPageCount());
            ps.setLong(4, bookForUpdate.getUserId());
            ps.setLong(5, bookForUpdate.getId());

            return ps;
        });
        return bookForUpdate;
    }

    @Override
    public BookDto getBookById(Long id) {
        final String SELECT_QUERY = "SELECT * FROM BOOK WHERE id=?";
        BookDto retrievedBook = jdbcTemplate.queryForObject(SELECT_QUERY, BookDto.class, id);
        if (retrievedBook == null) {
            throw new NotFoundException("Book with id: " + id + " was not found");
        }
        log.info("Retrieved book: {}",retrievedBook);
        return retrievedBook;
    }

    @Override
    public void deleteBookById(Long id) {
        final String DELETE_QUERY = "DELETE FROM BOOK WHERE id=?";
        jdbcTemplate.update(DELETE_QUERY, id);
        log.info("Deleted book id:{}", id);
    }

    @Override
    public List<Long> getBooksIdsByUserId(Long userId) {
        final String SELECT_BOOKS_IDS_QUERY = "SELECT id FROM BOOK WHERE user_id=?";
        List<Long> booksIds = jdbcTemplate.queryForList(SELECT_BOOKS_IDS_QUERY, Long.class, userId);
        return booksIds;
    }

    @Override
    public void deleteBooksByUserId(Long userId) {
        final String DELETE_BY_USER_ID_QUERY = "DELETE FROM BOOK WHERE user_id=?";
        jdbcTemplate.update(DELETE_BY_USER_ID_QUERY, userId);
        log.info("Deleted books by userId:{}", userId);
    }

    @Override
    public List<BookDto> getBooksByUserId(Long userId) {
        final String SELECT_BOOKS_BY_USERID_QUERY = "SELECT * FROM BOOK WHERE user_id=?";
        List<BookDto> books = jdbcTemplate.query(
                SELECT_BOOKS_BY_USERID_QUERY,
                (rs, rowNum) ->
                    new BookDto(
                        rs.getLong("id"),
                        rs.getLong("user_id"),
                        rs.getString("title"),
                        rs.getString("author"),
                        rs.getLong("page_count")
                    ),
                userId);
        return books;
    }
}
