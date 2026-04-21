package com.example.memotrip_kroniq.ui.profile.components

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.memotrip_kroniq.R
import com.example.memotrip_kroniq.ui.components.PrimaryButton
import innerShadow
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ProfileDatePickerOverlay(
    initialDate: LocalDate?,
    onDismiss: () -> Unit,
    onConfirm: (LocalDate) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onDismiss() }
        )

        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .clickable(enabled = false) {}
        ) {
            ProfileDatePickerContent(
                initialDate = initialDate,
                onConfirm = onConfirm
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun ProfileDatePickerContent(
    initialDate: LocalDate?,
    onConfirm: (LocalDate) -> Unit
) {
    val today = LocalDate.now()
    val minYear = today.year - 100
    val maxYear = today.year - 2

    val maxSelectableDate = today.minusYears(2)
    val defaultVisibleDate = initialDate ?: maxSelectableDate

    var currentYm by remember(initialDate) {
        mutableStateOf(YearMonth.from(defaultVisibleDate))
    }

    var selectedDate by remember(initialDate) {
        mutableStateOf(initialDate ?: maxSelectableDate)
    }

    val years = remember(today) {
        (minYear..maxYear).toList()
    }

    val yearListState = rememberLazyListState()
    val monthListState = rememberLazyListState()

    LaunchedEffect(currentYm.year) {
        val index = years.indexOf(currentYm.year)
        if (index >= 0) {
            yearListState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(currentYm.monthValue) {
        monthListState.animateScrollToItem(currentYm.monthValue - 1)
    }

    val sheetBg = Color(0xFF759F67)
    val selectedBg = Color.White
    val textPrimary = Color.White
    val dayTextDefault = Color(0xFF1B1E22)
    val monthTextInactive = Color(0xFF383A41)
    val locale = Locale.getDefault()
    val weekdayLabels = stringArrayResource(R.array.profile_date_picker_weekdays)

    Column {
        Box(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .align(Alignment.CenterHorizontally)
                .clip(RoundedCornerShape(16.dp))
                .background(sheetBg)
                .innerShadow()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                LazyRow(
                    state = yearListState,
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(years) { year ->
                        val selected = currentYm.year == year
                        Text(
                            text = year.toString(),
                            color = if (selected) Color.White else Color(0xFF383A41),
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

                var selectedMonthOffsetX by remember { mutableFloatStateOf(0f) }
                var selectedMonthWidth by remember { mutableFloatStateOf(0f) }

                Column {
                    LazyRow(
                        state = monthListState,
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        items((1..12).toList()) { m ->
                            val selected = currentYm.monthValue == m
                            val label = YearMonth.of(2000, m)
                                .month
                                .getDisplayName(TextStyle.FULL, locale)
                                .uppercase(locale)

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
                                    color = if (selected) Color.White else monthTextInactive,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(6.dp))

                    Box {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(2.dp)
                                .background(monthTextInactive.copy(alpha = 0.6f))
                        )
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

                SingleDateCalendarGrid(
                    yearMonth = currentYm,
                    selectedDate = selectedDate,
                    onDayClick = { selectedDate = it },
                    selectedBg = selectedBg,
                    dayTextDefault = dayTextDefault,
                    textPrimary = textPrimary,
                    weekdayLabels = weekdayLabels
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        Box(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            PrimaryButton(
                text = stringResource(R.string.common_next),
                enabled = selectedDate != null,
                modifier = Modifier.width(200.dp),
                onClick = {
                    selectedDate?.let(onConfirm)
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SingleDateCalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate?,
    onDayClick: (LocalDate) -> Unit,
    selectedBg: Color,
    dayTextDefault: Color,
    textPrimary: Color,
    weekdayLabels: Array<String>
) {
    val firstDay = yearMonth.atDay(1)
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDowIndex =
        ((firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7

    val totalCells = firstDowIndex + daysInMonth
    val rows = ((totalCells + 6) / 7)
    val cellHeight = 34.dp

    BoxWithConstraints {
        val cellWidth = maxWidth / 7

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                weekdayLabels.forEach {
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
                Row(
                    modifier = Modifier.width(cellWidth * 7)
                ) {
                    for (c in 0..6) {
                        val dayNum = r * 7 + c - firstDowIndex + 1

                        if (dayNum in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNum)
                            val isSelected = date == selectedDate

                            Box(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        onDayClick(date)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(cellHeight)
                                            .clip(CircleShape)
                                            .background(selectedBg)
                                    )
                                }

                                Text(
                                    text = dayNum.toString(),
                                    color = dayTextDefault,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            Spacer(
                                modifier = Modifier
                                    .width(cellWidth)
                                    .height(cellHeight)
                            )
                        }
                    }
                }
            }
        }
    }
}
