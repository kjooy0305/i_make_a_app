package com.example.repeatalarm.ui

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.repeatalarm.data.AlarmGroup
import com.example.repeatalarm.data.AlarmItem
import com.example.repeatalarm.databinding.ActivityAddEditGroupBinding
import com.example.repeatalarm.databinding.ItemAlarmEntryEditBinding
import com.example.repeatalarm.viewmodel.AlarmViewModel
import kotlinx.coroutines.launch

class AddEditGroupActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_GROUP_ID = "extra_group_id"
    }

    private lateinit var binding: ActivityAddEditGroupBinding
    private val viewModel: AlarmViewModel by viewModels()
    private var editGroupId: Int = -1

    data class ItemHolder(
        val binding: ItemAlarmEntryEditBinding,
        var startHour: Int = 9,
        var startMinute: Int = 0,
        var endHour: Int = 18,
        var endMinute: Int = 0
    )

    private val itemHolders = mutableListOf<ItemHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddEditGroupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        editGroupId = intent.getIntExtra(EXTRA_GROUP_ID, -1)
        title = if (editGroupId == -1) "새 알림 만들기" else "알림 수정"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        binding.btnAddItem.setOnClickListener { addItemView() }
        binding.btnSave.setOnClickListener { save() }
        binding.btnCancel.setOnClickListener { finish() }

        if (editGroupId != -1) loadGroupForEdit() else addItemView()
    }

    override fun onSupportNavigateUp(): Boolean { finish(); return true }

    private fun addItemView(
        content: String = "",
        startH: Int = 9, startM: Int = 0,
        endH: Int = 18, endM: Int = 0,
        interval: Int = 30
    ) {
        val itemBinding = ItemAlarmEntryEditBinding.inflate(
            LayoutInflater.from(this), binding.itemContainer, false
        )
        val holder = ItemHolder(itemBinding, startH, startM, endH, endM)
        itemHolders.add(holder)

        itemBinding.etContent.setText(content)
        itemBinding.etInterval.setText(interval.toString())
        updateTimeLabels(holder)

        itemBinding.btnStartTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                holder.startHour = h; holder.startMinute = m; updateTimeLabels(holder)
            }, holder.startHour, holder.startMinute, true).show()
        }
        itemBinding.btnEndTime.setOnClickListener {
            TimePickerDialog(this, { _, h, m ->
                holder.endHour = h; holder.endMinute = m; updateTimeLabels(holder)
            }, holder.endHour, holder.endMinute, true).show()
        }
        itemBinding.btnDeleteItem.setOnClickListener {
            binding.itemContainer.removeView(itemBinding.root)
            itemHolders.remove(holder)
        }

        binding.itemContainer.addView(itemBinding.root)
    }

    private fun updateTimeLabels(holder: ItemHolder) {
        holder.binding.btnStartTime.text = String.format("시작  %02d:%02d", holder.startHour, holder.startMinute)
        holder.binding.btnEndTime.text = String.format("종료  %02d:%02d", holder.endHour, holder.endMinute)
    }

    private fun save() {
        val groupName = binding.etGroupName.text.toString().trim()
        if (groupName.isEmpty()) { binding.etGroupName.error = "그룹 이름을 입력하세요"; return }
        if (itemHolders.isEmpty()) { Toast.makeText(this, "알림 항목을 1개 이상 추가하세요", Toast.LENGTH_SHORT).show(); return }

        val draftItems = mutableListOf<AlarmItem>()
        for (holder in itemHolders) {
            val content = holder.binding.etContent.text.toString().trim()
            if (content.isEmpty()) { holder.binding.etContent.error = "내용을 입력하세요"; return }
            val interval = holder.binding.etInterval.text.toString().toIntOrNull()
            if (interval == null || interval < 1) { holder.binding.etInterval.error = "올바른 간격(분)을 입력하세요"; return }
            if (holder.startHour * 60 + holder.startMinute >= holder.endHour * 60 + holder.endMinute) {
                Toast.makeText(this, "종료 시간은 시작 시간 이후여야 합니다", Toast.LENGTH_SHORT).show(); return
            }
            draftItems.add(AlarmItem(
                groupId = editGroupId.coerceAtLeast(0),
                content = content,
                startHour = holder.startHour, startMinute = holder.startMinute,
                endHour = holder.endHour, endMinute = holder.endMinute,
                intervalMinutes = interval
            ))
        }

        viewModel.saveGroup(
            AlarmGroup(id = if (editGroupId == -1) 0 else editGroupId, name = groupName),
            draftItems
        )
        Toast.makeText(this, if (editGroupId == -1) "알림이 등록되었습니다" else "알림이 수정되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }

    private fun loadGroupForEdit() {
        lifecycleScope.launch {
            val group = viewModel.getGroupById(editGroupId) ?: return@launch
            val items = viewModel.getItemsForGroup(editGroupId)
            binding.etGroupName.setText(group.name)
            items.forEach { addItemView(it.content, it.startHour, it.startMinute, it.endHour, it.endMinute, it.intervalMinutes) }
            if (items.isEmpty()) addItemView()
        }
    }
}
