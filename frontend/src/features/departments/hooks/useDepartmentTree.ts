import { useState, useCallback } from 'react';
import { DepartmentTreeNode, Department } from '../types';
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