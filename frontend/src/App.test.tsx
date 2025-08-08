import { render, screen } from '@testing-library/react';
import { BrowserRouter } from 'react-router-dom';
import { MantineProvider } from '@mantine/core';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import App from './App';
import React from 'react'; // 添加 React 导入以支持 JSX 语法

import { describe, it, expect } from 'vitest';

describe('App', () => {
  it('renders the application title', () => {
    render(<App />);
    expect(screen.getByText('Employee Management System')).toBeInTheDocument();
  });

  it('renders the setup complete message', () => {
    render(<App />);
    expect(screen.getByText('Frontend foundation setup complete!')).toBeInTheDocument();
  });
});