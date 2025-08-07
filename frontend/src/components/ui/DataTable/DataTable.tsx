import { Table, Pagination, TextInput, Select, Group, Text, ActionIcon } from '@mantine/core';
import { IconSearch, IconSortAscending, IconSortDescending } from '@tabler/icons-react';
import { useState, useMemo } from 'react';
import { LoadingSpinner } from '../LoadingSpinner';
import classes from './DataTable.module.css';

export interface Column<T = any> {
  /** Column key */
  key: string;
  /** Column header */
  header: string;
  /** Column width */
  width?: string | number;
  /** Whether column is sortable */
  sortable?: boolean;
  /** Custom render function */
  render?: (value: any, record: T, index: number) => React.ReactNode;
  /** Column alignment */
  align?: 'left' | 'center' | 'right';
}

export interface DataTableProps<T = any> {
  /** Table data */
  data: T[];
  /** Table columns */
  columns: Column<T>[];
  /** Loading state */
  loading?: boolean;
  /** Whether to show pagination */
  pagination?: boolean;
  /** Page size options */
  pageSizeOptions?: number[];
  /** Default page size */
  defaultPageSize?: number;
  /** Whether to show search */
  searchable?: boolean;
  /** Search placeholder */
  searchPlaceholder?: string;
  /** Empty state message */
  emptyMessage?: string;
  /** Table caption */
  caption?: string;
  /** Custom row key */
  rowKey?: string | ((record: T) => string);
  /** Row click handler */
  onRowClick?: (record: T, index: number) => void;
}

export const DataTable = <T extends Record<string, any>>({
  data,
  columns,
  loading = false,
  pagination = true,
  pageSizeOptions = [10, 20, 50, 100],
  defaultPageSize = 20,
  searchable = true,
  searchPlaceholder = 'Search...',
  emptyMessage = 'No data available',
  caption,
  rowKey = 'id',
  onRowClick,
}: DataTableProps<T>) => {
  const [currentPage, setCurrentPage] = useState(1);
  const [pageSize, setPageSize] = useState(defaultPageSize);
  const [searchQuery, setSearchQuery] = useState('');
  const [sortColumn, setSortColumn] = useState<string | null>(null);
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc');

  // Filter data based on search query
  const filteredData = useMemo(() => {
    if (!searchQuery) return data;
    
    return data.filter(record =>
      columns.some(column => {
        const value = record[column.key];
        return value?.toString().toLowerCase().includes(searchQuery.toLowerCase());
      })
    );
  }, [data, searchQuery, columns]);

  // Sort data
  const sortedData = useMemo(() => {
    if (!sortColumn) return filteredData;
    
    return [...filteredData].sort((a, b) => {
      const aValue = a[sortColumn];
      const bValue = b[sortColumn];
      
      if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }, [filteredData, sortColumn, sortDirection]);

  // Paginate data
  const paginatedData = useMemo(() => {
    if (!pagination) return sortedData;
    
    const startIndex = (currentPage - 1) * pageSize;
    return sortedData.slice(startIndex, startIndex + pageSize);
  }, [sortedData, currentPage, pageSize, pagination]);

  const totalPages = Math.ceil(sortedData.length / pageSize);

  const handleSort = (columnKey: string) => {
    if (sortColumn === columnKey) {
      setSortDirection(sortDirection === 'asc' ? 'desc' : 'asc');
    } else {
      setSortColumn(columnKey);
      setSortDirection('asc');
    }
  };

  const getRowKey = (record: T, index: number): string => {
    if (typeof rowKey === 'function') {
      return rowKey(record);
    }
    return record[rowKey] || index.toString();
  };

  if (loading) {
    return <LoadingSpinner centered text="Loading data..." />;
  }

  return (
    <div className={classes.container}>
      {searchable && (
        <div className={classes.toolbar}>
          <TextInput
            placeholder={searchPlaceholder}
            leftSection={<IconSearch size={16} />}
            value={searchQuery}
            onChange={(event) => setSearchQuery(event.currentTarget.value)}
            className={classes.search}
          />
        </div>
      )}

      <Table className={classes.table} captionSide="top">
        {caption && <Table.Caption>{caption}</Table.Caption>}
        <Table.Thead>
          <Table.Tr>
            {columns.map((column) => (
              <Table.Th
                key={column.key}
                style={{ width: column.width, textAlign: column.align }}
                className={column.sortable ? classes.sortableHeader : undefined}
                onClick={column.sortable ? () => handleSort(column.key) : undefined}
              >
                <Group gap="xs" justify={column.align === 'center' ? 'center' : column.align === 'right' ? 'flex-end' : 'flex-start'}>
                  <Text>{column.header}</Text>
                  {column.sortable && (
                    <ActionIcon
                      variant="transparent"
                      size="sm"
                      color={sortColumn === column.key ? 'blue' : 'gray'}
                    >
                      {sortColumn === column.key && sortDirection === 'desc' ? (
                        <IconSortDescending size={14} />
                      ) : (
                        <IconSortAscending size={14} />
                      )}
                    </ActionIcon>
                  )}
                </Group>
              </Table.Th>
            ))}
          </Table.Tr>
        </Table.Thead>
        <Table.Tbody>
          {paginatedData.length === 0 ? (
            <Table.Tr>
              <Table.Td colSpan={columns.length} className={classes.emptyState}>
                <Text c="dimmed">{emptyMessage}</Text>
              </Table.Td>
            </Table.Tr>
          ) : (
            paginatedData.map((record, index) => (
              <Table.Tr
                key={getRowKey(record, index)}
                className={onRowClick ? classes.clickableRow : undefined}
                onClick={onRowClick ? () => onRowClick(record, index) : undefined}
              >
                {columns.map((column) => (
                  <Table.Td
                    key={column.key}
                    style={{ textAlign: column.align }}
                  >
                    {column.render
                      ? column.render(record[column.key], record, index)
                      : record[column.key]
                    }
                  </Table.Td>
                ))}
              </Table.Tr>
            ))
          )}
        </Table.Tbody>
      </Table>

      {pagination && totalPages > 1 && (
        <div className={classes.pagination}>
          <Group justify="space-between" align="center">
            <Group gap="xs">
              <Text size="sm" c="dimmed">
                Showing {Math.min((currentPage - 1) * pageSize + 1, sortedData.length)} to{' '}
                {Math.min(currentPage * pageSize, sortedData.length)} of {sortedData.length} entries
              </Text>
            </Group>
            <Group gap="md">
              <Select
                data={pageSizeOptions.map(size => ({ value: size.toString(), label: `${size} per page` }))}
                value={pageSize.toString()}
                onChange={(value) => {
                  setPageSize(Number(value));
                  setCurrentPage(1);
                }}
                size="sm"
                w={120}
              />
              <Pagination
                value={currentPage}
                onChange={setCurrentPage}
                total={totalPages}
                size="sm"
              />
            </Group>
          </Group>
        </div>
      )}
    </div>
  );
};