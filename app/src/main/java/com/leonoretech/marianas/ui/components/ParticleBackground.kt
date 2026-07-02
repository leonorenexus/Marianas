package com.leonoretech.marianas.ui.components

import androidx.compose.animation.core.withInfiniteAnimationFrameMillis
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.sqrt
import kotlin.random.Random

private data class Particle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    val radius: Float,
    val alpha: Float
)

/**
 * Animated particle + grid background, ported from the web app's canvas
 * animation (particles drifting + connecting lines + faint grid overlay).
 * Drawn behind the chat content at low opacity so it doesn't interfere
 * with text readability.
 */
@Composable
fun ParticleBackground(modifier: Modifier = Modifier, particleCount: Int = 45) {
    var canvasSize by remember { mutableStateOf(Offset(1f, 1f)) }
    val particles = remember {
        List(particleCount) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                vx = (Random.nextFloat() - 0.5f) * 0.0006f,
                vy = (Random.nextFloat() - 0.5f) * 0.0006f,
                radius = Random.nextFloat() * 2.2f + 0.6f,
                alpha = Random.nextFloat() * 0.5f + 0.15f
            )
        }
    }
    var frameTick by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            withInfiniteAnimationFrameMillis { frameTick = it }
            particles.forEach { p ->
                p.x = (p.x + p.vx + 1f) % 1f
                p.y = (p.y + p.vy + 1f) % 1f
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        canvasSize = Offset(size.width, size.height)
        drawGrid(this)
        drawParticlesAndLinks(this, particles)
    }
}

private fun drawGrid(scope: DrawScope) {
    val gridSize = 60.dp_to_px(scope)
    val gridColor = Color(0xFF0064C8).copy(alpha = 0.05f)
    var x = 0f
    while (x < scope.size.width) {
        scope.drawLine(gridColor, Offset(x, 0f), Offset(x, scope.size.height), strokeWidth = 1f)
        x += gridSize
    }
    var y = 0f
    while (y < scope.size.height) {
        scope.drawLine(gridColor, Offset(0f, y), Offset(scope.size.width, y), strokeWidth = 1f)
        y += gridSize
    }
}

private fun drawParticlesAndLinks(scope: DrawScope, particles: List<Particle>) {
    val w = scope.size.width
    val h = scope.size.height
    val linkDistance = 110.dp_to_px(scope)

    val positions = particles.map { Offset(it.x * w, it.y * h) }

    for (i in particles.indices) {
        val p1 = positions[i]
        scope.drawCircle(
            color = Color(0xFF60C8FF).copy(alpha = particles[i].alpha),
            radius = particles[i].radius,
            center = p1
        )
        for (j in i + 1 until particles.size) {
            val p2 = positions[j]
            val dx = p1.x - p2.x
            val dy = p1.y - p2.y
            val dist = sqrt(dx * dx + dy * dy)
            if (dist < linkDistance) {
                val lineAlpha = 0.12f * (1f - dist / linkDistance)
                scope.drawLine(
                    color = Color(0xFF9B4DFF).copy(alpha = lineAlpha),
                    start = p1,
                    end = p2,
                    strokeWidth = 0.6f
                )
            }
        }
    }
}

/** Small helper so the grid/link distances scale sensibly across device densities. */
private fun Float.dp_to_px(scope: DrawScope): Float = this * scope.density
private fun Int.dp_to_px(scope: DrawScope): Float = this.toFloat() * scope.density
