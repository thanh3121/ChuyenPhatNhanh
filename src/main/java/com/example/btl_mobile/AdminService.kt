package com.example.btl_mobile

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class AdminService : Service() {
    private lateinit var adminServer: AdminServer

    override fun onCreate() {
        super.onCreate()
        val dbHelper = DbHelper(this)
        val adminApi = AdminApi(dbHelper)
        adminServer = AdminServer(this, adminApi)
        adminServer.start(8080)
        Log.d("AdminService", "Admin Server started in service")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        adminServer.stop()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
