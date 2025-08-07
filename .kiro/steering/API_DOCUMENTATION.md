# API Documentation

This document provides comprehensive information about the Employee Management System REST API, including authentication, endpoints, request/response formats, and integration examples.

## Table of Contents

1. [Authentication](#authentication)
2. [API Response Format](#api-response-format)
3. [Error Handling](#error-handling)
4. [Pagination](#pagination)
5. [Core Endpoints](#core-endpoints)
6. [Integration Examples](#integration-examples)
7. [Rate Limiting](#rate-limiting)
8. [Webhooks](#webhooks)

## Authentication

The API uses JWT (JSON Web Token) based authentication. All protected endpoints require a valid JWT token in the Authorization header.

### Getting a Token

**Endpoint:** `POST /api/auth/login`

**Request:**
```json
{
  "username": "john.doe",
  "password": "securePassword123",
  "rememberMe": false
}
```

**Response:**
```json
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
```

### Using the Token

Include the token in the Authorization header for all subsequent requests:

```bash
curl -X GET http://localhost:8080/api/employees \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
```

### Token Refresh

**Endpoint:** `POST /api/auth/refresh-token`

**Request:**
```bash
curl -X POST http://localhost:8080/api/auth/refresh-token \
  -d "refreshToken=your-refresh-token"
```

### Logout

**Endpoint:** `POST /api/auth/logout`

```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer your-jwt-token"
```

## API Response Format

All API responses follow a consistent format:

### Success Response
```json
{
  "success": true,
  "message": "Operation completed successfully",
  "data": {
    // Response data here
  },
  "timestamp": "2024-01-15T10:30:00Z"
}
```

### Error Response
```json
{
  "success": false,
  "message": "Error description",
  "errors": [
    {
      "field": "fieldName",
      "message": "Field-specific error message"
    }
  ],
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/endpoint"
}
```

## Error Handling

### HTTP Status Codes

| Status Code | Description |
|-------------|-------------|
| 200 | OK - Request successful |
| 201 | Created - Resource created successfully |
| 400 | Bad Request - Invalid request data |
| 401 | Unauthorized - Authentication required |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource not found |
| 409 | Conflict - Resource already exists |
| 422 | Unprocessable Entity - Validation failed |
| 500 | Internal Server Error - Server error |

### Common Error Examples

#### Validation Error (400)
```json
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
```

#### Authentication Error (401)
```json
{
  "success": false,
  "message": "Invalid credentials",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/auth/login"
}
```

#### Permission Error (403)
```json
{
  "success": false,
  "message": "Access denied. Required permission: EMPLOYEE_DELETE",
  "timestamp": "2024-01-15T10:30:00Z",
  "path": "/api/employees/1"
}
```

## Pagination

List endpoints support pagination using standard Spring Boot pagination parameters:

### Parameters
- `page`: Page number (0-based, default: 0)
- `size`: Page size (default: 20, max: 100)
- `sort`: Sort criteria (format: `property,direction`)

### Example Request
```bash
curl -X GET "http://localhost:8080/api/employees?page=0&size=10&sort=lastName,asc" \
  -H "Authorization: Bearer your-jwt-token"
```

### Paginated Response
```json
{
  "success": true,
  "message": "Employees retrieved successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "employeeNumber": "EMP001",
        "firstName": "Jane",
        "lastName": "Smith"
      }
    ],
    "pageable": {
      "pageNumber": 0,
      "pageSize": 10,
      "offset": 0
    },
    "totalElements": 150,
    "totalPages": 15,
    "last": false,
    "first": true,
    "numberOfElements": 10,
    "size": 10,
    "number": 0
  }
}
```
## Cor
e Endpoints

### Authentication Endpoints

#### Login
- **POST** `/api/auth/login`
- **Description**: Authenticate user and get JWT token
- **Permissions**: Public
- **Request Body**: LoginRequest
- **Response**: LoginResponse with JWT token

#### Logout
- **POST** `/api/auth/logout`
- **Description**: Invalidate JWT token
- **Permissions**: Authenticated
- **Headers**: Authorization: Bearer {token}
- **Response**: Success message

#### Refresh Token
- **POST** `/api/auth/refresh-token`
- **Description**: Refresh JWT token
- **Permissions**: Public
- **Parameters**: refreshToken (string)
- **Response**: New LoginResponse

#### Validate Token
- **GET** `/api/auth/validate-token`
- **Description**: Validate JWT token
- **Permissions**: Public
- **Headers**: Authorization: Bearer {token}
- **Response**: Boolean validation result

### User Management Endpoints

#### Get All Users
- **GET** `/api/users`
- **Description**: Retrieve paginated list of users
- **Permissions**: USER_READ
- **Parameters**: page, size, sort
- **Response**: Page<UserDto>

#### Get User by ID
- **GET** `/api/users/{id}`
- **Description**: Retrieve user by ID
- **Permissions**: USER_READ
- **Path Variables**: id (Long)
- **Response**: UserDto

#### Create User
- **POST** `/api/users`
- **Description**: Create new user
- **Permissions**: USER_CREATE
- **Request Body**: UserCreateRequest
- **Response**: UserDto

#### Update User
- **PUT** `/api/users/{id}`
- **Description**: Update existing user
- **Permissions**: USER_UPDATE
- **Path Variables**: id (Long)
- **Request Body**: UserUpdateRequest
- **Response**: UserDto

#### Delete User
- **DELETE** `/api/users/{id}`
- **Description**: Delete user
- **Permissions**: USER_DELETE
- **Path Variables**: id (Long)
- **Response**: Success message

#### Search Users
- **GET** `/api/users/search`
- **Description**: Search users by criteria
- **Permissions**: USER_READ
- **Parameters**: searchTerm, page, size, sort
- **Response**: Page<UserDto>

### Employee Management Endpoints

#### Get All Employees
- **GET** `/api/employees`
- **Description**: Retrieve paginated list of employees
- **Permissions**: EMPLOYEE_READ
- **Parameters**: page, size, sort
- **Response**: Page<EmployeeDto>

#### Get Employee by ID
- **GET** `/api/employees/{id}`
- **Description**: Retrieve employee by ID
- **Permissions**: EMPLOYEE_READ
- **Path Variables**: id (Long)
- **Response**: EmployeeDto

#### Create Employee
- **POST** `/api/employees`
- **Description**: Create new employee
- **Permissions**: EMPLOYEE_CREATE
- **Request Body**: EmployeeCreateRequest
- **Response**: EmployeeDto

#### Update Employee
- **PUT** `/api/employees/{id}`
- **Description**: Update existing employee
- **Permissions**: EMPLOYEE_UPDATE
- **Path Variables**: id (Long)
- **Request Body**: EmployeeUpdateRequest
- **Response**: EmployeeDto

#### Delete Employee
- **DELETE** `/api/employees/{id}`
- **Description**: Delete employee
- **Permissions**: EMPLOYEE_DELETE
- **Path Variables**: id (Long)
- **Response**: Success message

#### Search Employees
- **GET** `/api/employees/search`
- **Description**: Search employees by criteria
- **Permissions**: EMPLOYEE_READ
- **Parameters**: q (search term), page, size, sort
- **Response**: Page<EmployeeDto>

#### Advanced Employee Search
- **POST** `/api/employees/search/advanced`
- **Description**: Advanced search with multiple criteria
- **Permissions**: EMPLOYEE_READ
- **Request Body**: EmployeeSearchCriteria
- **Parameters**: page, size, sort
- **Response**: Page<EmployeeDto>

### Department Management Endpoints

#### Get Department Tree
- **GET** `/api/departments/tree`
- **Description**: Retrieve complete department hierarchy
- **Permissions**: DEPARTMENT_READ
- **Response**: List<DepartmentTreeDto>

#### Get All Departments
- **GET** `/api/departments`
- **Description**: Retrieve all departments as flat list
- **Permissions**: DEPARTMENT_READ
- **Response**: List<DepartmentDto>

#### Get Department by ID
- **GET** `/api/departments/{id}`
- **Description**: Retrieve department by ID
- **Permissions**: DEPARTMENT_READ
- **Path Variables**: id (Long)
- **Response**: DepartmentDto

#### Create Department
- **POST** `/api/departments`
- **Description**: Create new department
- **Permissions**: DEPARTMENT_CREATE
- **Request Body**: DepartmentCreateRequest
- **Response**: DepartmentDto

#### Update Department
- **PUT** `/api/departments/{id}`
- **Description**: Update existing department
- **Permissions**: DEPARTMENT_UPDATE
- **Path Variables**: id (Long)
- **Request Body**: DepartmentUpdateRequest
- **Response**: DepartmentDto

#### Delete Department
- **DELETE** `/api/departments/{id}`
- **Description**: Delete department
- **Permissions**: DEPARTMENT_DELETE
- **Path Variables**: id (Long)
- **Response**: Success message

### Position Management Endpoints

#### Get All Positions
- **GET** `/api/positions`
- **Description**: Retrieve all positions
- **Permissions**: POSITION_READ
- **Parameters**: page, size, sort
- **Response**: Page<PositionDto>

#### Get Position by ID
- **GET** `/api/positions/{id}`
- **Description**: Retrieve position by ID
- **Permissions**: POSITION_READ
- **Path Variables**: id (Long)
- **Response**: PositionDto

#### Create Position
- **POST** `/api/positions`
- **Description**: Create new position
- **Permissions**: POSITION_CREATE
- **Request Body**: PositionCreateRequest
- **Response**: PositionDto

#### Update Position
- **PUT** `/api/positions/{id}`
- **Description**: Update existing position
- **Permissions**: POSITION_UPDATE
- **Path Variables**: id (Long)
- **Request Body**: PositionUpdateRequest
- **Response**: PositionDto

#### Delete Position
- **DELETE** `/api/positions/{id}`
- **Description**: Delete position
- **Permissions**: POSITION_DELETE
- **Path Variables**: id (Long)
- **Response**: Success message##
 Integration Examples

### JavaScript/Node.js Integration

#### Basic Setup
```javascript
const API_BASE_URL = 'http://localhost:8080/api';
let authToken = null;

// Login function
async function login(username, password) {
  const response = await fetch(`${API_BASE_URL}/auth/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({
      username,
      password,
      rememberMe: false
    })
  });
  
  const result = await response.json();
  if (result.success) {
    authToken = result.data.token;
    return result.data;
  } else {
    throw new Error(result.message);
  }
}

// Authenticated request helper
async function apiRequest(endpoint, options = {}) {
  const config = {
    headers: {
      'Content-Type': 'application/json',
      ...(authToken && { 'Authorization': `Bearer ${authToken}` }),
      ...options.headers
    },
    ...options
  };
  
  const response = await fetch(`${API_BASE_URL}${endpoint}`, config);
  const result = await response.json();
  
  if (!response.ok) {
    throw new Error(result.message || 'API request failed');
  }
  
  return result;
}

// Example usage
async function getEmployees(page = 0, size = 20) {
  return await apiRequest(`/employees?page=${page}&size=${size}`);
}

async function createEmployee(employeeData) {
  return await apiRequest('/employees', {
    method: 'POST',
    body: JSON.stringify(employeeData)
  });
}
```

#### React Integration Example
```jsx
import React, { useState, useEffect } from 'react';

const EmployeeList = () => {
  const [employees, setEmployees] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    fetchEmployees();
  }, []);

  const fetchEmployees = async () => {
    try {
      setLoading(true);
      const result = await apiRequest('/employees');
      setEmployees(result.data.content);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      <h2>Employees</h2>
      <ul>
        {employees.map(employee => (
          <li key={employee.id}>
            {employee.firstName} {employee.lastName} - {employee.email}
          </li>
        ))}
      </ul>
    </div>
  );
};
```

### Python Integration

#### Using requests library
```python
import requests
import json

class EmployeeManagementAPI:
    def __init__(self, base_url='http://localhost:8080/api'):
        self.base_url = base_url
        self.token = None
        self.session = requests.Session()
    
    def login(self, username, password):
        """Authenticate and get JWT token"""
        response = self.session.post(
            f'{self.base_url}/auth/login',
            json={
                'username': username,
                'password': password,
                'rememberMe': False
            }
        )
        
        if response.status_code == 200:
            data = response.json()
            if data['success']:
                self.token = data['data']['token']
                self.session.headers.update({
                    'Authorization': f'Bearer {self.token}'
                })
                return data['data']
        
        raise Exception(f'Login failed: {response.json().get("message", "Unknown error")}')
    
    def get_employees(self, page=0, size=20, sort=None):
        """Get paginated list of employees"""
        params = {'page': page, 'size': size}
        if sort:
            params['sort'] = sort
        
        response = self.session.get(f'{self.base_url}/employees', params=params)
        response.raise_for_status()
        return response.json()
    
    def create_employee(self, employee_data):
        """Create new employee"""
        response = self.session.post(
            f'{self.base_url}/employees',
            json=employee_data
        )
        response.raise_for_status()
        return response.json()
    
    def get_department_tree(self):
        """Get department hierarchy"""
        response = self.session.get(f'{self.base_url}/departments/tree')
        response.raise_for_status()
        return response.json()

# Example usage
api = EmployeeManagementAPI()

# Login
user_data = api.login('admin', 'admin123')
print(f'Logged in as: {user_data["user"]["username"]}')

# Get employees
employees = api.get_employees(page=0, size=10)
print(f'Total employees: {employees["data"]["totalElements"]}')

# Create employee
new_employee = {
    'employeeNumber': 'EMP001',
    'firstName': 'John',
    'lastName': 'Doe',
    'email': 'john.doe@company.com',
    'departmentId': 1,
    'positionId': 1,
    'hireDate': '2024-01-15',
    'status': 'ACTIVE'
}

created = api.create_employee(new_employee)
print(f'Created employee: {created["data"]["id"]}')
```

### Java Integration

#### Using RestTemplate
```java
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

public class EmployeeManagementClient {
    private final RestTemplate restTemplate;
    private final String baseUrl;
    private String authToken;
    
    public EmployeeManagementClient(String baseUrl) {
        this.baseUrl = baseUrl;
        this.restTemplate = new RestTemplate();
    }
    
    public LoginResponse login(String username, String password) {
        String url = baseUrl + "/auth/login";
        
        Map<String, Object> request = new HashMap<>();
        request.put("username", username);
        request.put("password", password);
        request.put("rememberMe", false);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
        
        ResponseEntity<ApiResponse<LoginResponse>> response = restTemplate.exchange(
            url, HttpMethod.POST, entity, 
            new ParameterizedTypeReference<ApiResponse<LoginResponse>>() {}
        );
        
        if (response.getBody().isSuccess()) {
            this.authToken = response.getBody().getData().getToken();
            return response.getBody().getData();
        }
        
        throw new RuntimeException("Login failed: " + response.getBody().getMessage());
    }
    
    public Page<EmployeeDto> getEmployees(int page, int size) {
        String url = baseUrl + "/employees?page=" + page + "&size=" + size;
        
        HttpHeaders headers = createAuthHeaders();
        HttpEntity<?> entity = new HttpEntity<>(headers);
        
        ResponseEntity<ApiResponse<Page<EmployeeDto>>> response = restTemplate.exchange(
            url, HttpMethod.GET, entity,
            new ParameterizedTypeReference<ApiResponse<Page<EmployeeDto>>>() {}
        );
        
        return response.getBody().getData();
    }
    
    private HttpHeaders createAuthHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null) {
            headers.setBearerAuth(authToken);
        }
        return headers;
    }
}
```

## Rate Limiting

The API implements rate limiting to prevent abuse and ensure fair usage:

### Rate Limits
- **Authentication endpoints**: 5 requests per minute per IP
- **Read operations**: 100 requests per minute per user
- **Write operations**: 50 requests per minute per user
- **Bulk operations**: 10 requests per minute per user

### Rate Limit Headers
Response headers include rate limit information:
```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1642248000
```

### Rate Limit Exceeded Response
```json
{
  "success": false,
  "message": "Rate limit exceeded. Try again in 60 seconds.",
  "timestamp": "2024-01-15T10:30:00Z"
}
```

## Webhooks

The system supports webhooks for real-time notifications of important events:

### Supported Events
- `employee.created`
- `employee.updated`
- `employee.deleted`
- `department.created`
- `department.updated`
- `user.login`
- `payroll.processed`

### Webhook Configuration
Configure webhooks through the admin interface or API:

```json
{
  "url": "https://your-app.com/webhooks/employee-management",
  "events": ["employee.created", "employee.updated"],
  "secret": "your-webhook-secret",
  "active": true
}
```

### Webhook Payload Example
```json
{
  "event": "employee.created",
  "timestamp": "2024-01-15T10:30:00Z",
  "data": {
    "employee": {
      "id": 123,
      "employeeNumber": "EMP001",
      "firstName": "John",
      "lastName": "Doe",
      "email": "john.doe@company.com"
    }
  },
  "signature": "sha256=..."
}
```

## Best Practices

### Error Handling
- Always check the `success` field in responses
- Handle different HTTP status codes appropriately
- Implement retry logic for transient failures
- Log errors for debugging

### Performance
- Use pagination for large datasets
- Implement caching where appropriate
- Use bulk operations when available
- Monitor API response times

### Security
- Store JWT tokens securely
- Implement token refresh logic
- Use HTTPS in production
- Validate all input data
- Follow principle of least privilege for permissions

### Monitoring
- Monitor API usage and performance
- Set up alerts for error rates
- Track business metrics
- Use structured logging