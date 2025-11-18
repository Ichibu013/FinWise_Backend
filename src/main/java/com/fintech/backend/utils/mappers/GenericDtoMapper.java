package com.fintech.backend.utils.mappers;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The type Generic DTO Mapper.
 * Provides utility methods for mapping objects between different types, typically
 * from entities to DTOs and vice-versa, using ModelMapper.
 */
@Component
public class GenericDtoMapper {

    private final ModelMapper modelMapper;

    public GenericDtoMapper() {
        this.modelMapper = new ModelMapper();
        // Configure ModelMapper for strict matching and skipping nulls during mapping.
        this.modelMapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT)
                .setSkipNullEnabled(true);
    }

    /**
     * Maps a source object to a new instance of the destination class,
     * applying an optional custom mapping consumer.
     *
     * @param <D>             The type of the destination object.
     * @param <T>             The type of the source object.
     * @param source          The object to map from.
     * @param destinationClass The class of the destination object.
     * @param customMapping   An optional consumer to apply custom logic after the initial mapping.
     * @return A new instance of the destination class with mapped data.
     * @throws MappingException If an error occurs during the mapping process.
     */
    public <D, T> D map(final T source, Class<D> destinationClass, Consumer<D> customMapping) {
        if (source == null) {
            throw new MappingException("Source object cannot be null for mapping to " + destinationClass.getSimpleName());
        }
        try {
            D destination = modelMapper.map(source, destinationClass);
            if (customMapping != null) {
                customMapping.accept(destination); // Apply the custom mappings here
            }
            return destination;
        } catch (Exception e) {
            throw new MappingException(
                    "Failed to map " + source.getClass().getSimpleName() + " to " + destinationClass.getSimpleName(),
                    e);
        }
    }

    /**
     * Maps a source object to a new instance of the destination class.
     *
     * @param <D>             The type of the destination object.
     * @param <T>             The type of the source object.
     * @param source          The object to map from.
     * @param destinationClass The class of the destination object.
     * @return A new instance of the destination class with mapped data.
     * @throws MappingException If an error occurs during the mapping process.
     */
    public <D, T> D map(final T source, Class<D> destinationClass) {
        return map(source, destinationClass, (Consumer<D>) null); // Calls the method with custom mapping, passing null
    }

    /**
     * Maps a list of source objects to a list of new instances of the destination class.
     *
     * @param <D>             The type of the destination objects in the list.
     * @param <T>             The type of the source objects in the list.
     * @param sourceList      The list of objects to map from.
     * @param destinationClass The class of the destination objects.
     * @return A new list of destination objects with mapped data.
     * @throws MappingException If an error occurs during the mapping process for any item.
     */
    public <D, T> List<D> mapAll(final List<T> sourceList, Class<D> destinationClass) {
        if (sourceList == null) {
            return java.util.Collections.emptyList(); // Or throw new MappingException if null lists are not allowed
        }
        return sourceList.stream()
                .map(entity -> map(entity, destinationClass))
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of source objects to a list of new instances of the destination class,
     * applying an optional custom mapping consumer to each mapped item.
     *
     * @param <D>             The type of the destination objects in the list.
     * @param <T>             The type of the source objects in the list.
     * @param sourceList      The list of objects to map from.
     * @param destinationClass The class of the destination objects.
     * @param customMapping   An optional consumer to apply custom logic to each mapped item.
     * @return A new list of destination objects with mapped data.
     * @throws MappingException If an error occurs during the mapping process for any item.
     */
    public <D, T> List<D> mapAll(final List<T> sourceList, Class<D> destinationClass, Consumer<D> customMapping) {
        if (sourceList == null) {
            return java.util.Collections.emptyList(); // Or throw new MappingException if null lists are not allowed
        }
        return sourceList.stream()
                .map(entity -> map(entity, destinationClass, customMapping))
                .collect(Collectors.toList());
    }

    /**
     * Maps the properties from a source object to an existing destination object.
     *
     * @param <S>         The type of the source object.
     * @param <D>         The type of the destination object.
     * @param source      The object to map from.
     * @param destination The existing object to map to.
     * @return The updated destination object.
     * @throws MappingException If an error occurs during the mapping process.
     */
    public <S, D> D map(final S source, D destination) {
        if (source == null) {
            throw new MappingException("Source object cannot be null for mapping.");
        }
        if (destination == null) {
            throw new MappingException("Destination object cannot be null for mapping.");
        }
        try {
            modelMapper.map(source, destination);
            return destination;
        } catch (Exception e) {
            throw new MappingException("Failed to map " + source.getClass().getSimpleName() + " to "
                    + destination.getClass().getSimpleName(), e);
        }
    }

    /**
     * Maps the properties from a source object to an existing destination object,
     * applying an optional custom mapping consumer.
     *
     * @param <S>             The type of the source object.
     * @param <D>             The type of the destination object.
     * @param source          The object to map from.
     * @param destination     The existing object to map to.
     * @param customMapping   An optional consumer to apply custom logic after the initial mapping.
     * @return The updated destination object.
     * @throws MappingException If an error occurs during the mapping process.
     */
    public <S, D> D map(final S source, D destination, Consumer<D> customMapping) {
        if (source == null) {
            throw new MappingException("Source object cannot be null for mapping.");
        }
        if (destination == null) {
            throw new MappingException("Destination object cannot be null for mapping.");
        }
        try {
            modelMapper.map(source, destination);
            if (customMapping != null) {
                customMapping.accept(destination); // Apply the custom mappings here
            }
            return destination;
        } catch (Exception e) {
            throw new MappingException("Failed to map " + source.getClass().getSimpleName() + " to "
                    + destination.getClass().getSimpleName(), e);
        }
    }
}
