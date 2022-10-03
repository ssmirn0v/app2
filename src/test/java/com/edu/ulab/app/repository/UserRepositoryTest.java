package com.edu.ulab.app.repository;

import com.edu.ulab.app.config.SystemJpaTest;
import com.edu.ulab.app.entity.Book;
import com.edu.ulab.app.entity.Person;
import com.vladmihalcea.sql.SQLStatementCountValidator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static com.vladmihalcea.sql.SQLStatementCountValidator.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

/**
 * Тесты репозитория {@link UserRepository}.
 */
@SystemJpaTest
public class UserRepositoryTest {
    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        SQLStatementCountValidator.reset();

    }


    @DisplayName("Сохранить юзера. Число insert должно равняться 1")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void insertPerson_thenAssertDmlCount() {
        //Given
        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader1");
        person.setFullName("Test Test");
        person.setRating(77);

        //When
        Person result = userRepository.save(person);

        userRepository.flush();

        //Then
        assertThat(result.getAge()).isEqualTo(111);
        assertSelectCount(0);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    // update
    @DisplayName("Обновить поля юзера. " +
            "Число Insert должно быть равно 1. " +
            "Число Update должно быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void updateUser_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader1");
        person.setFullName("Test Test");
        person.setRating(2);

        Person personForUpdate = userRepository.save(person);

        personForUpdate.setTitle("The King");
        personForUpdate.setRating(7000);

        //When
        Person updatedPerson = userRepository.save(personForUpdate);

        userRepository.flush();

        //Then
        assertThat(updatedPerson.getTitle()).isEqualTo("The King");
        assertThat(updatedPerson.getRating()).isEqualTo(7000);
        assertSelectCount(0);
        assertInsertCount(1);
        assertUpdateCount(1);
        assertDeleteCount(0);
    }

    // get
    @DisplayName("Получить юзера. Число select должно равняться 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void getUser_thenAssertDmlCount() {
        //Given
        // Юзер уже сохранен в бд с помощью файла 2_insert_person_data.sql
        Person savedPerson = new Person();
        savedPerson.setId(1001L);
        savedPerson.setFullName("default user");
        savedPerson.setTitle("reader");
        savedPerson.setAge(55);
        savedPerson.setRating(1);


        Long savedPersonId = savedPerson.getId();

        //When
        Person retrievedUser = userRepository.findById(savedPersonId).get();

        //Then
        assertThat(retrievedUser).isEqualTo(savedPerson);
        assertSelectCount(1);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(0);
    }

    // get all
    @DisplayName("Получение всех юзеров из БД. Число Select должно быть равно 1. " +
            "Число Insert должны быть равно 1;.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void getAllUsers_thenAssertDmlCount() {
        //Given

        Person person = new Person();
        person.setAge(111);
        person.setTitle("reader1");
        person.setFullName("Test Test");
        person.setRating(2);

        Person savedPerson = userRepository.save(person);

        //When
        List<Person> userList =  userRepository.findAll();

        //Then
        assertThat(userList.size()).isEqualTo(2);
        assertSelectCount(1);
        assertInsertCount(1);
        assertUpdateCount(0);
        assertDeleteCount(0);

    }

    // delete
    @DisplayName("Удаление юзера из БД. Число Select должно быть равно 2. " +
            "Число Delete должны быть равно 1.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void deleteUser_thenAssertDmlCount() {
        // Given
        // Один юзер сохранены с помощью 3_insert_book_data.sql
        Long bookIdForDelete = 1001L;

        //When
        userRepository.deleteById(bookIdForDelete);
        long count = userRepository.count();

        //Then
        assertThat(count).isEqualTo(0);
        assertSelectCount(2);
        assertInsertCount(0);
        assertUpdateCount(0);
        assertDeleteCount(1);

    }

    // * failed
    @DisplayName("Сохранение пользователя с Null полем. Должно выброситься исключение.")
    @Test
    @Rollback
    @Sql({"classpath:sql/1_clear_schema.sql",
            "classpath:sql/2_insert_person_data.sql",
            "classpath:sql/3_insert_book_data.sql"
    })
    void saveBookWithNullField_thenAssertError() {
        //Given
        Person user = new Person();
        user.setFullName("Ben Dover");
        user.setRating(777);
        user.setAge(42);


        //When
        Throwable thrown = catchThrowable(() -> {
            Person savedPerson = userRepository.save(user);
        });

        //Then
        assertThat(thrown).isInstanceOf(DataIntegrityViolationException.class);

    }
}
