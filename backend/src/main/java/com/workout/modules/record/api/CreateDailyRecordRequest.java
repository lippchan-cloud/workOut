package com.workout.modules.record.api;

import com.workout.modules.record.domain.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

/**
 * 新建日记录请求（API 边界 DTO）。
 * 不含 userId；内容长度与空白在服务层再校验，以便返回统一中文文案。
 */
public class CreateDailyRecordRequest {

    @NotNull(message = "请选择类型")
    private RecordType type;

    @NotBlank(message = "请填写内容")
    private String content;

    @NotNull(message = "请填写记录时间")
    private Instant recordedAt;

    /**
     * 读取类型。
     */
    public RecordType getType() {
        return type;
    }

    /**
     * 写入类型。
     */
    public void setType(RecordType type) {
        this.type = type;
    }

    /**
     * 读取内容。
     */
    public String getContent() {
        return content;
    }

    /**
     * 写入内容。
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * 读取记录时间。
     */
    public Instant getRecordedAt() {
        return recordedAt;
    }

    /**
     * 写入记录时间。
     */
    public void setRecordedAt(Instant recordedAt) {
        this.recordedAt = recordedAt;
    }
}
