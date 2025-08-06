package com.example.demo.communication.announcement.controller;

import com.example.demo.communication.announcement.dto.AnnouncementCreateRequest;
import com.example.demo.communication.announcement.dto.AnnouncementDto;
import com.example.demo.communication.announcement.dto.AnnouncementUpdateRequest;
import com.example.demo.communication.announcement.service.AnnouncementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnnouncementController.class)
public class AnnouncementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnnouncementService announcementService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(authorities = "ANNOUNCEMENT_MANAGE")
    void createAnnouncement_shouldReturnCreated() throws Exception {
        AnnouncementCreateRequest createRequest = new AnnouncementCreateRequest();
        createRequest.setTitle("Test Title");
        createRequest.setContent("Test Content");

        AnnouncementDto returnedDto = new AnnouncementDto();
        returnedDto.setId(1L);
        returnedDto.setTitle("Test Title");
        returnedDto.setContent("Test Content");

        when(announcementService.createAnnouncement(any(AnnouncementCreateRequest.class), any(Long.class))).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/announcements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(authorities = "ANNOUNCEMENT_MANAGE")
    void updateAnnouncement_shouldReturnOk() throws Exception {
        Long announcementId = 1L;
        AnnouncementUpdateRequest updateRequest = new AnnouncementUpdateRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated Content");

        AnnouncementDto returnedDto = new AnnouncementDto();
        returnedDto.setId(announcementId);
        returnedDto.setTitle("Updated Title");
        returnedDto.setContent("Updated Content");

        when(announcementService.updateAnnouncement(eq(announcementId), any(AnnouncementUpdateRequest.class), any(Long.class))).thenReturn(returnedDto);

        mockMvc.perform(put("/api/v1/announcements/{id}", announcementId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
    }
}