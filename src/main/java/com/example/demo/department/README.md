# Department Management Module

## Overview

The Department Management Module provides comprehensive functionality for managing hierarchical department structures within the employee management system. It supports tree-based organization with parent-child relationships, path-based queries, and various administrative operations.

## Features

### Core Functionality
- **Hierarchical Structure**: Support for multi-level department trees with parent-child relationships
- **Path-Based Organization**: Automatic path generation (e.g., `/COMP/IT/DEV`) for efficient queries
- **CRUD Operations**: Complete Create, Read, Update, Delete operations with validation
- **Tree Operations**: Move departments, rebuild paths, and maintain hierarchy integrity
- **Search and Filtering**: Search by name, filter by level, and various query options

### Advanced Features
- **Circular Reference Prevention**: Automatic validation to prevent invalid hierarchy moves
- **Dependency Checking**: Validation before deletion to ensure data integrity
- **Statistics and Analytics**: Department statistics including employee counts and hierarchy depth
- **Caching**: Redis-based caching for improved performance
- **Audit Trail**: Automatic tracking of creation and modification timestamps

## API Endpoints

### Department CRUD
- `POST /api/departments` - Create new department
- `GET /api/departments/{id}` - Get department by ID
- `GET /api/departments/code/{code}` - Get department by code
- `PUT /api/departments/{id}` - Update department
- `DELETE /api/departments/{id}` - Delete department

### Hierarchy Operations
- `GET /api/departments/tree` - Get complete department tree
- `GET /api/departments/{id}/subtree` - Get department subtree
- `GET /api/departments/{id}/children` - Get direct children
- `GET /api/departments/root` - Get root departments
- `GET /api/departments/level/{level}` - Get departments by level

### Search and Query
- `GET /api/departments` - Get all departments (flat list)
- `GET /api/departments/search?q={term}` - Search departments by name

### Administrative Operations
- `PUT /api/departments/{id}/move?parentId={parentId}` - Move department
- `PUT /api/departments/{id}/enabled?enabled={true|false}` - Enable/disable department
- `PUT /api/departments/{id}/sort-order?sortOrder={order}` - Update sort order
- `POST /api/departments/rebuild-paths` - Rebuild all department paths (maintenance)

### Analytics and Information
- `GET /api/departments/{id}/path` - Get path from root to department
- `GET /api/departments/{id}/ancestors` - Get ancestor departments
- `GET /api/departments/{id}/descendants` - Get descendant departments
- `GET /api/departments/{id}/statistics` - Get department statistics
- `GET /api/departments/{id}/can-delete` - Check if department can be deleted

## Data Model

### Department Entity
```java
@Entity
@Table(name = "departments")
public class Department {
    private Long id;                    // Primary key
    private String name;                // Department name
    private String code;                // Unique department code
    private String description;         // Optional description
    private String location;            // Physical location
    private Long parentId;              // Parent department ID
    private String depPath;             // Full path (e.g., /COMP/IT/DEV)
    private Boolean isParent;           // Has child departments
    private Boolean enabled;            // Active status
    private Integer level;              // Hierarchy level (0 for root)
    private Integer sortOrder;          // Sort order within parent
    private Long managerId;             // Department manager employee ID
    private Instant createdAt;          // Creation timestamp
    private Instant updatedAt;          // Last update timestamp
    private Long createdBy;             // Creator user ID
    private Long updatedBy;             // Last updater user ID
}
```

### Key Features
- **Hierarchical Relationships**: Self-referencing foreign key for parent-child relationships
- **Path-Based Queries**: Automatic path generation for efficient subtree queries
- **Audit Fields**: Automatic tracking of creation and modification
- **Soft Hierarchy**: Support for both parent and leaf departments
- **Flexible Organization**: Sort order and enable/disable functionality

## Database Schema

### Tables
- `departments` - Main department table with hierarchical structure
- Indexes on frequently queried fields (name, code, parent_id, dep_path, level)
- Triggers for automatic path generation and hierarchy maintenance

### Key Constraints
- Unique constraint on department code
- Self-referencing foreign key for hierarchy
- Check constraints for data validation
- Cascade rules for referential integrity

## Security

### Permission-Based Access Control
- `DEPARTMENT_CREATE` - Create new departments
- `DEPARTMENT_READ` - View department information
- `DEPARTMENT_UPDATE` - Modify existing departments
- `DEPARTMENT_DELETE` - Delete departments
- `DEPARTMENT_ADMIN` - Administrative operations (rebuild paths)

### Validation
- Input validation on all fields
- Business rule validation (circular references, dependencies)
- Authorization checks on all endpoints
- Audit logging for security events

## Performance Optimizations

### Caching Strategy
- Redis caching for frequently accessed department data
- Cache invalidation on modifications
- Cached department trees and hierarchies

### Database Optimizations
- Indexes on frequently queried fields
- Path-based queries for efficient subtree operations
- Optimized recursive queries using PostgreSQL CTEs
- Connection pooling and query optimization

## Error Handling

### Custom Exceptions
- `DepartmentNotFoundException` - Department not found
- `DepartmentAlreadyExistsException` - Duplicate name/code
- `DepartmentHierarchyException` - Hierarchy violations
- `DepartmentInUseException` - Cannot delete due to dependencies

### Validation
- Input validation with detailed error messages
- Business rule validation
- Referential integrity checks
- Graceful error responses with proper HTTP status codes

## Testing

### Unit Tests
- Service layer tests with Mockito
- Repository tests with @DataJpaTest
- Controller tests with @WebMvcTest
- Exception handling tests

### Integration Tests
- Full API integration tests
- Database integration tests
- Security integration tests
- Performance tests

## Usage Examples

### Creating a Department
```java
DepartmentCreateRequest request = new DepartmentCreateRequest();
request.setName("Software Development");
request.setCode("DEV");
request.setDescription("Software development team");
request.setParentId(2L); // IT Department
request.setEnabled(true);

DepartmentDto created = departmentService.createDepartment(request);
```

### Getting Department Tree
```java
List<DepartmentTreeDto> tree = departmentService.getDepartmentTree();
// Returns hierarchical structure with children populated
```

### Moving a Department
```java
departmentService.moveDepartment(5L, 3L); // Move dept 5 under dept 3
// Automatically updates paths and validates hierarchy
```

### Searching Departments
```java
List<DepartmentDto> results = departmentService.searchDepartments("engineering");
// Returns all departments with "engineering" in the name
```

## Configuration

### Application Properties
```properties
# Caching configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=600000

# Database configuration
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
```

### Redis Configuration
- Department data cached with TTL
- Cache keys: `departments::{id}`, `departmentTree::tree`
- Automatic cache eviction on modifications

## Maintenance

### Path Rebuilding
The system includes a maintenance operation to rebuild all department paths:
```java
departmentService.rebuildDepartmentPaths();
```

This operation:
- Recalculates all department paths
- Updates hierarchy levels
- Fixes any inconsistencies
- Should be run during maintenance windows

### Monitoring
- Spring Boot Actuator endpoints for health checks
- Custom metrics for department operations
- Performance monitoring for tree operations
- Cache hit/miss ratios

## Future Enhancements

### Planned Features
- Department templates for quick setup
- Bulk operations for multiple departments
- Department history and versioning
- Advanced reporting and analytics
- Integration with organizational charts

### Performance Improvements
- Materialized path optimization
- Lazy loading for large hierarchies
- Pagination for tree operations
- Background processing for maintenance operations