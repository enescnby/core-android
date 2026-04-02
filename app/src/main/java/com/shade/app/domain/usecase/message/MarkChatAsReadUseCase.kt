package com.shade.app.domain.usecase.message

import com.shade.app.data.local.entities.MessageStatus
import com.shade.app.domain.repository.ChatRepository
import com.shade.app.domain.repository.MessageRepository
import javax.inject.Inject

class MarkChatAsReadUseCase @Inject constructor(
    private val messageRepository: MessageRepository,
    private val chatRepository: ChatRepository,
    private val sendReceiptUseCase: SendReceiptUseCase
) {
    suspend operator fun invoke(chatId: String) {
        val unreadMessages = messageRepository.getUnreadMessages(chatId)

        if (unreadMessages.isEmpty()) {
            chatRepository.resetUnreadCount(chatId)
            return
        }

        val messageIds = unreadMessages.map { it.messageId }

        messageIds.forEach { messageId ->
            messageRepository.updateMessageStatus(messageId, MessageStatus.READ)
        }

        sendReceiptUseCase.sendBatchReadReceipts(messageIds)

        chatRepository.resetUnreadCount(chatId)
    }
}
