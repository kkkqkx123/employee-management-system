package com.example.demo.communication.notification.controller;

import com.example.demo.communication.notification.dto.NotificationDto;
import com.example.demo.communication.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
public class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "1")
    void getMyNotifications_shouldReturnPageOfNotifications() throws Exception {
        NotificationDto notificationDto = new NotificationDto();
        notificationDto.setId(1L);
        notificationDto.setTitle("Test Title");
        Page<NotificationDto> page = new PageImpl<>(Collections.singletonList(notificationDto));

        when(notificationService.getUserNotifications(any(Long.class), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/notifications/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}