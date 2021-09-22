package com.ntihs_fk.functions

import com.github.kevinsawicki.http.HttpRequest
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.ntihs_fk.functions.DiscordOAuth2.Companion.serializeToMap
import io.ktor.features.*

class DiscordOAuth2 {
    companion object {

        private const val discordAPIUrl = "https://discord.com/api/v8"
        private val gson = Gson()

        private data class Data(
            val client_id: String,
            val client_secret: String,
            val grant_type: String,
            val code: String? = null,
            val redirect_uri: String? = null,
            val refresh_token: String? = null
        )

        private inline fun <I, reified O> I.convert(): O {
            val json = gson.toJson(this)
            return gson.fromJson(json, object : TypeToken<O>() {}.type)
        }

        private fun <T> T.serializeToMap(): Map<String, Any> {
            return convert()
        }

        private fun requestForm(formData: Map<String, Any>) {
            if (
                HttpRequest.post("$discordAPIUrl/oauth2/token")
                    .form(formData)
                    .created()
            ) throw BadRequestException("Discord OAuth2 error")
        }

        fun exchange_code(code: String, redirect_uri: String) {
            val data = Data(
                client_id = discordConfig.discord_id,
                client_secret = discordConfig.discord_secret,
                grant_type = "authorization_code",
                code = code,
                redirect_uri = redirect_uri
            )

            requestForm(data.serializeToMap())
        }

        fun refresh_token(refresh_token: String) {
            val data = gson.toJson(
                Data(
                    client_id = discordConfig.discord_id,
                    client_secret = discordConfig.discord_secret,
                    grant_type = "refresh_token",
                    refresh_token = refresh_token
                )
            )

            requestForm(data.serializeToMap())
        }
    }
}