package com.tange.detekt.rules

import com.tangem.detekt.rules.UnsafeStringResourceUsageRule
import io.gitlab.arturbosch.detekt.test.TestConfig
import io.gitlab.arturbosch.detekt.test.lint
import kotlin.test.Test
import kotlin.test.assertEquals

class UnsafeStringResourceUsageRuleTest {

    private val rule = UnsafeStringResourceUsageRule(TestConfig())

    @Test
    fun `detects Resources import and getString with resources receiver usage`() {
        val code = """
            import android.content.res.Resources

            fun test(resources: Resources) {
                val text = resources.getString(R.string.ok)
            }
        """.trimIndent()

        val findings = rule.lint(code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `detects Resources import and getString usage`() {
        val code = """
            import android.content.res.Resources

            fun Resources.test() {
                return this.getString(R.string.ok)
            }
        """.trimIndent()

        val findings = rule.lint(code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `detects getString with resources receiver usage`() {
        val code = """
            fun test(resources: Resources) {
                val text = resources.getString(R.string.ok)
            }
        """.trimIndent()

        val findings = rule.lint(code)
        assertEquals(1, findings.size)
    }

    @Test
    fun `doesn't detect getString usage`() {
        val code = """
            fun Resources.test() {
                return this.getString(R.string.ok)
            }
        """.trimIndent()

        val findings = rule.lint(code)
        assertEquals(0, findings.size)
    }
}
