package com.example.takstud.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.takstud.ui.theme.AccentOrange
import com.example.takstud.ui.theme.Neutral600
import com.example.takstud.ui.theme.Neutral900
import com.example.takstud.ui.theme.PrimaryBlue
import java.util.*

@Composable
fun WeeklyCalendarView(
    currentDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    hasEventsOnDate: (Calendar) -> Boolean
) {
    val daysOfWeek = listOf("Dom", "Seg", "Ter", "Qua", "Qui", "Sex", "Sáb")
    val calendar = currentDate.clone() as Calendar
    calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        // Cabeçalho dos dias da semana
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Neutral600
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Linha de dias
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            repeat(7) {
                val dayCalendar = calendar.clone() as Calendar
                DayCell(
                    day = dayCalendar.get(Calendar.DAY_OF_MONTH),
                    isSelected = dayCalendar.get(Calendar.DAY_OF_YEAR) == currentDate.get(Calendar.DAY_OF_YEAR),
                    hasEvents = hasEventsOnDate(dayCalendar),
                    onClick = { onDateSelected(dayCalendar) }
                )
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            }
        }
    }
}

@Composable
fun RowScope.DayCell(
    day: Int,
    isSelected: Boolean,
    hasEvents: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> PrimaryBlue
                    else -> Color.Transparent
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = when {
                    isSelected -> Color.White
                    else -> Neutral900
                },
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )

            if (hasEvents && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(AccentOrange)
                )
            }
        }
    }
}
