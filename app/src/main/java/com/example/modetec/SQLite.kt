package com.example.modetec

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ActivityLogDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "ActivityLog.db"

        private const val SQL_CREATE_ENTRIES =
            "CREATE TABLE activity_log (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "day TEXT," +
                    "time TEXT," +
                    "duration INTEGER," +
                    "activity TEXT)"

        private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS activity_log"
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
}


// Database operation
fun insertActivityLog(dbHelper: ActivityLogDbHelper, day: String, time: String, duration: Int, activity: String) {
    // Gets the data repository in write mode
    val db = dbHelper.writableDatabase

    // Create a new map of values, where column names are the keys
    val values = ContentValues().apply {
        put("day", day)
        put("time", time)
        put("duration", duration)
        put("activity", activity)
    }

    // Insert the new row, returning the primary key value of the new row
    val newRowId = db?.insert("activity_log", null, values)
}





