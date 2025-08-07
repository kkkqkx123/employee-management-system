import React, { useEffect, useState } from 'react';
import { Button, TextInput, Alert, Group, Stack, Container } from '@mantine/core';
import { IconSearch, IconPlus, IconChevronDown, IconChevronRight } from '@tabler/icons-react';
import { LoadingSpinner } from '../../../components/ui/LoadingSpinner';
import { Modal } from '../../../components/ui/Modal';
import { useDepartments } from '../hooks/useDepartments';
import { useDepartmentTree } from '../hooks/useDepartmentTree';
import { DepartmentNode } from './DepartmentNode';
import { DepartmentForm } from './DepartmentForm';
import { DepartmentDetail } from './DepartmentDetail';
import { Department, DepartmentTreeNode } from '../types';
import styles from './DepartmentTree.module.css';

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
    if (department.employeeCount > 0) {
      alert('Cannot delete department with employees. Please move employees first.');
      return;
    }

    if (department.children && department.children.length > 0) {
      alert('Cannot delete department with subdepartments. Please delete or move subdepartments first.');
      return;
    }

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

  const renderTreeNode = (node: DepartmentTreeNode, level: number = 0): React.ReactNode => {
    const isExpanded = expandedKeys.has(node.id);
    const isSelected = selectedNode?.id === node.id;

    return (
      <div key={node.id}>
        <DepartmentNode
          node={node}
          level={level}
          expanded={isExpanded}
          selected={isSelected}
          onToggleExpanded={() => toggleExpanded(node.id)}
          onSelect={() => selectNode(node)}
          onEdit={() => handleEditDepartment(node)}
          onDelete={() => handleDeleteDepartment(node)}
          onView={() => handleViewDepartment(node)}
          onAddChild={() => handleCreateDepartment(node)}
          onMove={handleMoveDepartment}
          isMoving={isMoving}
        />
        {isExpanded && node.children.map(child => renderTreeNode(child, level + 1))}
      </div>
    );
  };

  if (error) {
    return (
      <Container size="xl">
        <Alert color="red">
          Error loading departments: {error.message}
        </Alert>
      </Container>
    );
  }

  return (
    <Container size="xl" className={styles.container}>
      <div className={styles.header}>
        <h1>Departments</h1>
        <Group gap="md">
          <Button
            variant="outline"
            leftSection={<IconChevronDown size={16} />}
            onClick={expandAll}
            disabled={isLoading}
          >
            Expand All
          </Button>
          <Button
            variant="outline"
            leftSection={<IconChevronRight size={16} />}
            onClick={collapseAll}
            disabled={isLoading}
          >
            Collapse All
          </Button>
          <Button
            leftSection={<IconPlus size={16} />}
            onClick={() => handleCreateDepartment()}
            disabled={isLoading}
          >
            Add Department
          </Button>
        </Group>
      </div>

      <div className={styles.search}>
        <TextInput
          placeholder="Search departments..."
          leftSection={<IconSearch size={16} />}
          value={searchTerm}
          onChange={(e) => setSearchTerm(e.target.value)}
          disabled={isLoading}
          style={{ maxWidth: 400 }}
        />
      </div>

      <div className={styles.content}>
        {isLoading ? (
          <div className={styles.loading}>
            <LoadingSpinner size="lg" />
          </div>
        ) : (
          <Stack gap="xs" className={styles.treeContainer}>
            {treeData.length === 0 ? (
              <div className={styles.emptyState}>
                <p>No departments found.</p>
                <Button onClick={() => handleCreateDepartment()}>
                  Create First Department
                </Button>
              </div>
            ) : (
              treeData.map(node => renderTreeNode(node))
            )}
          </Stack>
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
    </Container>
  );
};