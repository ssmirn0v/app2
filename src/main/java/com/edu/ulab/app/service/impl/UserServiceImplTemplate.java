package com.edu.ulab.app.service.impl;

import com.edu.ulab.app.dto.UserDto;
import com.edu.ulab.app.exception.NotFoundException;
import com.edu.ulab.app.mapper.UserMapper;
import com.edu.ulab.app.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.PreparedStatement;
import java.util.Objects;

@Slf4j
@Service
public class UserServiceImplTemplate implements UserService {
    private final JdbcTemplate jdbcTemplate;
    private final UserMapper userMapper;

    public UserServiceImplTemplate(JdbcTemplate jdbcTemplate, UserMapper userMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.userMapper = userMapper;
    }

    @Override
    public UserDto createUser(UserDto userDto) {

        final String INSERT_SQL = "INSERT INTO PERSON(FULL_NAME, TITLE, AGE) VALUES (?,?,?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL, new String[]{"id"});
                    ps.setString(1, userDto.getFullName());
                    ps.setString(2, userDto.getTitle());
                    ps.setLong(3, userDto.getAge());
                    return ps;
                }, keyHolder);

        userDto.setId(Objects.requireNonNull(keyHolder.getKey()).longValue());
        return userDto;
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        UserDto userForUpdate = getUserById(userDto.getId());
        userMapper.updateUserDto(userDto, userForUpdate);
        log.info("Updated user dto: {}", userForUpdate);

        final String UPDATE_QUERY = "UPDATE PERSON SET full_name=?, title=?, age=? WHERE id = ?";
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(UPDATE_QUERY);
            ps.setString(1, userForUpdate.getFullName());
            ps.setString(2, userForUpdate.getTitle());
            ps.setLong(3, userForUpdate.getAge());
            ps.setLong(4, userForUpdate.getId());

            return ps;
        });
        return userForUpdate;
    }

    @Override
    public UserDto getUserById(Long id) {
        final String SELECT_QUERY = "SELECT * FROM PERSON WHERE id=?";
        UserDto user = jdbcTemplate.queryForObject(SELECT_QUERY,
                (rs, rowNum) ->
                        new UserDto(
                                rs.getLong("id"),
                                rs.getString("full_name"),
                                rs.getString("title"),
                                rs.getInt("age")
                        ),
                id);
        if (user == null) {
            throw new NotFoundException("User with id: " + id + " was not found");
        }
        return user;
    }

    @Override
    public void deleteUserById(Long id) {
        final String DELETE_QUERY = "DELETE FROM PERSON WHERE id=?";
        jdbcTemplate.update(DELETE_QUERY, id);
        log.info("Deleted user id:{}", id);
    }

    @Override
    public boolean existsById(Long id) {
        final String EXISTS_QUERY = "SELECT EXISTS(SELECT * FROM PERSON WHERE id=?)";
        boolean exists = jdbcTemplate.queryForObject(EXISTS_QUERY, Boolean.class, id);
        log.info("User id:{} exists: {}", id, exists);
        return exists;
    }
}
