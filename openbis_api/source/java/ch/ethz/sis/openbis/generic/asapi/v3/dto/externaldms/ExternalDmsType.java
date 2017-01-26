package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.externaldms.ExternalDmsType")
public enum ExternalDmsType
{
    OPENBIS, GIT, UNDEFINED;

    @JsonIgnore
    public static ExternalDmsType fromString(String str)
    {
        switch (str)
        {
            case "OPENBIS":
                return ExternalDmsType.OPENBIS;
            case "GIT":
                return ExternalDmsType.GIT;
            default:
                return ExternalDmsType.UNDEFINED;
        }
    }
}
