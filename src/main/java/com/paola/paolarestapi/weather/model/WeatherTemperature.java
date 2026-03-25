package com.paola.paolarestapi.weather.model;

/*
  Represents one weather row returned to clients.
  Contains city name and current temperature from DHMZ feed.
*/
public class WeatherTemperature {
    private String city;
    private String temperature;

    public WeatherTemperature() {
    }

    public WeatherTemperature(String city, String temperature) {
        this.city = city;
        this.temperature = temperature;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }
}
