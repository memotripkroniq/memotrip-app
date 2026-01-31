package com.example.memotrip_kroniq.ui.home.components.upsell

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.home.components.modifiers.planButtonTopInnerShadow
import com.example.memotrip_kroniq.ui.home.components.upsell.KroniqCtaButton
import innerShadow

@Composable
fun PlanCard(
    modifier: Modifier = Modifier,
    title: String,
    useTitleImage: Boolean = false,
    titleStrokeColor: Color,
    lines: List<String>,
    price: String?,
    buttonText: String,
    backgroundColor: Color,
    strokeColor: Color?,
    innerShadowEnabled: Boolean,
    buttonBackgroundColor: Color,
    onClick: (() -> Unit)?
) {
    val shape = RoundedCornerShape(10.dp)
    val isKroniq = title == "KroniQ"

    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .then(if (strokeColor != null) Modifier.border(1.dp, strokeColor, shape) else Modifier)
            .then(
                if (innerShadowEnabled) Modifier.innerShadow(
                    color = Color.Black.copy(alpha = 0.25f),
                    radius = 14.dp
                ) else Modifier
            )
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // TITLE (SVG)
        if (useTitleImage) {
            when (title) {
                "Free" -> TitleImage(R.drawable.package_upsell_free, "Free")
                "Premium" -> TitleImage(R.drawable.package_upsell_premium, "Premium")
                "KroniQ" -> TitleImage(R.drawable.package_upsell_kroniq, "KroniQ")
                else -> OutlinedTitleText(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    strokeColor = titleStrokeColor
                )
            }
        } else {
            OutlinedTitleText(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                strokeColor = titleStrokeColor
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        lines.forEach { line ->
            Text(
                text = line,
                modifier = Modifier.fillMaxWidth(),
                color = Color.White.copy(alpha = 0.92f),
                fontSize = 10.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(14.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        // PRICE (image)
        if (price != null) {
            val normalizedPrice = price.replace('\u00A0', ' ').trim()

            val priceDrawableId = when (normalizedPrice) {
                "5.99 €" -> R.drawable.package_upsell_premium_5_99
                "36 €" -> R.drawable.package_upsell_kroniq_36
                else -> null
            }

            if (priceDrawableId != null) {
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = priceDrawableId),
                        contentDescription = normalizedPrice,
                        modifier = Modifier.height(20.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
        } else {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // BUTTON
        if (isKroniq) {
            KroniqCtaButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = { onClick?.invoke() }
            )
        } else {
            Box(
                modifier = Modifier
                    .planButtonTopInnerShadow(
                        backgroundColor = buttonBackgroundColor,
                        onClick = onClick
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buttonText,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    letterSpacing = 0.4.sp,
                    maxLines = 1,
                    softWrap = false
                )
            }
        }
    }
}