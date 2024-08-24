package org.humanresources.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Employee {

    private String id;

    @JsonProperty("employee_id__c")
    private String employeeId;

    @JsonProperty("office__c")
    private String office;

    @JsonProperty("first_name__c")
    private String firstName;

    @JsonProperty("last_name__c")
    private String lastName;

    @JsonProperty("role__c")
    private List<String> roles;

    @JsonProperty("on_board_date__c")
    private String onboardingDate;
}