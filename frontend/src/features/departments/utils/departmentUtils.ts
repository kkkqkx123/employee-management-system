import { Department, DepartmentTreeNode } from '../types';

export const departmentUtils = {
  /**
   * Build hierarchical tree structure from flat department array
   */
  buildTree(departments: Department[]): DepartmentTreeNode[] {
    const departmentMap = new Map<number, DepartmentTreeNode>();
    const rootNodes: DepartmentTreeNode[] = [];

    // Convert departments to tree nodes
    departments.forEach(dept => {
      departmentMap.set(dept.id, {
        ...dept,
        expanded: false,
        selected: false,
        dragging: false,
        children: []
      });
    });

    // Build parent-child relationships
    departments.forEach(dept => {
      const node = departmentMap.get(dept.id)!;
      
      if (dept.parentId && departmentMap.has(dept.parentId)) {
        const parent = departmentMap.get(dept.parentId)!;
        parent.children.push(node);
      } else {
        rootNodes.push(node);
      }
    });

    return rootNodes;
  },

  /**
   * Get all node IDs from tree structure
   */
  getAllNodeIds(nodes: DepartmentTreeNode[]): number[] {
    const ids: number[] = [];
    
    const traverse = (nodeList: DepartmentTreeNode[]) => {
      nodeList.forEach(node => {
        ids.push(node.id);
        if (node.children.length > 0) {
          traverse(node.children);
        }
      });
    };

    traverse(nodes);
    return ids;
  },

  /**
   * Filter tree based on search term
   */
  filterTree(nodes: DepartmentTreeNode[], searchTerm: string): DepartmentTreeNode[] {
    const filtered: DepartmentTreeNode[] = [];

    const matchesSearch = (node: DepartmentTreeNode): boolean => {
      return node.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
             (node.description?.toLowerCase().includes(searchTerm.toLowerCase()) ?? false);
    };

    const filterNode = (node: DepartmentTreeNode): DepartmentTreeNode | null => {
      const filteredChildren = node.children
        .map(child => filterNode(child))
        .filter(child => child !== null) as DepartmentTreeNode[];

      const nodeMatches = matchesSearch(node);
      const hasMatchingChildren = filteredChildren.length > 0;

      if (nodeMatches || hasMatchingChildren) {
        return {
          ...node,
          children: filteredChildren,
          expanded: hasMatchingChildren // Auto-expand if has matching children
        };
      }

      return null;
    };

    nodes.forEach(node => {
      const filteredNode = filterNode(node);
      if (filteredNode) {
        filtered.push(filteredNode);
      }
    });

    return filtered;
  },

  /**
   * Get IDs of nodes that match search term
   */
  getMatchingNodeIds(nodes: DepartmentTreeNode[], searchTerm: string): number[] {
    const matchingIds: number[] = [];

    const traverse = (nodeList: DepartmentTreeNode[]) => {
      nodeList.forEach(node => {
        if (node.name.toLowerCase().includes(searchTerm.toLowerCase()) ||
            (node.description?.toLowerCase().includes(searchTerm.toLowerCase()) ?? false)) {
          matchingIds.push(node.id);
        }
        if (node.children.length > 0) {
          traverse(node.children);
        }
      });
    };

    traverse(nodes);
    return matchingIds;
  },

  /**
   * Find node by ID in tree structure
   */
  findNodeById(nodes: DepartmentTreeNode[], id: number): DepartmentTreeNode | null {
    for (const node of nodes) {
      if (node.id === id) {
        return node;
      }
      
      const found = this.findNodeById(node.children, id);
      if (found) {
        return found;
      }
    }
    
    return null;
  },

  /**
   * Get path to node (breadcrumb)
   */
  getNodePath(nodes: DepartmentTreeNode[], targetId: number): DepartmentTreeNode[] {
    const path: DepartmentTreeNode[] = [];

    const findPath = (nodeList: DepartmentTreeNode[], currentPath: DepartmentTreeNode[]): boolean => {
      for (const node of nodeList) {
        const newPath = [...currentPath, node];
        
        if (node.id === targetId) {
          path.push(...newPath);
          return true;
        }
        
        if (findPath(node.children, newPath)) {
          return true;
        }
      }
      
      return false;
    };

    findPath(nodes, []);
    return path;
  },

  /**
   * Check if moving a department would create a circular reference
   */
  wouldCreateCircularReference(
    nodes: DepartmentTreeNode[], 
    departmentId: number, 
    newParentId?: number
  ): boolean {
    if (!newParentId) return false;
    
    const department = this.findNodeById(nodes, departmentId);
    if (!department) return false;

    // Check if newParentId is a descendant of departmentId
    const isDescendant = (node: DepartmentTreeNode, ancestorId: number): boolean => {
      if (node.id === ancestorId) return true;
      return node.children.some(child => isDescendant(child, ancestorId));
    };

    return isDescendant(department, newParentId);
  }
};