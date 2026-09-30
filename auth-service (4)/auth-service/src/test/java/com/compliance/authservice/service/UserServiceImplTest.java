package com.compliance.authservice.service;

import com.compliance.authservice.dto.response.UserResponse;
import com.compliance.authservice.entity.User;
import com.compliance.authservice.exception.UserNotFoundException;
import com.compliance.authservice.mapper.UserMapper;
import com.compliance.authservice.repository.UserRepository;
import com.compliance.authservice.service.impl.UserServiceImpl;
import com.compliance.authservice.service.interfaces.RefreshTokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RefreshTokenService refreshTokenService;

    private UserServiceImpl userService;

    @BeforeEach
    void setUp() {

        userService = new UserServiceImpl(
                userRepository,
                userMapper,
                refreshTokenService
        );
    }

    @Test
    void getUserByIdShouldReturnUser() {

        User user = createUser();
        UserResponse response = createUserResponse();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        when(userMapper.toResponse(user))
                .thenReturn(response);

        UserResponse result =
                userService.getUserById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("saipriya", result.getUsername());
        assertEquals(
                "saipriya@example.com",
                result.getEmail()
        );

        verify(userRepository).findById(1L);
        verify(userMapper).toResponse(user);
    }

    @Test
    void getUserByIdShouldThrowWhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.getUserById(999L)
                );

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage()
        );
    }

    @Test
    void getAllUsersShouldReturnMappedUsers() {

        User firstUser = createUser();

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setFirstName("Test");
        secondUser.setLastName("User");
        secondUser.setUsername("testuser");
        secondUser.setEmail("testuser@example.com");
        secondUser.setPassword("encoded-password");
        secondUser.setEnabled(true);
        secondUser.setAccountNonLocked(true);

        UserResponse firstResponse =
                createUserResponse();

        UserResponse secondResponse =
                new UserResponse();

        secondResponse.setId(2L);
        secondResponse.setFirstName("Test");
        secondResponse.setLastName("User");
        secondResponse.setUsername("testuser");
        secondResponse.setEmail(
                "testuser@example.com"
        );

        when(userRepository.findAll())
                .thenReturn(
                        List.of(firstUser, secondUser)
                );

        when(userMapper.toResponse(firstUser))
                .thenReturn(firstResponse);

        when(userMapper.toResponse(secondUser))
                .thenReturn(secondResponse);

        List<UserResponse> result =
                userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(
                "saipriya",
                result.get(0).getUsername()
        );
        assertEquals(
                "testuser",
                result.get(1).getUsername()
        );
    }

    @Test
    void deleteUserShouldDeleteRefreshTokenAndUser() {

        User user = createUser();

        when(userRepository.findById(1L))
                .thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(refreshTokenService)
                .deleteByUser(user);

        verify(userRepository)
                .delete(user);
    }

    @Test
    void deleteUserShouldThrowWhenUserDoesNotExist() {

        when(userRepository.findById(999L))
                .thenReturn(Optional.empty());

        UserNotFoundException exception =
                assertThrows(
                        UserNotFoundException.class,
                        () -> userService.deleteUser(999L)
                );

        assertEquals(
                "User not found with ID: 999",
                exception.getMessage()
        );
    }

    private User createUser() {

        User user = new User();

        user.setId(1L);
        user.setFirstName("Sai");
        user.setLastName("Priya");
        user.setUsername("saipriya");
        user.setEmail("saipriya@example.com");
        user.setPassword("encoded-password");
        user.setEnabled(true);
        user.setAccountNonLocked(true);

        return user;
    }

    private UserResponse createUserResponse() {

        UserResponse response = new UserResponse();

        response.setId(1L);
        response.setFirstName("Sai");
        response.setLastName("Priya");
        response.setUsername("saipriya");
        response.setEmail("saipriya@example.com");

        return response;
    }
}