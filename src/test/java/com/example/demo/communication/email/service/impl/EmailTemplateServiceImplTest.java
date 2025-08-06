package com.example.demo.communication.email.service.impl;

import com.example.demo.communication.email.dto.EmailTemplateDto;
import com.example.demo.communication.email.entity.EmailTemplate;
import com.example.demo.communication.email.repository.EmailTemplateRepository;
import com.example.demo.communication.email.service.impl.EmailTemplateServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmailTemplateServiceImplTest {

    @Mock
    private EmailTemplateRepository emailTemplateRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private EmailTemplateServiceImpl emailTemplateService;

    @Test
    void createTemplate_shouldReturnEmailTemplateDto() {
        EmailTemplateDto templateDto = new EmailTemplateDto();
        templateDto.setName("Test Template");

        EmailTemplate template = new EmailTemplate();
        template.setName("Test Template");

        when(modelMapper.map(templateDto, EmailTemplate.class)).thenReturn(template);
        when(emailTemplateRepository.save(any(EmailTemplate.class))).thenReturn(template);
        when(modelMapper.map(template, EmailTemplateDto.class)).thenReturn(templateDto);

        EmailTemplateDto result = emailTemplateService.createTemplate(templateDto);

        assertNotNull(result);
        assertEquals(templateDto.getName(), result.getName());
    }

    @Test
    void getTemplateByCode_shouldReturnEmailTemplateDto() {
        String code = "test-code";
        EmailTemplate template = new EmailTemplate();
        template.setCode(code);

        EmailTemplateDto expectedDto = new EmailTemplateDto();
        expectedDto.setCode(code);

        when(emailTemplateRepository.findByCode(code)).thenReturn(Optional.of(template));
        when(modelMapper.map(template, EmailTemplateDto.class)).thenReturn(expectedDto);

        EmailTemplateDto result = emailTemplateService.getTemplateByCode(code);

        assertNotNull(result);
        assertEquals(expectedDto.getCode(), result.getCode());
    }
}