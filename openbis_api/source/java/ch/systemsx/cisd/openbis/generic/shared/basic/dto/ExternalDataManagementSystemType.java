package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("ExternalDataManagementSystemType")
public enum ExternalDataManagementSystemType
{
    OPENBIS, GIT, UNDEFINED;

    public static ExternalDataManagementSystemType fromString(String str)
    {
        switch (str)
        {
            case "OPENBIS":
                return ExternalDataManagementSystemType.OPENBIS;
            case "GIT":
                return ExternalDataManagementSystemType.GIT;
            default:
                return ExternalDataManagementSystemType.UNDEFINED;
        }
    }
}
