package com.example.demo.config;

import io.swagger.v3.oas.models.examples.Example;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * OpenAPI Examples for common request/response patterns
 * 
 * Provides reusable examples for API documentation to improve
 * developer experience and understanding of API usage.
 */
@Component
public class OpenAPIExamples {

    /**
     * Authentication examples
     */
    public static final Map<String, Example> AUTH_EXAMPLES = Map.of(
            "loginRequest", new Example()
                    .summary("Login Request")
                    .description("Standard user login with username and password")
                    .value("""
                            {
                              "username": "john.doe",
                              "password": "securePassword123",
                              "rememberMe": false
                            }
                            """),
            
            "loginResponse", new Example()
                    .summary("Login Response")
                    .description("Successful login response with JWT token and user information")
                    .value("""
                            {
                              "success": true,
                              "message": "Login successful",
                              "data": {
                                "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                                "tokenType": "Bearer",
                                "expiresIn": 3600,
                                "user": {
                                  "id": 1,
                                  "username": "john.doe",
                                  "email": "john.doe@company.com",
                                  "firstName": "John",
                                  "lastName": "Doe",
                                  "enabled": true,
                                  "roles": ["EMPLOYEE", "MANAGER"]
                                },
                                "permissions": ["USER_READ", "EMPLOYEE_READ", "DEPARTMENT_READ"],
                                "loginTime": "2024-01-15T10:30:00Z"
                              }
                            }
                            """)
    );

    /**
     * Employee management examples
     */
    public static final Map<String, Example> EMPLOYEE_EXAMPLES = Map.of(
            "employeeCreateRequest", new Example()
                    .summary("Create Employee Request")
                    .description("Request to create a new employee with comprehensive information")
                    .value("""
                            {
                              "employeeNumber": "EMP001",
                              "firstName": "Jane",
                              "lastName": "Smith",
                              "email": "jane.smith@company.com",
                              "phone": "+1-555-0123",
                              "departmentId": 1,
                              "positionId": 2,
                              "managerId": 5,
                              "hireDate": "2024-01-15",
                              "status": "ACTIVE",
                              "employmentType": "FULL_TIME",
                              "payType": "SALARY",
                              "salary": 75000.00,
                              "address": {
                                "street": "123 Main St",
                                "city": "New York",
                                "state": "NY",
                                "zipCode": "10001",
                                "country": "USA"
                              }
                            }
                            """),
            
            "employeeResponse", new Example()
                    .summary("Employee Response")
                    .description("Complete employee information response")
                    .value("""
                            {
                              "success": true,
                              "message": "Employee retrieved successfully",
                              "data": {
                                "id": 1,
                                "employeeNumber": "EMP001",
                                "firstName": "Jane",
                                "lastName": "Smith",
                                "email": "jane.smith@company.com",
                                "phone": "+1-555-0123",
                                "department": {
                                  "id": 1,
                                  "name": "Engineering",
                                  "code": "ENG"
                                },
                                "position": {
                                  "id": 2,
                                  "jobTitle": "Senior Software Engineer",
                                  "code": "SSE"
                                },
                                "manager": {
                                  "id": 5,
                                  "firstName": "John",
                                  "lastName": "Manager",
                                  "email": "john.manager@company.com"
                                },
                                "hireDate": "2024-01-15",
                                "status": "ACTIVE",
                                "employmentType": "FULL_TIME",
                                "payType": "SALARY",
                                "salary": 75000.00,
                                "createdAt": "2024-01-15T09:00:00Z",
                                "updatedAt": "2024-01-15T09:00:00Z"
                              }
                            }
                            """)
    );

    /**
     * Department management examples
     */
    public static final Map<String, Example> DEPARTMENT_EXAMPLES = Map.of(
            "departmentTreeResponse", new Example()
                    .summary("Department Tree Response")
                    .description("Hierarchical department structure")
                    .value("""
                            {
                              "success": true,
                              "message": "Department tree retrieved successfully",
                              "data": [
                                {
                                  "id": 1,
                                  "name": "Engineering",
                                  "code": "ENG",
                                  "level": 0,
                                  "children": [
                                    {
                                      "id": 2,
                                      "name": "Backend Development",
                                      "code": "BE",
                                      "level": 1,
                                      "children": []
                                    },
                                    {
                                      "id": 3,
                                      "name": "Frontend Development",
                                      "code": "FE",
                                      "level": 1,
                                      "children": []
                                    }
                                  ]
                                }
                              ]
                            }
                            """)
    );

    /**
     * Error response examples
     */
    public static final Map<String, Example> ERROR_EXAMPLES = Map.of(
            "validationError", new Example()
                    .summary("Validation Error")
                    .description("Request validation failed")
                    .value("""
                            {
                              "success": false,
                              "message": "Validation failed",
                              "errors": [
                                {
                                  "field": "email",
                                  "message": "Email format is invalid"
                                },
                                {
                                  "field": "firstName",
                                  "message": "First name is required"
                                }
                              ],
                              "timestamp": "2024-01-15T10:30:00Z",
                              "path": "/api/employees"
                            }
                            """),
            
            "notFoundError", new Example()
                    .summary("Not Found Error")
                    .description("Requested resource not found")
                    .value("""
                            {
                              "success": false,
                              "message": "Employee not found with ID: 999",
                              "timestamp": "2024-01-15T10:30:00Z",
                              "path": "/api/employees/999"
                            }
                            """),
            
            "unauthorizedError", new Example()
                    .summary("Unauthorized Error")
                    .description("Authentication required or invalid credentials")
                    .value("""
                            {
                              "success": false,
                              "message": "Invalid credentials",
                              "timestamp": "2024-01-15T10:30:00Z",
                              "path": "/api/auth/login"
                            }
                            """),
            
            "forbiddenError", new Example()
                    .summary("Forbidden Error")
                    .description("Insufficient permissions for the requested operation")
                    .value("""
                            {
                              "success": false,
                              "message": "Access denied. Required permission: EMPLOYEE_DELETE",
                              "timestamp": "2024-01-15T10:30:00Z",
                              "path": "/api/employees/1"
                            }
                            """)
    );

    /**
     * Pagination examples
     */
    public static final Map<String, Example> PAGINATION_EXAMPLES = Map.of(
            "pagedResponse", new Example()
                    .summary("Paginated Response")
                    .description("Standard paginated response format")
                    .value("""
                            {
                              "success": true,
                              "message": "Employees retrieved successfully",
                              "data": {
                                "content": [
                                  {
                                    "id": 1,
                                    "employeeNumber": "EMP001",
                                    "firstName": "Jane",
                                    "lastName": "Smith",
                                    "email": "jane.smith@company.com"
                                  }
                                ],
                                "pageable": {
                                  "sort": {
                                    "sorted": true,
                                    "unsorted": false
                                  },
                                  "pageNumber": 0,
                                  "pageSize": 20,
                                  "offset": 0,
                                  "paged": true,
                                  "unpaged": false
                                },
                                "totalElements": 150,
                                "totalPages": 8,
                                "last": false,
                                "first": true,
                                "numberOfElements": 20,
                                "size": 20,
                                "number": 0,
                                "sort": {
                                  "sorted": true,
                                  "unsorted": false
                                }
                              }
                            }
                            """)
    );
}