package com.paola.paolarestapi.weather.service;

import com.paola.paolarestapi.weather.model.WeatherTemperature;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/*
  Why it is used:
  - This is live weather data from DHMZ without adding heavy infrastructure.
  - This service isolates external XML fetch/parsing logic from REST resource code.

  How it is used:
  - WeatherResource calls findByCity(cityTerm) when /api/weather/temperature is requested.
  - Client can send full or partial city name (for example "zag") and receive all matches.

  How it works:
  - Downloads DHMZ XML feed from https://vrijeme.hr/hrvatska_n.xml via HttpURLConnection.
  - Parses <Grad> nodes and extracts <GradIme> (city) + <Temp> (temperature).
  - Applies case-insensitive partial matching and returns filtered rows as WeatherTemperature list.
*/
public class DhmzWeatherService {
    private static final String DHMZ_URL = "https://vrijeme.hr/hrvatska_n.xml";

    public List<WeatherTemperature> findByCity(String cityTerm) {
        List<WeatherTemperature> all = fetchAll();
        String term = cityTerm == null ? "" : cityTerm.trim().toLowerCase(Locale.ROOT);

        List<WeatherTemperature> filtered = new ArrayList<WeatherTemperature>();
        for (WeatherTemperature item : all) {
            String city = item.getCity() == null ? "" : item.getCity().toLowerCase(Locale.ROOT);
            if (city.contains(term)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private List<WeatherTemperature> fetchAll() {
        HttpURLConnection connection = null;
        try {
            URL url = new URL(DHMZ_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000);
            connection.setReadTimeout(10000);
            connection.connect();

            int statusCode = connection.getResponseCode();
            if (statusCode < 200 || statusCode >= 300) {
                throw new IllegalStateException("DHMZ request failed with HTTP " + statusCode);
            }

            InputStream stream = connection.getInputStream();
            return parseXml(stream);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to fetch or parse DHMZ weather XML.", exception);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }

    private List<WeatherTemperature> parseXml(InputStream stream) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        Document document = factory.newDocumentBuilder().parse(stream);
        NodeList cityNodes = document.getElementsByTagName("Grad");
        List<WeatherTemperature> result = new ArrayList<WeatherTemperature>();
        for (int i = 0; i < cityNodes.getLength(); i++) {
            Element cityElement = (Element) cityNodes.item(i);
            String cityName = firstText(cityElement, "GradIme");
            String temperature = firstTextInParent(firstElement(cityElement, "Podatci"), "Temp");
            if (cityName != null && !cityName.trim().isEmpty()) {
                result.add(new WeatherTemperature(cityName.trim(), cleanValue(temperature)));
            }
        }
        return result;
    }

    private Element firstElement(Element parent, String tag) {
        if (parent == null) {
            return null;
        }
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        return (Element) nodes.item(0);
    }

    private String firstText(Element parent, String tag) {
        if (parent == null) {
            return null;
        }
        NodeList nodes = parent.getElementsByTagName(tag);
        if (nodes.getLength() == 0) {
            return null;
        }
        return nodes.item(0).getTextContent();
    }

    private String firstTextInParent(Element parent, String tag) {
        return firstText(parent, tag);
    }

    private String cleanValue(String value) {
        if (value == null) {
            return "";
        }
        return value.trim();
    }
}
