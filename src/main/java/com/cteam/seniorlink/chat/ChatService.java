package com.cteam.seniorlink.chat;

import com.cteam.seniorlink.chat.message.ChatMessageDto;
import com.cteam.seniorlink.chat.message.ChatMessageEntity;
import com.cteam.seniorlink.chat.message.ChatMessageRepository;
import com.cteam.seniorlink.chat.room.ChatRoomDto;
import com.cteam.seniorlink.chat.room.ChatRoomEntity;
import com.cteam.seniorlink.chat.room.ChatRoomRepository;
import com.cteam.seniorlink.serviceBoard.ServiceEntity;
import com.cteam.seniorlink.serviceBoard.ServiceRepository;
import com.cteam.seniorlink.user.UserEntity;
import com.cteam.seniorlink.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final ServiceRepository serviceRepository;

    // 채팅방 목록 조회(마이페이지 -> 목록)
    @Transactional
    public List<ChatRoomDto> findRoomsByUser(String username) {
        return chatRoomRepository.findBySenderUsernameOrReceiverUsername(username, username).stream()
                .map(room -> new ChatRoomDto(
                        room.getId(),
                        room.getSender().getUsername(),
                        room.getReceiver().getUsername()))
                .collect(Collectors.toList());
    }

    // 채팅방 생성(서비스페이지 -> [문의하기])
    @Transactional
    public ChatRoomDto createOrFindChatRoom(String senderUsername, Long serviceId) {
        UserEntity sender = userRepository.findByUsername(senderUsername)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        UserEntity receiver = serviceRepository.findById(serviceId)
                .orElseThrow(() -> new EntityNotFoundException("Service not found"))
                .getCaregiver();

        return chatRoomRepository.findBySenderAndReceiver(sender, receiver)
                .map(ChatRoomDto::fromEntity)
                .orElseGet(() -> {
                    ChatRoomDto chatRoomDto = new ChatRoomDto();
                    chatRoomDto.setSender(sender.getUsername());
                    chatRoomDto.setReceiver(receiver.getUsername());
                    ChatRoomEntity chatRoom = chatRoomDto.toEntity(sender, receiver);
                    chatRoom = chatRoomRepository.save(chatRoom);
                    chatRoomDto.setRoomId(chatRoom.getId());
                    return chatRoomDto;
                });
    }

    // 메시지 저장
    @Transactional
    public void saveMessage(ChatMessageDto chatMessageDto) {
        chatMessageDto.setTimestamp(
                Optional.ofNullable(chatMessageDto.getTimestamp()).orElse(LocalDateTime.now())
        );

        ChatRoomEntity chatRoom = chatRoomRepository.findById(chatMessageDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 룸ID : " + chatMessageDto.getRoomId()));

        ChatMessageEntity chatMessageEntity = chatMessageDto.toEntity(chatRoom);
        chatMessageRepository.save(chatMessageEntity);
    }

    // 이전 채팅 메시지 조회
    @Transactional(readOnly = true)
    public List<ChatMessageDto> findMessagesByRoomId(Long roomId) {
        return chatMessageRepository.findByChatRoomId(roomId)
                .stream()
                .map(message -> new ChatMessageDto(
                        message.getChatRoom().getId(),
                        message.getSender(),
                        message.getMessage(),
                        message.getTimestamp()))
                .collect(Collectors.toList());
    }
}


