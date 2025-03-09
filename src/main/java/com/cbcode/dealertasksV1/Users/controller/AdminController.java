package com.cbcode.dealertasksV1.Users.controller;

import com.cbcode.dealertasksV1.Users.model.DTOs.UserDto;
import com.cbcode.dealertasksV1.Users.security.AdminService;
import com.cbcode.dealertasksV1.Users.security.DTOs.Request.SignUpRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;

@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@CrossOrigin(origins = "*")
@Validated
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @Operation(summary = "Create a new user",
            description = "Accessible only to admins. Returns a success message.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "User created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid user data")
    @ApiResponse(responseCode = "403", description = "Forbidden for non-admins")
    @ApiResponse(responseCode = "409", description = "User already exists")
    @ApiResponse(responseCode = "500", description = "Internal server error")
    @PostMapping("/signup")
    //@PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<String> signUp(
            @Parameter(description = "User details")
            @RequestBody @Valid SignUpRequest signUpRequest) {
        adminService.createUser(signUpRequest);
        // TODO: add a message to the welcome response email sent to the user.
        return ResponseEntity.ok("User with email " + signUpRequest.email() + " created!");
    }

    @Operation(summary = "Retrieve user details by ID",
            description = "Accessible only to admins. Returns user details excluding sensitive data.",
    security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @ApiResponse(responseCode = "400", description = "Invalid user ID")
    @ApiResponse(responseCode = "403", description = "Forbidden for non-admins")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/get-by-id/{id}")
    public ResponseEntity<UserDto> getUserById(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(adminService.getUserById(id));
    }

    @Operation(summary = "Retrieve a paginated list of all users",
            description = "Accessible only to admins. Returns user details excluding sensitive data.",
    security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200", description = "Successful retrieval")
    @ApiResponse(responseCode = "400", description = "Invalid pagination or sort parameters")
    @ApiResponse(responseCode = "403", description = "Forbidden for non-admins")
    @GetMapping(value = "/all", produces = "application/json")
    public ResponseEntity<Page<UserDto>> getAllUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Number of users per page (1-100)", example = "10")
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @Parameter(description = "Field to sort by (e.g., 'id', 'email')", example = "id")
            @RequestParam(defaultValue = "id") String sortBy
    ) {
        String[] validSortFields = {"id", "firstName", "lastName", "email", "roles"};
        if (!Arrays.asList(validSortFields).contains(sortBy)) {
            throw new IllegalArgumentException("Invalid sort field: " + sortBy);
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortBy));
        Page<UserDto> usersPage = adminService.getAllUsers(pageable);
        return ResponseEntity.ok(usersPage);
    }
}
