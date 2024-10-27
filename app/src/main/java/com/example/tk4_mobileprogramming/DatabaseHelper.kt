package com.example.tk4_mobileprogramming

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        private const val DATABASE_NAME = "covid_survey.db"
        private const val DATABASE_VERSION = 3  // Increased version for new column
        private const val TABLE_NAME = "surveys"
        private const val COLUMN_ID = "id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_AGE = "age"
        private const val COLUMN_ADDRESS = "address"
        private const val COLUMN_SYMPTOMS = "symptoms"
        private const val COLUMN_LATITUDE = "latitude"
        private const val COLUMN_LONGITUDE = "longitude"
        private const val COLUMN_SURVEYOR_EMAIL = "surveyor_email"  // New column
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableQuery = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_NAME TEXT,
                $COLUMN_AGE INTEGER,
                $COLUMN_ADDRESS TEXT,
                $COLUMN_SYMPTOMS TEXT,
                $COLUMN_LATITUDE REAL,
                $COLUMN_LONGITUDE REAL,
                $COLUMN_SURVEYOR_EMAIL TEXT
            )
        """.trimIndent()
        db.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            // Add location columns
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_LATITUDE REAL")
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_LONGITUDE REAL")
        }
        if (oldVersion < 3) {
            // Add surveyor_email column
            db.execSQL("ALTER TABLE $TABLE_NAME ADD COLUMN $COLUMN_SURVEYOR_EMAIL TEXT")
        }
    }

    fun addSurvey(survey: Survey): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, survey.name)
            put(COLUMN_AGE, survey.age)
            put(COLUMN_ADDRESS, survey.address)
            put(COLUMN_SYMPTOMS, survey.symptoms)
            put(COLUMN_LATITUDE, survey.latitude)
            put(COLUMN_LONGITUDE, survey.longitude)
            put(COLUMN_SURVEYOR_EMAIL, survey.surveyorEmail)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllSurveysByEmail(email: String): List<Survey> {
        val surveys = mutableListOf<Survey>()
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_SURVEYOR_EMAIL = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(email))

        cursor.use {
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID))
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME))
                val age = it.getInt(it.getColumnIndexOrThrow(COLUMN_AGE))
                val address = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDRESS))
                val symptoms = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOMS))
                val surveyorEmail = it.getString(it.getColumnIndexOrThrow(COLUMN_SURVEYOR_EMAIL))

                val latitude = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_LATITUDE))) null
                else it.getDouble(it.getColumnIndexOrThrow(COLUMN_LATITUDE))
                val longitude = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_LONGITUDE))) null
                else it.getDouble(it.getColumnIndexOrThrow(COLUMN_LONGITUDE))

                surveys.add(Survey(id, name, age, address, symptoms, latitude, longitude, surveyorEmail))
            }
        }
        return surveys
    }

    fun getSurvey(id: Long): Survey {
        val db = this.readableDatabase
        val selectQuery = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_ID = ?"
        val cursor = db.rawQuery(selectQuery, arrayOf(id.toString()))

        cursor.use {
            if (it.moveToFirst()) {
                val name = it.getString(it.getColumnIndexOrThrow(COLUMN_NAME))
                val age = it.getInt(it.getColumnIndexOrThrow(COLUMN_AGE))
                val address = it.getString(it.getColumnIndexOrThrow(COLUMN_ADDRESS))
                val symptoms = it.getString(it.getColumnIndexOrThrow(COLUMN_SYMPTOMS))
                val surveyorEmail = it.getString(it.getColumnIndexOrThrow(COLUMN_SURVEYOR_EMAIL))

                val latitude = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_LATITUDE))) null
                else it.getDouble(it.getColumnIndexOrThrow(COLUMN_LATITUDE))
                val longitude = if (it.isNull(it.getColumnIndexOrThrow(COLUMN_LONGITUDE))) null
                else it.getDouble(it.getColumnIndexOrThrow(COLUMN_LONGITUDE))

                return Survey(id, name, age, address, symptoms, latitude, longitude, surveyorEmail)
            }
        }
        throw IllegalArgumentException("Survey with id $id not found")
    }

    fun updateSurvey(survey: Survey): Int {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_NAME, survey.name)
            put(COLUMN_AGE, survey.age)
            put(COLUMN_ADDRESS, survey.address)
            put(COLUMN_SYMPTOMS, survey.symptoms)
            put(COLUMN_LATITUDE, survey.latitude)
            put(COLUMN_LONGITUDE, survey.longitude)
            put(COLUMN_SURVEYOR_EMAIL, survey.surveyorEmail)
        }
        return db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(survey.id.toString()))
    }

    fun deleteSurveyByIdAndEmail(id: Long, email: String): Int {
        val db = this.writableDatabase
        return db.delete(TABLE_NAME,
            "$COLUMN_ID = ? AND $COLUMN_SURVEYOR_EMAIL = ?",
            arrayOf(id.toString(), email)
        )
    }
}