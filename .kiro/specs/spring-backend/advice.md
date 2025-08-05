好的，遵照您的指示，以下是针对已发现的设计冲突和缺陷提供的具体、可执行的修改建议。这些修改旨在统一设计、消除冲突，并为开发团队提供清晰、一致的指导。

### 修改方案总览

核心修改原则是：
1.  **确立单一数据源**：所有核心业务实体（如用户、员工、部门、薪资）的最终定义以 `database-design.md` 为准，但前提是先根据以下建议修正 `database-design.md` 本身的冲突。
2.  **统一技术选型**：严格遵守“PostgreSQL为主，Redis为辅”的架构原则。
3.  **统一设计规范**：在整个项目中推行一致的字段命名和数据类型标准（特别是时间戳）。

---

### 1. 解决【薪资模块】的数据库策略冲突

这是最严重的冲突，必须首先解决。

**问题**：`payroll-implementation.md` 将薪资实体定义为JPA实体（存入PostgreSQL），而 `database-design.md` 将其定义为Redis实体。

**解决方案**：薪资数据是核心事务数据，必须存储在PostgreSQL中。

**具体修改指令**：

1.  **修改文件**: `database-design.md`
2.  **定位到**: "Section 6. Payroll Management Entities"
3.  **执行操作**:
    *   **删除所有Redis注解**: 移除 `PayrollLedger`, `PayPeriod`, 和 `SalaryComponent` 三个类定义中的所有 `@RedisHash` 和 `@Indexed` 注解。
    *   **替换为JPA注解**: 将这些类定义修改为标准的JPA实体。可以直接**复制 `payroll-implementation.md` 文件中对应的实体定义**来替换，因为那里的定义是正确的（使用了 `@Entity`, `@Table`, `@Id`, `@Column` 等）。

**示例 (`PayrollLedger` 的修改)**：

**修改前 (在 `database-design.md` 中)**:
```java
@RedisHash("payroll_ledgers")
public class PayrollLedger {
    @Id
    private Long id;
    @Indexed
    private Long employeeId;
    // ...
}
```

**修改后 (在 `database-design.md` 中)**:```java
@Entity
@Table(name = "payroll_ledgers", indexes = { /* ... */ })
@Getter
@Setter
// ...
public class PayrollLedger {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    // ... 完整定义参考 payroll-implementation.md
}
```

---

### 2. 解决【Employee实体】的不一致问题

**问题**：`employee-implementation.md` 中的 `Employee` 实体缺少支持薪资计算的关键字段 `payType` 和 `hourlyRate`。

**解决方案**：以 `database-design.md` 中更完整的 `Employee` 定义为标准，更新 `employee-implementation.md`。

**具体修改指令**：

1.  **修改文件**: `employee-implementation.md`
2.  **定位到**: "Employee Entity" (`Employee.java` 的代码块)
3.  **执行操作**:
    *   在 `Employee` 类中添加 `payType` 和 `hourlyRate` 字段。
    *   在 `com.example.demo.employee.entity` 包下（或新建一个 `enums` 子包）添加 `PayType` 枚举的定义。

**需要添加的代码**:

在 `Employee.java` 中添加以下字段：
```java
// ... 在 employmentType 字段之后添加

    // CRITICAL FIX: Support both salaried and hourly employees
    @Enumerated(EnumType.STRING)
    @Column(name = "pay_type", nullable = false, length = 10)
    private PayType payType = PayType.SALARY; // SALARY or HOURLY

    @Column(name = "hourly_rate", precision = 8, scale = 2)
    private BigDecimal hourlyRate; // Hourly rate for hourly employees
```

在 `employee-implementation.md` 文件中新增 `PayType` 枚举的定义：
```java
package com.example.demo.employee.entity;

// Pay Type Enum (CRITICAL FIX for payroll support)
public enum PayType {
    SALARY,    // Annual salary
    HOURLY     // Hourly rate
}
```

---

### 3. 解决【时间戳】的系统性不一致问题

**问题**：项目中大量实体错误地使用了 `LocalDateTime`，导致时区问题。

**解决方案**：在所有JPA实体中，将所有用于记录时间点的数据库字段的数据类型从 `LocalDateTime` 统一修改为 `java.time.Instant`。

**具体修改指令**：

1.  **执行一项全局修改**: 审查以下所有文件中的JPA实体定义，将所有 `LocalDateTime` 类型的字段（如 `createdAt`, `updatedAt`, `lastLogin`, `sentAt` 等）修改为 `Instant` 类型。

2.  **受影响的文件列表**:
    *   `security-implementation.md` (Role, Resource)
    *   `department-implementation.md` (Department)
    *   `employee-implementation.md` (Employee)
    *   `communication-implementation.md` (EmailTemplate, EmailLog, ChatRoom, ChatParticipant)
    *   `database-design.md` (Role, Resource, Department, EmailTemplate, EmailLog)
    *   `design.md` (所有实体定义)

**示例 (在任何受影响的实体中)**：

**修改前**:
```java
@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;
```

**修改后**:
```java
@CreatedDate
@Column(name = "created_at", nullable = false, updatable = false)
private Instant createdAt;
```

---

### 4. 解决【EmailTemplate实体】的字段冲突

**问题**：`communication-implementation.md` 和 `database-design.md` 中 `EmailTemplate` 实体的字段定义不匹配。

**解决方案**：选择一个更合理的定义并统一。`communication-implementation.md` 中的定义（`isDefault`, `enabled`）更清晰。

**具体修改指令**：

1.  **修改文件**: `database-design.md`
2.  **定位到**: "Email Template Entity" (`EmailTemplate.java` 的代码块)
3.  **执行操作**:
    *   将 `private Boolean active = true;` 字段重命名为 `enabled`：
        ```java
        @Column(name = "enabled", nullable = false)
        private Boolean enabled = true;
        ```
    *   添加缺失的 `isDefault` 字段：
        ```java
        @Column(name = "is_default", nullable = false)
        private boolean isDefault = false;
        ```

---

### 5. 解决【薪资快照】的设计缺陷

**问题**：`PayrollLedger` 中存储的 `employeeName` 和 `departmentName` 在源数据更新时不会同步，导致历史报表不准确。

**解决方案**：保留快照字段以满足历史报表需求，但必须在业务逻辑中明确其填充机制和只读属性。

**具体修改指令**：

1.  **修改所有相关文件**: `payroll-implementation.md`, `design.md`, `database-design.md`
2.  **定位到**: `PayrollLedger` 实体定义。
3.  **执行操作**:
    *   **保留快照字段**: 确认 `employeeName`, `departmentName`, `positionName` 等字符串字段存在。
    *   **确认ID字段**: 确保 `employeeId`, `departmentId`, `positionId` 等外键ID字段也存在，并作为关联的主要依据。
    *   **在业务逻辑中明确**: 在 `tasks.md` 或相关的服务层设计中，添加一条明确的业务规则：
        > "在创建 `PayrollLedger` 记录时，`PayrollService` 必须在事务中获取当前员工、部门、职位等关联实体的名称，并将这些名称作为**一次性快照**存入 `employeeName`, `departmentName` 等相应字段。这些快照字段在记录创建后**不应再被自动更新**，以确保薪资报表的历史准确性。"

---

### 修改摘要清单

| 文件 (File) | 实体/部分 (Entity/Section) | 具体修改指令 (Specific Modification Instruction) |
| :--- | :--- | :--- |
| `database-design.md` | `PayrollLedger`, `PayPeriod`, `SalaryComponent` | **移除 `@RedisHash`** 注解，**替换为JPA注解** (`@Entity`等)，与`payroll-implementation.md`保持一致。 |
| `employee-implementation.md` | `Employee` | 添加 `payType` (enum) 和 `hourlyRate` (`BigDecimal`) 字段，并定义 `PayType` 枚举。 |
| **所有设计文件** | 所有JPA实体 | 将所有 `LocalDateTime` 时间戳字段**统一修改为 `Instant`** 类型。 |
| `database-design.md` | `EmailTemplate` | 将 `active` 字段重命名为 `enabled`，并添加 `isDefault` 字段。 |
| `tasks.md` (或服务设计) | `PayrollService` | 添加业务规则：在创建薪资记录时，**以快照形式一次性填充**员工和部门名称，之后不再更新。 |