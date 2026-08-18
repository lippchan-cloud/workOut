package com.workout.modules.profile.infrastructure;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资料仓储（数据访问层）。
 * 按 userId 读写；CMS 批量查询用 IN，禁止循环单查。
 */
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    /**
     * 按用户加载资料。
     */
    Optional<ProfileEntity> findByUserId(Long userId);

    /**
     * 按一批 userId 批量加载资料，供 CMS 列表一次拼装。
     */
    List<ProfileEntity> findByUserIdIn(Collection<Long> userIds);

    /**
     * 按用户批量删除资料（注销账号），禁止循环 deleteById。
     */
    void deleteByUserId(Long userId);
}
