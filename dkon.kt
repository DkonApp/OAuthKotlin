import okhttp3.*
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.IOException

data class LoginResponse(
    @SerializedName("error") val error: Boolean,
    @SerializedName("error_code") val errorCode: Int,
    @SerializedName("accessToken") val accessToken: String?,
    @SerializedName("accountId") val accountId: String?
)

class OAuthClient(private val clientId: String) {
    private val client = OkHttpClient()
    private val gson = Gson()

    fun login(username: String, password: String, callback: (Boolean, String?) -> Unit) {
        val formBody = FormBody.Builder()
            .add("clientId", clientId)
            .add("username", username)
            .add("password", password)
            .build()

        val request = Request.Builder()
            .url("https://api.dkon.app/api/v3/method/account.signIn")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback(false, "An error occurred: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        callback(false, "Unexpected code $response")
                        return
                    }

                    val responseBody = response.body?.string()
                    val loginResponse = gson.fromJson(responseBody, LoginResponse::class.java)

                    if (loginResponse.error || loginResponse.errorCode != 0) {
                        callback(false, "Login failed. Please check your credentials.")
                    } else {
                        // Here you can save the token and account ID
                        // For example, in SharedPreferences or another storage
                        callback(true, "Login successful! Access Token: ${loginResponse.accessToken}")
                    }
                }
            }
        })
    }
}
