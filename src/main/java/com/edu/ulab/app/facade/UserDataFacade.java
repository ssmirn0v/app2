package com.edu.ulab.app.facade;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.BookMapper;
import com.edu.ulab.app.mapper.UserMapper;

import com.edu.ulab.app.service.BookService;
import com.edu.ulab.app.service.UserService;
import com.edu.ulab.app.service.impl.BookServiceImplTemplate;
import com.edu.ulab.app.service.impl.UserServiceImplTemplate;
import com.edu.ulab.app.web.request.BookRequest;
import com.edu.ulab.app.web.request.UserBookRequest;
import com.edu.ulab.app.web.response.BookResponse;
import com.edu.ulab.app.web.response.UserBookResponse;
import com.edu.ulab.app.web.response.UserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@Slf4j
@Component
public class UserDataFacade {
    private final UserService userService;
    private final BookService bookService;
    private final UserMapper userMapper;
    private final BookMapper bookMapper;

    public UserDataFacade(@Qualifier("UserServiceImpl") UserService userService,
                          @Qualifier("BookServiceImpl") BookService bookService,
                          UserMapper userMapper,
                          BookMapper bookMapper) {
        this.userService = userService;
        this.bookService = bookService;
        this.userMapper = userMapper;
        this.bookMapper = bookMapper;
    }

    @Transactional
    public UserBookResponse createUserWithBooks(UserBookRequest userBookRequest) {
        log.info("Got user book create request: {}", userBookRequest);
        UserDto userDto = userMapper.userRequestToUserDto(userBookRequest.getUserRequest());
        log.info("Mapped user request: {}", userDto);

        UserDto createdUser = userService.createUser(userDto);
        log.info("Created user: {}", createdUser);

        List<Long> bookIdList = createBooks(createdUser.getId(), userBookRequest.getBookRequests());
        log.info("Collected book ids: {}", bookIdList);

        return UserBookResponse.builder()
                .userId(createdUser.getId())
                .booksIdList(bookIdList)
                .build();
    }

    @Transactional
    public UserBookResponse updateUserWithBooks(Long userId, UserBookRequest userBookRequest) {
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User with id: " + userId + " was not found");
        }

        UserDto userDto = userMapper.userRequestToUserDto(userBookRequest.getUserRequest());
        userDto.setId(userId);
        log.info("Update: {}", userDto);
        UserDto updatedUser = userService.updateUser(userDto);
        log.info("Updated user: {}", updatedUser);

        List<Long> booksIds = createBooks(userId, userBookRequest.getBookRequests());
        log.info("Created books ids: {}", booksIds);

        return UserBookResponse.builder()
                .userId(userId)
                .booksIdList(booksIds)
                .build();
    }

    @Transactional
    public UserBookResponse getUserWithBooks(Long userId) {
        log.info("Got request for retrieving user id:{} and his books", userId);
        if (!userService.existsById(userId)) {
            throw new NotFoundException("User with id: " + userId + " was not found");
        }
        List<Long> bookIds = bookService.getBooksIdsByUserId(userId);
        log.info("Retrieved books ids: {} for user id:{}", bookIds, userId);
        return UserBookResponse.builder()
                .userId(userId)
                .booksIdList(bookIds)
                .build();
    }

    public UserResponse getUser(Long userId) {
        UserDto userDto = userService.getUserById(userId);
        log.info("Retrieved user: {}", userDto);
        return userMapper.userDtoToUserResponse(userDto);
    }

    public List<BookResponse> getUserBooks(Long userId) {
        List<BookDto> userBooks = bookService.getBooksByUserId(userId);
        log.info("Retrieved books with userId:{} :{}", userId, userBooks);
        return Stream.ofNullable(userBooks)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(bookMapper::bookDtoToBookResponse)
                .toList();
    }

    @Transactional
    public void deleteUserWithBooks(Long userId) {
        bookService.deleteBooksByUserId(userId);
        userService.deleteUserById(userId);
        log.info("Deleted user with id:{} and his books", userId);
    }

    private List<Long> createBooks(Long userId, List<BookRequest> bookRequests) {
        return Stream.ofNullable(bookRequests)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(bookMapper::bookRequestToBookDto)
                .peek(bookDto -> bookDto.setUserId(userId))
                .peek(bookDto -> log.info("Mapped book: {}", bookDto))
                .map(bookService::createBook)
                .peek(bookDto -> log.info("Created book: {}", bookDto))
                .map(BookDto::getId)
                .toList();
    }
}
