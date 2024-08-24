package org.humanresources.constants;

public class URIConstants {

    public static final String VAULT_API_URL = "https://mssandbox-clinical.veevavault.com/api/v24.2/";

    public static final String AUTHORIZATION = "auth";

    public static final String EMPLOYEE_OBJECTS = "vobjects/employee__c";
    public static final String EMPLOYEE_OBJECTS_WITH_FIELDS = EMPLOYEE_OBJECTS +
            "?fields=id,employee_id__c,office__c,first_name__c,last_name__c,role__c,on_board_date__c";

    public static final String OFFICE_OBJECTS = "vobjects/office__c";
    public static final String OFFICE_OBJECTS_WITH_FIELDS = OFFICE_OBJECTS + "?fields=id,name__v";

    public static final String ROLE_PICKLISTS = "objects/picklists/role__c";

}
