package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.entity.Person;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.repository.UserRepository;
import com.edu.ulab.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository,
                           UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        Person user = userMapper.userDtoToPerson(userDto);
        log.info("Mapped user: {}", user);
        Person savedUser = userRepository.save(user);
        log.info("Saved user: {}", savedUser);
        return userMapper.personToUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        Long id = userDto.getId();
        Person update = userMapper.userDtoToPerson(userDto);
        log.info("Mapped person: {}", update);
        Person person = userRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new NotFoundException("User with id: " + id + " was not found"));
        userMapper.updatePerson(update, person);
        Person updatedUser = userRepository.save(person);
        log.info("Updated person: {}", updatedUser);
        return userMapper.personToUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long id) {
        Person person = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User with id: " + id + " was not found"));

        log.info("Retrieved person: {}", person);
        return userMapper.personToUserDto(person);
    }

    @Override
    public boolean existsById(Long id) {
        return userRepository.existsById(id);
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
        log.info("Person id:{} deleted", id);
    }
}
