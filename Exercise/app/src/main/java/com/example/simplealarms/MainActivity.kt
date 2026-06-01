package com.example.simplealarms

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.rounded.Alarm
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

enum class AlarmDay(val shortLabel: String) {
    Monday("M"),
    Tuesday("Tu"),
    Wednesday("W"),
    Thursday("Th"),
    Friday("F"),
    Saturday("Sa"),
    Sunday("Su");

    companion object {
        val all = entries
    }
}

data class AlarmItem(
    val id: Long,
    val hour: Int,
    val minute: Int,
    val isPm: Boolean,
    val label: String,
    val days: Set<AlarmDay>,
    val enabled: Boolean = true,
)

data class AlarmDraft(
    val hour: Int = 3,
    val minute: Int = 4,
    val isPm: Boolean = true,
    val label: String = "A New Alarm!",
    val days: Set<AlarmDay> = setOf(
        AlarmDay.Monday,
        AlarmDay.Tuesday,
        AlarmDay.Wednesday,
        AlarmDay.Thursday,
        AlarmDay.Friday,
    ),
)

private enum class Screen {
    List,
    Editor,
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            MaterialTheme {
                AlarmApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmApp() {
    val alarms = remember {
        mutableStateListOf(
            AlarmItem(
                id = 1L,
                hour = 3,
                minute = 4,
                isPm = true,
                label = "A New Alarm!",
                days = setOf(
                    AlarmDay.Monday,
                    AlarmDay.Tuesday,
                    AlarmDay.Wednesday,
                    AlarmDay.Thursday,
                    AlarmDay.Friday,
                ),
            )
        )
    }
    var screen by remember { mutableStateOf(Screen.List) }
    var editingAlarmId by remember { mutableStateOf<Long?>(null) }
    var draft by remember { mutableStateOf(AlarmDraft()) }

    fun openNewAlarm() {
        editingAlarmId = null
        draft = AlarmDraft()
        screen = Screen.Editor
    }

    fun openExistingAlarm(item: AlarmItem) {
        editingAlarmId = item.id
        draft = AlarmDraft(
            hour = item.hour,
            minute = item.minute,
            isPm = item.isPm,
            label = item.label,
            days = item.days,
        )
        screen = Screen.Editor
    }

    fun saveDraft() {
        val currentDraft = draft
        val item = AlarmItem(
            id = editingAlarmId ?: System.currentTimeMillis(),
            hour = currentDraft.hour,
            minute = currentDraft.minute,
            isPm = currentDraft.isPm,
            label = currentDraft.label.ifBlank { "A New Alarm!" },
            days = currentDraft.days,
        )

        val existingIndex = alarms.indexOfFirst { it.id == item.id }
        if (existingIndex >= 0) {
            alarms[existingIndex] = item
        } else {
            alarms.add(0, item)
        }
        screen = Screen.List
    }

    fun deleteDraft() {
        val targetId = editingAlarmId
        if (targetId != null) {
            alarms.removeAll { it.id == targetId }
        }
        screen = Screen.List
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (screen == Screen.List) "SimpleAlarms" else "Add Alarm",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                navigationIcon = if (screen == Screen.Editor) {
                    {
                        IconButton(onClick = { screen = Screen.List }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                        }
                    }
                } else null,
                actions = if (screen == Screen.Editor) {
                    {
                        IconButton(onClick = { saveDraft() }) {
                            Icon(Icons.Default.Save, contentDescription = "Save", tint = Color.White)
                        }
                        IconButton(onClick = { deleteDraft() }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
                        }
                    }
                } else null,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AlarmTeal,
                    titleContentColor = Color.White,
                ),
            )
        },
        floatingActionButton = {
            if (screen == Screen.List) {
                FloatingActionButton(
                    onClick = { openNewAlarm() },
                    containerColor = AlarmPink,
                    contentColor = Color.White,
                    shape = CircleShape,
                ) {
                    Icon(Icons.Rounded.Alarm, contentDescription = "Add alarm")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
        containerColor = Color.White,
    ) { paddingValues ->
        when (screen) {
            Screen.List -> AlarmListScreen(
                alarms = alarms,
                modifier = Modifier.padding(paddingValues),
                onAlarmClick = { openExistingAlarm(it) },
            )

            Screen.Editor -> AlarmEditorScreen(
                draft = draft,
                modifier = Modifier.padding(paddingValues),
                onDraftChange = { draft = it },
                onSave = { saveDraft() },
                onDelete = { deleteDraft() },
            )
        }
    }
}

@Composable
private fun AlarmListScreen(
    alarms: List<AlarmItem>,
    modifier: Modifier = Modifier,
    onAlarmClick: (AlarmItem) -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White),
    ) {
        HorizontalDivider(color = DividerGray)
        alarms.forEach { alarm ->
            AlarmRow(alarm = alarm, onClick = { onAlarmClick(alarm) })
            HorizontalDivider(color = DividerGray)
        }
    }
}

@Composable
private fun AlarmRow(
    alarm: AlarmItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = alarm.displayHour(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Medium,
                        color = AlarmText,
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = alarm.displayMeridiem(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AlarmSubText,
                        modifier = Modifier.padding(bottom = 5.dp),
                    )
                }
            }
            Spacer(modifier = Modifier.width(18.dp))
            Column {
                Text(
                    text = alarm.label,
                    color = AlarmSubText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                )
                Spacer(modifier = Modifier.height(4.dp))
                DaySummaryRow(alarm.days)
            }
        }

        Icon(
            imageVector = Icons.Rounded.Alarm,
            contentDescription = null,
            tint = AlarmPink,
            modifier = Modifier.size(28.dp),
        )
    }
}

@Composable
private fun DaySummaryRow(days: Set<AlarmDay>) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        AlarmDay.all.forEach { day ->
            Text(
                text = day.shortLabel,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (day in days) AlarmPink else AlarmGray,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlarmEditorScreen(
    draft: AlarmDraft,
    modifier: Modifier = Modifier,
    onDraftChange: (AlarmDraft) -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 16.dp),
    ) {
        SectionLabel("Time")
        Spacer(modifier = Modifier.height(12.dp))
        AlarmTimePicker(
            hour = draft.hour,
            minute = draft.minute,
            isPm = draft.isPm,
            onHourChange = { onDraftChange(draft.copy(hour = it)) },
            onMinuteChange = { onDraftChange(draft.copy(minute = it)) },
            onMeridiemChange = { onDraftChange(draft.copy(isPm = it)) },
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionLabel("Label")
        Spacer(modifier = Modifier.height(8.dp))
        TextField(
            value = draft.label,
            onValueChange = { onDraftChange(draft.copy(label = it)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = {
                Text(
                    text = "A New Alarm!",
                    color = AlarmGray,
                    fontStyle = FontStyle.Italic,
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = DividerGray,
                unfocusedIndicatorColor = DividerGray,
                cursorColor = AlarmPink,
            ),
        )

        Spacer(modifier = Modifier.height(24.dp))
        SectionLabel("Days")
        Spacer(modifier = Modifier.height(8.dp))
        AlarmDay.all.forEach { day ->
            DayRow(
                label = day.name.lowercase().replaceFirstChar { it.uppercase() },
                checked = day in draft.days,
                onCheckedChange = { checked ->
                    val updated = draft.days.toMutableSet()
                    if (checked) {
                        updated.add(day)
                    } else {
                        updated.remove(day)
                    }
                    onDraftChange(draft.copy(days = updated))
                },
            )
        }
    }
}

@Composable
private fun DayRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, color = AlarmSubText, fontSize = 14.sp)
        Checkbox(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun AlarmTimePicker(
    hour: Int,
    minute: Int,
    isPm: Boolean,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onMeridiemChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        WheelColumn(
            previous = formatHour(previousHour(hour)),
            current = formatHour(hour),
            next = formatHour(nextHour(hour)),
            onPreviousClick = { onHourChange(previousHour(hour)) },
            onCurrentClick = { onHourChange(nextHour(hour)) },
            onNextClick = { onHourChange(nextHour(hour)) },
        )
        Text(
            text = ":",
            fontSize = 32.sp,
            color = AlarmSubText,
            modifier = Modifier.padding(horizontal = 4.dp),
        )
        WheelColumn(
            previous = formatMinute(previousMinute(minute)),
            current = formatMinute(minute),
            next = formatMinute(nextMinute(minute)),
            onPreviousClick = { onMinuteChange(previousMinute(minute)) },
            onCurrentClick = { onMinuteChange(nextMinute(minute)) },
            onNextClick = { onMinuteChange(nextMinute(minute)) },
        )
        Spacer(modifier = Modifier.width(16.dp))
        WheelColumn(
            previous = if (isPm) "AM" else "PM",
            current = if (isPm) "PM" else "AM",
            next = if (isPm) "AM" else "PM",
            onPreviousClick = { onMeridiemChange(!isPm) },
            onCurrentClick = { onMeridiemChange(!isPm) },
            onNextClick = { onMeridiemChange(!isPm) },
            width = 74.dp,
        )
    }
}

@Composable
private fun WheelColumn(
    previous: String,
    current: String,
    next: String,
    onPreviousClick: () -> Unit,
    onCurrentClick: () -> Unit,
    onNextClick: () -> Unit,
    width: Dp = 64.dp,
) {
    Column(
        modifier = Modifier.width(width),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = previous,
            fontSize = 14.sp,
            color = AlarmGray,
            modifier = Modifier
                .clickable(onClick = onPreviousClick)
                .padding(vertical = 6.dp),
        )
        Text(
            text = current,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = AlarmText,
            modifier = Modifier
                .clickable(onClick = onCurrentClick)
                .padding(vertical = 4.dp),
        )
        Divider(color = AlarmSubText, thickness = 2.dp, modifier = Modifier.padding(horizontal = 6.dp))
        Text(
            text = next,
            fontSize = 14.sp,
            color = AlarmGray,
            modifier = Modifier
                .clickable(onClick = onNextClick)
                .padding(vertical = 6.dp),
        )
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 20.sp,
        fontWeight = FontWeight.Medium,
        color = AlarmText,
    )
    Spacer(modifier = Modifier.height(2.dp))
    HorizontalDivider(color = DividerGray)
}

private fun AlarmItem.displayHour(): String = ((hour - 1) % 12 + 1).toString()

private fun AlarmItem.displayMeridiem(): String = if (isPm) "PM" else "AM"

private fun formatHour(hour: Int): String = ((hour - 1) % 12 + 1).toString()

private fun previousHour(hour: Int): Int = if (hour <= 1) 12 else hour - 1

private fun nextHour(hour: Int): Int = if (hour >= 12) 1 else hour + 1

private fun formatMinute(minute: Int): String = minute.toString().padStart(2, '0')

private fun previousMinute(minute: Int): Int = if (minute <= 0) 59 else minute - 1

private fun nextMinute(minute: Int): Int = if (minute >= 59) 0 else minute + 1

private val AlarmTeal = Color(0xFF05B6C8)
private val AlarmPink = Color(0xFFFF4E92)
private val AlarmText = Color(0xFF3A3A3A)
private val AlarmSubText = Color(0xFF8A8A8A)
private val AlarmGray = Color(0xFFB3B3B3)
private val DividerGray = Color(0xFFD9D9D9)
