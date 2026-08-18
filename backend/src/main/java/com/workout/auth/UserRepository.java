package com.workout.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 用户持久化仓储（数据访问层）。
 * 仅负责按用户名查询与存在性判断，不包含鉴权业务规则。
 */
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    /**
     * 按用户名加载用户实体。
     *
     * @param username 登录名
     * @return 用户实体（可能为空）
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 判断用户名是否已注册。
     *
     * @param username 登录名
     * @return true 表示已存在
     */
    boolean existsByUsername(String username);
}
