package com.mutissx.ruleplay.core.designsystem.art

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.lerp
import com.mutissx.ruleplay.domain.model.ArtGlyph
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Colours a glyph is painted with, derived from the sport's art palette. */
internal data class GlyphPalette(
    /** Main material colour (leather, rubber, frame). */
    val material: Color,
    /** Bright highlight used for seams, accents and lights. */
    val accent: Color,
    /** Deep colour for details and shading. */
    val ink: Color,
)

/**
 * Oversized equipment silhouettes — the "hero object" of each sport cover. Everything is drawn from
 * primitives around [c] with a nominal radius [r], so a glyph scales cleanly from thumbnails to heroes.
 */
internal fun DrawScope.drawGlyph(glyph: ArtGlyph, c: Offset, r: Float, p: GlyphPalette) {
    val g = GlyphPainter(this, c, r, p)
    when (glyph) {
        ArtGlyph.FOOTBALL -> g.football()
        ArtGlyph.BASKETBALL -> g.basketball()
        ArtGlyph.TENNIS_BALL -> g.tennisBall()
        ArtGlyph.PADEL_RACKET -> g.padelRacket()
        ArtGlyph.VOLLEYBALL -> g.volleyball(waterPolo = false)
        ArtGlyph.WATER_POLO_BALL -> g.volleyball(waterPolo = true)
        ArtGlyph.HANDBALL -> g.handball()
        ArtGlyph.RUGBY_BALL -> g.rugbyBall()
        ArtGlyph.AMERICAN_FOOTBALL -> g.americanFootball()
        ArtGlyph.CRICKET -> g.cricket()
        ArtGlyph.BASEBALL -> g.baseball()
        ArtGlyph.SHUTTLECOCK -> g.shuttlecock()
        ArtGlyph.TABLE_TENNIS -> g.tableTennis()
        ArtGlyph.PICKLEBALL -> g.pickleball()
        ArtGlyph.GOLF -> g.golf()
        ArtGlyph.BOXING_GLOVE -> g.boxingGlove()
        ArtGlyph.BELT -> g.belt()
        ArtGlyph.OCTAGON -> g.octagon()
        ArtGlyph.FENCING -> g.fencing()
        ArtGlyph.GOGGLES -> g.goggles()
        ArtGlyph.HURDLE -> g.hurdle()
        ArtGlyph.WHEEL -> g.wheel()
        ArtGlyph.CHEQUERED_FLAG -> g.chequeredFlag()
        ArtGlyph.PUCK -> g.puck()
        ArtGlyph.HOCKEY_STICK -> g.hockeyStick()
        ArtGlyph.SURFBOARD -> g.surfboard()
        ArtGlyph.SKATEBOARD -> g.skateboard()
        ArtGlyph.CURLING_STONE -> g.curlingStone()
        ArtGlyph.TARGET -> g.target()
        ArtGlyph.RAIDER -> g.raider()
        ArtGlyph.LACROSSE -> g.lacrosse()
        ArtGlyph.RATTAN_BALL -> g.rattanBall()
        ArtGlyph.HURLEY -> g.hurley()
        ArtGlyph.NETBALL_HOOP -> g.netballHoop()
        ArtGlyph.DISC -> g.disc()
        ArtGlyph.SQUASH -> g.squash()
        ArtGlyph.WRESTLING -> g.wrestling()
        ArtGlyph.OARS -> g.oars()
        ArtGlyph.SNOOKER -> g.snooker()
    }
}

private class GlyphPainter(val d: DrawScope, val c: Offset, val r: Float, val p: GlyphPalette) {

    fun o(dx: Float, dy: Float) = Offset(c.x + dx * r, c.y + dy * r)
    fun stroke(width: Float, cap: StrokeCap = StrokeCap.Round) = Stroke(width = width * r, cap = cap, join = StrokeJoin.Round)

    /** Shaded sphere; returns the clip path so seams can be drawn inside. */
    fun sphere(center: Offset, radius: Float, color: Color): Path {
        d.drawCircle(
            Brush.radialGradient(
                0f to lerp(color, Color.White, 0.35f),
                0.55f to color,
                1f to lerp(color, p.ink, 0.55f),
                center = Offset(center.x - radius * 0.35f, center.y - radius * 0.4f),
                radius = radius * 1.6f,
            ),
            radius, center,
        )
        return Path().apply { addOval(androidx.compose.ui.geometry.Rect(center, radius)) }
    }

    fun rimLight(center: Offset, radius: Float) {
        d.drawArc(
            Color.White.copy(alpha = 0.35f), 200f, 70f, false,
            topLeft = Offset(center.x - radius * 0.86f, center.y - radius * 0.86f),
            size = Size(radius * 1.72f, radius * 1.72f), style = Stroke(radius * 0.05f, cap = StrokeCap.Round),
        )
    }

    fun groundShadow(center: Offset, width: Float) {
        d.drawOval(
            Brush.radialGradient(listOf(Color.Black.copy(alpha = 0.45f), Color.Transparent), center = center, radius = width / 2),
            topLeft = Offset(center.x - width / 2, center.y - width * 0.12f), size = Size(width, width * 0.24f),
        )
    }

    // region balls

    fun football() {
        groundShadow(o(0f, 1.02f), r * 1.8f)
        val clip = sphere(c, r, lerp(Color.White, p.accent, 0.08f))
        d.clipPath(clip) {
            val ink = p.ink
            fun pentagon(center: Offset, size: Float, rotation: Float) {
                val path = Path()
                for (i in 0 until 5) {
                    val a = (rotation + i * 72 - 90) * PI / 180
                    val pt = Offset(center.x + (size * cos(a)).toFloat(), center.y + (size * sin(a)).toFloat())
                    if (i == 0) path.moveTo(pt.x, pt.y) else path.lineTo(pt.x, pt.y)
                }
                path.close()
                drawPath(path, ink)
            }
            pentagon(o(0.05f, -0.02f), r * 0.3f, 8f)
            for (i in 0 until 5) {
                val a = (8 + i * 72 - 90) * PI / 180
                val inner = Offset(c.x + 0.05f * r + (r * 0.3f * cos(a)).toFloat(), c.y - 0.02f * r + (r * 0.3f * sin(a)).toFloat())
                val outerCenter = Offset(c.x + (r * 0.92f * cos(a)).toFloat(), c.y + (r * 0.92f * sin(a)).toFloat())
                drawLine(ink.copy(alpha = 0.75f), inner, Offset((inner.x + outerCenter.x) / 2, (inner.y + outerCenter.y) / 2), r * 0.035f, StrokeCap.Round)
                pentagon(outerCenter, r * 0.26f, 8f + 36f)
            }
        }
        rimLight(c, r)
    }

    fun basketball() {
        groundShadow(o(0f, 1.02f), r * 1.8f)
        val clip = sphere(c, r, lerp(p.material, p.accent, 0.35f))
        d.clipPath(clip) {
            val seam = stroke(0.045f)
            val ink = p.ink.copy(alpha = 0.9f)
            drawLine(ink, o(0f, -1f), o(0f, 1f), r * 0.045f)
            drawLine(ink, o(-1f, 0f), o(1f, 0f), r * 0.045f)
            // Side seams: arcs of circles centred outside the ball, bowing towards the middle.
            drawArc(ink, -70f, 140f, false, o(-2.4f, -1.05f), Size(r * 2.1f, r * 2.1f), style = seam)
            drawArc(ink, 110f, 140f, false, o(0.3f, -1.05f), Size(r * 2.1f, r * 2.1f), style = seam)
        }
        rimLight(c, r)
    }

    fun tennisBall() {
        groundShadow(o(0f, 1.02f), r * 1.7f)
        val clip = sphere(c, r, p.accent)
        d.clipPath(clip) {
            val seam = stroke(0.07f)
            drawArc(Color.White.copy(alpha = 0.92f), -70f, 140f, false, o(-1.85f, -0.85f), Size(r * 1.7f, r * 1.7f), style = seam)
            drawArc(Color.White.copy(alpha = 0.92f), 110f, 140f, false, o(0.15f, -0.85f), Size(r * 1.7f, r * 1.7f), style = seam)
        }
        rimLight(c, r)
    }

    fun volleyball(waterPolo: Boolean) {
        groundShadow(o(0f, 1.02f), r * 1.8f)
        val base = if (waterPolo) p.accent else lerp(Color.White, p.accent, 0.15f)
        val clip = sphere(c, r, base)
        d.clipPath(clip) {
            val stripe = if (waterPolo) p.ink.copy(alpha = 0.85f) else p.ink.copy(alpha = 0.55f)
            for (i in 0 until 3) {
                rotate(i * 120f, c) {
                    val path = Path().apply {
                        moveTo(c.x, c.y)
                        quadraticTo(c.x + r * 0.55f, c.y - r * 0.2f, c.x + r * 0.7f, c.y - r * 1.0f)
                    }
                    drawPath(path, stripe, style = stroke(0.05f))
                    val path2 = Path().apply {
                        moveTo(c.x + r * 0.2f, c.y + r * 0.12f)
                        quadraticTo(c.x + r * 0.85f, c.y - r * 0.1f, c.x + r * 1.05f, c.y - r * 0.85f)
                    }
                    drawPath(path2, stripe, style = stroke(0.05f))
                    if (waterPolo) {
                        val fill = Path().apply {
                            moveTo(c.x, c.y)
                            quadraticTo(c.x + r * 0.55f, c.y - r * 0.2f, c.x + r * 0.7f, c.y - r * 1.0f)
                            lineTo(c.x + r * 1.05f, c.y - r * 0.85f)
                            quadraticTo(c.x + r * 0.85f, c.y - r * 0.1f, c.x + r * 0.2f, c.y + r * 0.12f)
                            close()
                        }
                        drawPath(fill, p.ink.copy(alpha = 0.35f))
                    }
                }
            }
        }
        rimLight(c, r)
    }

    fun handball() {
        groundShadow(o(0f, 1.02f), r * 1.8f)
        val clip = sphere(c, r, lerp(p.accent, Color.White, 0.2f))
        d.clipPath(clip) {
            val ink = p.ink.copy(alpha = 0.7f)
            for (row in -2..2) for (col in -2..2) {
                val cx = c.x + col * r * 0.62f + (if (row % 2 == 0) 0f else r * 0.31f)
                val cy = c.y + row * r * 0.54f
                val hex = Path()
                for (i in 0 until 6) {
                    val a = (i * 60 + 30) * PI / 180
                    val pt = Offset(cx + (r * 0.34f * cos(a)).toFloat(), cy + (r * 0.34f * sin(a)).toFloat())
                    if (i == 0) hex.moveTo(pt.x, pt.y) else hex.lineTo(pt.x, pt.y)
                }
                hex.close()
                drawPath(hex, ink, style = stroke(0.03f))
            }
        }
        rimLight(c, r)
    }

    fun rugbyBall() {
        groundShadow(o(0f, 0.8f), r * 2f)
        d.rotate(-28f, c) {
            val oval = androidx.compose.ui.geometry.Rect(c.x - r * 1.15f, c.y - r * 0.72f, c.x + r * 1.15f, c.y + r * 0.72f)
            drawOval(
                Brush.radialGradient(
                    listOf(lerp(Color.White, p.accent, 0.1f), lerp(Color.White, p.ink, 0.25f)),
                    center = o(-0.3f, -0.35f), radius = r * 1.6f,
                ),
                oval.topLeft, oval.size,
            )
            drawArc(p.ink.copy(alpha = 0.55f), 200f, 140f, false, Offset(c.x - r * 1.05f, c.y - r * 0.42f), Size(r * 2.1f, r * 1.2f), style = stroke(0.03f))
            drawArc(p.accent, 160f, 40f, false, Offset(c.x - r * 1.1f, c.y - r * 0.6f), Size(r * 2.2f, r * 1.2f), style = stroke(0.12f, StrokeCap.Butt))
            drawArc(p.accent, -20f, 40f, false, Offset(c.x - r * 1.1f, c.y - r * 0.6f), Size(r * 2.2f, r * 1.2f), style = stroke(0.12f, StrokeCap.Butt))
            for (i in -2..2) {
                drawLine(p.ink.copy(alpha = 0.6f), Offset(c.x + i * r * 0.12f, c.y - r * 0.5f), Offset(c.x + i * r * 0.12f, c.y - r * 0.36f), r * 0.035f, StrokeCap.Round)
            }
        }
    }

    fun americanFootball() {
        groundShadow(o(0f, 0.8f), r * 2f)
        d.rotate(-30f, c) {
            val path = Path().apply {
                moveTo(c.x - r * 1.25f, c.y)
                quadraticTo(c.x, c.y - r * 1.35f, c.x + r * 1.25f, c.y)
                quadraticTo(c.x, c.y + r * 1.35f, c.x - r * 1.25f, c.y)
                close()
            }
            drawPath(path, Brush.radialGradient(listOf(lerp(p.material, Color.White, 0.25f), lerp(p.material, Color.Black, 0.45f)), center = o(-0.3f, -0.4f), radius = r * 1.7f))
            clipPath(path) {
                drawLine(Color.White.copy(alpha = 0.9f), o(-0.78f, -1f), o(-0.62f, 1f), r * 0.07f)
                drawLine(Color.White.copy(alpha = 0.9f), o(0.78f, -1f), o(0.62f, 1f), r * 0.07f)
            }
            drawLine(Color.White, o(-0.38f, -0.33f), o(0.38f, -0.33f), r * 0.05f, StrokeCap.Round)
            for (i in -3..3) drawLine(Color.White, o(i * 0.1f, -0.42f), o(i * 0.1f, -0.24f), r * 0.045f, StrokeCap.Round)
        }
    }

    fun baseball() {
        groundShadow(o(0f, 1.02f), r * 1.7f)
        val clip = sphere(c, r, lerp(Color.White, p.accent, 0.06f))
        d.clipPath(clip) {
            val red = Color(0xFFE11D48)
            listOf(-1.8f to (-60f), 0.2f to 120f).forEach { (dx, start) ->
                val topLeft = o(dx, -0.8f)
                val size = Size(r * 1.6f, r * 1.6f)
                drawArc(red, start, 120f, false, topLeft, size, style = stroke(0.035f))
                // Stitches: short ticks perpendicular to the seam.
                val cx = topLeft.x + size.width / 2; val cy = topLeft.y + size.height / 2
                for (k in 0..10) {
                    val a = (start + 6 + k * 10.8f) * PI / 180
                    val inner = Offset(cx + (r * 0.72f * cos(a)).toFloat(), cy + (r * 0.72f * sin(a)).toFloat())
                    val outer = Offset(cx + (r * 0.88f * cos(a)).toFloat(), cy + (r * 0.88f * sin(a)).toFloat())
                    drawLine(red, inner, outer, r * 0.03f, StrokeCap.Round)
                }
            }
        }
        rimLight(c, r)
    }

    fun golf() {
        // Flag on a pin behind, ball in front.
        d.drawLine(lerp(Color.White, p.ink, 0.2f), o(0.55f, -1.5f), o(0.55f, 0.75f), r * 0.05f, StrokeCap.Round)
        val flag = Path().apply { moveTo(c.x + r * 0.58f, c.y - r * 1.5f); lineTo(c.x + r * 1.35f, c.y - r * 1.22f); lineTo(c.x + r * 0.58f, c.y - r * 0.95f); close() }
        d.drawPath(flag, p.accent)
        groundShadow(o(-0.25f, 0.95f), r * 1.4f)
        val ballC = o(-0.25f, 0.25f)
        val br = r * 0.7f
        val clip = sphere(ballC, br, Color.White)
        d.clipPath(clip) {
            for (row in -4..4) for (col in -4..4) {
                val cx = ballC.x + col * br * 0.26f + (if (row % 2 == 0) 0f else br * 0.13f)
                val cy = ballC.y + row * br * 0.23f
                drawCircle(p.ink.copy(alpha = 0.12f), br * 0.07f, Offset(cx, cy))
            }
        }
        rimLight(ballC, br)
    }

    fun rattanBall() {
        groundShadow(o(0f, 1.02f), r * 1.8f)
        val clip = sphere(c, r, p.accent)
        d.clipPath(clip) {
            val ink = p.ink.copy(alpha = 0.75f)
            for (angle in listOf(0f, 60f, 120f)) {
                rotate(angle, c) {
                    for (i in -6..6) {
                        val y = c.y + i * r * 0.32f
                        drawLine(ink, Offset(c.x - r * 1.2f, y), Offset(c.x + r * 1.2f, y), r * 0.09f)
                    }
                }
            }
        }
        rimLight(c, r)
    }

    // endregion

    // region rackets & paddles

    fun padelRacket() {
        d.rotate(-32f, c) {
            val head = androidx.compose.ui.geometry.Rect(c.x - r * 0.78f, c.y - r * 1.2f, c.x + r * 0.78f, c.y + r * 0.55f)
            drawLine(lerp(p.ink, Color.White, 0.15f), o(0f, 0.4f), o(0f, 1.45f), r * 0.26f, StrokeCap.Round)
            drawLine(p.accent, o(0f, 1.0f), o(0f, 1.45f), r * 0.27f, StrokeCap.Round)
            drawRoundRect(
                Brush.linearGradient(listOf(lerp(p.material, Color.White, 0.2f), p.material), start = head.topLeft, end = head.bottomRight),
                head.topLeft, head.size, CornerRadius(r * 0.78f, r * 0.85f),
            )
            drawRoundRect(p.accent, head.topLeft, head.size, CornerRadius(r * 0.78f, r * 0.85f), style = stroke(0.07f))
            for (row in 0..6) for (col in -3..3) {
                val hx = c.x + col * r * 0.2f + (if (row % 2 == 0) 0f else r * 0.1f)
                val hy = c.y - r * 0.9f + row * r * 0.2f
                if ((hx - c.x) * (hx - c.x) / (r * 0.62f * r * 0.62f) + (hy - (c.y - r * 0.32f)) * (hy - (c.y - r * 0.32f)) / (r * 0.75f * r * 0.75f) < 1f) {
                    drawCircle(p.ink.copy(alpha = 0.7f), r * 0.05f, Offset(hx, hy))
                }
            }
        }
    }

    fun tableTennis() {
        d.rotate(24f, c) {
            drawLine(lerp(Color(0xFF8B5A2B), Color.White, 0.1f), o(0f, 0.5f), o(0f, 1.45f), r * 0.3f, StrokeCap.Round)
            drawCircle(Brush.radialGradient(listOf(lerp(p.accent, Color.White, 0.2f), lerp(p.accent, Color.Black, 0.35f)), center = o(-0.3f, -0.5f), radius = r * 1.4f), r * 0.95f, o(0f, -0.25f))
            drawCircle(p.ink.copy(alpha = 0.6f), r * 0.95f, o(0f, -0.25f), style = stroke(0.05f))
        }
        val ball = o(1.0f, -0.95f)
        sphere(ball, r * 0.2f, Color.White)
    }

    fun pickleball() {
        d.rotate(-20f, c) {
            drawLine(p.ink, o(0f, 0.55f), o(0f, 1.45f), r * 0.28f, StrokeCap.Round)
            val face = androidx.compose.ui.geometry.Rect(c.x - r * 0.68f, c.y - r * 1.15f, c.x + r * 0.68f, c.y + r * 0.62f)
            drawRoundRect(Brush.linearGradient(listOf(lerp(p.material, Color.White, 0.25f), p.material), face.topLeft, face.bottomRight), face.topLeft, face.size, CornerRadius(r * 0.35f, r * 0.35f))
            drawRoundRect(p.ink.copy(alpha = 0.7f), face.topLeft, face.size, CornerRadius(r * 0.35f, r * 0.35f), style = stroke(0.06f))
        }
        val ballC = o(0.95f, -0.85f)
        val br = r * 0.38f
        val clip = sphere(ballC, br, p.accent)
        d.clipPath(clip) {
            for (i in 0 until 7) {
                val a = i * 2 * PI / 7
                drawCircle(p.ink.copy(alpha = 0.55f), br * 0.14f, Offset(ballC.x + (br * 0.55f * cos(a)).toFloat(), ballC.y + (br * 0.55f * sin(a)).toFloat()))
            }
            drawCircle(p.ink.copy(alpha = 0.55f), br * 0.14f, ballC)
        }
    }

    fun shuttlecock() {
        d.rotate(-35f, c) {
            val cork = o(0f, 0.85f)
            val skirt = Path().apply {
                moveTo(c.x - r * 0.3f, c.y + r * 0.62f)
                lineTo(c.x - r * 0.95f, c.y - r * 1.2f)
                quadraticTo(c.x, c.y - r * 1.45f, c.x + r * 0.95f, c.y - r * 1.2f)
                lineTo(c.x + r * 0.3f, c.y + r * 0.62f)
                close()
            }
            drawPath(skirt, Brush.verticalGradient(listOf(Color.White.copy(alpha = 0.95f), lerp(Color.White, p.accent, 0.3f)), startY = c.y - r * 1.3f, endY = c.y + r * 0.6f))
            for (i in -4..4) {
                val topX = c.x + i * r * 0.21f
                drawLine(p.ink.copy(alpha = 0.35f), Offset(topX, c.y - r * 1.25f), Offset(c.x + i * r * 0.065f, c.y + r * 0.6f), r * 0.025f)
            }
            drawLine(p.accent, o(-0.7f, -0.45f), o(0.7f, -0.45f), r * 0.06f)
            drawLine(p.accent, o(-0.48f, 0.15f), o(0.48f, 0.15f), r * 0.06f)
            drawArc(Brush.radialGradient(listOf(Color.White, lerp(Color.White, p.ink, 0.35f)), center = cork, radius = r * 0.5f), 0f, 180f, true, Offset(cork.x - r * 0.38f, cork.y - r * 0.38f), Size(r * 0.76f, r * 0.76f))
            drawLine(p.accent, o(-0.38f, 0.85f), o(0.38f, 0.85f), r * 0.1f)
        }
    }

    fun squash() {
        d.rotate(28f, c) {
            drawLine(p.ink, o(0f, 0.55f), o(0f, 1.55f), r * 0.17f, StrokeCap.Round)
            drawLine(lerp(p.material, Color.White, 0.2f), o(-0.08f, 0.45f), o(0f, 0.9f), r * 0.06f)
            drawLine(lerp(p.material, Color.White, 0.2f), o(0.08f, 0.45f), o(0f, 0.9f), r * 0.06f)
            val head = androidx.compose.ui.geometry.Rect(c.x - r * 0.55f, c.y - r * 1.3f, c.x + r * 0.55f, c.y + r * 0.5f)
            clipPath(Path().apply { addOval(head) }) {
                for (i in -6..6) drawLine(Color.White.copy(alpha = 0.45f), Offset(c.x + i * r * 0.09f, head.top), Offset(c.x + i * r * 0.09f, head.bottom), r * 0.015f)
                for (i in -9..9) drawLine(Color.White.copy(alpha = 0.45f), Offset(head.left, c.y - r * 0.4f + i * r * 0.1f), Offset(head.right, c.y - r * 0.4f + i * r * 0.1f), r * 0.015f)
            }
            drawOval(p.material, head.topLeft, head.size, style = stroke(0.1f))
        }
        sphere(o(-0.95f, 0.85f), r * 0.17f, Color(0xFF111111))
    }

    // endregion

    // region combat

    fun boxingGlove() {
        groundShadow(o(0f, 1.25f), r * 1.8f)
        d.rotate(-12f, c) {
            val body = Path().apply {
                moveTo(c.x - r * 0.55f, c.y + r * 0.6f)
                cubicTo(c.x - r * 1.05f, c.y + r * 0.2f, c.x - r * 1.0f, c.y - r * 1.1f, c.x - r * 0.05f, c.y - r * 1.15f)
                cubicTo(c.x + r * 0.95f, c.y - r * 1.2f, c.x + r * 1.05f, c.y - r * 0.1f, c.x + r * 0.6f, c.y + r * 0.6f)
                close()
            }
            val shade = Brush.radialGradient(listOf(lerp(p.accent, Color.White, 0.25f), p.accent, lerp(p.accent, Color.Black, 0.5f)), center = o(-0.35f, -0.6f), radius = r * 2f)
            drawPath(body, shade)
            val thumb = Path().apply {
                moveTo(c.x - r * 0.75f, c.y + r * 0.2f)
                cubicTo(c.x - r * 1.25f, c.y - r * 0.2f, c.x - r * 1.0f, c.y - r * 0.75f, c.x - r * 0.5f, c.y - r * 0.4f)
            }
            drawPath(thumb, lerp(p.accent, Color.Black, 0.3f), style = stroke(0.06f))
            drawArc(lerp(p.accent, Color.Black, 0.35f), 200f, 120f, false, o(-0.7f, -0.95f), Size(r * 1.4f, r * 1.0f), style = stroke(0.04f))
            val cuff = androidx.compose.ui.geometry.Rect(c.x - r * 0.6f, c.y + r * 0.55f, c.x + r * 0.62f, c.y + r * 1.3f)
            drawRoundRect(lerp(Color.White, p.accent, 0.1f), cuff.topLeft, cuff.size, CornerRadius(r * 0.12f))
            drawLine(p.accent, Offset(cuff.left, cuff.top + cuff.height * 0.45f), Offset(cuff.right, cuff.top + cuff.height * 0.45f), r * 0.12f)
        }
    }

    fun belt() {
        val color = if (p.material.luminanceApprox() > 0.5f) p.ink else p.material
        val beltColor = lerp(color, Color.Black, 0.2f)
        d.drawLine(beltColor, o(-1.6f, -0.1f), o(1.6f, 0.1f), r * 0.42f, StrokeCap.Butt)
        d.drawLine(lerp(beltColor, Color.White, 0.15f), o(-1.6f, -0.24f), o(1.6f, -0.04f), r * 0.03f)
        val knot = Path().apply {
            moveTo(c.x - r * 0.35f, c.y - r * 0.35f); lineTo(c.x + r * 0.35f, c.y - r * 0.3f)
            lineTo(c.x + r * 0.3f, c.y + r * 0.35f); lineTo(c.x - r * 0.3f, c.y + r * 0.3f); close()
        }
        d.drawPath(knot, lerp(beltColor, Color.White, 0.1f))
        val tail1 = Path().apply { moveTo(c.x - r * 0.15f, c.y + r * 0.25f); lineTo(c.x - r * 0.75f, c.y + r * 1.5f); lineTo(c.x - r * 0.35f, c.y + r * 1.6f); lineTo(c.x + r * 0.1f, c.y + r * 0.3f); close() }
        val tail2 = Path().apply { moveTo(c.x + r * 0.05f, c.y + r * 0.3f); lineTo(c.x + r * 0.6f, c.y + r * 1.55f); lineTo(c.x + r * 0.98f, c.y + r * 1.42f); lineTo(c.x + r * 0.3f, c.y + r * 0.25f); close() }
        d.drawPath(tail1, beltColor)
        d.drawPath(tail2, lerp(beltColor, Color.Black, 0.15f))
        d.drawLine(p.accent, o(-0.66f, 1.3f), o(-0.27f, 1.38f), r * 0.1f)
        d.drawLine(p.accent, o(0.52f, 1.35f), o(0.88f, 1.22f), r * 0.1f)
    }

    fun octagon() {
        fun oct(radius: Float): Path = Path().apply {
            for (i in 0 until 8) {
                val a = (i * 45 + 22.5) * PI / 180
                val pt = Offset(c.x + (radius * cos(a)).toFloat(), c.y + (radius * sin(a)).toFloat())
                if (i == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
            }
            close()
        }
        d.drawPath(oct(r * 1.3f), p.material.copy(alpha = 0.35f))
        d.drawPath(oct(r * 1.3f), p.accent, style = stroke(0.06f))
        d.drawPath(oct(r * 0.95f), Color.White.copy(alpha = 0.5f), style = stroke(0.025f))
        // Pictogram: a high roundhouse kick.
        pictogram(o(-0.2f, 0.1f), 0.95f, kick = true)
    }

    /** Minimal athlete pictogram, drawn with thick rounded strokes. */
    fun pictogram(at: Offset, scale: Float, kick: Boolean = false, lunge: Boolean = false, color: Color = Color.White) {
        val u = r * scale
        val w = Stroke(u * 0.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        fun pt(x: Float, y: Float) = Offset(at.x + x * u, at.y + y * u)
        val hip = pt(0f, 0.15f)
        val neck = pt(-0.12f, -0.55f)
        d.drawCircle(color, u * 0.19f, pt(-0.2f, -0.88f))
        d.drawLine(color, neck, hip, u * 0.22f, StrokeCap.Round)
        if (kick) {
            d.drawPath(Path().apply { moveTo(hip.x, hip.y); lineTo(pt(0.55f, -0.2f).x, pt(0.55f, -0.2f).y); lineTo(pt(1.15f, -0.65f).x, pt(1.15f, -0.65f).y) }, color, style = w)
            d.drawPath(Path().apply { moveTo(hip.x, hip.y); lineTo(pt(-0.15f, 0.62f).x, pt(-0.15f, 0.62f).y); lineTo(pt(-0.05f, 1.1f).x, pt(-0.05f, 1.1f).y) }, color, style = w)
            d.drawPath(Path().apply { moveTo(neck.x, neck.y); lineTo(pt(-0.55f, -0.3f).x, pt(-0.55f, -0.3f).y); lineTo(pt(-0.75f, -0.62f).x, pt(-0.75f, -0.62f).y) }, color, style = w)
            d.drawPath(Path().apply { moveTo(neck.x, neck.y); lineTo(pt(0.25f, -0.5f).x, pt(0.25f, -0.5f).y); lineTo(pt(0.3f, -0.85f).x, pt(0.3f, -0.85f).y) }, color, style = w)
        } else if (lunge) {
            d.drawPath(Path().apply { moveTo(hip.x, hip.y); lineTo(pt(0.6f, 0.45f).x, pt(0.6f, 0.45f).y); lineTo(pt(0.68f, 1.05f).x, pt(0.68f, 1.05f).y) }, color, style = w)
            d.drawPath(Path().apply { moveTo(hip.x, hip.y); lineTo(pt(-0.45f, 0.62f).x, pt(-0.45f, 0.62f).y); lineTo(pt(-0.95f, 0.98f).x, pt(-0.95f, 0.98f).y) }, color, style = w)
            d.drawPath(Path().apply { moveTo(neck.x, neck.y); lineTo(pt(0.4f, -0.45f).x, pt(0.4f, -0.45f).y); lineTo(pt(0.95f, -0.55f).x, pt(0.95f, -0.55f).y) }, color, style = w)
            d.drawPath(Path().apply { moveTo(neck.x, neck.y); lineTo(pt(-0.5f, -0.25f).x, pt(-0.5f, -0.25f).y); lineTo(pt(-0.9f, -0.05f).x, pt(-0.9f, -0.05f).y) }, color, style = w)
        }
    }

    fun raider() {
        // Kabaddi: the raider stretches to tag, mid-line behind.
        d.drawLine(p.accent, o(-1.1f, -1.6f), o(-1.1f, 1.6f), r * 0.08f)
        pictogram(o(0.05f, -0.05f), 1.05f, lunge = true)
        for (i in 0..2) {
            d.drawArc(p.accent.copy(alpha = 0.7f - i * 0.2f), -40f, 80f, false, o(0.75f + i * 0.25f, -1.05f), Size(r * 0.6f, r * 0.6f), style = stroke(0.06f))
        }
    }

    fun wrestling() {
        d.drawCircle(p.accent.copy(alpha = 0.9f), r * 1.35f, c, style = stroke(0.07f))
        pictogram(o(-0.35f, 0.05f), 0.85f, lunge = true)
        d.rotate(180f, o(0.35f, 0.05f)) {
            pictogram(o(0.35f, 0.05f), 0.85f, lunge = true, color = lerp(Color.White, p.accent, 0.5f))
        }
    }

    fun fencing() {
        fun blade(from: Offset, to: Offset, guardColor: Color) {
            d.drawLine(lerp(Color.White, p.accent, 0.15f), from, to, r * 0.045f, StrokeCap.Round)
            val gx = from.x + (to.x - from.x) * 0.2f
            val gy = from.y + (to.y - from.y) * 0.2f
            d.drawCircle(guardColor, r * 0.2f, Offset(gx, gy))
            d.drawLine(p.ink, from, Offset(from.x + (to.x - from.x) * 0.16f, from.y + (to.y - from.y) * 0.16f), r * 0.12f, StrokeCap.Round)
            d.drawCircle(p.accent, r * 0.05f, to)
        }
        blade(o(-1.2f, 1.2f), o(1.35f, -1.25f), lerp(Color.White, p.ink, 0.25f))
        blade(o(1.2f, 1.2f), o(-1.35f, -1.25f), lerp(Color.White, p.ink, 0.25f))
        // Mask mesh in the centre.
        val mask = androidx.compose.ui.geometry.Rect(c.x - r * 0.42f, c.y - r * 0.55f, c.x + r * 0.42f, c.y + r * 0.45f)
        d.drawOval(p.ink.copy(alpha = 0.85f), mask.topLeft, mask.size)
        d.clipPath(Path().apply { addOval(mask) }) {
            for (i in -6..6) drawLine(Color.White.copy(alpha = 0.3f), Offset(c.x + i * r * 0.08f, mask.top), Offset(c.x + i * r * 0.08f, mask.bottom), r * 0.015f)
            for (i in -8..8) drawLine(Color.White.copy(alpha = 0.3f), Offset(mask.left, c.y + i * r * 0.08f), Offset(mask.right, c.y + i * r * 0.08f), r * 0.015f)
        }
        d.drawOval(Color.White, mask.topLeft, mask.size, style = stroke(0.05f))
        d.drawLine(Color.White, Offset(c.x - r * 0.3f, mask.bottom), Offset(c.x + r * 0.3f, mask.bottom), r * 0.1f, StrokeCap.Round)
    }

    // endregion

    // region individual gear

    fun goggles() {
        val strap = Path().apply {
            moveTo(c.x - r * 1.6f, c.y + r * 0.1f)
            cubicTo(c.x - r * 1.0f, c.y + r * 1.1f, c.x + r * 1.0f, c.y + r * 1.1f, c.x + r * 1.6f, c.y + r * 0.1f)
        }
        d.drawPath(strap, p.material.copy(alpha = 0.9f), style = stroke(0.12f))
        listOf(-0.62f, 0.62f).forEach { dx ->
            val lens = androidx.compose.ui.geometry.Rect(c.x + dx * r - r * 0.5f, c.y - r * 0.35f, c.x + dx * r + r * 0.5f, c.y + r * 0.35f)
            d.drawOval(Brush.linearGradient(listOf(p.accent, lerp(p.accent, p.ink, 0.7f)), lens.topLeft, lens.bottomRight), lens.topLeft, lens.size)
            d.drawOval(p.ink, lens.topLeft, lens.size, style = stroke(0.07f))
            d.drawArc(Color.White.copy(alpha = 0.7f), 200f, 60f, false, Offset(lens.left + r * 0.12f, lens.top + r * 0.08f), Size(lens.width * 0.7f, lens.height * 0.7f), style = stroke(0.05f))
        }
        d.drawLine(p.ink, o(-0.14f, -0.05f), o(0.14f, -0.05f), r * 0.08f, StrokeCap.Round)
    }

    fun hurdle() {
        groundShadow(o(0f, 1.1f), r * 2.8f)
        listOf(-1.1f, 1.1f).forEach { dx ->
            d.drawLine(lerp(Color.White, p.ink, 0.2f), o(dx, -0.45f), o(dx, 1.05f), r * 0.09f, StrokeCap.Round)
            d.drawLine(lerp(Color.White, p.ink, 0.2f), o(dx - 0.35f, 1.05f), o(dx + 0.35f, 1.05f), r * 0.09f, StrokeCap.Round)
        }
        val bar = androidx.compose.ui.geometry.Rect(c.x - r * 1.35f, c.y - r * 0.7f, c.x + r * 1.35f, c.y - r * 0.32f)
        d.drawRect(Color.White, bar.topLeft, bar.size)
        d.clipRect(bar.left, bar.top, bar.right, bar.bottom) {
            for (i in -3..3) {
                val x0 = c.x + i * r * 0.42f
                val path = Path().apply { moveTo(x0, bar.bottom); lineTo(x0 + r * 0.2f, bar.top); lineTo(x0 + r * 0.42f, bar.top); lineTo(x0 + r * 0.22f, bar.bottom); close() }
                drawPath(path, p.ink)
            }
        }
        d.drawRect(p.ink, bar.topLeft, bar.size, style = stroke(0.03f))
    }

    fun wheel() {
        val tyre = r * 1.25f
        d.drawCircle(p.ink, tyre, c, style = stroke(0.2f))
        d.drawCircle(lerp(Color.White, p.accent, 0.35f), tyre - r * 0.13f, c, style = stroke(0.07f))
        val spokes = 24
        for (i in 0 until spokes) {
            val a = i * 2 * PI / spokes
            val hubPoint = Offset(c.x + (r * 0.1f * cos(a + 0.3)).toFloat(), c.y + (r * 0.1f * sin(a + 0.3)).toFloat())
            val rim = Offset(c.x + ((tyre - r * 0.16f) * cos(a)).toFloat(), c.y + ((tyre - r * 0.16f) * sin(a)).toFloat())
            d.drawLine(Color.White.copy(alpha = 0.75f), hubPoint, rim, r * 0.015f)
        }
        d.drawCircle(p.accent, r * 0.15f, c)
        d.drawArc(p.accent, -60f, 70f, false, Offset(c.x - tyre, c.y - tyre), Size(tyre * 2, tyre * 2), style = stroke(0.2f))
    }

    fun chequeredFlag() {
        d.drawLine(lerp(Color.White, p.ink, 0.3f), o(-1.15f, -1.2f), o(-1.15f, 1.6f), r * 0.07f, StrokeCap.Round)
        val cols = 8; val rows = 6
        val cw = r * 2.4f / cols; val ch = r * 1.7f / rows
        for (i in 0 until cols) for (j in 0 until rows) {
            fun wave(x: Float) = (sin(x / (r * 0.6f) * 1.0 + 0.4) * r * 0.16f).toFloat()
            val x0 = c.x - r * 1.12f + i * cw; val x1 = x0 + cw
            val y0 = c.y - r * 1.15f + j * ch
            val path = Path().apply {
                moveTo(x0, y0 + wave(x0)); lineTo(x1, y0 + wave(x1)); lineTo(x1, y0 + ch + wave(x1)); lineTo(x0, y0 + ch + wave(x0)); close()
            }
            val dark = (i + j) % 2 == 0
            val shade = 0.85f + 0.15f * cos((x0 - c.x) / r * 1.5).toFloat()
            d.drawPath(path, if (dark) Color(0xFF0B0B0F) else lerp(Color.White, p.accent, 0.05f).copy(alpha = shade))
        }
    }

    fun puck() {
        d.rotate(-12f, c) {
            // Stick blade sweeping under the puck.
            drawLine(lerp(p.material, Color.White, 0.2f), o(-1.6f, -1.6f), o(0.2f, 0.85f), r * 0.16f, StrokeCap.Round)
            drawLine(lerp(p.material, Color.White, 0.2f), o(0.2f, 0.85f), o(1.3f, 0.95f), r * 0.24f, StrokeCap.Round)
            drawLine(p.accent, o(-1.45f, -1.4f), o(-1.0f, -0.78f), r * 0.17f, StrokeCap.Round)
        }
        val top = androidx.compose.ui.geometry.Rect(c.x - r * 0.75f, c.y - r * 0.55f, c.x + r * 0.75f, c.y - r * 0.05f)
        d.drawRect(Color(0xFF0B0B0F), Offset(top.left, top.center.y), Size(top.width, r * 0.35f))
        d.drawOval(Color(0xFF0B0B0F), Offset(top.left, top.top + r * 0.35f), top.size)
        d.drawOval(Brush.linearGradient(listOf(Color(0xFF2A2A33), Color(0xFF0B0B0F)), top.topLeft, top.bottomRight), top.topLeft, top.size)
        d.drawOval(Color.White.copy(alpha = 0.18f), top.topLeft, top.size, style = stroke(0.02f))
    }

    fun hockeyStick() {
        d.rotate(-20f, c) {
            val path = Path().apply {
                moveTo(c.x + r * 0.2f, c.y - r * 1.7f)
                lineTo(c.x + r * 0.2f, c.y + r * 0.8f)
                quadraticTo(c.x + r * 0.2f, c.y + r * 1.35f, c.x - r * 0.35f, c.y + r * 1.3f)
                quadraticTo(c.x - r * 0.75f, c.y + r * 1.2f, c.x - r * 0.65f, c.y + r * 0.85f)
            }
            drawPath(path, lerp(p.material, Color.White, 0.15f), style = stroke(0.22f))
            drawLine(p.ink, o(0.2f, -1.7f), o(0.2f, -0.9f), r * 0.24f, StrokeCap.Round)
            drawLine(p.accent, o(0.2f, 0.2f), o(0.2f, 0.45f), r * 0.24f)
        }
        sphere(o(0.85f, 0.95f), r * 0.24f, Color.White)
    }

    fun hurley() {
        d.rotate(25f, c) {
            drawLine(lerp(Color(0xFFD9B98A), p.accent, 0.1f), o(0f, -1.7f), o(0f, 0.6f), r * 0.16f, StrokeCap.Round)
            val bas = Path().apply {
                moveTo(c.x - r * 0.08f, c.y + r * 0.45f)
                cubicTo(c.x - r * 0.55f, c.y + r * 0.8f, c.x - r * 0.5f, c.y + r * 1.55f, c.x + r * 0.05f, c.y + r * 1.55f)
                cubicTo(c.x + r * 0.45f, c.y + r * 1.5f, c.x + r * 0.3f, c.y + r * 0.8f, c.x + r * 0.08f, c.y + r * 0.45f)
                close()
            }
            drawPath(bas, Brush.linearGradient(listOf(Color(0xFFE8CFA4), Color(0xFFB98B54)), o(-0.5f, 0.5f), o(0.4f, 1.5f)))
            drawLine(p.ink.copy(alpha = 0.8f), o(-0.25f, 1.2f), o(0.25f, 1.2f), r * 0.06f)
            drawLine(p.ink, o(0f, -1.7f), o(0f, -1.15f), r * 0.18f, StrokeCap.Round)
        }
        val ballC = o(0.95f, -0.75f)
        val br = r * 0.32f
        val clip = sphere(ballC, br, Color(0xFFF2EBDD))
        d.clipPath(clip) {
            drawArc(p.ink.copy(alpha = 0.6f), 0f, 360f, false, Offset(ballC.x - br * 1.3f, ballC.y - br * 0.4f), Size(br * 2.6f, br * 0.8f), style = Stroke(br * 0.15f))
        }
    }

    fun lacrosse() {
        d.rotate(-28f, c) {
            drawLine(lerp(Color.White, p.ink, 0.25f), o(0f, -0.3f), o(0f, 1.7f), r * 0.12f, StrokeCap.Round)
            val head = Path().apply {
                moveTo(c.x - r * 0.12f, c.y - r * 0.3f)
                cubicTo(c.x - r * 0.75f, c.y - r * 0.7f, c.x - r * 0.75f, c.y - r * 1.6f, c.x, c.y - r * 1.65f)
                cubicTo(c.x + r * 0.75f, c.y - r * 1.6f, c.x + r * 0.75f, c.y - r * 0.7f, c.x + r * 0.12f, c.y - r * 0.3f)
            }
            clipPath(Path().apply { addPath(head); close() }) {
                for (i in -6..6) drawLine(Color.White.copy(alpha = 0.55f), Offset(c.x + i * r * 0.12f - r * 0.5f, c.y - r * 1.7f), Offset(c.x + i * r * 0.12f + r * 0.5f, c.y - r * 0.2f), r * 0.02f)
                for (i in -6..6) drawLine(Color.White.copy(alpha = 0.55f), Offset(c.x + i * r * 0.12f + r * 0.5f, c.y - r * 1.7f), Offset(c.x + i * r * 0.12f - r * 0.5f, c.y - r * 0.2f), r * 0.02f)
            }
            drawPath(head, p.accent, style = stroke(0.1f))
        }
        sphere(o(0.9f, 0.9f), r * 0.24f, lerp(Color.White, p.accent, 0.4f))
    }

    fun netballHoop() {
        d.drawLine(lerp(Color.White, p.ink, 0.3f), o(1.05f, -0.9f), o(1.05f, 1.8f), r * 0.1f, StrokeCap.Round)
        d.drawLine(lerp(Color.White, p.ink, 0.3f), o(0.75f, -0.9f), o(1.05f, -0.9f), r * 0.08f)
        val ring = androidx.compose.ui.geometry.Rect(c.x - r * 0.95f, c.y - r * 1.05f, c.x + r * 0.75f, c.y - r * 0.65f)
        val net = Path()
        for (i in 0..8) {
            val f = i / 8f
            val topX = ring.left + ring.width * f
            val bottomX = c.x - r * 0.48f + r * 0.76f * f
            net.moveTo(topX, ring.center.y); net.lineTo(bottomX, c.y + r * 0.35f)
        }
        for (k in 1..3) {
            val y = ring.center.y + (c.y + r * 0.35f - ring.center.y) * k / 4f
            val inset = r * 0.12f * k
            net.moveTo(ring.left + inset, y); net.lineTo(ring.right - inset, y)
        }
        d.drawPath(net, Color.White.copy(alpha = 0.7f), style = stroke(0.025f))
        d.drawOval(p.accent, ring.topLeft, ring.size, style = stroke(0.08f))
        sphere(o(-0.6f, 0.95f), r * 0.42f, lerp(Color.White, p.accent, 0.3f))
    }

    fun disc() {
        d.rotate(-14f, c) {
            val outer = androidx.compose.ui.geometry.Rect(c.x - r * 1.3f, c.y - r * 0.55f, c.x + r * 1.3f, c.y + r * 0.55f)
            drawOval(lerp(p.accent, Color.Black, 0.45f), Offset(outer.left, outer.top + r * 0.16f), outer.size)
            drawOval(Brush.linearGradient(listOf(lerp(p.accent, Color.White, 0.35f), p.accent), outer.topLeft, outer.bottomRight), outer.topLeft, outer.size)
            for (k in 1..3) {
                val f = 1f - k * 0.2f
                drawOval(p.ink.copy(alpha = 0.25f), Offset(c.x - outer.width * f / 2, c.y - outer.height * f / 2), Size(outer.width * f, outer.height * f), style = stroke(0.03f))
            }
            drawArc(Color.White.copy(alpha = 0.6f), 190f, 80f, false, outer.topLeft + Offset(r * 0.1f, r * 0.05f), Size(outer.width - r * 0.2f, outer.height - r * 0.1f), style = stroke(0.04f))
        }
        for (i in 0..2) d.drawLine(Color.White.copy(alpha = 0.25f - i * 0.07f), o(-1.6f - i * 0.2f, 0.3f + i * 0.2f), o(-2.3f - i * 0.2f, 0.45f + i * 0.2f), r * 0.05f, StrokeCap.Round)
    }

    fun surfboard() {
        d.rotate(-38f, c) {
            val board = Path().apply {
                moveTo(c.x, c.y - r * 1.9f)
                cubicTo(c.x + r * 0.62f, c.y - r * 1.1f, c.x + r * 0.6f, c.y + r * 1.2f, c.x + r * 0.15f, c.y + r * 1.75f)
                lineTo(c.x - r * 0.15f, c.y + r * 1.75f)
                cubicTo(c.x - r * 0.6f, c.y + r * 1.2f, c.x - r * 0.62f, c.y - r * 1.1f, c.x, c.y - r * 1.9f)
                close()
            }
            drawPath(board, Brush.linearGradient(listOf(lerp(Color.White, p.accent, 0.15f), lerp(Color.White, p.material, 0.4f)), o(-0.5f, -1.8f), o(0.5f, 1.8f)))
            clipPath(board) {
                drawRect(p.accent, Offset(c.x - r, c.y + r * 0.5f), Size(r * 2, r * 0.22f))
                drawRect(p.material, Offset(c.x - r, c.y + r * 0.8f), Size(r * 2, r * 0.1f))
            }
            drawLine(p.ink.copy(alpha = 0.45f), o(0f, -1.85f), o(0f, 1.7f), r * 0.03f)
            drawPath(board, p.ink.copy(alpha = 0.25f), style = stroke(0.03f))
        }
    }

    fun skateboard() {
        d.rotate(-30f, c) {
            val deck = androidx.compose.ui.geometry.Rect(c.x - r * 0.52f, c.y - r * 1.75f, c.x + r * 0.52f, c.y + r * 1.75f)
            listOf(-1.05f, 1.05f).forEach { dy ->
                drawRect(lerp(Color.White, p.ink, 0.4f), Offset(c.x - r * 0.62f, c.y + dy * r - r * 0.07f), Size(r * 1.24f, r * 0.14f))
                listOf(-0.62f, 0.62f).forEach { dx ->
                    drawRoundRect(lerp(Color.White, p.accent, 0.4f), Offset(c.x + dx * r - r * 0.13f, c.y + dy * r - r * 0.2f), Size(r * 0.26f, r * 0.4f), CornerRadius(r * 0.08f))
                }
            }
            drawRoundRect(Brush.linearGradient(listOf(p.material, lerp(p.material, p.accent, 0.5f)), deck.topLeft, deck.bottomRight), deck.topLeft, deck.size, CornerRadius(r * 0.52f))
            drawRoundRect(p.accent, deck.topLeft, deck.size, CornerRadius(r * 0.52f), style = stroke(0.05f))
            for (i in 0..3) drawLine(p.accent.copy(alpha = 0.7f), o(-0.52f, -0.5f + i * 0.35f), o(0.52f, -0.2f + i * 0.35f), r * 0.07f)
        }
    }

    fun curlingStone() {
        groundShadow(o(0f, 0.95f), r * 2.6f)
        val body = androidx.compose.ui.geometry.Rect(c.x - r * 1.2f, c.y - r * 0.25f, c.x + r * 1.2f, c.y + r * 0.85f)
        d.drawRoundRect(Brush.verticalGradient(listOf(Color(0xFF9CA3AF), Color(0xFF374151)), body.top, body.bottom), body.topLeft, body.size, CornerRadius(r * 0.6f, r * 0.5f))
        d.drawRoundRect(Color(0xFF1F2937), Offset(body.left + r * 0.05f, body.center.y - r * 0.06f), Size(body.width - r * 0.1f, r * 0.12f), CornerRadius(r * 0.06f))
        d.drawOval(Brush.linearGradient(listOf(lerp(p.accent, Color.White, 0.2f), lerp(p.accent, Color.Black, 0.3f)), o(-0.9f, -0.5f), o(0.9f, 0f)), Offset(c.x - r * 1.0f, c.y - r * 0.55f), Size(r * 2.0f, r * 0.62f))
        val handle = Path().apply {
            moveTo(c.x - r * 0.18f, c.y - r * 0.3f)
            lineTo(c.x - r * 0.12f, c.y - r * 0.82f)
            lineTo(c.x + r * 0.95f, c.y - r * 0.82f)
        }
        d.drawPath(handle, p.accent, style = stroke(0.2f))
        d.drawLine(Color.White.copy(alpha = 0.5f), o(0f, -0.88f), o(0.85f, -0.88f), r * 0.04f, StrokeCap.Round)
    }

    fun target() {
        val rings = listOf(Color.White, Color.White, Color(0xFF111111), Color(0xFF111111), Color(0xFF38BDF8), Color(0xFF38BDF8), Color(0xFFEF4444), Color(0xFFEF4444), Color(0xFFFACC15), Color(0xFFFACC15))
        val outer = r * 1.35f
        rings.forEachIndexed { i, color ->
            val radius = outer * (1f - i / 10f)
            d.drawCircle(color, radius, c)
            d.drawCircle(if (i in 2..3) Color.White.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.25f), radius, c, style = Stroke(r * 0.012f))
        }
        d.drawCircle(Color.Black.copy(alpha = 0.4f), r * 0.02f, c)
        // Arrow buried just off-centre.
        val tip = o(0.08f, -0.06f)
        val tail = o(1.6f, -1.4f)
        d.drawLine(lerp(Color.White, p.ink, 0.2f), tip, tail, r * 0.05f, StrokeCap.Round)
        val fletch = Path().apply {
            moveTo(tail.x, tail.y); lineTo(tail.x - r * 0.15f, tail.y - r * 0.32f); lineTo(tail.x - r * 0.42f, tail.y + r * 0.1f); close()
        }
        d.drawPath(fletch, p.accent)
    }

    fun oars() {
        fun oar(rotation: Float) {
            d.rotate(rotation, c) {
                drawLine(lerp(Color.White, p.ink, 0.25f), o(0f, -1.9f), o(0f, 0.8f), r * 0.1f, StrokeCap.Round)
                drawLine(p.ink, o(0f, -1.9f), o(0f, -1.45f), r * 0.14f, StrokeCap.Round)
                val blade = Path().apply {
                    moveTo(c.x - r * 0.05f, c.y + r * 0.6f)
                    cubicTo(c.x - r * 0.45f, c.y + r * 0.9f, c.x - r * 0.42f, c.y + r * 1.75f, c.x, c.y + r * 1.9f)
                    cubicTo(c.x + r * 0.42f, c.y + r * 1.75f, c.x + r * 0.45f, c.y + r * 0.9f, c.x + r * 0.05f, c.y + r * 0.6f)
                    close()
                }
                drawPath(blade, p.accent)
                drawLine(p.ink.copy(alpha = 0.5f), o(-0.3f, 1.3f), o(0.3f, 1.3f), r * 0.1f)
            }
        }
        oar(-35f)
        oar(35f)
    }

    fun snooker() {
        // Triangle of reds, the black, and a cue line.
        val red = Color(0xFFDC2626)
        val br = r * 0.2f
        var row = 0
        while (row < 5) {
            for (k in 0..row) {
                val x = c.x - r * 0.3f + row * br * 1.75f
                val y = c.y + (k - row / 2f) * br * 2.02f
                sphere(Offset(x, y), br, red)
            }
            row++
        }
        sphere(o(-1.15f, 0f), br, Color(0xFF111111))
        sphere(o(-1.7f, 0.6f), br, Color.White)
        d.drawLine(lerp(Color(0xFFD9B98A), p.accent, 0.1f), o(-2.0f, 0.95f), o(-3.4f, 2.5f), r * 0.07f, StrokeCap.Round)
    }

    fun cricket() {
        d.rotate(30f, c) {
            drawLine(p.ink, o(0f, -1.85f), o(0f, -0.95f), r * 0.14f, StrokeCap.Round)
            drawLine(p.accent, o(0f, -1.65f), o(0f, -1.1f), r * 0.16f, StrokeCap.Round)
            val blade = androidx.compose.ui.geometry.Rect(c.x - r * 0.32f, c.y - r * 1.0f, c.x + r * 0.32f, c.y + r * 1.6f)
            drawRoundRect(Brush.linearGradient(listOf(Color(0xFFF3E3C3), Color(0xFFC9A46A)), blade.topLeft, blade.bottomRight), blade.topLeft, blade.size, CornerRadius(r * 0.18f))
            drawLine(Color(0xFFB08850), o(0f, -0.9f), o(0f, 1.5f), r * 0.04f)
        }
        val ballC = o(1.0f, 0.95f)
        val br = r * 0.34f
        val clip = sphere(ballC, br, Color(0xFFB91C1C))
        d.clipPath(clip) {
            drawLine(Color.White.copy(alpha = 0.85f), Offset(ballC.x - br * 0.1f, ballC.y - br), Offset(ballC.x + br * 0.1f, ballC.y + br), br * 0.1f)
        }
    }

    // endregion
}

private fun Color.luminanceApprox(): Float = 0.2126f * red + 0.7152f * green + 0.0722f * blue
