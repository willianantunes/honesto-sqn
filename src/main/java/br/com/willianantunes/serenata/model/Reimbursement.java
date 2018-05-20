package br.com.willianantunes.serenata.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reimbursement {

    @JsonProperty("all_net_values")
    public List<Double> allNetValues = null;
    @JsonProperty("all_reimbursement_numbers")
    public List<Integer> allReimbursementNumbers = null;
    @JsonProperty("all_reimbursement_values")
    public List<Integer> allReimbursementValues = null;
    @JsonProperty("document_value")
    public Double documentValue;
    @JsonProperty("probability")
    public Object probability;
    @JsonProperty("receipt")
    public Receipt receipt;
    @JsonProperty("rosies_tweet")
    public Object rosiesTweet;
    @JsonProperty("remark_value")
    public Integer remarkValue;
    @JsonProperty("total_net_value")
    public Double totalNetValue;
    @JsonProperty("total_reimbursement_value")
    public Integer totalReimbursementValue;
    @JsonProperty("document_id")
    public Integer documentId;
    @JsonProperty("last_update")
    public ZonedDateTime lastUpdate;
    @JsonProperty("year")
    public Integer year;
    @JsonProperty("applicant_id")
    public Integer applicantId;
    @JsonProperty("congressperson_id")
    public Integer congresspersonId;
    @JsonProperty("congressperson_name")
    public String congresspersonName;
    @JsonProperty("congressperson_document")
    public Integer congresspersonDocument;
    @JsonProperty("party")
    public String party;
    @JsonProperty("state")
    public String state;
    @JsonProperty("term_id")
    public Integer termId;
    @JsonProperty("term")
    public Integer term;
    @JsonProperty("subquota_id")
    public Integer subquotaId;
    @JsonProperty("subquota_description")
    public String subquotaDescription;
    @JsonProperty("subquota_group_id")
    public Integer subquotaGroupId;
    @JsonProperty("subquota_group_description")
    public String subquotaGroupDescription;
    @JsonProperty("supplier")
    public String supplier;
    @JsonProperty("cnpj_cpf")
    public String cnpjCpf;
    @JsonProperty("document_type")
    public Integer documentType;
    @JsonProperty("document_number")
    public String documentNumber;
    @JsonProperty("issue_date")
    public LocalDate issueDate;
    @JsonProperty("month")
    public Integer month;
    @JsonProperty("installment")
    public Integer installment;
    @JsonProperty("batch_number")
    public Integer batchNumber;
    @JsonProperty("passenger")
    public String passenger;
    @JsonProperty("leg_of_the_trip")
    public String legOfTheTrip;
    @JsonProperty("suspicions")
    public Object suspicions;
    @JsonProperty("receipt_text")
    public Object receiptText;
    @JsonProperty("search_vector")
    public String searchVector;
}