package com.grabaseat.service;

import com.grabaseat.dto.LoginRequest;
import com.grabaseat.dto.LoginResponse;
import com.grabaseat.dto.RegisterRequest;
import com.grabaseat.dto.UserResponse;
import com.grabaseat.exception.EmailAlreadyExistsException;
import com.grabaseat.exception.InvalidCredentialsException;
import com.grabaseat.model.Role;
import com.grabaseat.model.User;
import com.grabaseat.repository.UserRepository;
import com.grabaseat.util.JwtUtil;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getName(), request.getEmail(), hashedPassword, Role.USER);
        User saved = userRepository.save(user);

        return new UserResponse(saved.getId(), saved.getName(), saved.getEmail(), saved.getRole());
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtUtil.createToken(user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getName(), user.getRole());
    }
}

