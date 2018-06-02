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
public class Suspicions {

    @JsonProperty("meal_price_outlier")
    private boolean mealPriceOutlier;
    @JsonProperty("irregular_companies_classifier")
    private boolean irregularCompaniesClassifier;
    @JsonProperty("meal_price_outlier_classifier")
    private boolean mealPriceOutlierClassifier;
    @JsonProperty("over_monthly_subquota_limit")
    private boolean overMonthlySubquotaLimit;
    @JsonProperty("traveled_speeds_classifier")
    private boolean traveledSpeedsClassifier;
    @JsonProperty("election_expenses_classifier")
    private boolean electionExpensesClassifier;
}