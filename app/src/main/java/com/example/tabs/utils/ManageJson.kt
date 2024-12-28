package com.example.tabs.utils

import android.content.Context
import com.example.tabs.utils.models.Assigned
import com.example.tabs.utils.models.Contact
import com.example.tabs.utils.models.Occasion
import com.example.tabs.utils.models.PresentHistory
import org.json.JSONArray
import org.json.JSONObject
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.text.ParseException

class ManageJson(private val context: Context) {

    private val _context = context

    // assets 폴더에서 JSON 파일 읽기
    fun getJsonDataFromAsset(fileName: String): String? {
        val jsonString: String
        try {
            val inputStream: InputStream = _context.assets.open(fileName)
            val size: Int = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charsets.UTF_8)
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }

    fun writeFileToInternalStorage(filename: String, content: String) {
        try {
            val fileOutputStream: FileOutputStream = _context.openFileOutput(filename, Context.MODE_PRIVATE)
            fileOutputStream.write(content.toByteArray())
            fileOutputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun readFileFromInternalStorage(filename: String): String {
        try {
            val fileInputStream = _context.openFileInput(filename)
            val content = fileInputStream.bufferedReader().use { it.readText() }
            fileInputStream.close()
            return content
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // JSON 문자열을 contact 모델 리스트로 파싱
    fun parseJsonToDataList(jsonString: String?): List<Contact> {
        val dataList = mutableListOf<Contact>()
        if (jsonString == null) {
            return dataList
        }
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val phoneNumber = jsonObject.getString("phoneNumber")
                val bDayString = jsonObject.getString("bDay")
                val bDay = parseDateString(bDayString)
                val recentContact = jsonObject.getInt("recentContact")
                val presentHistoryArray = jsonObject.getJSONArray("presentHistory")
                val presentHistoryList = mutableListOf<PresentHistory>()
                for (j in 0 until presentHistoryArray.length()) {
                    val presentHistoryObject = presentHistoryArray.getJSONObject(j)
                    val dateString = presentHistoryObject.getString("date")
                    val gift = presentHistoryObject.getString("gift")
                    val date = parseDateString(dateString)
                    val presentHistory = PresentHistory(date, gift)
                    presentHistoryList.add(presentHistory)
                }

                val data = Contact(name, phoneNumber, bDay, recentContact, presentHistoryList)
                dataList.add(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return dataList
    }

    private fun parseDateString(dateString: String): Date {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        formatter.isLenient = false // 엄격한 모드 설정
        return try {
            val res = formatter.parse(dateString)
            res ?: throw ParseException("Invalid date format", 0)
        } catch (e: ParseException) {
            println("Error parsing date string: $dateString")
            e.printStackTrace()
            Date()
        }
    }

    fun parseJsonToAssignedList(jsonString: String?): List<Assigned> {
        val assignedList = mutableListOf<Assigned>()
        if (jsonString == null) {
            return assignedList
        }
        try {
            val jsonArray = JSONArray(jsonString)
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val name = jsonObject.getString("name")
                val occasionsArray = jsonObject.getJSONArray("occasions")
                val occasionsList = mutableListOf<Occasion>()
                for (j in 0 until occasionsArray.length()) {
                    val presentHistoryObject = occasionsArray.getJSONObject(j)
                    val occasion = presentHistoryObject.getString("occasion")
                    val dateString = presentHistoryObject.getString("date")
                    val date = parseDateString(dateString)
                    val occasions = Occasion(occasion, date)
                    occasionsList.add(occasions)
                }

                val data = Assigned(name, occasionsList)
                assignedList.add(data)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return assignedList
    }
}
