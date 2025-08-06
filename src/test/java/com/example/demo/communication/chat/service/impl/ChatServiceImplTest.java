package com.example.demo.communication.chat.service.impl;

import com.example.demo.communication.chat.dto.ChatRoomDto;
import com.example.demo.communication.chat.entity.ChatRoom;
import com.example.demo.communication.chat.entity.ChatRoomType;
import com.example.demo.communication.chat.repository.ChatMessageRepository;
import com.example.demo.communication.chat.repository.ChatParticipantRepository;
import com.example.demo.communication.chat.repository.ChatRoomRepository;
import com.example.demo.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ChatServiceImplTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @Mock
    private ChatParticipantRepository chatParticipantRepository;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ChatServiceImpl chatService;

    @Test
    void createChatRoom_shouldReturnChatRoomDto() {
        Long creatorId = 1L;
        List<Long> participantIds = Collections.singletonList(2L);
        String roomName = "Test Room";
        ChatRoomType roomType = ChatRoomType.GROUP;

        ChatRoom chatRoom = new ChatRoom();
        chatRoom.setId(1L);
        chatRoom.setName(roomName);

        ChatRoomDto expectedDto = new ChatRoomDto();
        expectedDto.setId(1L);
        expectedDto.setName(roomName);

        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
        when(modelMapper.map(chatRoom, ChatRoomDto.class)).thenReturn(expectedDto);

        ChatRoomDto result = chatService.createChatRoom(roomName, roomType, "description", creatorId, participantIds);

        assertNotNull(result);
        assertEquals(expectedDto.getId(), result.getId());
    }
}