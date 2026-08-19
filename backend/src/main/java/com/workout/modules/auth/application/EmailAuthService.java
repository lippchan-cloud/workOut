package com.workout.modules.auth.application;

import com.workout.common.BusinessException;
import com.workout.common.UnauthorizedException;
import com.workout.modules.auth.api.AuthTokenResponse;
import com.workout.modules.auth.domain.AuthPrincipal;
import com.workout.modules.auth.domain.EmailCodePurpose;
import com.workout.modules.auth.infrastructure.EmailCodeEntity;
import com.workout.modules.auth.infrastructure.EmailCodeRepository;
import com.workout.modules.auth.infrastructure.UserEntity;
import com.workout.modules.auth.infrastructure.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.Duration;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 邮箱绑定、解绑与验证码登录（应用层）。
 * 验证码只存哈希；身份只信 JWT 或已绑定邮箱，禁止客户端传入 userId。
 */
@Service
public class EmailAuthService {

    private static final Logger log = LoggerFactory.getLogger(EmailAuthService.class);
    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final int MAX_FAILS = 5;
    private static final String GENERIC_LOGIN_ERROR = "邮箱或验证码错误";

    private final UserRepository userRepository;
    private final EmailCodeRepository emailCodeRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailSender emailSender;
    private final AuthService authService;
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 注入用户、验证码仓储、哈希、发信与会话签发。
     */
    public EmailAuthService(
            UserRepository userRepository,
            EmailCodeRepository emailCodeRepository,
            PasswordEncoder passwordEncoder,
            EmailSender emailSender,
            AuthService authService) {
        this.userRepository = userRepository;
        this.emailCodeRepository = emailCodeRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailSender = emailSender;
        this.authService = authService;
    }

    /**
     * 按用途发送 4 位数字验证码。LOGIN 可匿名；BIND/UNBIND 必须已登录。
     *
     * @param rawEmail  用户输入邮箱
     * @param rawPurpose BIND / UNBIND / LOGIN
     * @param principal 当前登录主体，匿名时为 null
     */
    @Transactional
    public void sendCode(String rawEmail, String rawPurpose, AuthPrincipal principal) {
        long startMs = System.currentTimeMillis();
        String email = normalizeEmail(rawEmail);
        EmailCodePurpose purpose = parsePurpose(rawPurpose);
        log.info("[邮箱验证码] sendCode start purpose={}, email={}, hasPrincipal={}",
                purpose, maskEmail(email), principal != null);
        Long userId = switch (purpose) {
            case LOGIN -> resolveLoginSendUserId(email);
            case BIND -> resolveBindSendUserId(email, principal);
            case UNBIND -> resolveUnbindSendUserId(email, principal);
        };
        // 60 秒内同一邮箱+用途不得重发
        enforceResendInterval(email, purpose);
        String code = String.format("%04d", secureRandom.nextInt(10_000));
        EmailCodeEntity row = new EmailCodeEntity();
        row.setEmail(email);
        row.setPurpose(purpose);
        // 明文只用于投递，落库为哈希
        row.setCodeHash(passwordEncoder.encode(code));
        row.setUserId(userId);
        Instant now = Instant.now();
        row.setCreatedAt(now);
        row.setExpiresAt(now.plus(CODE_TTL));
        row.setFailCount(0);
        EmailCodeEntity saved = emailCodeRepository.save(row);
        log.info(
                "[邮箱验证码] saved entityType=EmailCodeEntity id={}, purpose={}, userId={}, email={}",
                saved.getId(),
                purpose,
                userId,
                maskEmail(email));
        // 投递验证码（日志或 SMTP），HTTP 不回显
        emailSender.sendVerificationCode(email, purpose.name(), code);
        log.info(
                "[邮箱验证码] sendCode done purpose={}, email={}, elapsedMs={}",
                purpose,
                maskEmail(email),
                System.currentTimeMillis() - startMs);
    }

    /**
     * 校验 BIND 码后把邮箱写到当前用户。
     */
    @Transactional
    public void bind(Long userId, String rawEmail, String code) {
        long startMs = System.currentTimeMillis();
        String email = normalizeEmail(rawEmail);
        log.info("[邮箱绑定] bind start userId={}, email={}", userId, maskEmail(email));
        UserEntity user = loadUser(userId);
        log.info("[邮箱绑定] loaded entityType=UserEntity id={}, username={}, emailBound={}",
                user.getId(), user.getUsername(), user.getEmail() != null);
        if (user.getEmail() != null) {
            log.error("[邮箱绑定] bind failed code=400 msg=请先解绑当前邮箱 userId={}", userId);
            throw new BusinessException("请先解绑当前邮箱");
        }
        UserEntity occupied = userRepository.findByEmail(email).orElse(null);
        if (occupied != null && !occupied.getId().equals(userId)) {
            log.error("[邮箱绑定] bind failed code=400 msg=该邮箱已被绑定 email={}", maskEmail(email));
            throw new BusinessException("该邮箱已被绑定");
        }
        consumeCode(email, EmailCodePurpose.BIND, code, userId, "验证码不正确");
        user.setEmail(email);
        userRepository.save(user);
        log.info(
                "[邮箱绑定] bind done userId={}, email={}, elapsedMs={}",
                userId,
                maskEmail(email),
                System.currentTimeMillis() - startMs);
    }

    /**
     * 校验 UNBIND 码后清空当前用户邮箱。
     */
    @Transactional
    public void unbind(Long userId, String code) {
        long startMs = System.currentTimeMillis();
        log.info("[邮箱解绑] unbind start userId={}", userId);
        UserEntity user = loadUser(userId);
        log.info("[邮箱解绑] loaded entityType=UserEntity id={}, username={}, emailBound={}",
                user.getId(), user.getUsername(), user.getEmail() != null);
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("[邮箱解绑] unbind failed code=400 msg=尚未绑定邮箱 userId={}", userId);
            throw new BusinessException("尚未绑定邮箱");
        }
        String email = user.getEmail();
        consumeCode(email, EmailCodePurpose.UNBIND, code, userId, "验证码不正确");
        user.setEmail(null);
        userRepository.save(user);
        log.info("[邮箱解绑] unbind done userId={}, elapsedMs={}", userId, System.currentTimeMillis() - startMs);
    }

    /**
     * 用已绑定邮箱与 LOGIN 码签发与密码登录相同的 JWT。
     */
    @Transactional
    public AuthTokenResponse loginByEmail(String rawEmail, String code) {
        long startMs = System.currentTimeMillis();
        String email = normalizeEmail(rawEmail);
        log.info("[邮箱登录] loginByEmail start email={}", maskEmail(email));
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.error("[邮箱登录] loginByEmail failed code=400 generic=true email={}", maskEmail(email));
            throw new BusinessException(GENERIC_LOGIN_ERROR);
        }
        log.info("[邮箱登录] loaded entityType=UserEntity id={}, username={}", user.getId(), user.getUsername());
        consumeCode(email, EmailCodePurpose.LOGIN, code, user.getId(), GENERIC_LOGIN_ERROR);
        // 复用密码登录的角色纠偏与签发
        AuthTokenResponse token = authService.completeLogin(user);
        log.info(
                "[邮箱登录] loginByEmail done userId={}, elapsedMs={}",
                user.getId(),
                System.currentTimeMillis() - startMs);
        return token;
    }

    /**
     * LOGIN 发码：未绑定也返回同一失败文案，避免枚举。
     */
    private Long resolveLoginSendUserId(String email) {
        UserEntity user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            log.error("[邮箱验证码] send LOGIN failed code=400 generic=true email={}", maskEmail(email));
            throw new BusinessException(GENERIC_LOGIN_ERROR);
        }
        log.info("[邮箱验证码] login send target userId={}", user.getId());
        return user.getId();
    }

    /**
     * BIND 发码必须登录；邮箱不得被他人占用；已绑过须先解绑。
     */
    private Long resolveBindSendUserId(String email, AuthPrincipal principal) {
        AuthPrincipal current = requirePrincipal(principal);
        UserEntity user = loadUser(current.getUserId());
        if (user.getEmail() != null) {
            log.error("[邮箱验证码] send BIND failed code=400 msg=请先解绑当前邮箱 userId={}", user.getId());
            throw new BusinessException("请先解绑当前邮箱");
        }
        UserEntity occupied = userRepository.findByEmail(email).orElse(null);
        if (occupied != null && !occupied.getId().equals(user.getId())) {
            log.error("[邮箱验证码] send BIND failed code=400 msg=该邮箱已被绑定 email={}", maskEmail(email));
            throw new BusinessException("该邮箱已被绑定");
        }
        return user.getId();
    }

    /**
     * UNBIND 发码必须登录且已绑定；请求邮箱须与绑定邮箱一致。
     */
    private Long resolveUnbindSendUserId(String email, AuthPrincipal principal) {
        AuthPrincipal current = requirePrincipal(principal);
        UserEntity user = loadUser(current.getUserId());
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            log.error("[邮箱验证码] send UNBIND failed code=400 msg=尚未绑定邮箱 userId={}", user.getId());
            throw new BusinessException("尚未绑定邮箱");
        }
        if (!user.getEmail().equals(email)) {
            log.error("[邮箱验证码] send UNBIND failed code=400 msg=邮箱不匹配 userId={}", user.getId());
            throw new BusinessException("请使用当前绑定的邮箱");
        }
        return user.getId();
    }

    /**
     * BIND/UNBIND 必须带 JWT。
     */
    private AuthPrincipal requirePrincipal(AuthPrincipal principal) {
        if (principal == null) {
            log.info("[邮箱验证码] requirePrincipal failed unauthenticated=true");
            throw new UnauthorizedException("未登录或登录已过期");
        }
        return principal;
    }

    /**
     * 同一邮箱+用途 60 秒内拒绝重发。
     */
    private void enforceResendInterval(String email, EmailCodePurpose purpose) {
        EmailCodeEntity latest =
                emailCodeRepository.findTopByEmailAndPurposeOrderByCreatedAtDesc(email, purpose).orElse(null);
        if (latest != null && latest.getCreatedAt().isAfter(Instant.now().minus(RESEND_INTERVAL))) {
            log.error("[邮箱验证码] resend rejected code=400 email={}, purpose={}", maskEmail(email), purpose);
            throw new BusinessException("验证码发送过于频繁");
        }
    }

    /**
     * 校验最新未使用码：过期、次数、哈希，成功则标记 used。
     */
    private void consumeCode(
            String email, EmailCodePurpose purpose, String code, Long expectedUserId, String failMessage) {
        EmailCodeEntity row = emailCodeRepository
                .findTopByEmailAndPurposeAndUsedAtIsNullOrderByCreatedAtDesc(email, purpose)
                .orElse(null);
        if (row == null) {
            log.error("[邮箱验证码] consume failed code=400 missing=true purpose={}, email={}", purpose, maskEmail(email));
            throw new BusinessException(failMessage);
        }
        log.info(
                "[邮箱验证码] loaded entityType=EmailCodeEntity id={}, purpose={}, failCount={}, userId={}",
                row.getId(),
                row.getPurpose(),
                row.getFailCount(),
                row.getUserId());
        if (row.getExpiresAt().isBefore(Instant.now()) || row.getFailCount() >= MAX_FAILS) {
            row.setUsedAt(Instant.now());
            emailCodeRepository.save(row);
            log.error("[邮箱验证码] consume failed code=400 expiredOrLocked id={}", row.getId());
            throw new BusinessException(failMessage);
        }
        if (expectedUserId != null && row.getUserId() != null && !expectedUserId.equals(row.getUserId())) {
            log.error("[邮箱验证码] consume failed code=400 userMismatch id={}", row.getId());
            throw new BusinessException(failMessage);
        }
        if (!passwordEncoder.matches(code, row.getCodeHash())) {
            row.setFailCount(row.getFailCount() + 1);
            if (row.getFailCount() >= MAX_FAILS) {
                row.setUsedAt(Instant.now());
            }
            emailCodeRepository.save(row);
            log.error("[邮箱验证码] consume failed code=400 mismatch id={}, failCount={}", row.getId(), row.getFailCount());
            throw new BusinessException(failMessage);
        }
        row.setUsedAt(Instant.now());
        emailCodeRepository.save(row);
        log.info("[邮箱验证码] consume done id={}, purpose={}", row.getId(), purpose);
    }

    /**
     * 按主键加载用户。
     */
    private UserEntity loadUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(() -> new BusinessException("用户不存在"));
    }

    /**
     * 解析用途枚举。
     */
    private EmailCodePurpose parsePurpose(String rawPurpose) {
        try {
            return EmailCodePurpose.valueOf(rawPurpose.trim().toUpperCase(Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new BusinessException("验证码用途不支持");
        }
    }

    /**
     * 去空白并转小写，保证唯一索引可比。
     */
    private String normalizeEmail(String rawEmail) {
        if (rawEmail == null) {
            throw new BusinessException("请填写邮箱");
        }
        return rawEmail.trim().toLowerCase(Locale.ROOT);
    }

    /**
     * 日志脱敏邮箱。
     */
    private String maskEmail(String email) {
        int at = email == null ? -1 : email.indexOf('@');
        if (at <= 0) {
            return "***";
        }
        return email.charAt(0) + "***" + email.substring(at);
    }
}
