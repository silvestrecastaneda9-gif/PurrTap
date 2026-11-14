package com.example.data.weather

import java.util.Calendar
import kotlin.random.Random

class WeatherRepository {
  fun rollRandomWeather(
    previous: WeatherConditionKind? = null,
    shiftCount: Long = 1L,
    random: Random = Random.Default,
  ): LiveWeatherState {
    val candidates =
      if (previous != null && WeatherConditionKind.entries.size > 1) {
        WeatherConditionKind.entries.filter { it != previous }
      } else {
        WeatherConditionKind.entries
      }
    val chosen = candidates[random.nextInt(candidates.size)]
    val temp = random.nextInt(chosen.tempRangeC.first, chosen.tempRangeC.last + 1).toDouble()
    return LiveWeatherState(
      cityName = "Sanctuary Sky",
      temperatureC = temp,
      condition = chosen,
      nextShiftSeconds = WEATHER_INTERVAL_SECONDS,
      weatherShiftCount = shiftCount,
    )
  }

  companion object {
    const val WEATHER_INTERVAL_SECONDS = 60
    const val WEATHER_INTERVAL_MILLIS = 60_000L

    fun currentLocalMinuteOfDay(nowMillis: Long = System.currentTimeMillis()): Int {
      val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
      return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE)
    }
  }
}
