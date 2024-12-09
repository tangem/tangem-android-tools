package com.tangem.detekt.rules

import io.gitlab.arturbosch.detekt.api.*
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtPsiFactory

class UnsafeStringResourceUsageRule(config: Config) : Rule(config) {

    override val issue: Issue = Issue(
        id = "UnsafeStringResourceUsage",
        severity = Severity.Security,
        description = "Avoid using stringResource directly in the code.",
        debt = Debt.FIVE_MINS,
    )

    private val unsafeComposableFunctionNames = listOf("stringResource", "pluralStringResource")
    private val unsafeResourcesFunctionNames = listOf("getString", "getQuantityString")

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        val functionName = expression.calleeExpression?.text
        if (functionName in unsafeComposableFunctionNames) {
            report(
                CodeSmell(
                    issue = issue,
                    entity = Entity.from(expression),
                    message = "Usage of $functionName method is unsafe. Use the ${functionName}Safe instead.",
                )
            )

            val psiFactory = KtPsiFactory(expression)
            val newCallee = psiFactory.createExpression("${functionName}Safe")
            expression.calleeExpression?.replace(newCallee)
        }
    }

    override fun visit(root: KtFile) {
        super.visit(root)

        val imports = root.importDirectives
        val hasResourcesImport = imports.any { it.importPath?.pathStr == "android.content.res.Resources" }

        if (hasResourcesImport) {
            root.accept(DotQualifiedExpressionVisitor(requiredReceiver = false))
        } else {
            root.accept(DotQualifiedExpressionVisitor(requiredReceiver = true))
        }
    }

    private inner class DotQualifiedExpressionVisitor(private val requiredReceiver: Boolean) : DetektVisitor() {

        override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
            super.visitDotQualifiedExpression(expression)

            val callExpression = expression.selectorExpression as? KtCallExpression
            val functionName = callExpression?.calleeExpression?.text

            val isResourcesReceiver = if (requiredReceiver) {
                val receiverText = expression.receiverExpression.text
                receiverText == "resources"
            } else {
                true
            }

            if (isResourcesReceiver && functionName in unsafeResourcesFunctionNames) {
                report(
                    CodeSmell(
                        issue = issue,
                        entity = Entity.from(expression),
                        message = "Usage of $functionName method is unsafe. Use the ${functionName}Safe instead.",
                    )
                )
            }
        }
    }
}
