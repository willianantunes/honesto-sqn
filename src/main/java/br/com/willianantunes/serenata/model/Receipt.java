package br.com.willianantunes.serenata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Receipt {

    @JsonProperty("fetched")
    private Boolean fetched;
    @JsonProperty("url")
    private String url;
}