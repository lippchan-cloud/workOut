package com.workout.modules.auth.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 修改密码请求（API 边界 DTO）。
 * 不含 userId；身份只从 JWT 取。
 */
public class ChangePasswordRequest {

    @NotBlank(message = "请填写当前密码")
    private String currentPassword;

    @NotBlank(message = "密码长度需为 6～64 位")
    @Size(min = 6, max = 64, message = "密码长度需为 6～64 位")
    private String newPassword;

    /**
     * 当前密码明文。
     */
    public String getCurrentPassword() {
        return currentPassword;
    }

    /**
     * 写入当前密码。
     */
    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    /**
     * 新密码明文。
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * 写入新密码。
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
