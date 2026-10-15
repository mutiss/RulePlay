package com.mutissx.ruleplay.core.designsystem.art

import android.content.res.AssetManager
import android.graphics.BitmapFactory
import android.graphics.Camera
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Rect
import android.util.LruCache
import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.mutissx.ruleplay.core.designsystem.theme.RulePlayTheme
import com.mutissx.ruleplay.domain.model.ArtSpec
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random
import androidx.compose.ui.graphics.Canvas as ComposeCanvas

enum class ArtStyle {
    /** Full editorial composition: tilted playing surface, hero object, light and grain. */
    COVER,
    /** Lighter composition for small cards. */
    CARD,
    /** Hero object only, on a transparent background. */
    GLYPH,
}

/**
 * Cover artwork for a sport. When the sport has a bundled open-licensed photo, the cover is that photo
 * graded with the sport's palette; otherwise it is original on-device art — a unique combination of
 * palette, playing-surface motif and hero equipment. [ArtStyle.GLYPH] always draws the equipment.
 *
 * Rendering is expensive (paths, gradients, grain), so each composition is painted once on a background
 * thread into two cached bitmaps — the backdrop and the hero object — keyed by sport, style and pixel size.
 * On screen, scrolling and transitions only move those two textures; a cheap gradient stands in for the
 * few frames before a cover is ready.
 *
 * @param parallax 0..1 scroll fraction used to drift the hero object in scroll-responsive heroes.
 */
@Composable
fun SportIllustration(
    art: ArtSpec,
    modifier: Modifier = Modifier,
    style: ArtStyle = ArtStyle.COVER,
    contentDescription: String? = null,
    parallax: () -> Float = { 0f },
    glyphScale: Float = 1f,
) {
    val semantics = if (contentDescription != null) Modifier.semantics { this.contentDescription = contentDescription } else Modifier
    val density = LocalDensity.current.density
    val reducedMotion = RulePlayTheme.motion.reduced
    val assets = LocalContext.current.assets

    BoxWithConstraints(modifier.then(semantics).clipToBounds()) {
        val width = if (constraints.hasBoundedWidth) constraints.maxWidth else 0
        val height = if (constraints.hasBoundedHeight) constraints.maxHeight else 0
        val key = remember(art, style, width, height, glyphScale) { ArtKey(art, style, width, height, glyphScale, density) }
        var layers by remember(key) { mutableStateOf(ArtCache.get(key)) }
        val reveal = remember(key) { Animatable(if (layers != null) 1f else 0f) }

        // Unconditional, so that publishing the layers doesn't remove the effect and cancel its own reveal.
        LaunchedEffect(key) {
            if (layers != null || width <= 0 || height <= 0) return@LaunchedEffect
            layers = withContext(Dispatchers.Default) { ArtCache.getOrRender(key, assets) }
            if (reducedMotion) reveal.snapTo(1f) else reveal.animateTo(1f, androidx.compose.animation.core.tween(220))
        }

        // Backdrop: the cached bitmap, or a matching gradient for the frames before it is ready.
        if (style != ArtStyle.GLYPH) {
            Spacer(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        val ready = layers?.backdrop
                        if (ready == null || reveal.value < 1f) drawBackdrop(art)
                        if (ready != null) drawImage(ready, alpha = reveal.value)
                    },
            )
        }
        // Hero object on its own layer so it can drift independently for parallax.
        Spacer(
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    val drift = parallax()
                    translationY = -size.height * drift * 0.12f
                    scaleX = 1f + drift * 0.06f
                    scaleY = 1f + drift * 0.06f
                    alpha = reveal.value
                }
                .drawBehind { layers?.glyph?.let { drawImage(it) } },
        )
    }
}

/** Everything that determines a rendered composition. */
private data class ArtKey(
    val art: ArtSpec,
    val style: ArtStyle,
    val width: Int,
    val height: Int,
    val glyphScale: Float,
    val density: Float,
)

private class ArtLayers(val backdrop: ImageBitmap?, val glyph: ImageBitmap?) {
    val bytes: Int get() = listOfNotNull(backdrop, glyph).sumOf { it.width * it.height * 4 }
}

/** Memory-bounded cache of rendered covers, shared by every screen. */
private object ArtCache {
    private val maxBytes = (Runtime.getRuntime().maxMemory() / 6).coerceAtMost(96L * 1024 * 1024).toInt()

    private val cache = object : LruCache<ArtKey, ArtLayers>(maxBytes) {
        override fun sizeOf(key: ArtKey, value: ArtLayers) = value.bytes
    }

    fun get(key: ArtKey): ArtLayers? = cache.get(key)

    fun getOrRender(key: ArtKey, assets: AssetManager): ArtLayers = cache.get(key) ?: render(key, assets).also { cache.put(key, it) }

    private fun render(key: ArtKey, assets: AssetManager): ArtLayers {
        val size = Size(key.width.toFloat(), key.height.toFloat())
        val density = Density(key.density)
        val photo = key.art.photo?.takeIf { key.style != ArtStyle.GLYPH }?.let { decodePhoto(assets, it, key.width, key.height) }
        if (photo != null) {
            // The photo already shows the sport, so it replaces both the drawn surface and the hero object.
            val backdrop = paint(key.width, key.height, density, size) { drawPhoto(photo, key.art) }
            photo.recycle()
            return ArtLayers(backdrop, glyph = null)
        }
        val backdrop = if (key.style == ArtStyle.GLYPH) null else paint(key.width, key.height, density, size) {
            drawBackdrop(key.art)
            drawPerspectiveMotif(key.art, key.style, key.density)
            drawLightAndGrain(grainPoints(key.art.seed))
        }
        val glyph = paint(key.width, key.height, density, size) {
            drawHeroObject(key.art, key.style, key.glyphScale)
        }
        return ArtLayers(backdrop, glyph)
    }

    /** Decodes a bundled photo, subsampled to the smallest power-of-two size that still covers the target. */
    private fun decodePhoto(assets: AssetManager, path: String, width: Int, height: Int): android.graphics.Bitmap? = runCatching {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        assets.open(path).use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= width && bounds.outHeight / (sample * 2) >= height) sample *= 2
        assets.open(path).use { BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample }) }
    }.getOrNull()

    private inline fun paint(width: Int, height: Int, density: Density, size: Size, crossinline block: DrawScope.() -> Unit): ImageBitmap {
        val bitmap = ImageBitmap(width, height)
        CanvasDrawScope().draw(density, LayoutDirection.Ltr, ComposeCanvas(bitmap), size) { block() }
        return bitmap
    }
}

private fun DrawScope.glyphPlacement(style: ArtStyle, scale: Float): Pair<Offset, Float> = when (style) {
    ArtStyle.COVER -> Offset(size.width * 0.7f, size.height * 0.3f) to size.minDimension * 0.25f * scale
    ArtStyle.CARD -> Offset(size.width * 0.66f, size.height * 0.33f) to size.minDimension * 0.24f * scale
    ArtStyle.GLYPH -> Offset(size.width / 2, size.height / 2) to size.minDimension * 0.3f * scale
}

private fun DrawScope.drawBackdrop(art: ArtSpec) {
    val base = Color(art.base)
    val mid = Color(art.mid)
    val highlight = Color(art.highlight)
    drawRect(Brush.linearGradient(listOf(lerp(mid, base, 0.25f), base), start = Offset(size.width, 0f), end = Offset(0f, size.height)))
    // A broad glow of the sport's colour, offset per sport so covers don't line up identically.
    val gx = size.width * (0.55f + (art.seed % 5) * 0.08f)
    drawCircle(
        Brush.radialGradient(listOf(mid.copy(alpha = 0.85f), Color.Transparent), center = Offset(gx, size.height * 0.15f), radius = size.maxDimension * 0.75f),
        radius = size.maxDimension * 0.75f, center = Offset(gx, size.height * 0.15f),
    )
    drawCircle(
        Brush.radialGradient(listOf(highlight.copy(alpha = 0.12f), Color.Transparent), center = Offset(size.width * 0.1f, size.height), radius = size.maxDimension * 0.6f),
        radius = size.maxDimension * 0.6f, center = Offset(size.width * 0.1f, size.height),
    )
}

/**
 * The playing surface tilted in 3D for a stadium-like perspective. Reproduces the transform a hardware
 * layer would apply (scale, then camera rotation about the centre, then translation) on a software canvas.
 */
private fun DrawScope.drawPerspectiveMotif(art: ArtSpec, style: ArtStyle, density: Float) {
    val camera = Camera()
    camera.setLocation(0f, 0f, -9f * density)
    camera.rotateX(if (style == ArtStyle.COVER) 52f else 46f)
    camera.rotateZ(-(-14f + (art.seed % 7) * 2f))
    val cameraMatrix = Matrix()
    camera.getMatrix(cameraMatrix)
    val pivotX = size.width / 2
    val pivotY = size.height / 2
    cameraMatrix.preTranslate(-pivotX, -pivotY)
    cameraMatrix.postTranslate(pivotX, pivotY + size.height * 0.18f)
    val matrix = Matrix().apply {
        setScale(1.45f, 1.45f, pivotX, pivotY)
        postConcat(cameraMatrix)
    }
    val canvas = drawContext.canvas.nativeCanvas
    canvas.save()
    canvas.concat(matrix)
    val alpha = if (style == ArtStyle.COVER) 0.9f else 0.75f
    drawMotif(art.motif, lerp(Color(art.highlight), Color.White, 0.4f).copy(alpha = 0.22f * alpha), size.minDimension * 0.006f)
    canvas.restore()
}

private fun DrawScope.drawHeroObject(art: ArtSpec, style: ArtStyle, glyphScale: Float) {
    val highlight = Color(art.highlight)
    val (center, radius) = glyphPlacement(style, glyphScale)
    if (style != ArtStyle.GLYPH) {
        drawCircle(
            Brush.radialGradient(listOf(highlight.copy(alpha = 0.35f), Color.Transparent), center = center, radius = radius * 2.4f),
            radius = radius * 2.4f, center = center,
        )
    }
    drawGlyph(art.glyph, center, radius, GlyphPalette(material = Color(art.mid), accent = highlight, ink = Color(art.base)))
}

/** Centre-crops [photo] into the canvas and grades it toward the sport's palette, keeping the bottom dark for text. */
private fun DrawScope.drawPhoto(photo: android.graphics.Bitmap, art: ArtSpec) {
    val scale = maxOf(size.width / photo.width, size.height / photo.height)
    val srcW = (size.width / scale).toInt()
    val srcH = (size.height / scale).toInt()
    val left = (photo.width - srcW) / 2
    val top = (photo.height - srcH) / 2
    drawContext.canvas.nativeCanvas.drawBitmap(
        photo, Rect(left, top, left + srcW, top + srcH), Rect(0, 0, size.width.toInt(), size.height.toInt()),
        Paint(Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG),
    )
    val base = Color(art.base)
    drawRect(Color(art.mid).copy(alpha = 0.35f), blendMode = BlendMode.Softlight)
    drawRect(
        Brush.verticalGradient(
            0f to base.copy(alpha = 0.55f), 0.22f to base.copy(alpha = 0.12f), 0.5f to base.copy(alpha = 0.55f), 1f to base.copy(alpha = 0.95f),
        ),
    )
    drawRect(Brush.horizontalGradient(0f to base.copy(alpha = 0.5f), 0.7f to Color.Transparent))
}

private fun DrawScope.drawLightAndGrain(grain: List<Offset>) {
    // Two diagonal light streaks.
    withTransform({ rotate(-28f, Offset(size.width / 2, size.height / 2)) }) {
        drawRect(Color.White.copy(alpha = 0.035f), Offset(size.width * 0.15f, -size.height), Size(size.width * 0.12f, size.height * 3))
        drawRect(Color.White.copy(alpha = 0.025f), Offset(size.width * 0.34f, -size.height), Size(size.width * 0.04f, size.height * 3))
    }
    drawPoints(
        grain.map { Offset(it.x * size.width, it.y * size.height) },
        PointMode.Points, Color.White.copy(alpha = 0.06f), strokeWidth = 1.4f,
    )
    // Vignette.
    drawRect(
        Brush.radialGradient(
            0.55f to Color.Transparent, 1f to Color.Black.copy(alpha = 0.45f),
            center = Offset(size.width * 0.6f, size.height * 0.4f), radius = size.maxDimension * 0.85f,
        ),
    )
}

private fun grainPoints(seed: Int): List<Offset> {
    val random = Random(seed)
    return List(700) { Offset(random.nextFloat(), random.nextFloat()) }
}

/** Flat field-of-play diagram for the sport detail screen. */
@Composable
fun FieldDiagram(
    art: ArtSpec,
    lineColor: Color,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier) {
        drawMotif(art.motif, lineColor, size.minDimension * 0.008f)
    }
}
