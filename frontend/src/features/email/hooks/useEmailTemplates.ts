// Email templates hook

import { useQuery } from '@tanstack/react-query';
import { EmailApiService } from '../services/emailApi';
import type { EmailTemplateWithVariables } from '../types';

export const useEmailTemplates = () => {
  return useQuery({
    queryKey: ['email', 'templates'],
    queryFn: async () => {
      const response = await EmailApiService.getEmailTemplates();
      return response.data;
    },
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useEmailTemplate = (id: number | undefined) => {
  return useQuery({
    queryKey: ['email', 'templates', id],
    queryFn: async () => {
      if (!id) throw new Error('Template ID is required');
      const response = await EmailApiService.getEmailTemplate(id);
      return response.data;
    },
    enabled: !!id,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};

export const useEmailTemplatesByCategory = (category: string | undefined) => {
  return useQuery({
    queryKey: ['email', 'templates', 'category', category],
    queryFn: async () => {
      if (!category) throw new Error('Category is required');
      const response = await EmailApiService.getEmailTemplatesByCategory(category);
      return response.data;
    },
    enabled: !!category,
    staleTime: 5 * 60 * 1000, // 5 minutes
  });
};