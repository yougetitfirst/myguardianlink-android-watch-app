package com.example.myguardianwatch.presentation

object WatchUrgentAssistBridge {
    const val PHONE_PACKAGE = "com.myapp"
    const val CAPABILITY_URGENT_ASSIST = "urgent_assist"
    const val MESSAGE_PATH = "/urgent_assist"
    const val MESSAGE_PAYLOAD = "trigger"
    const val ACTION_WATCH_URGENT_ASSIST = "com.myapp.action.WATCH_URGENT_ASSIST"
    const val URGENT_ASSIST_DEEP_LINK = "myguardianlink://watch/urgent_assist"
    const val ACTIVITY_CLASS = "com.myapp.watch.WatchUrgentAssistActivity"
}
