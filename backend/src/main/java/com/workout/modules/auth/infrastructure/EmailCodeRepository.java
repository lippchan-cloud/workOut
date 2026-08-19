package com.workout.modules.auth.infrastructure;

import com.workout.modules.auth.domain.EmailCodePurpose;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 邮箱验证码仓储（数据访问层）。
 * 按邮箱+用途取最新一条，注销时按用户或邮箱批量删除，禁止循环单查。
 */
public interface EmailCodeRepository extends JpaRepository<EmailCodeEntity, Long> {

    /**
     * 取该邮箱该用途最近一条（含已使用），用于重发限流。
     */
    Optional<EmailCodeEntity> findTopByEmailAndPurposeOrderByCreatedAtDesc(String email, EmailCodePurpose purpose);

    /**
     * 取该邮箱该用途最近一条未使用码，供校验。
     */
    Optional<EmailCodeEntity> findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(
            String email, EmailCodePurpose purpose);

    /**
     * 注销时按用户批量删除验证码。
     */
    void deleteByUserId(Long userId);

    /**
     * 注销时按邮箱批量删除验证码，避免残留 LOGIN 码。
     */
    void deleteByEmail(String email);
}
