package com.workout.ai;

import static org.assertj.core.api.Assertions.assertThat;

import com.workout.modules.ai.application.PhysioScientistPrompts;
import org.junit.jupiter.api.Test;

/**
 * 系统提示必须强制简体中文 Markdown。
 */
class PhysioScientistPromptsTest {

    @Test
    void systemPromptShouldRequireSimplifiedChineseMarkdown() {
        assertThat(PhysioScientistPrompts.SYSTEM)
                .contains("简体中文")
                .contains("Markdown")
                .contains("代码围栏");
        assertThat(PhysioScientistPrompts.userMessage(7L, "userId=7\nCURRENT"))
                .contains("简体中文 Markdown")
                .contains("userId=7");
    }
}
