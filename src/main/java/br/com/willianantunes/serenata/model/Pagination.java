package br.com.willianantunes.serenata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Pagination {

    @JsonProperty("count")
    public Integer count;
    @JsonProperty("next")
    public String next;
    @JsonProperty("previous")
    public Object previous;
    @JsonProperty("results")
    public List<Reimbursement> results;
}