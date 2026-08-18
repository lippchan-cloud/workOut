package com.workout.modules.ai.application;

import com.workout.modules.ai.infrastructure.AiContextChunkEntity;
import com.workout.modules.ai.infrastructure.AiContextChunkRepository;
import com.workout.modules.share.api.ShareSnapshotResponse;
import com.workout.modules.share.api.ShareSnapshotResponse.ShareBodyPoint;
import com.workout.modules.share.api.ShareSnapshotResponse.ShareRecordItem;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.StringJoiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 本地上下文压缩与简易向量化（应用层）。
 * 摘要 + hash + embedding_json 全部落 MySQL；按 userId 隔离；不上 Redis/独立向量库。
 */
@Service
public class AiContextCompressService {

    private static final Logger log = LoggerFactory.getLogger(AiContextCompressService.class);
    private static final int MAX_CONTENT = 40;
    private static final int MAX_RECORDS = 40;
    private static final int EMBED_DIM = 32;

    private final AiContextChunkRepository chunkRepository;

    /**
     * 注入摘要仓储。
     */
    public AiContextCompressService(AiContextChunkRepository chunkRepository) {
        this.chunkRepository = chunkRepository;
    }

    /**
     * 压缩快照为 prompt 上下文，并按 userId+hash 落库去重（含 embedding_json）。
     *
     * @param userId   分享所有者，必须与快照一致
     * @param snapshot 仅含该用户数据的快照
     * @param token    分享 token 作 source_ref
     */
    @Transactional
    public String compressAndStore(Long userId, ShareSnapshotResponse snapshot, String token) {
        log.info(
                "[上下文] compress start userId={}, tokenPrefix={}, recordSize={}",
                userId,
                tokenPrefix(token),
                snapshot.getRecords() == null ? 0 : snapshot.getRecords().size());
        if (userId == null) {
            throw new IllegalArgumentException("userId required");
        }
        String summary = buildSummary(userId, snapshot);
        String hash = sha256(summary);
        // 同用户 hash 命中则复用，绝不跨 userId 查询
        Optional<AiContextChunkEntity> existing =
                chunkRepository.findFirstByUserIdAndEmbedHashOrderByCreatedAtDesc(userId, hash);
        if (existing.isPresent()) {
            log.info(
                    "[上下文] compress reuse entityType=AiContextChunkEntity id={}, userId={}",
                    existing.get().getId(),
                    userId);
            return existing.get().getSummaryText();
        }
        String embeddingJson = toEmbeddingJson(summary);
        AiContextChunkEntity row = new AiContextChunkEntity();
        row.setUserId(userId);
        row.setSourceType("SHARE");
        row.setSourceRef(token);
        row.setSummaryText(summary);
        row.setEmbedHash(hash);
        row.setEmbeddingJson(embeddingJson);
        row.setCreatedAt(Instant.now());
        AiContextChunkEntity saved = chunkRepository.save(row);
        log.info(
                "[上下文] compress saved entityType=AiContextChunkEntity id={}, userId={}, hashPrefix={}, embedDim={}",
                saved.getId(),
                userId,
                hash.substring(0, 8),
                EMBED_DIM);
        return summary;
    }

    /**
     * 构建含 userId 边界的摘要文本。
     */
    String buildSummary(Long userId, ShareSnapshotResponse snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("userId=").append(userId).append('\n');
        sb.append("displayName=").append(nullToEmpty(snapshot.getDisplayName())).append('\n');
        sb.append("range=").append(snapshot.getFrom()).append("~").append(snapshot.getTo()).append('\n');
        List<ShareBodyPoint> body = snapshot.getBodyHistory();
        if (body != null && !body.isEmpty()) {
            ShareBodyPoint first = body.get(0);
            ShareBodyPoint last = body.get(body.size() - 1);
            sb.append("heightCm=")
                    .append(last.getHeightCm() != null ? last.getHeightCm() : first.getHeightCm())
                    .append('\n');
            sb.append("weightKg=")
                    .append(last.getWeightKg() != null ? last.getWeightKg() : first.getWeightKg())
                    .append('\n');
            sb.append("bodyPoints=").append(body.size()).append('\n');
        } else {
            sb.append("bodyPoints=0\n");
        }
        List<ShareRecordItem> records = snapshot.getRecords();
        int consume = 0;
        int intake = 0;
        if (records != null) {
            int limit = Math.min(records.size(), MAX_RECORDS);
            for (int i = 0; i < limit; i++) {
                ShareRecordItem item = records.get(i);
                if ("CONSUME".equals(item.getType())) {
                    consume++;
                } else if ("INTAKE".equals(item.getType())) {
                    intake++;
                }
                String content = nullToEmpty(item.getContent());
                if (content.length() > MAX_CONTENT) {
                    content = content.substring(0, MAX_CONTENT) + "…";
                }
                sb.append(item.getType())
                        .append('|')
                        .append(item.getRecordedAt())
                        .append('|')
                        .append(content)
                        .append('\n');
            }
        }
        sb.append("consumeCount=").append(consume).append(" intakeCount=").append(intake).append('\n');
        return sb.toString();
    }

    /**
     * 简易 hashing trick 向量，序列化为 JSON 数组存 MySQL TEXT。
     */
    String toEmbeddingJson(String summary) {
        double[] vec = new double[EMBED_DIM];
        String[] tokens = summary.toLowerCase(Locale.ROOT).split("[\\s|=~,，。；;]+");
        for (String token : tokens) {
            if (token.isBlank()) {
                continue;
            }
            int idx = Math.floorMod(token.hashCode(), EMBED_DIM);
            vec[idx] += 1.0;
        }
        StringJoiner joiner = new StringJoiner(",", "[", "]");
        Arrays.stream(vec).forEach(v -> joiner.add(String.format(Locale.ROOT, "%.4f", v)));
        return joiner.toString();
    }

    private static String sha256(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception ex) {
            throw new IllegalStateException("sha256 failed", ex);
        }
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String tokenPrefix(String token) {
        if (token == null || token.isEmpty()) {
            return "";
        }
        return token.substring(0, Math.min(8, token.length())) + "...";
    }
}
