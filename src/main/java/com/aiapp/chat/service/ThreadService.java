package com.aiapp.chat.service;

import com.aiapp.chat.entity.Thread;
import com.aiapp.chat.exception.ThreadNotFoundException;
import com.aiapp.chat.repository.ThreadRepository;
import com.aiapp.user.entity.User;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ThreadService {

    private final ThreadRepository threadRepository;

    private static final int THREAD_TIMEOUT_MINUTES = 30;

    @Transactional(readOnly = true)
    public Page<Thread> getUserThreads(User user, Pageable pageable) {
        return threadRepository.findByUser(user, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Thread> getAllThreads(Pageable pageable) {
        return threadRepository.findAll(pageable);
    }

    @Transactional
    public Thread getOrCreateThread(User user, String initialQuestion) {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(THREAD_TIMEOUT_MINUTES);

        // 30분 이내에 업데이트된 스레드가 있는지 확인
        return threadRepository.findRecentThreadByUser(user, threshold)
                .orElseGet(() -> createNewThread(user, initialQuestion));
    }

    @Transactional
    public Thread createNewThread(User user, String title) {
        Thread thread = Thread.builder()
                .user(user)
                .title(formatTitle(title))
                .build();

        return threadRepository.save(thread);
    }

    private String formatTitle(String title) {
        if (title == null || title.isBlank()) {
            return "새 대화";
        }
        return title;
    }

    @Transactional
    public Thread updateThreadTitle(Long threadId, String newTitle) {
        Thread thread = getThreadByIdOrThrow(threadId);
        thread.updateTitle(formatTitle(newTitle));
        return threadRepository.save(thread);
    }

    @Transactional(readOnly = true)
    public Thread getThreadByIdOrThrow(Long id) {
        return threadRepository.findById(id)
                .orElseThrow(() -> ThreadNotFoundException.EXCEPTION);
    }

    @Transactional
    public void delete(Thread thread) {
        threadRepository.delete(thread);
    }
}
