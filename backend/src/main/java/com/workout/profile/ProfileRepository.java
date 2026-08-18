package com.workout.profile;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 资料仓储（数据访问层）。
 * 按 userId 读写，禁止跨用户查询。
 */
public interface ProfileRepository extends JpaRepository<ProfileEntity, Long> {

    /**
     * 按用户加载资料。
     */
    Optional<ProfileEntity> findByUserId(Long userId);
}
