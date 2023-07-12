package com.polar.androidblesdk

import android.util.Log
import java.io.BufferedInputStream
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL


class Backend {
    fun post_request(url: String, postData: String) {
        val urlObject = URL(url)
        val connection = urlObject.openConnection() as HttpURLConnection

        Log.d("POST", "Trying to send post request")

        // Set the necessary headers
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.setRequestProperty("charset", "utf-8")
        connection.setRequestProperty("Content-Length", postData.length.toString())

        // Enable output and input streams
        connection.doOutput = true
        connection.doInput = true

        try {
            // Write the data to the request body
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(postData)
            outputStream.flush()
            outputStream.close()

            // Read the response
            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()

                // Process the response
                val responseBody = response.toString()
                // Do something with the response
                Log.d("POST", responseBody)
            } else {
                // Handle error cases
                Log.d("POST", "Error in post! ${responseCode}")

            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
    }

//    fun get_request_with_filter(url: String, filter: String) {
//        val urlObject = URL(url)
//        val connection = urlObject.openConnection() as HttpURLConnection
//
//        Log.d("GET", "Trying to send get request")
//
//        // Set the necessary headers
//        connection.requestMethod = "GET"
//        connection.setRequestProperty("Content-Type", "application/json")
//        connection.setRequestProperty("charset", "utf-8")
//        connection.setRequestProperty("Content-Length", filter.length.toString())
//
//        // Enable output and input streams
//        connection.doOutput = true
//        connection.doInput = true
//
//        try {
//            // Write the data to the request body
//            val outputStream = DataOutputStream(connection.outputStream)
//            outputStream.writeBytes(filter)
//            outputStream.flush()
//            outputStream.close()
//
//            // Read the response
//            val responseCode = connection.responseCode
//            if (responseCode == HttpURLConnection.HTTP_OK) {
//                val reader = BufferedReader(InputStreamReader(connection.inputStream))
//                val response = StringBuilder()
//                var line: String?
//                while (reader.readLine().also { line = it } != null) {
//                    response.append(line)
//                }
//                reader.close()
//
//                // Process the response
//                val responseBody = response.toString()
//                // Do something with the response
//                Log.d("GET", responseBody)
//            } else {
//                // Handle error cases
//                Log.d("GET", "Error in get! ${responseCode}")
//
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//        } finally {
//            connection.disconnect()
//        }
//    }
//
//    fun sendGetRequest(url: String): String {
//        val obj = URL(url)
//        val connection = obj.openConnection() as HttpURLConnection
//
//        Log.d("GET", "Trying to send get request")
//
//        // Imposta il metodo HTTP sulla GET
//        connection.requestMethod = "GET"
//
//        // Leggi la risposta
//        val responseCode = connection.responseCode
//        println("Response Code: $responseCode")
//
//        val input = BufferedReader(InputStreamReader(connection.inputStream))
//        val response = StringBuffer()
//
//        var inputLine = input.readLine()
//        while (inputLine != null) {
//            response.append(inputLine)
//            inputLine = input.readLine()
//        }
//        input.close()
//
//        Log.d("GET", "Good get request")
//        //return Response(200, response.toString())
//        return response.toString()
//    }


    fun getRequest(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection

        Log.d("GET", "Trying to send get request")

        // Setting the request method (GET)
        connection.requestMethod = "GET"

        try {
            // Reading the response
            val inputStream = BufferedInputStream(connection.inputStream)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String?

            while (bufferedReader.readLine().also { line = it } != null) {
                stringBuilder.append(line)
            }

            bufferedReader.close()
            inputStream.close()

            // Returning the response as a string
            return stringBuilder.toString()
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            connection.disconnect()
        }
        return ""
    }

    fun deleteWithFilter(url: String) {
        val url = URL(url)
        val connection = url.openConnection() as HttpURLConnection

        Log.d("DELETE", "Trying to send delete request")

        connection.requestMethod = "DELETE"

        val responseCode = connection.responseCode
        if (responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val response = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                response.append(line)
            }
            reader.close()

            // Process the response
            val responseBody = response.toString()
            // Do something with the response
            Log.d("DELETE", responseBody)
        } else {
            // Handle error cases
            Log.d("DELETE", "Error in delete! ${responseCode}")

        }
    }
}

