package com.workout.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.modules.ai.application.AiContextCompressService;
import com.workout.modules.ai.infrastructure.AiContextChunkEntity;
import com.workout.modules.ai.infrastructure.AiContextChunkRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import com.workout.support.TestUsernames;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 询问 AI 时按字数上限拼入同用户历史压缩记录。
 */
@SpringBootTest
@ActiveProfiles("test")
class AiContextHistoryAssembleTest {

    @Autowired
    private AiContextCompressService compressService;

    @Autowired
    private AiContextChunkRepository chunkRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void assembleShouldIncludeOwnHistoryWithin1000CharsAndSkipOthers() {
        Long userA = insertUser("histA");
        Long userB = insertUser("histB");
        insertChunk(userB, Instant.parse("2026-08-01T00:00:00Z"), "OTHER-USER-SHOULD-NOT-APPEAR");
        insertChunk(userA, Instant.parse("2026-08-10T00:00:00Z"), "HISTORY-OLD-" + "o".repeat(920));
        insertChunk(userA, Instant.parse("2026-08-17T00:00:00Z"), "HISTORY-NEW-MARKER");
        String current = "userId=" + userA + "\nCURRENT-MARKER\n";

        String assembled = compressService.assembleWithHistory(userA, current);

        assertThat(assembled).contains("CURRENT-MARKER");
        assertThat(assembled).contains("HISTORY-NEW-MARKER");
        assertThat(assembled).doesNotContain("OTHER-USER-SHOULD-NOT-APPEAR");
        assertThat(assembled).doesNotContain("HISTORY-OLD-");
        assertThat(assembled.length()).isLessThanOrEqualTo(AiContextCompressService.MAX_PROMPT_CHARS);
    }

    private Long insertUser(String prefix) {
        UserEntity user = new UserEntity();
        user.setUsername(TestUsernames.unique(prefix));
        user.setPasswordHash("x");
        user.setCreatedAt(Instant.now());
        return userRepository.save(user).getId();
    }

    private void insertChunk(Long userId, Instant createdAt, String summary) {
        AiContextChunkEntity row = new AiContextChunkEntity();
        row.setUserId(userId);
        row.setSourceType("SHARE");
        row.setSourceRef("t-" + createdAt.toEpochMilli());
        row.setSummaryText(summary);
        row.setEmbedHash(Integer.toHexString(summary.hashCode()) + createdAt.toEpochMilli());
        row.setEmbeddingJson("[0]");
        row.setCreatedAt(createdAt);
        chunkRepository.save(row);
    }
}
