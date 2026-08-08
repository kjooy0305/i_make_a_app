package com.example.repeatalarm.ui

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.repeatalarm.databinding.ActivityMainBinding
import com.example.repeatalarm.viewmodel.AlarmViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: AlarmViewModel by viewModels()

    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* handled silently */ }

    private val addEditLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { /* results via LiveData */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermissionIfNeeded()
        setupRecyclerView()
        binding.fabAddAlarm.setOnClickListener {
            addEditLauncher.launch(Intent(this, AddEditGroupActivity::class.java))
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun setupRecyclerView() {
        val adapter = AlarmGroupAdapter(
            onEdit = { gw ->
                val intent = Intent(this, AddEditGroupActivity::class.java).apply {
                    putExtra(AddEditGroupActivity.EXTRA_GROUP_ID, gw.group.id)
                }
                addEditLauncher.launch(intent)
            },
            onDelete = { gw ->
                AlertDialog.Builder(this)
                    .setTitle("알림 삭제")
                    .setMessage("'${gw.group.name}' 알림을 삭제할까요?")
                    .setPositiveButton("삭제") { _, _ -> viewModel.deleteGroup(gw.group) }
                    .setNegativeButton("취소", null)
                    .show()
            },
            onToggle = { gw -> viewModel.toggleGroup(gw.group) }
        )

        binding.recyclerView.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        viewModel.allGroupsWithItems.observe(this) { groups ->
            adapter.submitList(groups)
            binding.tvEmpty.visibility = if (groups.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
