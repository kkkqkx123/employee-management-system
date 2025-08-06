package com.example.demo.communication.notification.service.impl;

import com.example.demo.communication.notification.dto.NotificationCreateRequest;
import com.example.demo.communication.notification.dto.NotificationDto;
import com.example.demo.communication.notification.entity.Notification;
import com.example.demo.communication.notification.repository.NotificationRepository;
import com.example.demo.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Test
    void createNotification_shouldReturnNotificationDto() {
        NotificationCreateRequest request = new NotificationCreateRequest();
        request.setTitle("Test Title");
        request.setContent("Test Content");
        request.setUserId(1L);

        Notification notification = new Notification();
        notification.setId(1L);
        notification.setTitle("Test Title");

        NotificationDto expectedDto = new NotificationDto();
        expectedDto.setId(1L);
        expectedDto.setTitle("Test Title");

        when(modelMapper.map(request, Notification.class)).thenReturn(notification);
        when(notificationRepository.save(any(Notification.class))).thenReturn(notification);
        when(modelMapper.map(notification, NotificationDto.class)).thenReturn(expectedDto);

        NotificationDto result = notificationService.createNotification(request, 2L);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
    }
}