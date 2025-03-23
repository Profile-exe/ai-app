package com.aiapp.chat.repository;

import com.aiapp.chat.entity.Chat;
import com.aiapp.chat.entity.Thread;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {

    Chat findTopByThreadOrderByCreatedAtDesc(Thread thread);
    
    List<Chat> findByThreadOrderByCreatedAtAsc(Thread thread);
    
    Page<Chat> findByThread(Thread thread, Pageable pageable);
    
    void deleteByThread(Thread thread);
}
