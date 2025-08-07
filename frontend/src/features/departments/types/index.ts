export interface Department {
  id: number;
  name: string;
  description?: string;
  parentId?: number;
  children?: Department[];
  employeeCount: number;
  level: number;
  path: string;
  createdAt: string;
  updatedAt: string;
}

export interface DepartmentTreeNode extends Department {
  expanded: boolean;
  selected: boolean;
  dragging: boolean;
  children: DepartmentTreeNode[];
}

export interface DepartmentCreateRequest {
  name: string;
  description?: string;
  parentId?: number;
}

export interface DepartmentUpdateRequest extends DepartmentCreateRequest {
  id: number;
}

export interface DepartmentMoveRequest {
  departmentId: number;
  newParentId?: number;
  position: number;
}

export interface DepartmentStatistics {
  totalDepartments: number;
  maxDepth: number;
  departmentsWithEmployees: number;
  averageEmployeesPerDepartment: number;
}