# Department Management Module

This module provides comprehensive department management functionality including:

## Features

- **Hierarchical Department Tree**: Visual tree structure with expand/collapse functionality
- **CRUD Operations**: Create, read, update, and delete departments
- **Search & Filter**: Real-time search through department names and descriptions
- **Form Validation**: Client-side validation for department data
- **Parent-Child Relationships**: Support for nested department structures
- **Employee Count Tracking**: Display employee counts per department
- **Responsive Design**: Mobile-friendly interface

## Components

### DepartmentTree
Main component that displays the hierarchical department structure with:
- Expandable/collapsible tree nodes
- Search functionality
- Action buttons (Add, Edit, Delete, View)
- Bulk operations (Expand All, Collapse All)

### DepartmentForm
Form component for creating and editing departments with:
- Name and description fields
- Parent department selection
- Form validation
- Error handling

### DepartmentDetail
Detail view component showing:
- Department information
- Employee count
- Creation/update dates
- Department path
- Subdepartments list

### DepartmentNode
Individual tree node component with:
- Expand/collapse controls
- Action buttons
- Employee count badge
- Hover effects

## Hooks

### useDepartments
Main hook for department data management:
- Fetches department tree and flat list
- Handles CRUD operations
- Manages loading and error states

### useDepartmentTree
Hook for tree-specific functionality:
- Tree state management
- Expand/collapse logic
- Search filtering
- Node selection

### useDepartmentForm
Hook for form management:
- Form validation
- Submit handling
- Error management

## Services

### departmentService
API service layer providing:
- Department CRUD operations
- Tree structure fetching
- Move operations
- Statistics retrieval

## Types

Comprehensive TypeScript types for:
- Department entities
- Tree node structures
- API requests/responses
- Form data

## Usage

```tsx
import { DepartmentTree } from '../features/departments';

export const DepartmentsPage = () => {
  return <DepartmentTree />;
};
```

## Testing

The module includes comprehensive tests for:
- Component rendering
- Form validation
- User interactions
- API integration

## Future Enhancements

- Drag-and-drop reordering (requires react-dnd installation)
- Bulk operations
- Export functionality
- Advanced filtering
- Department templates