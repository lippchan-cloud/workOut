package com.workout.modules.profile.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资料变更历史仓储（数据访问层）。
 * 按 userId 批量读写，禁止按记录循环单查。
 */
public interface ProfileHistoryRepository extends JpaRepository<ProfileHistoryEntity, Long> {

    /**
     * 按用户加载全部快照，变更时间升序，供导出匹配与曲线。
     */
    List<ProfileHistoryEntity> findByUserIdOrderByChangedAtAscIdAsc(Long userId);

    /**
     * 取该用户最新一条快照，用于判断是否需要追加历史。
     */
    Optional<ProfileHistoryEntity> findTopByUserIdOrderByChangedAtDescIdDesc(Long userId);

    /**
     * 按用户批量删除历史（注销账号），禁止循环 deleteById。
     */
    void deleteByUserId(Long userId);
}
