import React from 'react';
import { clsx } from 'clsx';
import { Button } from '../Button';
import { PaginationConfig } from '../types/ui.types';
import styles from './DataTable.module.css';

interface DataTablePaginationProps {
  pagination: PaginationConfig;
}

export const DataTablePagination: React.FC<DataTablePaginationProps> = ({
  pagination
}) => {
  const {
    current,
    pageSize,
    total,
    showSizeChanger = true,
    showQuickJumper = false,
    showTotal,
    onChange,
    onShowSizeChange
  } = pagination;

  const totalPages = Math.ceil(total / pageSize);
  const startItem = (current - 1) * pageSize + 1;
  const endItem = Math.min(current * pageSize, total);

  const handlePageChange = (page: number) => {
    if (page >= 1 && page <= totalPages && page !== current) {
      onChange?.(page, pageSize);
    }
  };

  const handleSizeChange = (newSize: number) => {
    onShowSizeChange?.(current, newSize);
  };

  const renderPageNumbers = () => {
    const pages: (number | string)[] = [];
    const maxVisiblePages = 7;
    
    if (totalPages <= maxVisiblePages) {
      for (let i = 1; i <= totalPages; i++) {
        pages.push(i);
      }
    } else {
      pages.push(1);
      
      if (current > 4) {
        pages.push('...');
      }
      
      const start = Math.max(2, current - 2);
      const end = Math.min(totalPages - 1, current + 2);
      
      for (let i = start; i <= end; i++) {
        pages.push(i);
      }
      
      if (current < totalPages - 3) {
        pages.push('...');
      }
      
      pages.push(totalPages);
    }
    
    return pages;
  };

  if (totalPages <= 1) return null;

  return (
    <div className={styles.pagination}>
      <div className={styles.paginationInfo}>
        {showTotal ? (
          showTotal(total, [startItem, endItem])
        ) : (
          <span className={styles.totalText}>
            Showing {startItem} to {endItem} of {total} entries
          </span>
        )}
      </div>

      <div className={styles.paginationControls}>
        {showSizeChanger && (
          <div className={styles.sizeChanger}>
            <select
              value={pageSize}
              onChange={(e) => handleSizeChange(Number(e.target.value))}
              className={styles.sizeSelect}
            >
              {[10, 20, 50, 100].map(size => (
                <option key={size} value={size}>
                  {size} / page
                </option>
              ))}
            </select>
          </div>
        )}

        <div className={styles.pageControls}>
          <Button
            variant="outline"
            size="sm"
            disabled={current === 1}
            onClick={() => handlePageChange(current - 1)}
          >
            Previous
          </Button>

          <div className={styles.pageNumbers}>
            {renderPageNumbers().map((page, index) => (
              <React.Fragment key={index}>
                {page === '...' ? (
                  <span className={styles.ellipsis}>...</span>
                ) : (
                  <Button
                    variant={current === page ? 'primary' : 'ghost'}
                    size="sm"
                    onClick={() => handlePageChange(page as number)}
                    className={styles.pageButton}
                  >
                    {page}
                  </Button>
                )}
              </React.Fragment>
            ))}
          </div>

          <Button
            variant="outline"
            size="sm"
            disabled={current === totalPages}
            onClick={() => handlePageChange(current + 1)}
          >
            Next
          </Button>
        </div>

        {showQuickJumper && (
          <div className={styles.quickJumper}>
            <span>Go to</span>
            <input
              type="number"
              min={1}
              max={totalPages}
              className={styles.jumpInput}
              onKeyPress={(e) => {
                if (e.key === 'Enter') {
                  const page = parseInt((e.target as HTMLInputElement).value);
                  if (page >= 1 && page <= totalPages) {
                    handlePageChange(page);
                    (e.target as HTMLInputElement).value = '';
                  }
                }
              }}
            />
          </div>
        )}
      </div>
    </div>
  );
};