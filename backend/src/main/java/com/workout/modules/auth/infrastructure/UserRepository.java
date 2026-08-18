package com.workout.modules.auth.infrastructure;

import java.util.Collection;
import java.util.List;
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
     * 批量按用户名加载（种子赋 key 等），禁止循环 findByUsername。
     */
    List<UserEntity> findByUsernameIn(Collection<String> usernames);

    /**
     * 判断用户名是否已注册。
     *
     * @param username 登录名
     * @return true 表示已存在
     */
    boolean existsByUsername(String username);
}
