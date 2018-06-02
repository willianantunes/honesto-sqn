package br.com.willianantunes.serenata.model;

import br.com.willianantunes.util.deser.LocalDateFromISODateDeserializer;
import br.com.willianantunes.util.deser.ZonedDateTimeFromISOOffsetDateTimeDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Reimbursement {

    @JsonProperty("all_net_values")
    private List<Double> allNetValues = null;
    @JsonProperty("all_reimbursement_numbers")
    private List<Integer> allReimbursementNumbers = null;
    @JsonProperty("all_reimbursement_values")
    private List<Integer> allReimbursementValues = null;
    @JsonProperty("document_value")
    private Double documentValue;
    @JsonProperty("probability")
    private Object probability;
    @JsonProperty("receipt")
    private Receipt receipt;
    @JsonProperty("rosies_tweet")
    private Object rosiesTweet;
    @JsonProperty("remark_value")
    private Integer remarkValue;
    @JsonProperty("total_net_value")
    private Double totalNetValue;
    @JsonProperty("total_reimbursement_value")
    private Integer totalReimbursementValue;
    @JsonProperty("document_id")
    private Integer documentId;
    @JsonProperty("last_update")
    private ZonedDateTime lastUpdate;
    @JsonProperty("year")
    private Integer year;
    @JsonProperty("applicant_id")
    private Integer applicantId;
    @JsonProperty("congressperson_id")
    private Integer congresspersonId;
    @JsonProperty("congressperson_name")
    private String congresspersonName;
    @JsonProperty("congressperson_document")
    private Integer congresspersonDocument;
    @JsonProperty("party")
    private String party;
    @JsonProperty("state")
    private String state;
    @JsonProperty("term_id")
    private Integer termId;
    @JsonProperty("term")
    private Integer term;
    @JsonProperty("subquota_id")
    private Integer subquotaId;
    @JsonProperty("subquota_description")
    private String subquotaDescription;
    @JsonProperty("subquota_group_id")
    private Integer subquotaGroupId;
    @JsonProperty("subquota_group_description")
    private String subquotaGroupDescription;
    @JsonProperty("supplier")
    private String supplier;
    @JsonProperty("cnpj_cpf")
    private String cnpjCpf;
    @JsonProperty("document_type")
    private Integer documentType;
    @JsonProperty("document_number")
    private String documentNumber;
    @JsonProperty("issue_date")
    private LocalDate issueDate;
    @JsonProperty("month")
    private Integer month;
    @JsonProperty("installment")
    private Integer installment;
    @JsonProperty("batch_number")
    private Integer batchNumber;
    @JsonProperty("passenger")
    private String passenger;
    @JsonProperty("leg_of_the_trip")
    private String legOfTheTrip;
    @JsonProperty("suspicions")
    private Suspicions suspicions;
    @JsonProperty("receipt_text")
    private Object receiptText;
    @JsonProperty("search_vector")
    private String searchVector;

    public ReimbursementDto toDto() {

        return ReimbursementDto.builder()
            .year(year)
            .subquotaDescription(subquotaDescription)
            .subquotaGroupDescription(subquotaGroupDescription)
            .supplier(supplier)
            .cnpjCpf(cnpjCpf)
            .issueDate(issueDate)
            .documentId(documentId)
            .documentValue(documentValue)
            .suspicions(evaluateWhichSuspicion())
            .receiptUrl(receipt != null && receipt.getUrl() != null ? receipt.getUrl() : "Indisponível")
            .build();
    }

    private String evaluateWhichSuspicion() {

        // TODO: Use messages.properties instead of hard-coded values
        if (suspicions != null) {
            if (suspicions.isIrregularCompaniesClassifier())
                return "CNPJ irregular";
            if (suspicions.isMealPriceOutlier())
                return "Preço de refeição muito incomum";
            if (suspicions.isMealPriceOutlierClassifier())
                return "Valor suspeito de refeição";
            if (suspicions.isOverMonthlySubquotaLimit())
                return "Extrapolou o limite da (sub)cota parlamentar";
            if (suspicions.isTraveledSpeedsClassifier())
                return "Viagens muito rápidas";
            if (suspicions.isElectionExpensesClassifier())
                return "Valor supeito gasto para eleições";
        }

        return "N/A";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ReimbursementDto {

        private Integer year;
        private String subquotaDescription;
        private String subquotaGroupDescription;
        private String supplier;
        private String cnpjCpf;
        private LocalDate issueDate;
        private Integer documentId;
        private Double documentValue;
        private String suspicions;
        private String receiptUrl;

        public Object[] toParameters() {

            return Arrays.asList(year, subquotaDescription, subquotaGroupDescription, supplier, cnpjCpf, issueDate, documentId, documentValue, suspicions, receiptUrl).toArray();
        }
    }
}