package ru.practicum.shareit.user.service;

import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;

public interface UserService {
    List<UserDto> getAllUsers();

    UserDto getUserById(long userId);

    UserDto saveNewUser(UserDto userDto);

    UserDto updateUser(UserDto userDto);

    void deleteUser(long userId);
}
