package br.com.willianantunes.converter;

import java.io.IOException;
import java.util.Map;

import org.apache.camel.Converter;
import org.apache.camel.TypeConverters;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @see <a href="https://stackoverflow.com/questions/43836518/how-to-pass-namedquery-parameters-in-apache-camel-jpa-by-header?noredirect=1&lq=1">Stack Overflow source</a>
 */
public class StringToMapTypeConverter implements TypeConverters {

    private static final ObjectMapper mapper = new ObjectMapper();
    private static JavaType mapType;

    static {
        
        mapType = mapper.getTypeFactory().constructMapType(Map.class, String.class, Object.class);
    }

    @Converter
    public Map<String, Object> toMap(String map) throws IOException {
        
        return mapper.readValue(map, mapType);
    }
}