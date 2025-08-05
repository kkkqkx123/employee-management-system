# Database Design Document - Employee Management System

## Overview

This document provides comprehensive database design specifications for the Spring Boot Employee Management System using a hybrid data storage approach with PostgreSQL as the primary database and Redis for caching and real-time features. The design supports all system modules including security, employee management, departments, positions, communication, and payroll with enhanced data integrity, security, and compliance features.

## Technology Stack

- **Primary Database**: PostgreSQL 15.x
- **Cache Layer**: Redis 7.x
- **ORM**: Spring Data JPA with Hibernate
- **Caching**: Redis with Spring Cache abstraction
- **Data Modeling**: JPA entities with proper relationships and constraints
- **Transactions**: JPA transactions with Spring @Transactional
- **Security**: Field-level encryption for sensitive data
- **Migration**: Flyway for database versioning

## Database Architecture

### Hybrid Data Storage Strategy

The system uses a hybrid approach with PostgreSQL as the primary database for all persistent data and Redis exclusively for caching and real-time features:

**PostgreSQL (Primary Database - All Persistent Data):**
- All core business entities (User, Role, Resource, Employee, Department, Position, PayrollLedger)
- All transactional data with ACID compliance and referential integrity
- Complex relational queries, joins, and reporting capabilities
- Foreign key constraints and check constraints for data integrity
- Comprehensive audit trails and historical data
- User authentication data and role-based permissions

**Redis (Cache & Real-time Features Only):**
- JWT token blacklisting for secure logout functionality
- Caching of frequently accessed data (department trees, user permissions)
- Real-time chat messages (temporary storage with TTL)
- WebSocket session management and connection tracking
- Search result caching for performance optimization
- Notification queues for real-time delivery

**Critical Design Principle:** 
Redis is used exclusively for caching and real-time features. All persistent, relational data is stored in PostgreSQL to ensure ACID compliance, referential integrity, and proper transaction support. This addresses the fundamental flaw of using Redis as a primary database for relational data.

### Connection Configuration

```properties
# PostgreSQL Configuration
spring.datasource.url=jdbc:postgresql://localhost:5432/employee_management
spring.datasource.username=${DB_USERNAME:employee_admin}
spring.datasource.password=${DB_PASSWORD:secure_password}
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA Configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.jdbc.batch_size=20
spring.jpa.properties.hibernate.order_inserts=true
spring.jpa.properties.hibernate.order_updates=true

# Flyway Configuration
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true

# Redis Configuration (for caching and real-time features)
spring.redis.host=localhost
spring.redis.port=6379
spring.redis.database=0
spring.redis.password=
spring.redis.timeout=2000ms
spring.redis.jedis.pool.max-active=8
spring.redis.jedis.pool.max-idle=8
spring.redis.jedis.pool.min-idle=0
spring.redis.jedis.pool.max-wait=-1ms

# Cache Configuration
spring.cache.type=redis
spring.cache.redis.time-to-live=600000
spring.cache.redis.cache-null-values=false
```

## Core Entity Schemas

### 1. Security Module Entities

#### User Entity
```java
@Entity
@Table(name = "users", 
    indexes = {
        @Index(name = "idx_user_username", columnList = "username"),
        @Index(name = "idx_user_email", columnList = "email"),
        @Index(name = "idx_user_enabled", columnList = "enabled"),
        @Index(name = "idx_user_account_locked", columnList = "account_locked")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_username", columnNames = "username"),
        @UniqueConstraint(name = "uk_user_email", columnNames = "email")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "username", nullable = false, length = 50)
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;        // Unique username for login
    
    @Column(name = "password", nullable = false)
    @NotBlank(message = "Password is required")
    private String password;        // BCrypt encoded password
    
    @Column(name = "email", nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;          // User email address
    
    @Column(name = "first_name", length = 50)
    private String firstName;      // User first name
    
    @Column(name = "last_name", length = 50)
    private String lastName;       // User last name
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; // Account status
    
    @Column(name = "last_login")
    private Instant lastLogin;     // Time-zone aware timestamp
    
    @Column(name = "login_attempts", nullable = false)
    private Integer loginAttempts = 0;
    
    @Column(name = "account_locked", nullable = false)
    private Boolean accountLocked = false;
    
    @Column(name = "account_locked_until")
    private Instant accountLockedUntil; // Automatic unlock time
    
    @Column(name = "password_expired", nullable = false)
    private Boolean passwordExpired = false;
    
    @Column(name = "password_change_required", nullable = false)
    private Boolean passwordChangeRequired = false;
    
    @Column(name = "password_changed_at")
    private Instant passwordChangedAt; // Track password changes
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // Time-zone aware timestamp
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;     // Time-zone aware timestamp
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Many-to-Many relationship with Role
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"),
        foreignKey = @ForeignKey(name = "fk_user_roles_user"),
        inverseForeignKey = @ForeignKey(name = "fk_user_roles_role")
    )
    private Set<Role> roles = new HashSet<>();
    
    // Helper methods for business logic
    public boolean isAccountNonLocked() {
        if (!accountLocked) return true;
        if (accountLockedUntil != null && Instant.now().isAfter(accountLockedUntil)) {
            accountLocked = false;
            accountLockedUntil = null;
            return true;
        }
        return false;
    }
    
    public void incrementLoginAttempts() {
        this.loginAttempts++;
        if (this.loginAttempts >= 5) { // Configurable threshold
            this.accountLocked = true;
            this.accountLockedUntil = Instant.now().plus(Duration.ofMinutes(30));
        }
    }
    
    public void resetLoginAttempts() {
        this.loginAttempts = 0;
        this.accountLocked = false;
        this.accountLockedUntil = null;
        this.lastLogin = Instant.now();
    }
}
```

**Database Table:**
- Table: `users`
- Indexes: `idx_user_username`, `idx_user_email`, `idx_user_enabled`
- Constraints: Unique constraints on username and email

#### Role Entity
```java
@Entity
@Table(name = "roles", indexes = {
    @Index(name = "idx_role_name", columnList = "name"),
    @Index(name = "idx_role_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;           // Role name (ADMIN, HR_MANAGER, EMPLOYEE)
    
    @Column(name = "description", length = 255)
    private String description;    // Role description
    
    @Column(name = "active", nullable = false)
    private Boolean active = true; // Role status
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Many-to-Many relationship with User
    @ManyToMany(mappedBy = "roles", fetch = FetchType.LAZY)
    private Set<User> users = new HashSet<>();
    
    // Many-to-Many relationship with Resource
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "role_resources",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "resource_id")
    )
    private Set<Resource> resources = new HashSet<>();
}
```

**Database Table:**
- Table: `roles`
- Indexes: `idx_role_name`, `idx_role_active`
- Constraints: Unique constraint on name

#### Resource Entity
```java
public enum ResourceCategory {
    USER,
    EMPLOYEE,
    DEPARTMENT,
    POSITION,
    PAYROLL,
    COMMUNICATION,
    SECURITY,
    OTHER
}

@Entity
@Table(name = "resources", indexes = {
    @Index(name = "idx_resource_url", columnList = "url"),
    @Index(name = "idx_resource_method", columnList = "method"),
    @Index(name = "idx_resource_category", columnList = "category"),
    @Index(name = "idx_resource_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
public class Resource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;           // Resource name
    
    @Column(name = "url", nullable = false, length = 255)
    private String url;            // URL pattern
    
    @Column(name = "method", nullable = false, length = 10)
    private String method;         // HTTP method (GET, POST, PUT, DELETE)
    
    @Column(name = "description", length = 255)
    private String description;    // Resource description
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private ResourceCategory category; // Resource category (USER, EMPLOYEE, DEPARTMENT, etc.)
    
    @Column(name = "active", nullable = false)
    private Boolean active = true; // Resource status
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Many-to-Many relationship with Role
    @ManyToMany(mappedBy = "resources", fetch = FetchType.LAZY)
    private Set<Role> roles = new HashSet<>();
}
```

**Database Table:**
- Table: `resources`
- Indexes: `idx_resource_url`, `idx_resource_method`, `idx_resource_category`, `idx_resource_active`
- Constraints: Composite unique constraint on (url, method)### 
2. Department Management Entities

#### Department Entity
```java
@Entity
@Table(name = "departments", indexes = {
    @Index(name = "idx_department_name", columnList = "name"),
    @Index(name = "idx_department_code", columnList = "code"),
    @Index(name = "idx_department_parent_id", columnList = "parent_id"),
    @Index(name = "idx_department_dep_path", columnList = "dep_path"),
    @Index(name = "idx_department_enabled", columnList = "enabled"),
    @Index(name = "idx_department_manager_id", columnList = "manager_id")
})
@EntityListeners(AuditingEntityListener.class)
public class Department {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;           // Department name
    
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;           // Department code (unique)
    
    @Column(name = "description", length = 500)
    private String description;    // Department description
    
    @Column(name = "location", length = 255)
    private String location;       // Department physical location
    
    @Column(name = "parent_id")
    private Long parentId;         // Parent department ID (null for root)
    
    @Column(name = "dep_path", length = 500)
    private String depPath;        // Hierarchical path (e.g., "/1/2/3")
    
    @Column(name = "is_parent", nullable = false)
    private Boolean isParent = false; // Has child departments
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; // Department status (renamed from active for consistency)
    
    @Column(name = "level")
    private Integer level = 0;     // Hierarchy level (0 for root)
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0; // Display order
    
    @Column(name = "manager_id")
    private Long managerId;        // Department manager (Employee ID)
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Self-referencing relationship for parent department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Department parent;
    
    // One-to-Many relationship for child departments
    @OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private Set<Department> children = new HashSet<>();
    
    // One-to-Many relationship with employees
    @OneToMany(mappedBy = "department", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();
    
    // Transient field for employee count (calculated dynamically)
    @Transient
    private Long employeeCount;
}
```

**Database Table:**
- Table: `departments`
- Indexes: Multiple indexes for efficient querying
- Constraints: Unique constraint on code, foreign key to self for parent_id

### 3. Position Management Entities

#### Position Entity
```java
@Entity
@Table(name = "positions", indexes = {
    @Index(name = "idx_position_job_title", columnList = "job_title"),
    @Index(name = "idx_position_code", columnList = "code"),
    @Index(name = "idx_position_department_id", columnList = "department_id"),
    @Index(name = "idx_position_level", columnList = "level"),
    @Index(name = "idx_position_enabled", columnList = "enabled"),
    @Index(name = "idx_position_category", columnList = "category")
})
@EntityListeners(AuditingEntityListener.class)
public class Position {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "job_title", nullable = false, length = 100)
    private String jobTitle;       // Job title
    
    @Column(name = "professional_title", length = 100)
    private String professionalTitle; // Professional title
    
    @Column(name = "code", unique = true, nullable = false, length = 20)
    private String code;           // Position code (unique)
    
    @Column(name = "description", length = 1000)
    private String description;    // Position description
    
    @Column(name = "requirements", length = 2000)
    private String requirements;   // Job requirements
    
    @Column(name = "responsibilities", length = 2000)
    private String responsibilities; // Job responsibilities
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private PositionCategory category = PositionCategory.TECHNICAL; // TECHNICAL, MANAGEMENT, ADMINISTRATIVE, etc.
    
    @Column(name = "salary_grade", length = 10)
    private String salaryGrade;    // Salary grade/band
    
    @Column(name = "department_id", nullable = false)
    @NotNull(message = "Department is required")
    private Long departmentId;     // Associated department
    
    @Enumerated(EnumType.STRING)
    @Column(name = "level", nullable = false, length = 20)
    private PositionLevel level = PositionLevel.JUNIOR; // JUNIOR, SENIOR, MANAGER, etc.
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; // Position enabled status
    
    @Column(name = "min_salary", precision = 12, scale = 2)
    private BigDecimal minSalary;  // Minimum salary range
    
    @Column(name = "max_salary", precision = 12, scale = 2)
    private BigDecimal maxSalary;  // Maximum salary range
    
    @Column(name = "required_skills", length = 1000)
    private String requiredSkills; // Required skills (JSON or comma-separated)
    
    @Column(name = "required_education", length = 500)
    private String requiredEducation; // Required education level
    
    @Column(name = "required_experience")
    private Integer requiredExperience; // Required years of experience
    
    @Column(name = "benefits", length = 1000)
    private String benefits;       // Position benefits
    
    @Column(name = "work_location", length = 255)
    private String workLocation;   // Work location (OFFICE, REMOTE, HYBRID)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType = EmploymentType.FULL_TIME; // FULL_TIME, PART_TIME, CONTRACT
    
    @Column(name = "is_managerial", nullable = false)
    private Boolean isManagerial = false; // Is this a management position
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // Time-zone aware timestamp
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;     // Time-zone aware timestamp
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Many-to-One relationship with Department
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false)
    private Department department;
    
    // One-to-Many relationship with employees
    @OneToMany(mappedBy = "position", fetch = FetchType.LAZY)
    private Set<Employee> employees = new HashSet<>();
    
    // Transient field for employee count (calculated dynamically)
    @Transient
    private Long employeeCount;
    
    // Business logic methods
    public boolean isWithinSalaryRange(BigDecimal salary) {
        if (salary == null) return true;
        boolean aboveMin = minSalary == null || salary.compareTo(minSalary) >= 0;
        boolean belowMax = maxSalary == null || salary.compareTo(maxSalary) <= 0;
        return aboveMin && belowMax;
    }
}

// Position Category Enum
public enum PositionCategory {
    TECHNICAL,
    MANAGEMENT,
    ADMINISTRATIVE,
    SALES,
    MARKETING,
    FINANCE,
    HR,
    OPERATIONS,
    LEGAL,
    OTHER
}

// Position Level Enum
public enum PositionLevel {
    INTERN,
    JUNIOR,
    SENIOR,
    LEAD,
    MANAGER,
    SENIOR_MANAGER,
    DIRECTOR,
    SENIOR_DIRECTOR,
    VP,
    SVP,
    EXECUTIVE
}
```

**Database Table:**
- Table: `positions`
- Indexes: Multiple indexes for efficient querying
- Constraints: Unique constraint on code, foreign key to departments

### 4. Employee Management Entities

#### Employee Entity
```java
@Entity
@Table(name = "employees", 
    indexes = {
        @Index(name = "idx_employee_number", columnList = "employee_number"),
        @Index(name = "idx_employee_email", columnList = "email"),
        @Index(name = "idx_employee_department_id", columnList = "department_id"),
        @Index(name = "idx_employee_position_id", columnList = "position_id"),
        @Index(name = "idx_employee_manager_id", columnList = "manager_id"),
        @Index(name = "idx_employee_status", columnList = "status"),
        @Index(name = "idx_employee_pay_type", columnList = "pay_type"),
        @Index(name = "idx_employee_hire_date", columnList = "hire_date"),
        @Index(name = "idx_employee_enabled", columnList = "enabled")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_employee_number", columnNames = "employee_number"),
        @UniqueConstraint(name = "uk_employee_email", columnNames = "email")
    }
)
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "employee_number", nullable = false, length = 20)
    @NotBlank(message = "Employee number is required")
    private String employeeNumber; // Unique employee number
    
    @Column(name = "first_name", nullable = false, length = 50)
    @NotBlank(message = "First name is required")
    private String firstName;      // First name
    
    @Column(name = "last_name", nullable = false, length = 50)
    @NotBlank(message = "Last name is required")
    private String lastName;       // Last name
    
    @Column(name = "email", nullable = false, length = 100)
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;          // Email address (unique)
    
    @Column(name = "phone", length = 20)
    private String phone;          // Phone number
    
    @Column(name = "mobile_phone", length = 20)
    private String mobilePhone;    // Mobile phone number
    
    // Address fields (detailed breakdown)
    @Column(name = "address", length = 255)
    private String address;        // Street address
    
    @Column(name = "city", length = 100)
    private String city;           // City
    
    @Column(name = "state", length = 100)
    private String state;          // State/Province
    
    @Column(name = "zip_code", length = 20)
    private String zipCode;        // ZIP/Postal code
    
    @Column(name = "country", length = 100)
    private String country;        // Country
    
    @Column(name = "date_of_birth_encrypted", length = 255)
    private String dateOfBirthEncrypted; // Encrypted date of birth (AES)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 10)
    private Gender gender;         // MALE, FEMALE, OTHER, PREFER_NOT_TO_SAY
    
    @Column(name = "nationality", length = 50)
    private String nationality;    // Nationality
    
    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", length = 20)
    private MaritalStatus maritalStatus; // SINGLE, MARRIED, DIVORCED, WIDOWED
    
    @Column(name = "department_id", nullable = false)
    @NotNull(message = "Department is required")
    private Long departmentId;     // Department assignment
    
    @Column(name = "position_id", nullable = false)
    @NotNull(message = "Position is required")
    private Long positionId;       // Position assignment
    
    @Column(name = "manager_id")
    private Long managerId;        // Direct manager ID
    
    @Column(name = "hire_date", nullable = false)
    @NotNull(message = "Hire date is required")
    private LocalDate hireDate;    // Hire date
    
    @Column(name = "termination_date")
    private LocalDate terminationDate; // Termination date (if applicable)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmployeeStatus status = EmployeeStatus.ACTIVE;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_type", nullable = false, length = 20)
    private EmploymentType employmentType = EmploymentType.FULL_TIME;
    
    // CRITICAL FIX: Support both salaried and hourly employees
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = 10)
    private PayType payType = PayType.SALARY; // SALARY or HOURLY
    
    @Column(name = "salary", precision = 12, scale = 2)
    private BigDecimal salary;     // Annual salary for salaried employees
    
    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private BigDecimal hourlyRate; // Hourly rate for hourly employees
    
    @Column(name = "salary_grade", length = 10)
    private String salaryGrade;    // Salary grade/band
    
    @Enumerated(EnumType.STRING)
    @Column(name = "work_location", length = 20)
    private WorkLocation workLocation = WorkLocation.OFFICE; // OFFICE, REMOTE, HYBRID
    
    // Encrypted sensitive fields (AES encryption)
    @Column(name = "bank_account_encrypted", length = 255)
    private String bankAccountEncrypted; // Bank account details (encrypted)
    
    @Column(name = "tax_id_encrypted", length = 255)
    private String taxIdEncrypted;       // Tax identification (encrypted)
    
    // Skills and qualifications
    @Column(name = "skills", columnDefinition = "TEXT")
    private String skills;         // Skills (JSON format)
    
    @Column(name = "education", length = 1000)
    private String education;      // Education background
    
    @Column(name = "certifications", length = 1000)
    private String certifications; // Professional certifications
    
    @Column(name = "notes", length = 2000)
    private String notes;          // Additional notes
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true; // Account enabled status
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // Time-zone aware timestamp
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;     // Time-zone aware timestamp
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    // Profile image URL
    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;
    
    // Emergency contact information
    @Column(name = "emergency_contact_name", length = 100)
    private String emergencyContactName;
    
    @Column(name = "emergency_contact_phone", length = 20)
    private String emergencyContactPhone;
    
    @Column(name = "emergency_contact_relation", length = 50)
    private String emergencyContactRelation;
    
    // Relationships with proper foreign key constraints
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_employee_department"))
    private Department department;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_employee_position"))
    private Position position;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id", insertable = false, updatable = false,
                foreignKey = @ForeignKey(name = "fk_employee_manager"))
    private Employee manager;
    
    @OneToMany(mappedBy = "manager", fetch = FetchType.LAZY)
    private Set<Employee> directReports = new HashSet<>();
    
    @OneToMany(mappedBy = "employee", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<PayrollLedger> payrollLedgers = new HashSet<>();
    
    // Business logic methods
    public String getFullName() {
        return firstName + " " + lastName;
    }
    
    public boolean isActive() {
        return status == EmployeeStatus.ACTIVE && enabled;
    }
    
    public boolean isSalaried() {
        return payType == PayType.SALARY;
    }
    
    public boolean isHourly() {
        return payType == PayType.HOURLY;
    }
    
    // Validation method for salary range
    public void validateSalaryAgainstPosition(Position position) {
        if (isSalaried() && salary != null && position != null) {
            if (position.getMinSalary() != null && salary.compareTo(position.getMinSalary()) < 0) {
                throw new ValidationException("Salary below minimum range for position");
            }
            if (position.getMaxSalary() != null && salary.compareTo(position.getMaxSalary()) > 0) {
                throw new ValidationException("Salary above maximum range for position");
            }
        }
    }
}

// Employee Status Enum
public enum EmployeeStatus {
    ACTIVE,
    INACTIVE,
    TERMINATED,
    ON_LEAVE,
    SUSPENDED
}

// Employment Type Enum
public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
    INTERN,
    CONSULTANT
}

// Pay Type Enum (CRITICAL FIX for payroll support)
public enum PayType {
    SALARY,    // Annual salary
    HOURLY     // Hourly rate
}

// Gender Enum
public enum Gender {
    MALE,
    FEMALE,
    OTHER,
    PREFER_NOT_TO_SAY
}

// Marital Status Enum
public enum MaritalStatus {
    SINGLE,
    MARRIED,
    DIVORCED,
    WIDOWED,
    SEPARATED
}

// Work Location Enum
public enum WorkLocation {
    OFFICE,
    REMOTE,
    HYBRID
}
```

**Database Table:**
- Table: `employees`
- Indexes: Multiple indexes for efficient querying
- Constraints: Unique constraints on employee_number and email
- Security: Sensitive fields (dateOfBirth, bankAccount, taxId) should be encrypted##
# 5. Communication System Entities

#### Email Template Entity
```java
@Entity
@Table(name = "email_templates", indexes = {
    @Index(name = "idx_email_template_name", columnList = "name"),
    @Index(name = "idx_email_template_code", columnList = "code"),
    @Index(name = "idx_email_template_category", columnList = "category"),
    @Index(name = "idx_email_template_active", columnList = "active")
})
@EntityListeners(AuditingEntityListener.class)
public class EmailTemplate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name;           // Template name
    
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;           // Template code (unique)
    
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;        // Email subject template
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;        // Email content (unified field)
    
    @Enumerated(EnumType.STRING)
    @Column(name = "template_type", nullable = false, length = 10)
    private TemplateType templateType = TemplateType.HTML; // HTML, TEXT, MIXED
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private TemplateCategory category; // Template category
    
    @Column(name = "enabled", nullable = false)
    private Boolean enabled = true;
    
    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;
    
    @Column(name = "variables", columnDefinition = "TEXT")
    private String variables;      // JSON array of template variables
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private Long createdBy;        // User who created template
    
    @Column(name = "updated_by")
    private Long updatedBy;
    
    @OneToMany(mappedBy = "template", fetch = FetchType.LAZY)
    private Set<EmailLog> emailLogs = new HashSet<>();
}

// Template Type Enum
public enum TemplateType {
    HTML,
    TEXT,
    MIXED
}
```

#### Email Log Entity
```java
@Entity
@Table(name = "email_logs", indexes = {
    @Index(name = "idx_email_log_template_code", columnList = "template_code"),
    @Index(name = "idx_email_log_to_email", columnList = "to_email"),
    @Index(name = "idx_email_log_status", columnList = "status"),
    @Index(name = "idx_email_log_sent_at", columnList = "sent_at"),
    @Index(name = "idx_email_log_sent_by", columnList = "sent_by")
})
@EntityListeners(AuditingEntityListener.class)
public class EmailLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "template_code", length = 50)
    private String templateCode;   // Template code used (instead of ID for flexibility)
    
    @Column(name = "to_email", nullable = false, length = 255)
    private String toEmail;        // Primary recipient email
    
    @Column(name = "cc_emails", length = 1000)
    private String ccEmails;       // CC recipients (comma-separated)
    
    @Column(name = "bcc_emails", length = 1000)
    private String bccEmails;      // BCC recipients (comma-separated)
    
    @Column(name = "subject", nullable = false, length = 255)
    private String subject;        // Actual subject sent
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;        // Actual content sent
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailStatus status = EmailStatus.PENDING; // PENDING, SENT, FAILED, BOUNCED
    
    @Column(name = "sent_at")
    private Instant sentAt;  // When email was sent
    
    @Column(name = "error_message", length = 1000)
    private String errorMessage;   // Error details if failed
    
    @Column(name = "retry_count")
    private Integer retryCount = 0; // Number of retry attempts
    
    @Column(name = "sent_by")
    private Long sentBy;           // User who sent email
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    // Relationship with template (optional, as template might be deleted)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private EmailTemplate template;
}

// Email Status Enum
public enum EmailStatus {
    PENDING,
    SENT,
    FAILED,
    BOUNCED,
    DELIVERED,
    OPENED,
    CLICKED
}
```

#### Chat Message Entity
```java
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_chatmessage_room_id", columnList = "room_id"),
    @Index(name = "idx_chatmessage_sender_id", columnList = "sender_id"),
    @Index(name = "idx_chatmessage_created_at", columnList = "created_at")
})
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;
      
    @Column(name = "sender_id", nullable = false)
    private Long senderId;         // Message sender
      
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;        // Message content
      
    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", length = 20)
    private ChatMessageType messageType;    // Mapped from ChatMessageType enum (TEXT, IMAGE, FILE, SYSTEM)
      
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // Time-zone aware timestamp
      
    @Column(name = "is_edited", nullable = false)
    private Boolean isEdited = false;
      
    @Column(name = "edited_at")
    private Instant editedAt;      // Time-zone aware timestamp
      
    @Column(name = "reply_to_id")
    private Long replyToId;        // Reply to message ID
      
    @Column(name = "attachment_url", length = 500)
    private String attachmentUrl;
      
    @Column(name = "attachment_type", length = 100)
    private String attachmentType;
      
    @Column(name = "attachment_size")
    private Long attachmentSize;
      
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
      
    @Column(name = "deleted_at")
    private Instant deletedAt;     // Time-zone aware timestamp
      
    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;       // JSON
      
    @Column(name = "read_receipts", columnDefinition = "TEXT")
    private String readReceipts;   // JSON
    }
```

**Note:** Chat messages are stored in PostgreSQL to ensure data integrity and support complex queries.

#### Chat Room Entity
```java
@Entity
@Table(name = "chat_rooms", indexes = {
    @Index(name = "idx_chatroom_name", columnList = "name"),
    @Index(name = "idx_chatroom_type", columnList = "type"),
    @Index(name = "idx_chatroom_created_by", columnList = "created_by")
})
@EntityListeners(AuditingEntityListener.class)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @Column(name = "name", length = 100)
    private String name;           // Room name
  
    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ChatRoomType type;     // DIRECT, GROUP, CHANNEL
  
    @Column(name = "description", length = 500)
    private String description;
  
    @Column(name = "created_by")
    private Long createdBy;
  
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
  
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // Time-zone aware timestamp
  
    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;     // Time-zone aware timestamp
  
    @Column(name = "last_message_at")
    private Instant lastMessageAt; // Time-zone aware timestamp
  
    @Column(name = "is_private", nullable = false)
    private Boolean isPrivate = false;
  
    @Column(name = "max_participants")
    private Integer maxParticipants;
  
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatMessage> messages = new HashSet<>();
    
    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ChatParticipant> participants = new HashSet<>();
}

public enum ChatRoomType {
    DIRECT,
    GROUP,
    CHANNEL
}

#### ChatParticipant Entity
```java
@Entity
@Table(name = "chat_participants", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"room_id", "user_id"}, name = "uk_participant_room_user")
})
public class ChatParticipant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private ChatParticipantRole role; // OWNER, ADMIN, MEMBER
    
    @Column(name = "joined_at", nullable = false)
    private Instant joinedAt;
    
    @Column(name = "last_read_at")
    private Instant lastReadAt;
    
    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;
    
    @Column(name = "is_muted", nullable = false)
    private boolean isMuted = false;
    
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;
    
    @Column(name = "left_at")
    private Instant leftAt;
}
```

#### Notification Entity
```java
@Entity
@Table(name = "notifications", indexes = {
    @Index(name = "idx_notification_user_id", columnList = "user_id"),
    @Index(name = "idx_notification_type", columnList = "type"),
    @Index(name = "idx_notification_read_status", columnList = "read_status")
})
@EntityListeners(AuditingEntityListener.class)
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
  
    @Column(name = "user_id", nullable = false)
    private Long userId;           // Target user
  
    @Column(name = "type", length = 50)
    private String type;           // SYSTEM, EMAIL, CHAT, TASK, etc.
  
    @Column(name = "title", length = 255)
    private String title;
  
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;
  
    @Column(name = "data", columnDefinition = "TEXT")
    private String data;           // Additional JSON data
  
    @Column(name = "read_status", nullable = false)
    private Boolean read = false;
  
    @Column(name = "priority", length = 20)
    private String priority;       // LOW, MEDIUM, HIGH, URGENT
  
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;     // Time-zone aware timestamp
  
    @Column(name = "read_at")
    private Instant readAt;        // Time-zone aware timestamp
  
    @Column(name = "expires_at")
    private Instant expiresAt;     // Time-zone aware timestamp
  
    @Column(name = "action_url", length = 500)
    private String actionUrl;
  
    @Column(name = "related_entity_id")
    private Long relatedEntityId;
  
    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;
}
```
### 6. Payroll Management Entities

### PayrollLedger Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import com.example.demo.employee.entity.Employee;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_ledgers", indexes = {
    @Index(name = "idx_payrollledger_employee_id", columnList = "employee_id"),
    @Index(name = "idx_payrollledger_period_id", columnList = "payroll_period_id"),
    @Index(name = "idx_payrollledger_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(name = "payroll_period_id", nullable = false)
    private Long payrollPeriodId;

    @Column(name = "employee_number", length = 20)
    private String employeeNumber;

    @Column(name = "employee_name", length = 100)
    private String employeeName;

    @Column(name = "department_id")
    private Long departmentId;

    @Column(name = "department_name", length = 100)
    private String departmentName;

    @Column(name = "position_id")
    private Long positionId;

    @Column(name = "position_name", length = 100)
    private String positionName;

    @Column(name = "base_salary", precision = 12, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "hourly_rate", precision = 12, scale = 2)
    private BigDecimal hourlyRate;

    @Column(name = "hours_worked", precision = 10, scale = 2)
    private BigDecimal hoursWorked;

    @Column(name = "overtime_hours", precision = 10, scale = 2)
    private BigDecimal overtimeHours;

    @Column(name = "overtime_rate", precision = 12, scale = 2)
    private BigDecimal overtimeRate;

    @Column(name = "total_allowances", precision = 12, scale = 2)
    private BigDecimal totalAllowances;

    @Column(name = "total_deductions", precision = 12, scale = 2)
    private BigDecimal totalDeductions;

    @Column(name = "gross_salary", precision = 12, scale = 2)
    private BigDecimal grossSalary;

    @Column(name = "net_salary", precision = 12, scale = 2)
    private BigDecimal netSalary;

    @Column(name = "employer_contributions", precision = 12, scale = 2)
    private BigDecimal employerContributions;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "currency", length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 20)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollLedgerStatus status;

    @Column(name = "pay_date")
    private LocalDate payDate;

    @Lob
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;

    @Lob
    @Column(name = "calculation_details", columnDefinition = "TEXT")
    private String calculationDetails; // JSON string for all components
}
```

> **关键业务规则：薪资记录快照**
>
> `PayrollLedger` 实体中的以下字段必须被视为 **一次性创建的快照（Snapshot）**，以确保历史薪资报告的永久准确性：
>
> *   `employeeName`
> *   `departmentName`
> *   `positionName`
> *   `baseSalary`, `hourlyRate`, 等所有与薪资计算相关的数值字段
>
> **实现要求**：
> 1.  在创建一条新的 `PayrollLedger` 记录的业务逻辑（例如在 `PayrollService` 中）时，必须在一个事务内，从关联的 `Employee`, `Department`, `Position` 等实体中获取当前的名称和值，并填充到这些快照字段中。
> 2.  一旦 `PayrollLedger` 记录被创建，这些快照字段 **绝对不能** 因为源实体信息（如员工改名、部门调动）的变更而被更新。它们必须永久保留创建时刻的状态。
> 3.  此规则是确保系统财务审计合规性的核心要求。

### PayrollPeriod Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "payroll_periods", indexes = {
    @Index(name = "idx_payrollperiod_start_end", columnList = "start_date, end_date"),
    @Index(name = "idx_payrollperiod_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollPeriod {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PayrollPeriodType type;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDate endDate;

    @Column(name = "pay_date", nullable = false)
    private LocalDate payDate;

    @Column(name = "working_days")
    private Integer workingDays;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PayrollPeriodStatus status;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "updated_by")
    private Long updatedBy;
}
```

### SalaryComponent Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "salary_components", indexes = {
    @Index(name = "idx_salarycomponent_code", columnList = "code", unique = true),
    @Index(name = "idx_salarycomponent_type", columnList = "type")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SalaryComponent {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private SalaryComponentType type; // ALLOWANCE, DEDUCTION

    @Enumerated(EnumType.STRING)
    @Column(name = "calculation_type", nullable = false, length = 20)
    private CalculationType calculationType; // FIXED, PERCENTAGE

    @Column(name = "value", precision = 12, scale = 2)
    private BigDecimal value; // Amount for FIXED, percentage for PERCENTAGE

    @Column(name = "is_taxable", nullable = false)
    private boolean isTaxable = false;

    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private Instant updatedAt;
}
```

### PayrollAudit Entity
```java
package com.example.demo.payroll.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "payroll_audits", indexes = {
    @Index(name = "idx_payrollaudit_ledger_id", columnList = "payroll_ledger_id"),
    @Index(name = "idx_payrollaudit_performed_by", columnList = "performed_by")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class PayrollAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payroll_ledger_id", nullable = false)
    private Long payrollLedgerId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Lob
    @Column(name = "details", columnDefinition = "TEXT")
    private String details; // JSON string of changes

    @Column(name = "reason", length = 500)
    private String reason;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @CreatedDate
    @Column(name = "performed_at", nullable = false, updatable = false)
    private Instant performedAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;
}
```

## Relationship Management

### Many-to-Many Relationships

Many-to-many relationships like `User-Role` and `Role-Resource` are managed directly via JPA's `@ManyToMany` and `@JoinTable` annotations within the `User` and `Role` entities. This ensures that the relationships are stored in PostgreSQL junction tables (`user_roles`, `role_resources`), maintaining full ACID compliance and referential integrity.

The previous design of storing these critical relationships in Redis (`@RedisHash`) was a severe design flaw and has been removed. The correct implementation is already defined in the `User` and `Role` entities.

## Caching Strategy

### Cache Configurations

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    // User permissions cache - 30 minutes TTL
    @Bean
    public RedisCacheConfiguration userPermissionsCache() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(30))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()));
    }
    
    // Department tree cache - 1 hour TTL
    @Bean
    public RedisCacheConfiguration departmentTreeCache() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofHours(1));
    }
    
    // Employee search cache - 15 minutes TTL
    @Bean
    public RedisCacheConfiguration employeeSearchCache() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(15));
    }
}
```

### Cache Keys Pattern

```
Cache Key Patterns:
- User permissions: cache:user_permissions:{userId}
- Department tree: cache:department_tree:all
- Employee search: cache:employee_search:{searchHash}
- Position list: cache:positions:department:{departmentId}
- Payroll summary: cache:payroll_summary:{employeeId}:{period}
```#
# Data Initialization and Seeding

### Initial Data Requirements

#### Default Roles
```java
// System default roles to be created on startup
public enum DefaultRoles {
    SUPER_ADMIN("Super Administrator", "Full system access"),
    ADMIN("Administrator", "System administration access"),
    HR_MANAGER("HR Manager", "Human resources management"),
    DEPARTMENT_MANAGER("Department Manager", "Department management"),
    EMPLOYEE("Employee", "Basic employee access");
}
```

#### Default Resources
```java
// System resources for permission management
public class DefaultResources {
    // User Management
    USER_CREATE("/api/users", "POST", "Create new user"),
    USER_READ("/api/users/**", "GET", "View user information"),
    USER_UPDATE("/api/users/**", "PUT", "Update user information"),
    USER_DELETE("/api/users/**", "DELETE", "Delete user"),
    
    // Employee Management
    EMPLOYEE_CREATE("/api/employees", "POST", "Create new employee"),
    EMPLOYEE_READ("/api/employees/**", "GET", "View employee information"),
    EMPLOYEE_UPDATE("/api/employees/**", "PUT", "Update employee information"),
    EMPLOYEE_DELETE("/api/employees/**", "DELETE", "Delete employee"),
    EMPLOYEE_IMPORT("/api/employees/import", "POST", "Import employees"),
    EMPLOYEE_EXPORT("/api/employees/export", "GET", "Export employees"),
    
    // Department Management
    DEPARTMENT_CREATE("/api/departments", "POST", "Create department"),
    DEPARTMENT_READ("/api/departments/**", "GET", "View departments"),
    DEPARTMENT_UPDATE("/api/departments/**", "PUT", "Update department"),
    DEPARTMENT_DELETE("/api/departments/**", "DELETE", "Delete department"),
    
    // Payroll Management
    PAYROLL_CREATE("/api/payroll/**", "POST", "Create payroll"),
    PAYROLL_READ("/api/payroll/**", "GET", "View payroll"),
    PAYROLL_UPDATE("/api/payroll/**", "PUT", "Update payroll"),
    PAYROLL_PROCESS("/api/payroll/process", "POST", "Process payroll");
}
```

### Data Migration Scripts

#### Version 1.0 - Initial Schema
```java
@Component
public class DatabaseInitializer implements ApplicationRunner {
    
    @Override
    public void run(ApplicationArguments args) throws Exception {
        // Create default roles
        createDefaultRoles();
        
        // Create default resources
        createDefaultResources();
        
        // Create default admin user
        createDefaultAdminUser();
        
        // Create default salary components
        createDefaultSalaryComponents();
        
        // Create default email templates
        createDefaultEmailTemplates();
    }
}
```

## Performance Optimization

### Indexing Strategy

```java
// Custom indexes for complex queries
@Configuration
public class RedisIndexConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // Configure serializers
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());
        
        return template;
    }
    
    // Create composite indexes for complex queries
    @PostConstruct
    public void createIndexes() {
        // Employee search index
        createEmployeeSearchIndex();
        
        // Department hierarchy index
        createDepartmentHierarchyIndex();
        
        // Payroll period index
        createPayrollPeriodIndex();
    }
}
```

### Query Optimization Patterns

```java
// Efficient query patterns for Redis
public class QueryOptimizationPatterns {
    
    // Use pipeline for batch operations
    public List<Employee> findEmployeesByIds(Set<Long> ids) {
        return redisTemplate.executePipelined(connection -> {
            ids.forEach(id -> connection.get(("employees:" + id).getBytes()));
            return null;
        });
    }
    
    // **Architectural Mandate:** Caching layers are only to be updated or invalidated *after* a successful transaction in the primary PostgreSQL database.
    // The following Lua script example is removed as it violates this principle.
}
```

## Backup and Recovery

### Backup Strategy

```yaml
# Redis backup configuration
redis:
  backup:
    enabled: true
    schedule: "0 2 * * *"  # Daily at 2 AM
    retention: 30          # Keep 30 days of backups
    location: "/backup/redis"
    compression: true
```

### Data Recovery Procedures

```java
@Service
public class DataRecoveryService {
    
    public void createBackup(String backupName) {
        // Create Redis snapshot
        redisTemplate.execute(connection -> {
            connection.bgSave();
            return null;
        });
    }
    
    public void restoreFromBackup(String backupFile) {
        // Restore from Redis dump file
        // Implementation depends on deployment strategy
    }
    
    public void validateDataIntegrity() {
        // Validate referential integrity
        validateUserRoleReferences();
        validateEmployeeDepartmentReferences();
        validatePayrollEmployeeReferences();
    }
}
```

## Monitoring and Metrics

### Database Monitoring

```java
@Component
public class DatabaseMetrics {
    
    private final MeterRegistry meterRegistry;
    private final RedisTemplate redisTemplate;
    
    @EventListener
    public void handleDatabaseOperation(DatabaseOperationEvent event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("database.operation")
            .tag("operation", event.getOperation())
            .tag("entity", event.getEntityType())
            .register(meterRegistry));
    }
    
    @Scheduled(fixedRate = 60000) // Every minute
    public void collectRedisMetrics() {
        RedisInfo info = (RedisInfo) redisTemplate.execute(connection -> {
            return connection.info();
        });
        
        // Collect memory usage, connections, operations/sec
        meterRegistry.gauge("redis.memory.used", info.getUsedMemory());
        meterRegistry.gauge("redis.connections.connected", info.getConnectedClients());
    }
}
```

## Security and Compliance

### Field-Level Encryption

To address GDPR, CCPA, and other privacy regulations, sensitive PII fields must be encrypted at rest:

```java
@Configuration
public class EncryptionConfig {
    
    @Bean
    public AESUtil aesUtil() {
        return new AESUtil(encryptionKey());
    }
    
    private String encryptionKey() {
        // Load from secure key management system
        return System.getenv("DB_ENCRYPTION_KEY");
    }
}

// Custom JPA converter for encrypted fields
@Converter
public class EncryptedStringConverter implements AttributeConverter<String, String> {
    
    @Autowired
    private AESUtil aesUtil;
    
    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null) return null;
        return aesUtil.encrypt(attribute);
    }
    
    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null) return null;
        return aesUtil.decrypt(dbData);
    }
}

// Usage in entities
@Entity
public class Employee {
    // ... other fields
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "date_of_birth")
    private String dateOfBirth; // Encrypted
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "bank_account")
    private String bankAccount; // Encrypted
    
    @Convert(converter = EncryptedStringConverter.class)
    @Column(name = "tax_id")
    private String taxId; // Encrypted
}
```

### Audit Trail Configuration

```java
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class AuditConfig {
    
    @Bean
    public AuditorAware<Long> auditorProvider() {
        return new SecurityAuditorAware();
    }
}

@Component
public class SecurityAuditorAware implements AuditorAware<Long> {
    
    @Override
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return Optional.of(userPrincipal.getId());
    }
}
```

### Permission-Based Data Access

```java
// Repository with permission-based filtering
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // Department managers can only see employees in their department
    @Query("SELECT e FROM Employee e WHERE e.departmentId = :departmentId OR :hasGlobalAccess = true")
    Page<Employee> findByDepartmentWithPermission(
        @Param("departmentId") Long departmentId,
        @Param("hasGlobalAccess") boolean hasGlobalAccess,
        Pageable pageable
    );
    
    // HR can see all, managers can see their direct reports
    @Query("SELECT e FROM Employee e WHERE e.managerId = :managerId OR :hasHRAccess = true")
    List<Employee> findDirectReportsWithPermission(
        @Param("managerId") Long managerId,
        @Param("hasHRAccess") boolean hasHRAccess
    );
}
```

## Database Migration Scripts

### Flyway Migration Structure

```
src/main/resources/db/migration/
├── V1__Initial_schema.sql
├── V2__Add_encryption_support.sql
├── V3__Add_audit_fields.sql
├── V4__Create_indexes.sql
├── V5__Insert_default_data.sql
└── V6__Add_permission_constraints.sql
```

### Sample Migration Script (V1__Initial_schema.sql)

```sql
-- Users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    enabled BOOLEAN NOT NULL DEFAULT true,
    last_login TIMESTAMP,
    login_attempts INTEGER DEFAULT 0,
    account_locked BOOLEAN DEFAULT false,
    password_expired BOOLEAN DEFAULT false,
    password_change_required BOOLEAN DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Roles table
CREATE TABLE roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    description VARCHAR(255),
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- Resources table
CREATE TABLE resources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    url VARCHAR(255) NOT NULL,
    method VARCHAR(10) NOT NULL,
    description VARCHAR(255),
    category VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    UNIQUE(url, method)
);

-- User-Role junction table
CREATE TABLE user_roles (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    expires_at TIMESTAMP,
    active BOOLEAN DEFAULT true,
    PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE
);

-- Role-Resource junction table
CREATE TABLE role_resources (
    role_id BIGINT NOT NULL,
    resource_id BIGINT NOT NULL,
    assigned_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    assigned_by BIGINT,
    active BOOLEAN DEFAULT true,
    PRIMARY KEY (role_id, resource_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (resource_id) REFERENCES resources(id) ON DELETE CASCADE
);

-- Departments table
CREATE TABLE departments (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(20) UNIQUE NOT NULL,
    description VARCHAR(500),
    location VARCHAR(255),
    parent_id BIGINT,
    dep_path VARCHAR(500),
    is_parent BOOLEAN NOT NULL DEFAULT false,
    enabled BOOLEAN NOT NULL DEFAULT true,
    level INTEGER DEFAULT 0,
    sort_order INTEGER DEFAULT 0,
    manager_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (parent_id) REFERENCES departments(id)
);

-- Continue with other tables...
```

## Performance Optimization

### Database Indexes Strategy

```sql
-- Composite indexes for common query patterns
CREATE INDEX idx_employee_dept_status ON employees(department_id, status);
CREATE INDEX idx_employee_manager_active ON employees(manager_id, enabled);
CREATE INDEX idx_payroll_employee_period ON payroll_ledgers(employee_id, pay_period_id);
CREATE INDEX idx_email_log_status_date ON email_logs(status, sent_at);

-- Partial indexes for active records
CREATE INDEX idx_active_employees ON employees(department_id) WHERE enabled = true;
CREATE INDEX idx_active_departments ON departments(parent_id) WHERE enabled = true;

-- Full-text search indexes
CREATE INDEX idx_employee_search ON employees USING gin(to_tsvector('english', first_name || ' ' || last_name || ' ' || email));

-- Announcements Table
CREATE TABLE announcements (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content TEXT NOT NULL,
    author_id BIGINT NOT NULL,
    target_audience VARCHAR(50),
    department_id BIGINT,
    publish_date DATE,
    expiry_date DATE,
    published BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

### Query Optimization Patterns

```java
// Use projections for list views to reduce data transfer
public interface EmployeeListProjection {
    Long getId();
    String getEmployeeNumber();
    String getFirstName();
    String getLastName();
    String getEmail();
    String getDepartmentName();
    String getPositionTitle();
    EmployeeStatus getStatus();
}

@Query("SELECT e.id as id, e.employeeNumber as employeeNumber, " +
       "e.firstName as firstName, e.lastName as lastName, e.email as email, " +
       "d.name as departmentName, p.jobTitle as positionTitle, e.status as status " +
       "FROM Employee e LEFT JOIN Department d ON e.departmentId = d.id " +
       "LEFT JOIN Position p ON e.positionId = p.id " +
       "WHERE (:departmentId IS NULL OR e.departmentId = :departmentId) " +
       "AND (:status IS NULL OR e.status = :status)")
Page<EmployeeListProjection> findEmployeeList(
    @Param("departmentId") Long departmentId,
    @Param("status") EmployeeStatus status,
    Pageable pageable
);
```

## Monitoring and Health Checks

### Database Health Monitoring

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                    .withDetail("database", "PostgreSQL")
                    .withDetail("status", "Connected")
                    .build();
            }
        } catch (SQLException e) {
            return Health.down()
                .withDetail("database", "PostgreSQL")
                .withDetail("error", e.getMessage())
                .build();
        }
        return Health.down().build();
    }
}
```

This comprehensive database design document provides:

1. **Hybrid Architecture** - PostgreSQL for persistent data, Redis for caching and real-time features
2. **Complete Entity Schemas** - All entities with proper JPA annotations and relationships
3. **Security & Compliance** - Field-level encryption, audit trails, and permission-based access
4. **Data Migration** - Flyway scripts for database versioning
5. **Performance Optimization** - Strategic indexing and query optimization
6. **Monitoring** - Database health checks and metrics
7. **Conflict Resolution** - Addresses all entity field mismatches identified in the conflict analysis

The design resolves all conflicts identified in the conflict analysis and provides a robust, secure, and scalable foundation for the Spring Boot implementation.