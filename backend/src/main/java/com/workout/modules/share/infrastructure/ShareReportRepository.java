package com.workout.modules.share.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 分享报告仓储（数据访问层）。
 * 按 token 公开读取、按 userId 批量删除，禁止循环单查。
 */
public interface ShareReportRepository extends JpaRepository<ShareReportEntity, Long> {

    /**
     * 按公开 token 加载快照。
     */
    Optional<ShareReportEntity> findByToken(String token);

    /**
     * 注销时按用户批量删除分享，禁止循环 deleteById。
     */
    void deleteByUserId(Long userId);

    /**
     * CMS 详情：一次列出该用户全部已有分享，按创建时间倒序。
     */
    List<ShareReportEntity> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * CMS 报告列表：一次列出全部已有分享，按创建时间倒序，禁止循环单查。
     */
    List<ShareReportEntity> findAllByOrderByCreatedAtDesc();
}
