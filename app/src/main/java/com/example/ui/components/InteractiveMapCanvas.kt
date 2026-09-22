package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import kotlin.math.*

data class MapMarker(
    val title: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double,
    val type: MarkerType
)

enum class MarkerType {
    PICKUP,
    DROP,
    CAPTAIN_BIKE,
    NEARBY_BIKE,
    USER_LOCATION
}

@OptIn(ExperimentalTextApi::class)
@Composable
fun InteractiveMapCanvas(
    modifier: Modifier = Modifier,
    pickupLat: Double? = 12.9784,
    pickupLng: Double? = 77.6408,
    pickupTitle: String? = "Indiranagar 100ft Rd",
    dropLat: Double? = 12.9352,
    dropLng: Double? = 77.6245,
    dropTitle: String? = "Koramangala Sony Signal",
    captainLat: Double? = null,
    captainLng: Double? = null,
    captainName: String? = "Ramesh (Hero Splendor)",
    nearbyCaptains: List<Pair<Double, Double>> = listOf(
        Pair(12.9740, 77.6350),
        Pair(12.9810, 77.6450),
        Pair(12.9710, 77.6390)
    ),
    routeProgress: Float = 0f, // 0.0f to 1.0f along path
    showTraffic: Boolean = true,
    etaMinutes: Int? = 8,
    distanceKm: Double? = 4.8
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    // Pulsing radar animation for markers
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 10f,
        targetValue = 35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .background(Color(0xFF0F172A))
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val center = Offset(width / 2f + offsetX, height / 2f + offsetY)

            // 1. Draw Map Background Geometries (Water body, Green Tech Parks, Road Grid)
            drawMapBase(center, zoomScale, width, height)

            // Center coordinate anchor
            val baseLat = pickupLat ?: 12.9784
            val baseLng = pickupLng ?: 77.6408

            fun geoToScreen(lat: Double, lng: Double): Offset {
                val scale = 3200f * zoomScale
                val x = center.x + ((lng - baseLng) * scale).toFloat()
                val y = center.y - ((lat - baseLat) * scale * 1.15f).toFloat()
                return Offset(x, y)
            }

            // 2. Draw Route Polyline if both Pickup & Drop exist
            if (pickupLat != null && pickupLng != null && dropLat != null && dropLng != null) {
                val pPoint = geoToScreen(pickupLat, pickupLng)
                val dPoint = geoToScreen(dropLat, dropLng)

                // Create realistic curved street route path
                val mid1 = Offset(pPoint.x + (dPoint.x - pPoint.x) * 0.4f - 30f * zoomScale, pPoint.y + (dPoint.y - pPoint.y) * 0.2f)
                val mid2 = Offset(pPoint.x + (dPoint.x - pPoint.x) * 0.7f + 25f * zoomScale, pPoint.y + (dPoint.y - pPoint.y) * 0.8f)

                val routePath = Path().apply {
                    moveTo(pPoint.x, pPoint.y)
                    cubicTo(mid1.x, mid1.y, mid2.x, mid2.y, dPoint.x, dPoint.y)
                }

                // Outer route glow (Dark Teal/Amber)
                drawPath(
                    path = routePath,
                    color = VeloAmber.copy(alpha = 0.35f),
                    style = Stroke(width = 14f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Main route line
                drawPath(
                    path = routePath,
                    color = VeloAmber,
                    style = Stroke(width = 6f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )

                // Traveled path progress line (Emerald Green)
                if (routeProgress > 0f) {
                    val currentPos = Offset(
                        pPoint.x + (dPoint.x - pPoint.x) * routeProgress,
                        pPoint.y + (dPoint.y - pPoint.y) * routeProgress
                    )
                    drawLine(
                        color = VeloEmerald,
                        start = pPoint,
                        end = currentPos,
                        strokeWidth = 6f * zoomScale,
                        cap = StrokeCap.Round
                    )
                }
            }

            // 3. Draw Nearby Captains
            for ((nLat, nLng) in nearbyCaptains) {
                val pt = geoToScreen(nLat, nLng)
                drawNearbyBike(pt, zoomScale)
            }

            // 4. Draw Active Captain Motorbike if present
            if (captainLat != null && captainLng != null) {
                val cPt = geoToScreen(captainLat, captainLng)
                drawActiveCaptain(
                    cPt,
                    pulseRadius,
                    pulseAlpha,
                    zoomScale,
                    captainName ?: "Captain",
                    textMeasurer
                )
            }

            // 5. Draw Pickup Marker
            if (pickupLat != null && pickupLng != null) {
                val pPt = geoToScreen(pickupLat, pickupLng)
                drawMarker(
                    pt = pPt,
                    color = VeloEmerald,
                    title = "PICKUP",
                    subtitle = pickupTitle ?: "Current Location",
                    pulseR = pulseRadius,
                    pulseA = pulseAlpha,
                    textMeasurer = textMeasurer,
                    zoom = zoomScale
                )
            }

            // 6. Draw Drop Marker
            if (dropLat != null && dropLng != null) {
                val dPt = geoToScreen(dropLat, dropLng)
                drawMarker(
                    pt = dPt,
                    color = VeloRed,
                    title = "DESTINATION",
                    subtitle = dropTitle ?: "Drop Point",
                    pulseR = 0f,
                    pulseA = 0f,
                    textMeasurer = textMeasurer,
                    zoom = zoomScale
                )
            }
        }

        // Map Control Floating Badges (Zoom in/out, Recenter, Traffic Indicator)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { zoomScale = (zoomScale * 1.25f).coerceAtMost(2.5f) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White)
            }
            IconButton(
                onClick = { zoomScale = (zoomScale / 1.25f).coerceAtLeast(0.6f) },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White)
            }
            IconButton(
                onClick = {
                    offsetX = 0f
                    offsetY = 0f
                    zoomScale = 1.0f
                },
                modifier = Modifier
                    .size(40.dp)
                    .background(Color(0xFF1E293B).copy(alpha = 0.9f), CircleShape)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = VeloAmber)
            }
        }

        // Floating ETA & Live Distance Pill
        if (etaMinutes != null && distanceKm != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 12.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A).copy(alpha = 0.88f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(VeloEmerald, CircleShape)
                    )
                    Text(
                        text = "$distanceKm km  •  $etaMinutes min ETA",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMapBase(center: Offset, zoom: Float, width: Float, height: Float) {
    // 1. Draw River curve / Lake
    val waterPath = Path().apply {
        moveTo(0f, height * 0.85f)
        cubicTo(width * 0.3f, height * 0.75f, width * 0.6f, height * 0.95f, width, height * 0.88f)
        lineTo(width, height)
        lineTo(0f, height)
        close()
    }
    drawPath(waterPath, color = Color(0xFF0C2444))

    // 2. Draw Green Parks (Cubbon Park / Lalbagh Style)
    drawRoundRect(
        color = Color(0xFF063327),
        topLeft = Offset(center.x - 220f * zoom, center.y - 180f * zoom),
        size = androidx.compose.ui.geometry.Size(140f * zoom, 90f * zoom),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(16f * zoom)
    )
    drawRoundRect(
        color = Color(0xFF063327),
        topLeft = Offset(center.x + 160f * zoom, center.y + 80f * zoom),
        size = androidx.compose.ui.geometry.Size(180f * zoom, 120f * zoom),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(20f * zoom)
    )

    // 3. Draw Road Grid (Primary Avenues & Ring Roads)
    val roadPaint = Color(0xFF1E293B)
    val arterialRoadPaint = Color(0xFF334155)

    // Horizontal roads
    for (i in -4..4) {
        val y = center.y + i * 90f * zoom
        drawLine(
            color = if (i % 2 == 0) arterialRoadPaint else roadPaint,
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = if (i % 2 == 0) 4f * zoom else 2f * zoom
        )
    }

    // Vertical roads
    for (i in -4..4) {
        val x = center.x + i * 110f * zoom
        drawLine(
            color = if (i % 2 == 0) arterialRoadPaint else roadPaint,
            start = Offset(x, 0f),
            end = Offset(x, height),
            strokeWidth = if (i % 2 == 0) 4f * zoom else 2f * zoom
        )
    }

    // Diagonal Ring Road
    drawLine(
        color = Color(0xFF475569),
        start = Offset(0f, center.y - 200f * zoom),
        end = Offset(width, center.y + 200f * zoom),
        strokeWidth = 5f * zoom
    )
}

private fun DrawScope.drawNearbyBike(pt: Offset, zoom: Float) {
    // Small yellow bike dot with halo
    drawCircle(color = VeloAmber.copy(alpha = 0.25f), radius = 12f * zoom, center = pt)
    drawCircle(color = VeloAmber, radius = 6f * zoom, center = pt)
    drawCircle(color = Color.Black, radius = 2f * zoom, center = pt)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawActiveCaptain(
    pt: Offset,
    pulseR: Float,
    pulseA: Float,
    zoom: Float,
    name: String,
    textMeasurer: TextMeasurer
) {
    // Pulsing radar wave
    drawCircle(
        color = VeloAmber.copy(alpha = pulseA),
        radius = pulseR * zoom,
        center = pt,
        style = Stroke(width = 2f * zoom)
    )

    // Motorbike Badge outer ring
    drawCircle(color = Color.Black, radius = 16f * zoom, center = pt)
    drawCircle(color = VeloAmber, radius = 13f * zoom, center = pt)
    drawCircle(color = Color(0xFF0F172A), radius = 9f * zoom, center = pt)

    // Directional Pointer Needle
    val arrowPath = Path().apply {
        moveTo(pt.x, pt.y - 16f * zoom)
        lineTo(pt.x - 5f * zoom, pt.y - 10f * zoom)
        lineTo(pt.x + 5f * zoom, pt.y - 10f * zoom)
        close()
    }
    drawPath(arrowPath, color = VeloAmber)

    // Captain Name Tag above marker
    val textResult = textMeasurer.measure(
        text = AnnotatedString("🏍️ $name"),
        style = TextStyle(
            color = Color.White,
            fontSize = (10 * zoom).sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
        )
    )
    val tagTopLeft = Offset(pt.x - textResult.size.width / 2f, pt.y - 36f * zoom)
    drawRoundRect(
        color = Color(0xFF0F172A).copy(alpha = 0.9f),
        topLeft = Offset(tagTopLeft.x - 6f, tagTopLeft.y - 4f),
        size = androidx.compose.ui.geometry.Size(
            textResult.size.width.toFloat() + 12f,
            textResult.size.height.toFloat() + 8f
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f)
    )
    drawText(textResult, topLeft = tagTopLeft)
}

@OptIn(ExperimentalTextApi::class)
private fun DrawScope.drawMarker(
    pt: Offset,
    color: Color,
    title: String,
    subtitle: String,
    pulseR: Float,
    pulseA: Float,
    textMeasurer: TextMeasurer,
    zoom: Float
) {
    if (pulseR > 0f) {
        drawCircle(
            color = color.copy(alpha = pulseA),
            radius = pulseR * zoom,
            center = pt,
            style = Stroke(width = 2.5f * zoom)
        )
    }

    // Pin Shadow
    drawCircle(color = Color.Black.copy(alpha = 0.4f), radius = 8f * zoom, center = Offset(pt.x, pt.y + 4f * zoom))

    // Pin Base
    drawCircle(color = Color.White, radius = 12f * zoom, center = pt)
    drawCircle(color = color, radius = 9f * zoom, center = pt)
    drawCircle(color = Color.White, radius = 4f * zoom, center = pt)

    // Marker Label
    val textResult = textMeasurer.measure(
        text = AnnotatedString(title),
        style = TextStyle(
            color = Color.White,
            fontSize = (9 * zoom).sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold
        )
    )
    val tagTopLeft = Offset(pt.x - textResult.size.width / 2f, pt.y - 28f * zoom)
    drawRoundRect(
        color = color.copy(alpha = 0.95f),
        topLeft = Offset(tagTopLeft.x - 6f, tagTopLeft.y - 3f),
        size = androidx.compose.ui.geometry.Size(
            textResult.size.width.toFloat() + 12f,
            textResult.size.height.toFloat() + 6f
        ),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f)
    )
    drawText(textResult, topLeft = tagTopLeft)
}
