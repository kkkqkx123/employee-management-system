# Department Management Module Implementation Guide

## Overview
This document provides detailed implementation specifications for the department management module, including hierarchical department tree, CRUD operations, and drag-and-drop functionality.

## File Structure
```
src/features/departments/
├── components/
│   ├── DepartmentTree.tsx
│   ├── DepartmentForm.tsx
│   ├── DepartmentDetail.tsx
│   ├── DepartmentNode.tsx
│   └── DepartmentSearch.tsx
├── hooks/
│   ├── useDepartments.ts
│   ├── useDepartmentForm.ts
│   └── useDepartmentTree.ts
├── services/
│   └── departmentService.ts
├── types/
│   └── department.types.ts
├── utils/
│   └── departmentUtils.ts
└── index.ts
```

## Type Definitions

### department.types.ts
```typescript
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
```

## Services

### departmentService.ts
```typescript
import { apiClient } from '../../services/api';
import { 
  Department, 
  DepartmentCreateRequest, 
  DepartmentUpdateRequest,
  DepartmentMoveRequest,
  DepartmentStatistics 
} from '../types/department.types';

export const departmentService = {
  async getDepartmentTree(): Promise<Department[]> {
    const response = await apiClient.get<Department[]>('/departments/tree');
    return response.data;
  },

  async getDepartments(): Promise<Department[]> {
    const response = await apiClient.get<Department[]>('/departments');
    return response.data;
  },

  async getDepartment(id: number): Promise<Department> {
    const response = await apiClient.get<Department>(`/departments/${id}`);
    return response.data;
  },

  async createDepartment(department: DepartmentCreateRequest): Promise<Department> {
    const response = await apiClient.post<Department>('/departments', department);
    return response.data;
  },

  async updateDepartment(department: DepartmentUpdateRequest): Promise<Department> {
    const response = await apiClient.put<Department>(
      `/departments/${department.id}`, 
      department
    );
    return response.data;
  },

  async deleteDepartment(id: number): Promise<void> {
    await apiClient.delete(`/departments/${id}`);
  },

  async moveDepartment(moveRequest: DepartmentMoveRequest): Promise<Department[]> {
    const response = await apiClient.post<Department[]>(
      '/departments/move',
      moveRequest
    );
    return response.data;
  },

  async getDepartmentStatistics(): Promise<DepartmentStatistics> {
    const response = await apiClient.get<DepartmentStatistics>('/departments/statistics');
    return response.data;
  },

  async validateDepartmentMove(
    departmentId: number, 
    newParentId?: number
  ): Promise<{ valid: boolean; message?: string }> {
    const response = await apiClient.post<{ valid: boolean; message?: string }>(
      '/departments/validate-move',
      { departmentId, newParentId }
    );
    return response.data;
  }
};
```

## Custom Hooks

### useDepartments.ts
```typescript
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { departmentService } from '../services/departmentService';
import { Department, DepartmentMoveRequest } from '../types/department.types';

export const useDepartments = () => {
  const queryClient = useQueryClient();

  const departmentsQuery = useQuery({
    queryKey: ['departments'],
    queryFn: departmentService.getDepartments,
  });

  const departmentTreeQuery = useQuery({
    queryKey: ['departments', 'tree'],
    queryFn: departmentService.getDepartmentTree,
  });

  const deleteDepartmentMutation = useMutation({
    mutationFn: departmentService.deleteDepartment,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
    },
  });

  const moveDepartmentMutation = useMutation({
    mutationFn: (moveRequest: DepartmentMoveRequest) => 
      departmentService.moveDepartment(moveRequest),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['departments'] });
    },
  });

  return {
    departments: departmentsQuery.data || [],
    departmentTree: departmentTreeQuery.data || [],
    isLoading: departmentsQuery.isLoading || departmentTreeQuery.isLoading,
    error: departmentsQuery.error || departmentTreeQuery.error,
    deleteDepartment: deleteDepartmentMutation.mutate,
    isDeleting: deleteDepartmentMutation.isPending,
    moveDepartment: moveDepartmentMutation.mutate,
    isMoving: moveDepartmentMutation.isPending,
    refetch: () => {
      departmentsQuery.refetch();
      departmentTreeQuery.refetch();
    },
  };
};

export const useDepartment = (id: number) => {
  return useQuery({
    queryKey: ['department', id],
    queryFn: () => departmentService.getDepartment(id),
    enabled: !!id,
  });
};
```### useD
epartmentTree.ts
```typescript
import { useState, useCallback } from 'react';
import { DepartmentTreeNode, Department } from '../types/department.types';
import { departmentUtils } from '../utils/departmentUtils';

export const useDepartmentTree = (departments: Department[]) => {
  const [treeData, setTreeData] = useState<DepartmentTreeNode[]>([]);
  const [selectedNode, setSelectedNode] = useState<DepartmentTreeNode | null>(null);
  const [expandedKeys, setExpandedKeys] = useState<Set<number>>(new Set());

  // Convert departments to tree structure
  const initializeTree = useCallback(() => {
    const tree = departmentUtils.buildTree(departments);
    setTreeData(tree);
  }, [departments]);

  // Expand/collapse node
  const toggleExpanded = useCallback((nodeId: number) => {
    setExpandedKeys(prev => {
      const newSet = new Set(prev);
      if (newSet.has(nodeId)) {
        newSet.delete(nodeId);
      } else {
        newSet.add(nodeId);
      }
      return newSet;
    });
  }, []);

  // Select node
  const selectNode = useCallback((node: DepartmentTreeNode) => {
    setSelectedNode(node);
  }, []);

  // Expand all nodes
  const expandAll = useCallback(() => {
    const allIds = departmentUtils.getAllNodeIds(treeData);
    setExpandedKeys(new Set(allIds));
  }, [treeData]);

  // Collapse all nodes
  const collapseAll = useCallback(() => {
    setExpandedKeys(new Set());
  }, []);

  // Search in tree
  const searchTree = useCallback((searchTerm: string) => {
    if (!searchTerm) {
      initializeTree();
      return;
    }

    const filteredTree = departmentUtils.filterTree(treeData, searchTerm);
    setTreeData(filteredTree);
    
    // Expand all nodes that contain search results
    const matchingIds = departmentUtils.getMatchingNodeIds(filteredTree, searchTerm);
    setExpandedKeys(new Set(matchingIds));
  }, [treeData, initializeTree]);

  return {
    treeData,
    selectedNode,
    expandedKeys,
    initializeTree,
    toggleExpanded,
    selectNode,
    expandAll,
    collapseAll,
    searchTree,
  };
};
```

## Components

### DepartmentTree.tsx
```typescript
import React, { useEffect, useState } from 'react';
import { DndProvider } from 'react-dnd';
import { HTML5Backend } from 'react-dnd-html5-backend';
import { Button, TextInput, Alert } from '@mantine/core';
import { LoadingSpinner } from '../../components/ui/LoadingSpinner';
import { Modal } from '../../components/ui/Modal';
import { useDepartments } from '../hooks/useDepartments';
import { useDepartmentTree } from '../hooks/useDepartmentTree';
import { DepartmentNode } from './DepartmentNode';
import { DepartmentForm } from './DepartmentForm';
import { DepartmentDetail } from './DepartmentDetail';
import { Department, DepartmentTreeNode } from '../types/department.types';

export const DepartmentTree: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showDetailModal, setShowDetailModal] = useState(false);
  const [selectedDepartment, setSelectedDepartment] = useState<Department | null>(null);
  const [parentDepartment, setParentDepartment] = useState<Department | null>(null);

  const {
    departmentTree,
    isLoading,
    error,
    deleteDepartment,
    isDeleting,
    moveDepartment,
    isMoving,
  } = useDepartments();

  const {
    treeData,
    selectedNode,
    expandedKeys,
    initializeTree,
    toggleExpanded,
    selectNode,
    expandAll,
    collapseAll,
    searchTree,
  } = useDepartmentTree(departmentTree);

  useEffect(() => {
    initializeTree();
  }, [departmentTree, initializeTree]);

  useEffect(() => {
    const debounceTimer = setTimeout(() => {
      searchTree(searchTerm);
    }, 300);

    return () => clearTimeout(debounceTimer);
  }, [searchTerm, searchTree]);

  const handleCreateDepartment = (parent?: Department) => {
    setParentDepartment(parent || null);
    setSelectedDepartment(null);
    setShowCreateModal(true);
  };

  const handleEditDepartment = (department: Department) => {
    setSelectedDepartment(department);
    setParentDepartment(null);
    setShowCreateModal(true);
  };

  const handleViewDepartment = (department: Department) => {
    setSelectedDepartment(department);
    setShowDetailModal(true);
  };

  const handleDeleteDepartment = (department: Department) => {
    if (window.confirm(
      `Are you sure you want to delete "${department.name}"? This action cannot be undone.`
    )) {
      deleteDepartment(department.id);
    }
  };

  const handleMoveDepartment = (
    departmentId: number,
    newParentId?: number,
    position: number = 0
  ) => {
    moveDepartment({
      departmentId,
      newParentId,
      position,
    });
  };

  if (error) {
    return (
      <Alert color="red">
        Error loading departments: {error.message}
      </Alert>
    );
  }

  return (
    <DndProvider backend={HTML5Backend}>
      <div className="department-tree">
        <div className="department-tree-header">
          <h1>Departments</h1>
          <div className="department-tree-actions">
            <Button
              variant="outline"
              onClick={expandAll}
              disabled={isLoading}
            >
              Expand All
            </Button>
            <Button
              variant="outline"
              onClick={collapseAll}
              disabled={isLoading}
            >
              Collapse All
            </Button>
            <Button
              onClick={() => handleCreateDepartment()}
              disabled={isLoading}
            >
              Add Department
            </Button>
          </div>
        </div>

        <div className="department-tree-search">
          <TextInput
            placeholder="Search departments..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            disabled={isLoading}
          />
        </div>

        <div className="department-tree-content">
          {isLoading ? (
            <LoadingSpinner size="lg" />
          ) : (
            <div className="tree-nodes">
              {treeData.map((node) => (
                <DepartmentNode
                  key={node.id}
                  node={node}
                  level={0}
                  expanded={expandedKeys.has(node.id)}
                  selected={selectedNode?.id === node.id}
                  onToggleExpanded={() => toggleExpanded(node.id)}
                  onSelect={() => selectNode(node)}
                  onEdit={() => handleEditDepartment(node)}
                  onDelete={() => handleDeleteDepartment(node)}
                  onView={() => handleViewDepartment(node)}
                  onAddChild={() => handleCreateDepartment(node)}
                  onMove={handleMoveDepartment}
                  isMoving={isMoving}
                />
              ))}
            </div>
          )}
        </div>

        {/* Create/Edit Modal */}
        <Modal
          isOpen={showCreateModal}
          onClose={() => {
            setShowCreateModal(false);
            setSelectedDepartment(null);
            setParentDepartment(null);
          }}
          title={
            selectedDepartment 
              ? 'Edit Department' 
              : `Add Department${parentDepartment ? ` to ${parentDepartment.name}` : ''}`
          }
          size="md"
        >
          <DepartmentForm
            department={selectedDepartment}
            parentDepartment={parentDepartment}
            onSuccess={() => {
              setShowCreateModal(false);
              setSelectedDepartment(null);
              setParentDepartment(null);
            }}
          />
        </Modal>

        {/* Detail Modal */}
        <Modal
          isOpen={showDetailModal}
          onClose={() => {
            setShowDetailModal(false);
            setSelectedDepartment(null);
          }}
          title="Department Details"
          size="lg"
        >
          {selectedDepartment && (
            <DepartmentDetail department={selectedDepartment} />
          )}
        </Modal>
      </div>
    </DndProvider>
  );
};
```