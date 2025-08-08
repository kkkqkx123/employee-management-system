import type { Meta, StoryObj } from '@storybook/react';
import { fn } from '@storybook/test';
import { Button } from './Button';

const meta: Meta<typeof Button> = {
  title: 'UI/Button',
  component: Button,
  parameters: {
    layout: 'centered',
  },
  tags: ['autodocs'],
  argTypes: {
    variant: {
      control: { type: 'select' },
      options: ['filled', 'light', 'outline', 'subtle', 'transparent', 'gradient'],
    },
    size: {
      control: { type: 'select' },
      options: ['xs', 'sm', 'md', 'lg', 'xl'],
    },
    color: {
      control: { type: 'select' },
      options: ['blue', 'red', 'green', 'yellow', 'gray'],
    },
    disabled: {
      control: { type: 'boolean' },
    },
    loading: {
      control: { type: 'boolean' },
    },
  },
  args: { onClick: fn() },
};

export default meta;
type Story = StoryObj<typeof meta>;

export const Primary: Story = {
  args: {
    variant: 'filled',
    children: 'Button',
  },
};

export const Secondary: Story = {
  args: {
    variant: 'outline',
    children: 'Button',
  },
};

export const Large: Story = {
  args: {
    size: 'lg',
    children: 'Button',
  },
};

export const Small: Story = {
  args: {
    size: 'sm',
    children: 'Button',
  },
};

export const Disabled: Story = {
  args: {
    disabled: true,
    children: 'Button',
  },
};

export const Loading: Story = {
  args: {
    loading: true,
    children: 'Button',
  },
};

export const Danger: Story = {
  args: {
    color: 'red',
    children: 'Delete',
  },
};

export const Success: Story = {
  args: {
    color: 'green',
    children: 'Save',
  },
};