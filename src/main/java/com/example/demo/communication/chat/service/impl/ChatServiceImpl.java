package com.example.demo.communication.chat.service.impl;

import com.example.demo.communication.chat.dto.ChatMessageDto;
import com.example.demo.communication.chat.dto.ChatRoomDto;
import com.example.demo.communication.chat.dto.ChatParticipantDto;
import com.example.demo.communication.chat.entity.*;
import com.example.demo.communication.chat.repository.ChatMessageRepository;
import com.example.demo.communication.chat.repository.ChatParticipantRepository;
import com.example.demo.communication.chat.repository.ChatRoomRepository;
import com.example.demo.communication.chat.service.ChatService;
import com.example.demo.communication.exception.ChatRoomNotFoundException;
import com.example.demo.communication.exception.ChatMessageNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    public ChatRoomDto createChatRoom(String name, ChatRoomType type, String description, Long createdBy, List<Long> participantIds) {
        log.info("Creating chat room: name={}, type={}, createdBy={}", name, type, createdBy);
        
        ChatRoom chatRoom = ChatRoom.builder()
                .name(name)
                .type(type)
                .description(description)
                .createdBy(createdBy)
                .isActive(true)
                .build();
        
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        
        // Add creator as owner
        ChatParticipant owner = ChatParticipant.builder()
                .room(savedRoom)
                .userId(createdBy)
                .role(ChatParticipantRole.OWNER)
                .joinedAt(Instant.now())
                .isActive(true)
                .build();
        chatParticipantRepository.save(owner);
        
        // Add other participants as members
        if (participantIds != null && !participantIds.isEmpty()) {
            List<ChatParticipant> participants = participantIds.stream()
                    .filter(userId -> !userId.equals(createdBy)) // Don't add creator twice
                    .map(userId -> ChatParticipant.builder()
                            .room(savedRoom)
                            .userId(userId)
                            .role(ChatParticipantRole.MEMBER)
                            .joinedAt(Instant.now())
                            .isActive(true)
                            .build())
                    .collect(Collectors.toList());
            chatParticipantRepository.saveAll(participants);
        }
        
        return convertToDto(savedRoom);
    }

    @Override
    public ChatRoomDto getChatRoom(Long roomId, Long userId) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found: " + roomId));
        
        if (!chatRoomRepository.hasUserAccess(roomId, userId)) {
            throw new AccessDeniedException("User does not have access to this chat room");
        }
        
        return convertToDto(room);
    }

    @Override
    public List<ChatRoomDto> getUserChatRooms(Long userId) {
        List<ChatRoom> rooms = chatRoomRepository.findUserChatRooms(userId);
        return rooms.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ChatMessageDto sendMessage(Long roomId, Long senderId, String content, ChatMessageType messageType) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found: " + roomId));
        
        if (!chatRoomRepository.hasUserAccess(roomId, senderId)) {
            throw new AccessDeniedException("User does not have access to this chat room");
        }
        
        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSenderId(senderId);
        message.setContent(content);
        message.setMessageType(messageType != null ? messageType : ChatMessageType.TEXT);
        message.setIsEdited(false);
        message.setIsDeleted(false);
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        
        // Update room's last message info
        room.setLastMessageAt(savedMessage.getCreatedAt());
        room.setLastMessageId(savedMessage.getId());
        chatRoomRepository.save(room);
        
        ChatMessageDto messageDto = convertToDto(savedMessage);
        
        // Send real-time notification to room participants
        messagingTemplate.convertAndSend("/topic/chat/room/" + roomId, messageDto);
        
        log.info("Message sent to room {}: {}", roomId, savedMessage.getId());
        return messageDto;
    }

    @Override
    public Page<ChatMessageDto> getChatMessages(Long roomId, Long userId, Pageable pageable) {
        if (!chatRoomRepository.hasUserAccess(roomId, userId)) {
            throw new AccessDeniedException("User does not have access to this chat room");
        }
        
        Page<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
        return messages.map(this::convertToDto);
    }

    @Override
    @Transactional
    public ChatMessageDto editMessage(Long messageId, Long userId, String newContent) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatMessageNotFoundException("Message not found: " + messageId));
        
        if (!message.getSenderId().equals(userId)) {
            throw new AccessDeniedException("Only message sender can edit the message");
        }
        
        if (message.getIsDeleted()) {
            throw new IllegalStateException("Cannot edit deleted message");
        }
        
        message.setContent(newContent);
        message.setIsEdited(true);
        message.setEditedAt(Instant.now());
        
        ChatMessage savedMessage = chatMessageRepository.save(message);
        ChatMessageDto messageDto = convertToDto(savedMessage);
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/chat/room/" + message.getRoom().getId() + "/edit", messageDto);
        
        return messageDto;
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new ChatMessageNotFoundException("Message not found: " + messageId));
        
        // Check if user can delete (sender or room admin/owner)
        boolean canDelete = message.getSenderId().equals(userId) || 
                           isRoomAdminOrOwner(message.getRoom().getId(), userId);
        
        if (!canDelete) {
            throw new AccessDeniedException("User does not have permission to delete this message");
        }
        
        message.setIsDeleted(true);
        message.setDeletedAt(Instant.now());
        chatMessageRepository.save(message);
        
        // Send real-time update
        messagingTemplate.convertAndSend("/topic/chat/room/" + message.getRoom().getId() + "/delete", 
                                       java.util.Map.of("messageId", messageId));
        
        log.info("Message deleted: {}", messageId);
    }

    @Override
    @Transactional
    public ChatParticipantDto addParticipant(Long roomId, Long userId, Long addedBy) {
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ChatRoomNotFoundException("Chat room not found: " + roomId));
        
        if (!isRoomAdminOrOwner(roomId, addedBy)) {
            throw new AccessDeniedException("Only room admin or owner can add participants");
        }
        
        // Check if user is already a participant
        if (chatParticipantRepository.existsByRoomIdAndUserIdAndIsActiveTrue(roomId, userId)) {
            throw new IllegalStateException("User is already a participant in this room");
        }
        
        ChatParticipant participant = ChatParticipant.builder()
                .room(room)
                .userId(userId)
                .role(ChatParticipantRole.MEMBER)
                .joinedAt(Instant.now())
                .isActive(true)
                .build();
        
        ChatParticipant savedParticipant = chatParticipantRepository.save(participant);
        
        // Send system message
        sendMessage(roomId, addedBy, "User joined the room", ChatMessageType.SYSTEM);
        
        return convertToDto(savedParticipant);
    }

    @Override
    @Transactional
    public void removeParticipant(Long roomId, Long userId, Long removedBy) {
        if (!isRoomAdminOrOwner(roomId, removedBy) && !userId.equals(removedBy)) {
            throw new AccessDeniedException("Only room admin/owner or the user themselves can remove participant");
        }
        
        chatParticipantRepository.removeParticipant(roomId, userId, Instant.now());
        
        // Send system message
        sendMessage(roomId, removedBy, "User left the room", ChatMessageType.SYSTEM);
        
        log.info("Participant removed from room {}: {}", roomId, userId);
    }

    @Override
    @Transactional
    public void markMessagesAsRead(Long roomId, Long userId, Long lastReadMessageId) {
        chatParticipantRepository.updateLastRead(roomId, userId, Instant.now(), lastReadMessageId);
        log.debug("Messages marked as read for user {} in room {}", userId, roomId);
    }

    @Override
    public Long getUnreadMessageCount(Long userId) {
        return chatMessageRepository.countTotalUnreadMessages(userId);
    }

    @Override
    public Long getUnreadMessageCount(Long roomId, Long userId) {
        return chatMessageRepository.countUnreadMessages(roomId, userId);
    }

    @Override
    public Page<ChatMessageDto> searchMessages(Long roomId, Long userId, String searchTerm, Pageable pageable) {
        if (!chatRoomRepository.hasUserAccess(roomId, userId)) {
            throw new AccessDeniedException("User does not have access to this chat room");
        }
        
        Page<ChatMessage> messages = chatMessageRepository.searchMessagesInRoom(roomId, searchTerm, pageable);
        return messages.map(this::convertToDto);
    }

    private boolean isRoomAdminOrOwner(Long roomId, Long userId) {
        List<ChatParticipant> adminParticipants = chatParticipantRepository
                .findByRoomIdAndRoleAndIsActiveTrue(roomId, ChatParticipantRole.ADMIN);
        List<ChatParticipant> ownerParticipants = chatParticipantRepository
                .findByRoomIdAndRoleAndIsActiveTrue(roomId, ChatParticipantRole.OWNER);
        
        return adminParticipants.stream().anyMatch(p -> p.getUserId().equals(userId)) ||
               ownerParticipants.stream().anyMatch(p -> p.getUserId().equals(userId));
    }

    private ChatRoomDto convertToDto(ChatRoom room) {
        return ChatRoomDto.builder()
                .id(room.getId())
                .name(room.getName())
                .type(room.getType())
                .description(room.getDescription())
                .avatarUrl(room.getAvatarUrl())
                .createdBy(room.getCreatedBy())
                .isPrivate(room.isPrivate())
                .isActive(room.isActive())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .lastMessageAt(room.getLastMessageAt())
                .lastMessageId(room.getLastMessageId())
                .build();
    }

    private ChatMessageDto convertToDto(ChatMessage message) {
        return ChatMessageDto.builder()
                .id(message.getId())
                .roomId(message.getRoom().getId())
                .senderId(message.getSenderId())
                .content(message.getContent())
                .messageType(message.getMessageType())
                .createdAt(message.getCreatedAt())
                .isEdited(message.getIsEdited())
                .editedAt(message.getEditedAt())
                .isDeleted(message.getIsDeleted())
                .deletedAt(message.getDeletedAt())
                .build();
    }

    private ChatParticipantDto convertToDto(ChatParticipant participant) {
        return ChatParticipantDto.builder()
                .id(participant.getId())
                .roomId(participant.getRoom().getId())
                .userId(participant.getUserId())
                .role(participant.getRole())
                .joinedAt(participant.getJoinedAt())
                .lastReadAt(participant.getLastReadAt())
                .lastReadMessageId(participant.getLastReadMessageId())
                .isMuted(participant.isMuted())
                .isActive(participant.isActive())
                .leftAt(participant.getLeftAt())
                .build();
    }
}