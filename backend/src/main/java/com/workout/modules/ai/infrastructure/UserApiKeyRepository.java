package com.workout.modules.ai.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户 API Key 仓储（数据访问层）。
 * 按 userId 单查或批量 IN 查询，禁止循环 findById。
 */
public interface UserApiKeyRepository extends JpaRepository<UserApiKeyEntity, Long> {

    /**
     * 按用户加载绑定的 key。
     */
    Optional<UserApiKeyEntity> findByUserId(Long userId);

    /**
     * 批量按用户加载，供 CMS 列表与批量改写。
     */
    List<UserApiKeyEntity> findByUserIdIn(Collection<Long> userIds);
}
