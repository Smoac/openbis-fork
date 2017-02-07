package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("ExternalDataManagementSystemType")
public enum ExternalDataManagementSystemType
{
    OPENBIS, URL, FILE_SYSTEM;

    public static ExternalDataManagementSystemType fromString(String str)
    {
        if ("OPENBIS".equals(str))
        {
            return ExternalDataManagementSystemType.OPENBIS;
        } else if ("URL".equals(str))
        {
            return ExternalDataManagementSystemType.URL;
        } else if ("FILE_SYSTEM".equals(str))
        {
            return ExternalDataManagementSystemType.FILE_SYSTEM;
        } else
        {
            throw new IllegalArgumentException("Unknown type " + str);
        }
    }
}
