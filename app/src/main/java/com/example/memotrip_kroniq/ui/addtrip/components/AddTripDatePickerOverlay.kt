package com.example.memotrip_kroniq.ui.addtrip.components

import PreviewUiScaler
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.example.memotrip_kroniq.ui.addtrip.DateRange
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import com.example.memotrip_kroniq.ui.core.LocalUiScaler
import com.example.memotrip_kroniq.ui.theme.MemoTripTheme
import innerShadow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripDatePickerOverlay(
    initialStartDate: LocalDate?,
    initialEndDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (DateRange) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable { onDismiss() }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .clip(RoundedCornerShape(16.dp))
                //.background(Color(0xFF759F67))
                .innerShadow()
                .clickable(enabled = false) {} // ❗ blok kliků skrz
        ) {
            AddTripDatePickerContent(
                initialStartDate = initialStartDate,
                initialEndDate = initialEndDate,
                onConfirm = onConfirm
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AddTripDatePickerContent(
    initialStartDate: LocalDate?,
    initialEndDate: LocalDate?,

    onConfirm: (DateRange) -> Unit
) {
    val today = LocalDate.now()
    val seedDate = initialStartDate ?: today

    var currentYm by remember(initialStartDate, initialEndDate) {
        mutableStateOf(YearMonth.from(seedDate))
    }

    var startDate by remember(initialStartDate, initialEndDate) {
        mutableStateOf(initialStartDate)
    }

    var endDate by remember(initialStartDate, initialEndDate) {
        mutableStateOf(initialEndDate)
    }

    // 🎨 COLORS (Figmoidní)
    val sheetBg = Color(0xFF759F67)
    val rangeBg = Color(0xFF383A41).copy(alpha = 0.75f)
    val selectedBg = Color.White
    val textPrimary = Color.White
    val dayTextDefault = Color(0xFF1B1E22)
    val dayTextMuted = dayTextDefault.copy(alpha = 0.75f)
    val monthTextInactive = Color(0xFF383A41)


    Column(
        modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                //.padding(horizontal = 16.dp, vertical = 12.dp)
        ) {

            // 🟩 ZELENÝ BOX (CARD)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)) // ⬅️ horní + spodní radius
                    .background(sheetBg)
                    .innerShadow()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {

                    // ───────── YEARS ─────────
                    val yearTextDefault = Color(0xFF383A41)

                    val years = remember(today) { (today.year - 3..today.year + 1).toList() }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items(years) { year ->
                            val selected = currentYm.year == year

                            Text(
                                text = year.toString(),
                                color = if (selected)
                                    Color.White
                                else
                                    yearTextDefault,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 20.sp,
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        currentYm = YearMonth.of(year, currentYm.month)
                                    }
                                    .padding(horizontal = 4.dp, vertical = 6.dp)
                            )
                        }
                    }


                    Spacer(Modifier.height(10.dp))

                    // ───────── MONTHS ─────────
                    var selectedMonthOffsetX by remember { mutableStateOf(0f) }
                    var selectedMonthWidth by remember { mutableStateOf(0f) }

                    Column {

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items((1..12).toList()) { m ->
                                val selected = currentYm.monthValue == m
                                val label = YearMonth.of(2000, m)
                                    .month
                                    .getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                                    .uppercase(Locale.ENGLISH)


                                Box(
                                    modifier = Modifier
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) {
                                            currentYm = YearMonth.of(currentYm.year, m)
                                        }
                                        .onGloballyPositioned { coords ->
                                            if (selected) {
                                                selectedMonthOffsetX = coords.positionInParent().x
                                                selectedMonthWidth = coords.size.width.toFloat()
                                            }
                                        }
                                ) {
                                    Text(
                                        text = label,
                                        color = if (selected)
                                            Color.White
                                        else
                                            monthTextInactive,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(6.dp))

                        // ── PODTRŽENÍ ──
                        Box {

                            // základní (šedá) čára
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                                    .background(monthTextInactive.copy(alpha = 0.6f))
                            )

                            // aktivní (bílý) segment
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset(selectedMonthOffsetX.toInt(), 0) }
                                    .width(with(LocalDensity.current) { selectedMonthWidth.toDp() })
                                    .height(2.dp)
                                    .background(Color.White)
                            )
                        }
                    }










                    Spacer(Modifier.height(14.dp))

                    // 📅 CALENDAR – BEZE ZMĚN
                    CalendarGrid(
                        yearMonth = currentYm,
                        startDate = startDate,
                        endDate = endDate,
                        onDayClick = { day ->
                            when {
                                startDate == null -> {
                                    startDate = day
                                    endDate = null
                                }
                                endDate == null -> {
                                    endDate = day
                                    if (endDate!!.isBefore(startDate!!)) {
                                        val tmp = startDate
                                        startDate = endDate
                                        endDate = tmp
                                    }
                                }
                                else -> {
                                    startDate = day
                                    endDate = null
                                }
                            }
                        },
                        rangeBg = rangeBg,
                        selectedBg = selectedBg,
                        dayTextDefault = dayTextDefault,
                        dayTextMuted = dayTextMuted,
                        textPrimary = textPrimary
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }

            Spacer(Modifier.height(6.dp))

            // ⬛ BLACK FOOTER
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(
                        top = 20.dp,
                        bottom = 28.dp
                    ),
                contentAlignment = Alignment.Center
            ) {
                PrimaryButton(
                    text = "Next",
                    enabled = startDate != null && endDate != null,
                    fontSize = 20.sp,
                    modifier = Modifier.width(280.dp),
                    onClick = {
                        val s = startDate!!
                        val e = endDate!!
                        onConfirm(
                            if (e.isBefore(s)) DateRange(e, s)
                            else DateRange(s, e)
                        )
                    }
                )

                Spacer(Modifier.height(14.dp))
            }
        }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    startDate: LocalDate?,
    endDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
    rangeBg: Color,
    selectedBg: Color,
    dayTextDefault: Color,
    dayTextMuted: Color,
    textPrimary: Color
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDowIndex =
        ((firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7

    val totalCells = firstDowIndex + daysInMonth
    val rows = ((totalCells + 6) / 7)

    val cellHeight = 34.dp
    val pillRadius = cellHeight / 2

    BoxWithConstraints {
        val cellWidth = maxWidth / 7
        val cellHeight = 34.dp
        val pillRadius = cellHeight / 2

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

            // Week header
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                listOf("MON","TUE","WED","THU","FRI","SAT","SUN").forEach {
                    Box(
                        modifier = Modifier.width(cellWidth),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            it,
                            color = textPrimary.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        )
                    }
                }
            }


            for (r in 0 until rows) {

                val weekDates = (0..6).mapNotNull { c ->
                    val dayNum = r * 7 + c - firstDowIndex + 1
                    if (dayNum in 1..daysInMonth) yearMonth.atDay(dayNum) else null
                }

                val rangeDates = weekDates.filter { date ->
                    startDate != null && endDate != null &&
                            !date.isBefore(startDate) &&
                            !date.isAfter(endDate)
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cellHeight)
                ) {

                    // 🔥 RANGE BACKGROUND (správně po týdnech)
                    if (rangeDates.isNotEmpty()) {

                        val startIndex = (0..6).first { c ->
                            val dayNum = r * 7 + c - firstDowIndex + 1
                            dayNum in 1..daysInMonth &&
                                    yearMonth.atDay(dayNum) == rangeDates.first()
                        }

                        val endIndex = (0..6).first { c ->
                            val dayNum = r * 7 + c - firstDowIndex + 1
                            dayNum in 1..daysInMonth &&
                                    yearMonth.atDay(dayNum) == rangeDates.last()
                        }


                        val isFirstRow = rangeDates.first() == startDate
                        val isLastRow = rangeDates.last() == endDate

                        val offsetX = when {
                            isFirstRow -> startIndex * cellWidth + pillRadius
                            else -> startIndex * cellWidth
                        }

                        val width = when {
                            isFirstRow && isLastRow ->
                                ((endIndex - startIndex + 1) * cellWidth) - pillRadius * 2

                            isFirstRow ->
                                ((7 - startIndex) * cellWidth) - pillRadius

                            isLastRow ->
                                ((endIndex - startIndex + 1) * cellWidth) -pillRadius

                            else ->
                                7 * cellWidth
                        }


                        Box(
                            modifier = Modifier
                                .offset(x = offsetX)
                                .width(width)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(pillRadius))
                                .background(rangeBg)
                        )
                    }

                    // 📅 DAYS
                    Row(
                        modifier = Modifier.width(cellWidth * 7)
                    ) {
                        for (c in 0..6) {
                            val dayNum = r * 7 + c - firstDowIndex + 1
                            if (dayNum in 1..daysInMonth) {
                                val date = yearMonth.atDay(dayNum)
                                val isStart = date == startDate
                                val isEnd = date == endDate

                                Box(
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .height(cellHeight)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null   // ❌ vypne ripple / pressed efekt
                                        ) {
                                            onDayClick(date)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {

                                    // 🔥 DOKONALÝ KRUH
                                    if (isStart || isEnd) {
                                        Box(
                                            modifier = Modifier
                                                .size(cellHeight)      // ⬅️ čtverec
                                                .clip(CircleShape)     // ⬅️ kruh
                                                .background(selectedBg)
                                        )
                                    }

                                    Text(
                                        text = dayNum.toString(),
                                        color = dayTextDefault,
                                        fontWeight =
                                            if (isStart || isEnd) FontWeight.Bold
                                            else FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .width(cellWidth)
                                        .height(34.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(
    showBackground = true,
    backgroundColor = 0xFF000000,
    widthDp = 412,
    heightDp = 1000
)
@Composable
private fun AddTripDatePickerSheetPreview() {
    CompositionLocalProvider(
        LocalUiScaler provides PreviewUiScaler
    ) {
        MemoTripTheme {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                AddTripDatePickerOverlay(
                    initialStartDate = LocalDate.of(2025, 4, 2),
                    initialEndDate = LocalDate.of(2025, 4, 24),
                    onDismiss = {},
                    onConfirm = {}
                )
            }
        }
    }
}