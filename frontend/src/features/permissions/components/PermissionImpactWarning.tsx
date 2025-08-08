// Permission Impact Warning Component

import React from 'react';
import {
  Alert,
  Text,
  List,
  Badge,
  Group,
  Stack,
  ThemeIcon,
  Collapse,
  Button,
} from '@mantine/core';
import {
  IconAlertTriangle,
  IconAlertCircle,
  IconInfoCircle,
  IconShield,
  IconUsers,
  IconChevronDown,
  IconChevronUp,
} from '@tabler/icons-react';
import { useDisclosure } from '@mantine/hooks';
import type { PermissionImpact, RiskLevel } from '../types';

interface PermissionImpactWarningProps {
  impact: PermissionImpact;
  action: 'ADD' | 'REMOVE' | 'DELETE' | 'MODIFY';
  resourceName?: string;
  onProceed?: () => void;
  onCancel?: () => void;
  showActions?: boolean;
}

const getRiskConfig = (riskLevel: RiskLevel) => {
  switch (riskLevel) {
    case 'CRITICAL':
      return {
        color: 'red',
        icon: IconAlertTriangle,
        title: 'Critical Risk',
        description: 'This action may severely impact system security or functionality.',
      };
    case 'HIGH':
      return {
        color: 'orange',
        icon: IconAlertCircle,
        title: 'High Risk',
        description: 'This action may significantly affect user access or system operations.',
      };
    case 'MEDIUM':
      return {
        color: 'yellow',
        icon: IconInfoCircle,
        title: 'Medium Risk',
        description: 'This action will affect some users or system features.',
      };
    case 'LOW':
      return {
        color: 'blue',
        icon: IconInfoCircle,
        title: 'Low Risk',
        description: 'This action has minimal impact on the system.',
      };
    default:
      return {
        color: 'gray',
        icon: IconInfoCircle,
        title: 'Unknown Risk',
        description: 'Unable to assess the impact of this action.',
      };
  }
};

const getActionText = (action: string, resourceName?: string) => {
  const resource = resourceName || 'this resource';
  switch (action) {
    case 'ADD':
      return `adding permissions to ${resource}`;
    case 'REMOVE':
      return `removing permissions from ${resource}`;
    case 'DELETE':
      return `deleting ${resource}`;
    case 'MODIFY':
      return `modifying ${resource}`;
    default:
      return `performing this action on ${resource}`;
  }
};

export const PermissionImpactWarning: React.FC<PermissionImpactWarningProps> = ({
  impact,
  action,
  resourceName,
  onProceed,
  onCancel,
  showActions = true,
}) => {
  const [detailsOpened, { toggle: toggleDetails }] = useDisclosure(false);
  const riskConfig = getRiskConfig(impact.riskLevel);
  const RiskIcon = riskConfig.icon;

  return (
    <Alert
      icon={<RiskIcon size={20} />}
      title={riskConfig.title}
      color={riskConfig.color}
      variant="light"
    >
      <Stack spacing="sm">
        <Text size="sm">
          {riskConfig.description} The impact of {getActionText(action, resourceName)} includes:
        </Text>

        {/* Summary Stats */}
        <Group spacing="md">
          {impact.affectedUsers > 0 && (
            <Group spacing={4}>
              <ThemeIcon size="sm" color="blue" variant="light">
                <IconUsers size={12} />
              </ThemeIcon>
              <Text size="sm" weight={500}>
                {impact.affectedUsers} user{impact.affectedUsers !== 1 ? 's' : ''} affected
              </Text>
            </Group>
          )}
          
          {impact.affectedRoles.length > 0 && (
            <Group spacing={4}>
              <ThemeIcon size="sm" color="green" variant="light">
                <IconShield size={12} />
              </ThemeIcon>
              <Text size="sm" weight={500}>
                {impact.affectedRoles.length} role{impact.affectedRoles.length !== 1 ? 's' : ''} affected
              </Text>
            </Group>
          )}
        </Group>

        {/* Warnings */}
        {impact.warnings.length > 0 && (
          <Stack spacing={4}>
            <Text size="sm" weight={500} color={riskConfig.color}>
              Warnings:
            </Text>
            <List size="sm" spacing={2}>
              {impact.warnings.map((warning, index) => (
                <List.Item key={index}>
                  <Text size="sm">{warning}</Text>
                </List.Item>
              ))}
            </List>
          </Stack>
        )}

        {/* Expandable Details */}
        {(impact.affectedRoles.length > 0 || impact.dependentPermissions.length > 0) && (
          <>
            <Button
              variant="subtle"
              size="xs"
              onClick={toggleDetails}
              rightIcon={detailsOpened ? <IconChevronUp size={14} /> : <IconChevronDown size={14} />}
            >
              {detailsOpened ? 'Hide' : 'Show'} Details
            </Button>

            <Collapse in={detailsOpened}>
              <Stack spacing="sm">
                {/* Affected Roles */}
                {impact.affectedRoles.length > 0 && (
                  <div>
                    <Text size="sm" weight={500} mb={4}>
                      Affected Roles:
                    </Text>
                    <Group spacing={4}>
                      {impact.affectedRoles.map((roleName, index) => (
                        <Badge key={index} size="sm" variant="light" color="blue">
                          {roleName}
                        </Badge>
                      ))}
                    </Group>
                  </div>
                )}

                {/* Dependent Permissions */}
                {impact.dependentPermissions.length > 0 && (
                  <div>
                    <Text size="sm" weight={500} mb={4}>
                      Dependent Permissions:
                    </Text>
                    <Group spacing={4}>
                      {impact.dependentPermissions.map((permissionName, index) => (
                        <Badge key={index} size="sm" variant="light" color="orange">
                          {permissionName}
                        </Badge>
                      ))}
                    </Group>
                  </div>
                )}
              </Stack>
            </Collapse>
          </>
        )}

        {/* Action Buttons */}
        {showActions && (onProceed || onCancel) && (
          <Group position="right" spacing="sm" mt="sm">
            {onCancel && (
              <Button variant="light" size="sm" onClick={onCancel}>
                Cancel
              </Button>
            )}
            {onProceed && (
              <Button
                size="sm"
                color={impact.riskLevel === 'CRITICAL' ? 'red' : riskConfig.color}
                onClick={onProceed}
              >
                Proceed Anyway
              </Button>
            )}
          </Group>
        )}
      </Stack>
    </Alert>
  );
};

export default PermissionImpactWarning;