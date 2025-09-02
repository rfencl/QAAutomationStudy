package com.qa.distributed.communication;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Generic message wrapper for communication between coordinator and nodes.
 * Design Decision: Type-safe message envelope pattern to handle different
 * message types in a unified communication protocol.
 */
public class Message<T> {
    public enum Type {
        TASK_ASSIGNMENT, TASK_RESULT, HEARTBEAT, NODE_REGISTRATION, 
        NODE_STATUS_UPDATE, SHUTDOWN_REQUEST, ACK
    }
    
    private final String messageId;
    private final Type type;
    private final String senderId;
    private final String receiverId;
    private final T payload;
    private final LocalDateTime timestamp;
    
    @JsonCreator
    public Message(
            @JsonProperty("messageId") String messageId,
            @JsonProperty("type") Type type,
            @JsonProperty("senderId") String senderId,
            @JsonProperty("receiverId") String receiverId,
            @JsonProperty("payload") T payload,
            @JsonProperty("timestamp") LocalDateTime timestamp) {
        this.messageId = Objects.requireNonNull(messageId, "Message ID cannot be null");
        this.type = Objects.requireNonNull(type, "Message type cannot be null");
        this.senderId = Objects.requireNonNull(senderId, "Sender ID cannot be null");
        this.receiverId = receiverId; // Can be null for broadcast messages
        this.payload = payload;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
    
    public String getMessageId() { return messageId; }
    public Type getType() { return type; }
    public String getSenderId() { return senderId; }
    public String getReceiverId() { return receiverId; }
    public T getPayload() { return payload; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    @Override
    public String toString() {
        return String.format("Message{id='%s', type=%s, from='%s', to='%s'}", 
                           messageId, type, senderId, receiverId);
    }
}
