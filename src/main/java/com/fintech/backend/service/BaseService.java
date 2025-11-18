package com.fintech.backend.service;

import com.fintech.backend.config.Exceptions.UserNotFoundException;
import com.fintech.backend.models.Users;
import com.fintech.backend.repository.UsersRepository;
import com.fintech.backend.utils.mappers.GenericDtoMapper;
import com.fintech.backend.utils.mappers.GenericResponseFactory;

import java.util.Map;
import java.util.Optional;


public class BaseService {

    public final GenericDtoMapper mapper;

    public final GenericResponseFactory responseFactory;

    protected final UsersRepository usersRepository;

    /**
     * Constructs a base service with common utilities shared across services.
     *
     * @param mapper a generic DTO mapper used for object conversions
     * @param responseFactory a factory for building consistent API responses
     * @param usersRepository repository for accessing {@link Users} entities
     */
    public BaseService(GenericDtoMapper mapper, GenericResponseFactory responseFactory, UsersRepository usersRepository) {
        this.mapper = mapper;
        this.responseFactory = responseFactory;
        this.usersRepository = usersRepository;
    }

    /**
     * Fetches a user by ID or throws if the user does not exist.
     *
     * @param id the unique identifier of the user
     * @return the persisted {@link Users} entity
     * @throws UserNotFoundException if no user is found for the given ID
     */
    public Users getUserById(Long id) {
        Optional<Users> user = usersRepository.findById(id);
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        return user.get();
    }

}