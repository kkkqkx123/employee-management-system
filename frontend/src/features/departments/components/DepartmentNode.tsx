import React from 'react';
import { Group, Text, ActionIcon, Badge, Box } from '@mantine/core';
import {
  IconChevronRight,
  IconChevronDown,
  IconPlus,
  IconEdit,
  IconTrash,
  IconEye,
  IconUsers,
} from '@tabler/icons-react';
import { DepartmentTreeNode } from '../types';
import styles from './DepartmentNode.module.css';

interface DepartmentNodeProps {
  node: DepartmentTreeNode;
  level: number;
  expanded: boolean;
  selected: boolean;
  onToggleExpanded: () => void;
  onSelect: () => void;
  onEdit: () => void;
  onDelete: () => void;
  onView: () => void;
  onAddChild: () => void;
  onMove: (departmentId: number, newParentId?: number, position?: number) => void;
  isMoving: boolean;
}

export const DepartmentNode: React.FC<DepartmentNodeProps> = ({
  node,
  level,
  expanded,
  selected,
  onToggleExpanded,
  onSelect,
  onEdit,
  onDelete,
  onView,
  onAddChild,
  isMoving,
}) => {
  const hasChildren = node.children && node.children.length > 0;
  const paddingLeft = level * 24;

  return (
    <div className={styles.nodeContainer}>
      <Box
        className={`${styles.nodeContent} ${selected ? styles.selected : ''}`}
        style={{ paddingLeft }}
        onClick={onSelect}
      >
        <Group gap="xs" style={{ flex: 1 }}>
          {/* Expand/Collapse Button */}
          <ActionIcon
            variant="subtle"
            size="sm"
            onClick={(e) => {
              e.stopPropagation();
              onToggleExpanded();
            }}
            disabled={!hasChildren}
            style={{ visibility: hasChildren ? 'visible' : 'hidden' }}
          >
            {expanded ? <IconChevronDown size={16} /> : <IconChevronRight size={16} />}
          </ActionIcon>

          {/* Department Name */}
          <Text fw={500} size="sm" style={{ flex: 1 }}>
            {node.name}
          </Text>

          {/* Employee Count Badge */}
          <Badge
            variant="light"
            size="xs"
            leftSection={<IconUsers size={12} />}
          >
            {node.employeeCount}
          </Badge>

          {/* Action Buttons */}
          <Group gap={4} className={styles.actions}>
            <ActionIcon
              variant="subtle"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onView();
              }}
              title="View Details"
            >
              <IconEye size={14} />
            </ActionIcon>

            <ActionIcon
              variant="subtle"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onAddChild();
              }}
              title="Add Subdepartment"
            >
              <IconPlus size={14} />
            </ActionIcon>

            <ActionIcon
              variant="subtle"
              size="sm"
              onClick={(e) => {
                e.stopPropagation();
                onEdit();
              }}
              title="Edit Department"
            >
              <IconEdit size={14} />
            </ActionIcon>

            <ActionIcon
              variant="subtle"
              size="sm"
              color="red"
              onClick={(e) => {
                e.stopPropagation();
                onDelete();
              }}
              title="Delete Department"
              disabled={hasChildren || node.employeeCount > 0}
            >
              <IconTrash size={14} />
            </ActionIcon>
          </Group>
        </Group>
      </Box>

      {/* Child Nodes */}
      {expanded && hasChildren && (
        <div className={styles.children}>
          {node.children.map((child) => (
            <DepartmentNode
              key={child.id}
              node={child}
              level={level + 1}
              expanded={false} // You'd need to track this in parent component
              selected={false} // You'd need to track this in parent component
              onToggleExpanded={() => {}} // Pass from parent
              onSelect={() => {}} // Pass from parent
              onEdit={onEdit}
              onDelete={onDelete}
              onView={onView}
              onAddChild={onAddChild}
              onMove={onMove}
              isMoving={isMoving}
            />
          ))}
        </div>
      )}
    </div>
  );
};