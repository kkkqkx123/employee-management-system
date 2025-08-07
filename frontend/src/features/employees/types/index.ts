// Employee management types

export interface Employee {
  id: number;
  employeeNumber: string;
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  mobilePhone?: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: 'MALE' | 'FEMALE' | 'OTHER';
  maritalStatus?: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
  nationality?: string;
  departmentId: number;
  departmentName?: string;
  positionId: number;
  positionName?: string;
  managerId?: number;
  managerName?: string;
  hireDate: string;
  terminationDate?: string;
  status: EmployeeStatus;
  employmentType: EmploymentType;
  payType: PayType;
  salary?: number;
  hourlyRate?: number;
  bankAccount?: string;
  taxId?: string;
  enabled: boolean;
  createdAt: string;
  updatedAt: string;
  fullName: string;
}

export enum EmployeeStatus {
  ACTIVE = 'ACTIVE',
  INACTIVE = 'INACTIVE',
  TERMINATED = 'TERMINATED',
  ON_LEAVE = 'ON_LEAVE',
  PROBATION = 'PROBATION',
  SUSPENDED = 'SUSPENDED'
}

export enum EmploymentType {
  FULL_TIME = 'FULL_TIME',
  PART_TIME = 'PART_TIME',
  CONTRACT = 'CONTRACT',
  TEMPORARY = 'TEMPORARY',
  INTERN = 'INTERN'
}

export enum PayType {
  SALARIED = 'SALARIED',
  HOURLY = 'HOURLY'
}

export enum Gender {
  MALE = 'MALE',
  FEMALE = 'FEMALE',
  OTHER = 'OTHER'
}

export enum MaritalStatus {
  SINGLE = 'SINGLE',
  MARRIED = 'MARRIED',
  DIVORCED = 'DIVORCED',
  WIDOWED = 'WIDOWED'
}

export interface EmployeeCreateRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  mobilePhone?: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  nationality?: string;
  departmentId: number;
  positionId: number;
  managerId?: number;
  hireDate: string;
  status: EmployeeStatus;
  employmentType: EmploymentType;
  payType: PayType;
  salary?: number;
  hourlyRate?: number;
  bankAccount?: string;
  taxId?: string;
}

export interface EmployeeUpdateRequest {
  firstName: string;
  lastName: string;
  email: string;
  phone?: string;
  mobilePhone?: string;
  address?: string;
  city?: string;
  state?: string;
  zipCode?: string;
  country?: string;
  dateOfBirth?: string;
  gender?: Gender;
  maritalStatus?: MaritalStatus;
  nationality?: string;
  departmentId: number;
  positionId: number;
  managerId?: number;
  hireDate: string;
  terminationDate?: string;
  status: EmployeeStatus;
  employmentType: EmploymentType;
  salary?: number;
  hourlyRate?: number;
  bankAccount?: string;
  taxId?: string;
  enabled: boolean;
}

export interface EmployeeSearchCriteria {
  firstName?: string;
  lastName?: string;
  email?: string;
  departmentId?: number;
  positionId?: number;
  status?: EmployeeStatus;
  employmentType?: EmploymentType;
  hireDate?: string;
  enabled?: boolean;
}

export interface EmployeeImportResult {
  totalRecords: number;
  successfulImports: number;
  failedImports: number;
  errors: string[];
  importedEmployees: Employee[];
}

export interface EmployeeExportRequest {
  employeeIds?: number[];
  searchCriteria?: EmployeeSearchCriteria;
  includeFields?: string[];
  format?: 'EXCEL' | 'CSV';
}

export interface EmployeeBulkOperation {
  employeeIds: number[];
  operation: 'DELETE' | 'ACTIVATE' | 'DEACTIVATE' | 'UPDATE_DEPARTMENT' | 'UPDATE_STATUS';
  parameters?: Record<string, any>;
}