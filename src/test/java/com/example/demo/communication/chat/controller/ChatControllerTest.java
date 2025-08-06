package com.example.demo.communication.chat.controller;

import com.example.demo.communication.chat.dto.ChatRoomDto;
import com.example.demo.communication.chat.dto.CreateChatRoomRequest;
import com.example.demo.communication.chat.entity.ChatRoomType;
import com.example.demo.communication.chat.service.ChatService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChatController.class)
public class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "1")
    void createChatRoom_shouldReturnCreated() throws Exception {
        CreateChatRoomRequest createRequest = new CreateChatRoomRequest();
        createRequest.setName("Test Room");
        createRequest.setParticipantIds(Collections.singletonList(2L));
        createRequest.setType(ChatRoomType.GROUP);

        ChatRoomDto returnedDto = new ChatRoomDto();
        returnedDto.setId(1L);
        returnedDto.setName("Test Room");

        when(chatService.createChatRoom(anyString(), any(ChatRoomType.class), anyString(), any(Long.class), anyList())).thenReturn(returnedDto);

        mockMvc.perform(post("/api/v1/chat/rooms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }
}