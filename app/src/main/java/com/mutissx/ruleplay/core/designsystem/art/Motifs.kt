package com.mutissx.ruleplay.core.designsystem.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.mutissx.ruleplay.domain.model.ArtMotif
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Line drawings of playing surfaces. Each motif fills the current draw area and is drawn with a single
 * colour so it can serve both as cover texture (tilted, low alpha) and as a flat field diagram.
 */
internal fun DrawScope.drawMotif(motif: ArtMotif, color: Color, stroke: Float) {
    val m = MotifPainter(this, color, stroke)
    when (motif) {
        ArtMotif.PITCH -> m.pitch()
        ArtMotif.COURT -> m.court()
        ArtMotif.NET_COURT -> m.netCourt()
        ArtMotif.PADEL_CAGE -> m.padelCage()
        ArtMotif.WALL_COURT -> m.wallCourt()
        ArtMotif.DIAMOND -> m.diamond()
        ArtMotif.GRIDIRON -> m.gridiron()
        ArtMotif.OVAL -> m.oval()
        ArtMotif.TABLE -> m.table()
        ArtMotif.GREEN -> m.green()
        ArtMotif.RING -> m.ring()
        ArtMotif.MAT -> m.mat()
        ArtMotif.PISTE -> m.piste()
        ArtMotif.POOL -> m.pool()
        ArtMotif.TRACK -> m.track()
        ArtMotif.ROAD -> m.road()
        ArtMotif.CIRCUIT -> m.circuit()
        ArtMotif.RINK -> m.rink()
        ArtMotif.WAVE -> m.wave()
        ArtMotif.PARK -> m.park()
        ArtMotif.SHEET -> m.sheet()
        ArtMotif.RANGE -> m.range()
        ArtMotif.RIVER -> m.river()
        ArtMotif.CLOTH -> m.cloth()
        ArtMotif.FIELD -> m.field()
    }
}

private class MotifPainter(val d: DrawScope, val color: Color, val sw: Float) {
    val w = d.size.width
    val h = d.size.height
    val s = Stroke(width = sw, cap = StrokeCap.Round)

    /** Playing area inset so lines never touch the edges. */
    val area = Rect(w * 0.08f, h * 0.1f, w * 0.92f, h * 0.9f)

    fun x(f: Float) = area.left + area.width * f
    fun y(f: Float) = area.top + area.height * f

    fun line(x1: Float, y1: Float, x2: Float, y2: Float, width: Float = sw, alpha: Float = 1f, effect: PathEffect? = null) =
        d.drawLine(color.copy(alpha = color.alpha * alpha), Offset(x(x1), y(y1)), Offset(x(x2), y(y2)), width, StrokeCap.Round, effect)

    fun rect(l: Float, t: Float, r: Float, b: Float, alpha: Float = 1f, corner: Float = 0f) =
        d.drawRoundRect(
            color.copy(alpha = color.alpha * alpha),
            topLeft = Offset(x(l), y(t)),
            size = Size(x(r) - x(l), y(b) - y(t)),
            cornerRadius = CornerRadius(corner, corner),
            style = s,
        )

    fun fillRect(l: Float, t: Float, r: Float, b: Float, alpha: Float) =
        d.drawRect(color.copy(alpha = color.alpha * alpha), Offset(x(l), y(t)), Size(x(r) - x(l), y(b) - y(t)))

    fun circle(cx: Float, cy: Float, radius: Float, alpha: Float = 1f, fill: Boolean = false) =
        d.drawCircle(
            color.copy(alpha = color.alpha * alpha),
            radius = radius,
            center = Offset(x(cx), y(cy)),
            style = if (fill) androidx.compose.ui.graphics.drawscope.Fill else s,
        )

    fun arc(cx: Float, cy: Float, radius: Float, start: Float, sweep: Float, alpha: Float = 1f) =
        d.drawArc(
            color.copy(alpha = color.alpha * alpha), start, sweep, false,
            topLeft = Offset(x(cx) - radius, y(cy) - radius), size = Size(radius * 2, radius * 2), style = s,
        )

    val unit get() = minOf(area.width, area.height)

    fun pitch() {
        rect(0f, 0f, 1f, 1f)
        line(0.5f, 0f, 0.5f, 1f)
        circle(0.5f, 0.5f, unit * 0.16f)
        circle(0.5f, 0.5f, sw * 1.4f, fill = true)
        for (side in listOf(0f, 1f)) {
            val dir = if (side == 0f) 1f else -1f
            rect(minOf(side, side + dir * 0.16f), 0.22f, maxOf(side, side + dir * 0.16f), 0.78f)
            rect(minOf(side, side + dir * 0.055f), 0.37f, maxOf(side, side + dir * 0.055f), 0.63f)
            circle(side + dir * 0.11f, 0.5f, sw * 1.2f, fill = true)
            arc(side + dir * 0.11f, 0.5f, unit * 0.16f, if (side == 0f) -53f else 127f, 106f)
        }
    }

    fun court() {
        rect(0f, 0f, 1f, 1f)
        line(0.5f, 0f, 0.5f, 1f)
        circle(0.5f, 0.5f, unit * 0.13f)
        for (side in listOf(0f, 1f)) {
            val dir = if (side == 0f) 1f else -1f
            rect(minOf(side, side + dir * 0.2f), 0.34f, maxOf(side, side + dir * 0.2f), 0.66f)
            arc(side + dir * 0.2f, 0.5f, unit * 0.13f, if (side == 0f) -90f else 90f, 180f)
            // Three-point line: corner straights joined by a wide arc.
            line(side, 0.06f, side + dir * 0.1f, 0.06f)
            line(side, 0.94f, side + dir * 0.1f, 0.94f)
            arc(side + dir * 0.05f, 0.5f, area.height * 0.44f, if (side == 0f) -79f else 101f, 158f)
            circle(side + dir * 0.05f, 0.5f, unit * 0.025f)
        }
    }

    fun netCourt() {
        rect(0f, 0f, 1f, 1f)
        line(0f, 0.12f, 1f, 0.12f)
        line(0f, 0.88f, 1f, 0.88f)
        line(0.5f, -0.04f, 0.5f, 1.04f, width = sw * 2.4f)
        line(0.24f, 0.12f, 0.24f, 0.88f)
        line(0.76f, 0.12f, 0.76f, 0.88f)
        line(0.24f, 0.5f, 0.76f, 0.5f)
        line(0f, 0.5f, 0.02f, 0.5f)
        line(1f, 0.5f, 0.98f, 0.5f)
    }

    fun padelCage() {
        rect(0f, 0f, 1f, 1f, corner = sw)
        line(0.5f, -0.03f, 0.5f, 1.03f, width = sw * 2.4f)
        line(0.2f, 0f, 0.2f, 1f)
        line(0.8f, 0f, 0.8f, 1f)
        line(0.2f, 0.5f, 0.8f, 0.5f)
        // Glass and mesh panels behind the baselines.
        for (i in 1..4) {
            val f = i / 5f
            line(-0.04f, f, 0f, f, alpha = 0.6f)
            line(1f, f, 1.04f, f, alpha = 0.6f)
        }
        rect(-0.04f, 0f, 0f, 1f, alpha = 0.6f)
        rect(1f, 0f, 1.04f, 1f, alpha = 0.6f)
    }

    fun wallCourt() {
        // Front wall seen head-on: out line, service line, tin; plus floor lines.
        rect(0f, 0f, 1f, 1f)
        line(0f, 0.12f, 1f, 0.12f)
        line(0f, 0.42f, 1f, 0.42f)
        line(0f, 0.6f, 1f, 0.6f, width = sw * 3f)
        line(0f, 0.82f, 1f, 0.82f)
        line(0.5f, 0.82f, 0.5f, 1f)
        rect(0f, 0.82f, 0.2f, 1f)
        rect(0.8f, 0.82f, 1f, 1f)
    }

    fun diamond() {
        val home = Offset(x(0.5f), y(0.95f))
        val r = area.height * 0.42f
        val first = Offset(home.x + r * 0.7f, home.y - r * 0.7f)
        val second = Offset(home.x, home.y - r * 1.4f)
        val third = Offset(home.x - r * 0.7f, home.y - r * 0.7f)
        val p = Path().apply { moveTo(home.x, home.y); lineTo(first.x, first.y); lineTo(second.x, second.y); lineTo(third.x, third.y); close() }
        d.drawPath(p, color, style = s)
        listOf(first, second, third).forEach { d.drawRect(color, it - Offset(sw * 2, sw * 2), Size(sw * 4, sw * 4)) }
        d.drawCircle(color, r * 0.12f, Offset(home.x, home.y - r * 0.68f), style = s)
        // Foul lines and the outfield fence.
        d.drawLine(color, home, Offset(home.x + r * 2.2f, home.y - r * 2.2f), sw)
        d.drawLine(color, home, Offset(home.x - r * 2.2f, home.y - r * 2.2f), sw)
        d.drawArc(color, -135f, 90f, false, Offset(home.x - r * 2.1f, home.y - r * 2.1f), Size(r * 4.2f, r * 4.2f), style = s)
        d.drawArc(color, -135f, 90f, false, Offset(home.x - r * 1.25f, home.y - r * 1.25f), Size(r * 2.5f, r * 2.5f), style = s, alpha = 0.6f)
    }

    fun gridiron() {
        rect(0f, 0f, 1f, 1f)
        fillRect(0f, 0f, 0.08f, 1f, 0.18f)
        fillRect(0.92f, 0f, 1f, 1f, 0.18f)
        for (i in 0..10) {
            val f = 0.08f + 0.084f * i
            line(f, 0f, f, 1f, alpha = if (i == 5) 1f else 0.75f)
        }
        for (i in 0..40) {
            val f = 0.08f + 0.021f * i
            line(f, 0.36f, f, 0.39f, alpha = 0.6f)
            line(f, 0.61f, f, 0.64f, alpha = 0.6f)
        }
    }

    fun oval() {
        d.drawOval(color, Offset(x(0f), y(0f)), Size(area.width, area.height), style = s)
        d.drawOval(color.copy(alpha = color.alpha * 0.6f), Offset(x(0.18f), y(0.16f)), Size(area.width * 0.64f, area.height * 0.68f), style = Stroke(sw, pathEffect = PathEffect.dashPathEffect(floatArrayOf(sw * 4, sw * 4))))
        rect(0.455f, 0.3f, 0.545f, 0.7f)
        line(0.44f, 0.36f, 0.56f, 0.36f)
        line(0.44f, 0.64f, 0.56f, 0.64f)
    }

    fun table() {
        rect(0f, 0.08f, 1f, 0.92f, corner = sw)
        line(0f, 0.5f, 1f, 0.5f, alpha = 0.6f)
        line(0.5f, 0f, 0.5f, 1f, width = sw * 2.4f)
    }

    fun green() {
        // Topographic contours around the hole.
        val c = Offset(x(0.62f), y(0.45f))
        for (i in 1..9) {
            val rx = area.width * 0.07f * i
            val ry = area.height * 0.06f * i
            val path = Path()
            val steps = 48
            for (k in 0..steps) {
                val a = (2 * PI * k / steps)
                val wobble = 1f + 0.06f * sin(a * 3 + i).toFloat()
                val px = c.x + (rx * wobble * cos(a)).toFloat()
                val py = c.y + (ry * wobble * sin(a)).toFloat()
                if (k == 0) path.moveTo(px, py) else path.lineTo(px, py)
            }
            d.drawPath(path, color.copy(alpha = color.alpha * (1f - i * 0.07f)), style = s)
        }
        d.drawCircle(color, unit * 0.025f, c)
    }

    fun ring() {
        rect(0.06f, 0.06f, 0.94f, 0.94f)
        for (i in 1..3) {
            val f = 0.06f - i * 0.018f
            rect(f, f, 1f - f, 1f - f, alpha = 0.7f)
        }
        listOf(0.06f to 0.06f, 0.94f to 0.06f, 0.06f to 0.94f, 0.94f to 0.94f).forEach { (cx, cy) ->
            circle(cx, cy, sw * 3f, fill = true)
        }
        line(0.06f, 0.06f, 0.94f, 0.94f, alpha = 0.15f)
        line(0.94f, 0.06f, 0.06f, 0.94f, alpha = 0.15f)
    }

    fun mat() {
        val c = Offset(x(0.5f), y(0.5f))
        rect(0.05f, 0f, 0.95f, 1f, alpha = 0.6f)
        d.drawCircle(color, unit * 0.48f, c, style = s)
        d.drawCircle(color.copy(alpha = color.alpha * 0.35f), unit * 0.4f, c, style = Stroke(sw * 5))
        d.drawCircle(color, unit * 0.06f, c, style = s)
        line(0.46f, 0.5f, 0.54f, 0.5f)
    }

    fun piste() {
        rect(0f, 0.38f, 1f, 0.62f)
        line(0.5f, 0.38f, 0.5f, 0.62f)
        line(0.43f, 0.38f, 0.43f, 0.62f, alpha = 0.7f)
        line(0.57f, 0.38f, 0.57f, 0.62f, alpha = 0.7f)
        fillRect(0f, 0.38f, 0.1f, 0.62f, 0.2f)
        fillRect(0.9f, 0.38f, 1f, 0.62f, 0.2f)
        line(0.1f, 0.38f, 0.1f, 0.62f)
        line(0.9f, 0.38f, 0.9f, 0.62f)
    }

    fun pool() {
        rect(0f, 0f, 1f, 1f)
        val lanes = 8
        for (i in 1 until lanes) {
            val f = i / lanes.toFloat()
            line(0f, f, 1f, f, width = sw * 1.6f, effect = PathEffect.dashPathEffect(floatArrayOf(sw * 2.5f, sw * 2.5f)))
        }
        for (i in 0 until lanes) {
            val f = (i + 0.5f) / lanes
            line(0.04f, f, 0.96f, f, width = sw * 2f, alpha = 0.35f)
            line(0.04f, f - 0.03f, 0.04f, f + 0.03f, width = sw * 2f, alpha = 0.35f)
            line(0.96f, f - 0.03f, 0.96f, f + 0.03f, width = sw * 2f, alpha = 0.35f)
        }
        line(0.15f, 0f, 0.15f, 1f, alpha = 0.4f, effect = PathEffect.dashPathEffect(floatArrayOf(sw, sw * 3)))
        line(0.85f, 0f, 0.85f, 1f, alpha = 0.4f, effect = PathEffect.dashPathEffect(floatArrayOf(sw, sw * 3)))
    }

    fun track() {
        for (i in 0..7) {
            val inset = i * 0.035f
            val l = x(inset); val t = y(inset * 1.6f); val r = x(1f - inset); val b = y(1f - inset * 1.6f)
            val radius = (b - t) / 2f
            d.drawRoundRect(color.copy(alpha = color.alpha * (0.5f + i * 0.06f)), Offset(l, t), Size(r - l, b - t), CornerRadius(radius, radius), style = s)
        }
        line(0.6f, 0f, 0.6f, 0.28f, width = sw * 2.4f)
    }

    fun road() {
        val p = Path().apply {
            moveTo(x(-0.1f), y(1.05f))
            cubicTo(x(0.3f), y(0.7f), x(0.1f), y(0.3f), x(0.55f), y(0.25f))
            cubicTo(x(0.9f), y(0.2f), x(0.85f), y(-0.05f), x(1.1f), y(-0.1f))
        }
        d.drawPath(p, color.copy(alpha = color.alpha * 0.18f), style = Stroke(unit * 0.22f, cap = StrokeCap.Round))
        d.drawPath(p, color, style = Stroke(sw * 1.4f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(sw * 6, sw * 5))))
    }

    fun circuit() {
        val p = Path().apply {
            moveTo(x(0.12f), y(0.7f))
            cubicTo(x(0.05f), y(0.2f), x(0.35f), y(0.05f), x(0.5f), y(0.3f))
            cubicTo(x(0.6f), y(0.45f), x(0.75f), y(0.05f), x(0.9f), y(0.2f))
            cubicTo(x(1.02f), y(0.32f), x(0.95f), y(0.8f), x(0.75f), y(0.88f))
            cubicTo(x(0.55f), y(0.96f), x(0.2f), y(0.98f), x(0.12f), y(0.7f))
            close()
        }
        d.drawPath(p, color.copy(alpha = color.alpha * 0.16f), style = Stroke(unit * 0.11f, cap = StrokeCap.Round))
        d.drawPath(p, color, style = s)
        d.drawPath(p, color.copy(alpha = color.alpha * 0.8f), style = Stroke(sw * 3f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(sw * 3, sw * 3))))
        line(0.3f, 0.86f, 0.34f, 0.98f, width = sw * 2.4f)
    }

    fun rink() {
        val radius = area.height * 0.28f
        d.drawRoundRect(color, Offset(x(0f), y(0f)), Size(area.width, area.height), CornerRadius(radius, radius), style = s)
        line(0.5f, 0f, 0.5f, 1f, width = sw * 2.5f)
        line(0.33f, 0f, 0.33f, 1f, width = sw * 2f, alpha = 0.8f)
        line(0.67f, 0f, 0.67f, 1f, width = sw * 2f, alpha = 0.8f)
        line(0.06f, 0f, 0.06f, 1f, alpha = 0.7f)
        line(0.94f, 0f, 0.94f, 1f, alpha = 0.7f)
        circle(0.5f, 0.5f, unit * 0.15f)
        listOf(0.17f, 0.83f).forEach { cx -> listOf(0.27f, 0.73f).forEach { cy -> circle(cx, cy, unit * 0.13f); circle(cx, cy, sw * 1.6f, fill = true) } }
        arc(0.06f, 0.5f, unit * 0.06f, -90f, 180f)
        arc(0.94f, 0.5f, unit * 0.06f, 90f, 180f)
    }

    fun wave() {
        for (i in 0..11) {
            val baseY = h * (0.15f + i * 0.075f)
            val amp = h * (0.03f + 0.004f * i)
            val p = Path()
            val steps = 40
            for (k in 0..steps) {
                val px = w * k / steps
                val py = baseY + amp * sin((k / steps.toFloat()) * 2 * PI * 1.6 + i * 0.6).toFloat()
                if (k == 0) p.moveTo(px, py) else p.lineTo(px, py)
            }
            d.drawPath(p, color.copy(alpha = color.alpha * (0.4f + i * 0.05f)), style = s)
        }
    }

    fun park() {
        for (i in 0..6) {
            val inset = i * 0.05f
            d.drawArc(color.copy(alpha = color.alpha * (1f - i * 0.1f)), 0f, 180f, false,
                Offset(x(0.05f + inset), y(-0.6f + inset)), Size(area.width * (0.9f - inset * 2), area.height * (1.4f - inset * 2)), style = s)
        }
        line(0f, 0.95f, 1f, 0.95f)
        rect(0.35f, 0.82f, 0.65f, 0.95f, alpha = 0.8f)
    }

    fun sheet() {
        rect(0.06f, 0f, 0.94f, 1f)
        val c = Offset(x(0.5f), y(0.24f))
        listOf(0.36f to 1f, 0.27f to 0.7f, 0.17f to 1f, 0.06f to 0.7f).forEach { (r, a) ->
            d.drawCircle(color.copy(alpha = color.alpha * a), area.width * r, c, style = Stroke(sw * 2f))
        }
        line(0.06f, 0.24f, 0.94f, 0.24f)
        line(0.5f, 0f, 0.5f, 1f, alpha = 0.6f)
        line(0.06f, 0.55f, 0.94f, 0.55f, width = sw * 2f)
        line(0.06f, 0.92f, 0.94f, 0.92f, width = sw * 2f, alpha = 0.6f)
    }

    fun range() {
        for (i in 0..5) {
            val f = 0.1f + i * 0.17f
            line(f, 0f, f, 1f, alpha = 0.35f, effect = PathEffect.dashPathEffect(floatArrayOf(sw * 2, sw * 4)))
        }
        for (i in 0..4) {
            val cx = 0.12f + i * 0.19f
            circle(cx, 0.2f, unit * 0.07f)
            circle(cx, 0.2f, unit * 0.035f, alpha = 0.7f)
        }
        line(0f, 0.85f, 1f, 0.85f, width = sw * 2f)
    }

    fun river() {
        for (i in 0..5) {
            val f = 0.1f + i * 0.16f
            line(0f, f, 1f, f, width = sw * 2f, effect = PathEffect.dashPathEffect(floatArrayOf(sw * 0.1f, sw * 5f)))
        }
        line(0.85f, 0f, 0.85f, 1f, width = sw * 2f)
    }

    fun cloth() {
        rect(0f, 0f, 1f, 1f, corner = sw * 3)
        line(0.21f, 0f, 0.21f, 1f)
        arc(0.21f, 0.5f, unit * 0.17f, 90f, 180f)
        listOf(0f, 0.5f, 1f).forEach { cx -> listOf(0f, 1f).forEach { cy -> circle(cx, cy, sw * 3.5f, fill = true) } }
        listOf(0.21f, 0.5f, 0.75f, 0.91f).forEach { cx -> circle(cx, 0.5f, sw * 1.2f, fill = true) }
    }

    fun field() {
        rect(0f, 0f, 1f, 1f)
        line(0.18f, 0f, 0.18f, 1f)
        line(0.82f, 0f, 0.82f, 1f)
        line(0.5f, 0f, 0.5f, 1f, alpha = 0.7f)
        circle(0.09f, 0.5f, unit * 0.09f)
        circle(0.91f, 0.5f, unit * 0.09f)
        circle(0.5f, 0.5f, sw * 2f, fill = true)
    }
}
