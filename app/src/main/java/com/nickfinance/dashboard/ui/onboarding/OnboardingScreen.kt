package com.nickfinance.dashboard.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nickfinance.dashboard.data.model.TemplateType
import com.nickfinance.dashboard.ui.theme.AppTheme

@Composable
fun OnboardingScreen(
    onComplete: (List<TemplateType>) -> Unit,
    onSkip: () -> Unit
) {
    val colors = AppTheme.colors
    val selectedTemplates = remember { mutableStateListOf<TemplateType>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = "\uD83D\uDCB0",
            fontSize = 64.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "财务仪表盘",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "管理你的个人财务数据\n可视化资产、收支与投资趋势",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(48.dp))

        Text(
            text = "选择要创建的数据表模板",
            style = MaterialTheme.typography.titleSmall,
            color = colors.textPrimary
        )

        Spacer(modifier = Modifier.height(16.dp))

        TemplateType.entries.forEach { template ->
            val isSelected = template in selectedTemplates
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(colors.cardSurface)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) colors.accentBlue else colors.border,
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable {
                        if (isSelected) selectedTemplates.remove(template)
                        else selectedTemplates.add(template)
                    }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = template.icon,
                    fontSize = 24.sp
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = template.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "${template.columns.size} 列",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {
                        if (it) selectedTemplates.add(template)
                        else selectedTemplates.remove(template)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = colors.accentBlue,
                        uncheckedColor = colors.textTertiary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = { onComplete(selectedTemplates.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = selectedTemplates.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(
                containerColor = colors.accentBlue
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("开始使用", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onSkip) {
            Text(
                text = "稍后再说",
                color = colors.textSecondary
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
