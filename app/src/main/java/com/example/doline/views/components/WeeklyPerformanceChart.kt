package com.example.doline.views.components

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import com.example.doline.ui.theme.Spacing
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.AnimationMode
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line

@Composable
fun WeeklyPerformanceChart(labels: List<String>, amounts: List<Double>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    ){
        LineChart(
            modifier = Modifier.fillMaxSize(),
            data = remember(labels, amounts) {
                listOf(
                    Line(
                        label = "Sales",
                        values = amounts,
                        color = SolidColor(Color(0xFF9E4300)),
                        firstGradientFillColor = Color(0xFF9E4300).copy(alpha = .5f),
                        secondGradientFillColor = Color.Transparent,
                        strokeAnimationSpec = tween(
                            2000,
                            easing = EaseInOutCubic
                        ),
                        gradientAnimationDelay = 1000,
                        drawStyle = DrawStyle.Stroke(width = 2.dp),
                    )
                )
            },
            animationMode = AnimationMode.Together(delayBuilder = {
                it * 500L
            }),
            gridProperties = GridProperties(
                enabled = true,
                xAxisProperties = GridProperties.AxisProperties(
                    enabled = true,
                ),
                yAxisProperties = GridProperties.AxisProperties(
                    enabled = true,
                    lineCount = 7,
                ),
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true
            ),
            labelProperties = LabelProperties(
                labels = labels,
                enabled = true
            )
        )
    }
}
