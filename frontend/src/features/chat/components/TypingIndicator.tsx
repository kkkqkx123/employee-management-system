import { Box, Text, Group, Avatar } from '@mantine/core';
import { IconUser } from '@tabler/icons-react';
import classes from './TypingIndicator.module.css';

interface TypingIndicatorProps {
  userName: string;
  avatar?: string;
}

export const TypingIndicator = ({ userName, avatar }: TypingIndicatorProps) => {
  return (
    <Box className={classes.container}>
      <Group gap="sm" align="flex-start" wrap="nowrap">
        <Avatar src={avatar} size="sm" radius="xl">
          <IconUser size={16} />
        </Avatar>

        <Box className={classes.content}>
          <Text size="xs" c="dimmed" mb={4}>
            {userName} is typing...
          </Text>
          
          <Box className={classes.bubble}>
            <div className={classes.dots}>
              <div className={classes.dot}></div>
              <div className={classes.dot}></div>
              <div className={classes.dot}></div>
            </div>
          </Box>
        </Box>
      </Group>
    </Box>
  );
};