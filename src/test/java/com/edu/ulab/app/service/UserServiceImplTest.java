package com.edu.ulab.app.service;

import com.edu.ulab.app.dto.BookDto;
import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.repository.UserRepository;
import com.edu.ulab.app.service.impl.UserServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Тестирование функционала {@link UserServiceImpl}.
 */
@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DisplayName("Testing user functionality.")
public class UserServiceImplTest {
    @InjectMocks
    UserServiceImpl userService;

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper userMapper;

    @Test
    @DisplayName("Создание пользователя. Должно пройти успешно.")
    void savePerson_Test() {
        //given

        UserDto userDto = new UserDto();
        userDto.setAge(11);
        userDto.setFullName("test name");
        userDto.setTitle("test title");

        Person person  = new Person();
        person.setFullName("test name");
        person.setAge(11);
        person.setTitle("test title");

        Person savedPerson  = new Person();
        savedPerson.setId(1L);
        savedPerson.setFullName("test name");
        savedPerson.setAge(11);
        savedPerson.setTitle("test title");

        UserDto result = new UserDto();
        result.setId(1L);
        result.setAge(11);
        result.setFullName("test name");
        result.setTitle("test title");


        //when

        when(userMapper.userDtoToPerson(userDto)).thenReturn(person);
        when(userRepository.save(person)).thenReturn(savedPerson);
        when(userMapper.personToUserDto(savedPerson)).thenReturn(result);


        //then

        UserDto userDtoResult = userService.createUser(userDto);
        assertEquals(1L, userDtoResult.getId());
    }

    // update
    @Test
    @DisplayName("Обновление полей юзера")
    void updateBook_Test() {
        //Given
        Long id = 2L;

        UserDto userDtoUpdate = new UserDto();
        userDtoUpdate.setId(2L);
        userDtoUpdate.setTitle("test title");
        userDtoUpdate.setFullName("Eric Shun");
        userDtoUpdate.setAge(12);

        Person userUpdate = new Person();
        userUpdate.setId(2L);
        userUpdate.setTitle("test title");
        userUpdate.setFullName("Eric Shun");
        userUpdate.setAge(12);

        Person userFromDb = new Person();
        userFromDb.setId(2L);
        userFromDb.setTitle("test title");
        userFromDb.setFullName("test Fullname");
        userFromDb.setAge(12);

        Person updatedUser = new Person();
        updatedUser.setId(2L);
        updatedUser.setTitle("test title");
        updatedUser.setFullName("Eric Shun");
        updatedUser.setAge(12);

        Person savedUpdatedUser = new Person();
        savedUpdatedUser.setId(2L);
        savedUpdatedUser.setTitle("test title");
        savedUpdatedUser.setFullName("Eric Shun");
        savedUpdatedUser.setAge(12);

        UserDto updatedUserDto = new UserDto();
        updatedUserDto.setId(2L);
        updatedUserDto.setTitle("test title");
        updatedUserDto.setFullName("Eric Shun");
        updatedUserDto.setAge(12);

        //When
        when(userMapper.userDtoToPerson(userDtoUpdate)).thenReturn(userUpdate);
        when(userRepository.findByIdForUpdate(id)).thenReturn(Optional.of(userFromDb));
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((Person) args[1]).setFullName("Eric Shun");
            return null;
        }).when(userMapper).updatePerson(userUpdate, userFromDb);
        when(userRepository.save(userFromDb)).thenReturn(savedUpdatedUser);
        when(userMapper.personToUserDto(savedUpdatedUser)).thenReturn(updatedUserDto);

        //Then
        UserDto result = userService.updateUser(userDtoUpdate);
        assertEquals("Eric Shun", result.getFullName());

    }


    // get
    @Test
    @DisplayName("Получение пользователя.")
    void getBookById_Test() {
        //Given
        Long id = 202L;

        Person user = new Person();
        user.setId(202L);
        user.setTitle("test title");
        user.setFullName("Eric Shun");
        user.setAge(12);

        UserDto userDto = new UserDto();
        userDto.setId(202L);
        userDto.setTitle("test title");
        userDto.setFullName("Eric Shun");
        userDto.setAge(12);


        //When
        when(userRepository.findById(id)).thenReturn(Optional.of(user));
        when(userMapper.personToUserDto(user)).thenReturn(userDto);

        //Then
        UserDto userDtoResult = userService.getUserById(id);
        assertEquals(id, userDtoResult.getId());
        assertEquals("Eric Shun", userDtoResult.getFullName());
    }

}
