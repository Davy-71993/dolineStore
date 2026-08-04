package com.example.doline.data


import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import com.google.gson.Gson
import com.google.gson.JsonObject

class PricingDetailsAdapter : TypeAdapter<PricingDetails>() {

    private val gson = Gson()

    override fun write(out: JsonWriter, value: PricingDetails?) {
        if (value == null) {
            out.nullValue()
            return
        }

        out.beginObject()

        when (value) {
            is PricingDetails.Fixed -> {
                out.name("type").value("Fixed")
            }

            is PricingDetails.UnitPrice -> {
                out.name("type").value("UnitPrice")
                out.name("data").jsonValue(gson.toJson(value))
            }

            is PricingDetails.PriceRange -> {
                out.name("type").value("PriceRange")
                out.name("data").jsonValue(gson.toJson(value))
            }

            is PricingDetails.RecurringPrice -> {
                out.name("type").value("RecurringPrice")
                out.name("data").jsonValue(gson.toJson(value))
            }

            is PricingDetails.PriceMenu -> {
                out.name("type").value("PriceMenu")
                out.name("data").jsonValue(gson.toJson(value))
            }
        }

        out.endObject()
    }

    override fun read(`in`: JsonReader): PricingDetails? {
        val jsonObject: JsonObject = try {
            gson.fromJson(`in`, JsonObject::class.java)
        } catch (e: Exception) {
            return null
        } ?: return null

        return when (jsonObject.get("scheme")?.asString) {
            "Fixed" -> gson.fromJson(jsonObject, PricingDetails.Fixed::class.java)
            "Unit" -> gson.fromJson(jsonObject, PricingDetails.UnitPrice::class.java)
            "Range" -> gson.fromJson(jsonObject, PricingDetails.PriceRange::class.java)
            "Recurring" -> gson.fromJson(jsonObject, PricingDetails.RecurringPrice::class.java)
            "Menu" -> gson.fromJson(jsonObject, PricingDetails.PriceMenu::class.java)
            else -> null
        }
    }
}