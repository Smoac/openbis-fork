package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("ExternalDataManagementSystemType")
public enum ExternalDataManagementSystemType
{
    OPENBIS, GIT, UNDEFINED;

    public static ExternalDataManagementSystemType fromString(String str)
    {
        if ("OPENBIS".equals(str))
        {
            return ExternalDataManagementSystemType.OPENBIS;
        } else if ("GIT".equals(str))
        {
            return ExternalDataManagementSystemType.GIT;
        } else
        {
            return ExternalDataManagementSystemType.UNDEFINED;
        }
    }
}
