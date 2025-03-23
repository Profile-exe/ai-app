package com.aiapp.chat.repository;

import com.aiapp.chat.entity.Thread;
import com.aiapp.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThreadRepository extends JpaRepository<Thread, Long> {

    Page<Thread> findByUser(User user, Pageable pageable);

    @Query("SELECT t FROM Thread t WHERE t.user = :user AND t.updatedAt > :timestamp ORDER BY t.updatedAt DESC")
    Optional<Thread> findRecentThreadByUser(@Param("user") User user, @Param("timestamp") LocalDateTime timestamp);

    boolean existsByIdAndUser(Long id, User user);
}
