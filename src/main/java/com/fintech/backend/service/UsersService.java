package com.fintech.backend.service;

import com.fintech.backend.config.Exceptions.InvalidPasswordException;
import com.fintech.backend.config.Exceptions.UserExistsException;
import com.fintech.backend.config.Exceptions.UserNotFoundException;
import com.fintech.backend.dto.ChangePasswordDto;
import com.fintech.backend.dto.LoginDto;
import com.fintech.backend.dto.SignupDto;
import com.fintech.backend.models.Accounts;
import com.fintech.backend.models.Users;
import com.fintech.backend.repository.AccountRepository;
import com.fintech.backend.repository.UsersRepository;
import com.fintech.backend.utils.mappers.GenericDtoMapper;
import com.fintech.backend.utils.mappers.GenericResponseFactory;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UsersService extends BaseService implements UserDetailsService {
    private final UsersRepository usersRepository;
    private final AccountRepository accountRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;

    public UsersService(GenericDtoMapper mapper,
                        GenericResponseFactory responseFactory,
                        UsersRepository usersRepository,
                        AccountRepository accountRepository,
                        SimpMessagingTemplate simpMessagingTemplate) {
        super(mapper, responseFactory, usersRepository);
        this.usersRepository = usersRepository;
        this.accountRepository = accountRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param loginDto the credential payload containing email and password
     * @return a map containing the authenticated user's ID with key {@code "userId"}
     * @throws RuntimeException if the user is not found or the password is invalid
     */
    public Map<String, Long> login(LoginDto loginDto) {
        Optional<Users> user = usersRepository.findByEmail(loginDto.getEmail());
        if (user.isEmpty()) {
            throw new UserNotFoundException("User not found");
        }
        if (!user.get().getPassword().equals(loginDto.getPassword())) {
            throw new InvalidPasswordException("Invalid password");
        }
        return Map.of("userId", Long.valueOf(loadUserByUsername(String.valueOf(user.get().getUserId())).getUsername()));
    }

    /**
     * Registers a new user and creates a linked account. The method validates that the email
     * is not already in use, creates a new user with the provided details, and initializes
     * an associated account.
     *
     * @param signupDto the user signup payload containing email, password, and other user details
     * @return the newly created and persisted {@link Users} entity
     * @throws UserExistsException if a user with the same email already exists
     */
    @Transactional
    public Users createUser(SignupDto signupDto) {
        Optional<Users> user = usersRepository.findByEmail(signupDto.getEmail());
        if (user.isPresent()) {
            throw new UserExistsException("User already exists");
        }
        Users newUser = mapper.map(signupDto, Users.class);
        newUser.setPassword(signupDto.getPassword());
        Accounts newAccount = new Accounts();
        newAccount.setUserId(newUser);
        usersRepository.save(newUser);
        accountRepository.save(newAccount);
        log.info("User and new account created successfully");
        return newUser;
    }

    /**
     * Updates an existing user's profile details.
     *
     * @param id        the ID of the user to update
     * @param signupDto the new profile values
     * @return a confirmation message
     * @throws RuntimeException if the user does not exist
     */
    @Transactional
    public Map<String, String> updateUser(Long id, SignupDto signupDto) {
        try {
            Users user = getUserById(id);
            user.setFullName(signupDto.getFullName());
            user.setEmail(signupDto.getEmail());
            user.setPhoneNumber(signupDto.getPhoneNumber());
            usersRepository.save(user);
            log.info("User updated successfully");
            pushUserUpdate(id);
            return Map.of("message", "User updated successfully");
        } catch (Exception e) {
            throw new RuntimeException("Failed to update user");
        }
    }

    public SignupDto getUserDetails(Long id) {
        return mapper.map(getUserById(id), SignupDto.class);
    }

    @Transactional
    public String changePassword(Long userId, ChangePasswordDto changePasswordDto) {
        Users user = getUserById(userId);
        if (!user.getPassword().equals(changePasswordDto.getOldPassword())) {
            throw new InvalidPasswordException("Invalid old password");
        }

        user.setPassword(changePasswordDto.getNewPassword());
        usersRepository.save(user);
        log.info("Password changed successfully");
        pushUserUpdate(userId);
        return "Password changed successfully";
    }


    /**
     * Saves or replaces the profile picture for a given user. The image will be stored
     * as a byte array in the database.
     *
     * @param image the uploaded image file (supported formats: JPEG, PNG; max size: 5MB)
     * @param id    the user's ID
     * @return a map containing a success message with key "message"
     * @throws RuntimeException         if the user does not exist
     * @throws IllegalArgumentException if the image is empty or exceeds size limit
     */
    @Transactional
    public Map<String, String> saveProfilePicture(MultipartFile image, Long id) {
        Users user = getUserById(id);
        try {
            user.setProfilePicture(image.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Failed to save profile picture");
        }
        usersRepository.save(user);
        log.info("Profile picture saved successfully");
        pushUserUpdate(id);
        return Map.of("message", "Profile picture saved successfully");
    }

    /**
     * Retrieves the profile picture for a given user.
     *
     * @param id the ID of the user whose profile picture is to be retrieved
     * @return the profile picture as a byte array
     * @throws RuntimeException if the user does not exist
     */
    public byte[] getProfilePicture(Long id) {
        return getUserById(id).getProfilePicture();
    }

    /**
     * Deletes a user by ID along with associated data as per repository cascade rules.
     *
     * @param id the ID of the user to delete
     * @return a confirmation message
     * @throws RuntimeException if the user does not exist
     */
    @Transactional
    public String deleteUserById(Long id) {
        Users user = getUserById(id);
        usersRepository.delete(user);
        log.info("User deleted successfully");
        return "User deleted successfully";
    }

    @Transactional
    public String uploadFingerPrintId(Long userId, String fingerPrintKey) {
        Users user = getUserById(userId);

        // 1. Check if the specified user already has a fingerprint set.
        // Must check for null before checking if the string is empty.
        if (user.getFingerPrintId() != null && !user.getFingerPrintId().isEmpty()) {
            throw new UserExistsException("User ID " + userId + " already has an assigned fingerprint.");
        }

        // 2. Check if the fingerprint key is already assigned to ANY user.
        Users userByFingerPrint = usersRepository.findByFingerPrintId(fingerPrintKey);

        if (userByFingerPrint != null) {
            // If the existing user with the key is NOT the user we are trying to update,
            // then the key is already in use by someone else.
            if (!userByFingerPrint.getUserId().equals(userId)) {
                throw new UserExistsException("FingerPrint key already exists for another user.");
            }
            // If it is the same user, it means the key is already assigned to them
            // (which should have been caught by the check above, but provides safety).
        }

        // 3. Update and Save
        user.setFingerPrintId(fingerPrintKey);
        usersRepository.save(user);
        log.info("User fingerprint saved successfully for user ID: {}", userId);
        pushUserUpdate(userId);
        return "User fingerprint saved successfully";
    }

    public Map<String, Long> loginByFingerPrint(String fingerPrintKey) {
        Users user = usersRepository.findByFingerPrintId(fingerPrintKey);
        if (user == null) {
            throw new UserNotFoundException("FingerPrint key not registered for any user ");
        }
        return Map.of("userId", user.getUserId());
    }

    @Transactional
    public String deleteFingerPrintId(Long userId) {
        // 1. Retrieve the user by ID.
        // Assuming getUserById() handles UserNotFoundException if the ID doesn't exist.
        Users user = getUserById(userId);

        // 2. Check if the user currently has a fingerprint ID set.
        String currentFingerprint = user.getFingerPrintId();

        // Checks for null and empty string (the safest way to check for an unassigned value).
        if (currentFingerprint == null || currentFingerprint.isEmpty()) {
            throw new RuntimeException("User ID " + userId + " does not have an assigned fingerprint ID to delete.");
        }

        // 3. Clear the fingerprint ID and save the update.
        user.setFingerPrintId(null); // Set to null to remove the key
        usersRepository.save(user);

        log.info("User fingerprint successfully deleted for user ID: {}", userId);
        return "User fingerprint successfully deleted";
    }

    public String getFingerprintForUser(Long userId) {
        log.info("Getting fingerprint for user ID: {}", getUserById(userId).getFingerPrintId());
        return getUserById(userId).getFingerPrintId();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findByUserId(Long.valueOf(username)).orElseThrow(() -> new UserNotFoundException(username + " not found"));
    }

    private void pushUserUpdate(Long userId) {
        final String destination = "/topic/users/" + userId;
        try {
            simpMessagingTemplate.convertAndSend(destination, "User Details updated on server");
            log.info("User Update broadcast on {}", destination);
        } catch (Exception e) {
            log.error("Failed to broadcast user details on {}. Error: {}", destination, e.getMessage());
        }
    }
}

