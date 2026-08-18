package com.workout.modules.profile.domain;

import com.workout.modules.profile.infrastructure.ProfileHistoryEntity;
import java.time.Instant;
import java.util.List;

/**
 * 按时间点解析当时有效的身体资料快照（领域匹配，无 I/O）。
 * 输入历史必须已按 changedAt、id 升序。
 */
public final class ProfileHistoryResolver {

    private ProfileHistoryResolver() {}

    /**
     * 返回 changedAt 不晚于 at 的最后一条快照；没有则 null。
     *
     * @param sortedAsc 已升序的用户历史
     * @param at        事项记录时间
     * @return 当时有效快照，可能为 null
     */
    public static ProfileHistoryEntity resolve(List<ProfileHistoryEntity> sortedAsc, Instant at) {
        if (sortedAsc == null || sortedAsc.isEmpty() || at == null) {
            return null;
        }
        ProfileHistoryEntity match = null;
        for (ProfileHistoryEntity row : sortedAsc) {
            if (row.getChangedAt() == null || row.getChangedAt().isAfter(at)) {
                break;
            }
            match = row;
        }
        return match;
    }
}
