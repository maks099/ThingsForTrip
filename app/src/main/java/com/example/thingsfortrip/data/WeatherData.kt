package com.example.thingsfortrip.data

import com.example.thingsfortrip.R


enum class WeatherData(val code: Int, val drawableResource: Int, val stringResource: Int) {
    clear(1000, R.drawable.clear, R.string.clear), mostlyClear(
        1100,
        R.drawable.mostly_clear,
        R.string.mostlyClear
    ),
    partlyCloudy(1101, R.drawable.party_cloudly, R.string.partlyCloudy), mostlyCloudy(
        1102,
        R.drawable.mostly_cloudy,
        R.string.mostlyCloudy
    ),
    mostlyCloudy2(4205, R.drawable.mostly_cloudy, R.string.mostlyCloudy), cloudy(
        1001,
        R.drawable.cloudy,
        R.string.cloudy
    ),
    fog(2000, R.drawable.fog, R.string.fog), lightFog(
        2100,
        R.drawable.fog_light,
        R.string.light_fog
    ),
    lightWind(3000, R.drawable.clear, R.string.lightWind), drizzle(
        4000,
        R.drawable.drizzle,
        R.string.drizzle
    ),
    rain(4001, R.drawable.rain, R.string.rain), lightRain(
        4200,
        R.drawable.rain_light,
        R.string.lightRain
    ),
    heavyRain(4201, R.drawable.rain_heavy, R.string.heavyRain), snow(
        5000,
        R.drawable.sno,
        R.string.snow
    ),
    flurries(5001, R.drawable.flurries, R.string.flurries), lightSnow(
        5100,
        R.drawable.light_snow,
        R.string.lightSnow
    ),
    heavySnow(5101, R.drawable.snow_heavy, R.string.heavySnow), freezingDrizzle(
        6000,
        R.drawable.freezing_drizzle,
        R.string.freezingDrizzle
    ),
    freezingRain(6001, R.drawable.freezing_rain, R.string.freezingRain), lightFreezingRain(
        6201,
        R.drawable.freezing_rain_heavy,
        R.string.lightFreezingRain
    ),
    icePellets(7000, R.drawable.ice_pellets, R.string.icePellets), heavyIcePellets(
        7101,
        R.drawable.ice_pellets_heavy,
        R.string.heavyIcePellets
    ),
    lightIcePellets(
        7102,
        R.drawable.ice_pellets_light,
        R.string.lightIcePellets
    ),
    thunderstorm(8000, R.drawable.thunderstorm, R.string.thunderstorm),
    empty(0, 0, 0);

    companion object {
        fun findByCode(n: Int): WeatherData? {
            for (c in values()) {
                if (c.code == n) {
                    return c
                }
            }
            return null
        }
    }
}