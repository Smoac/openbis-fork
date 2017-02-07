package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ch.systemsx.cisd.base.annotation.JsonObject;
import ch.systemsx.cisd.common.annotation.TechPreview;

@JsonObject("as.dto.externaldms.ExternalDmsAddressType")
@TechPreview
public enum ExternalDmsAddressType
{
    OPENBIS, URL, FILE_SYSTEM;

    @JsonIgnore
    public static ExternalDmsAddressType fromString(String str)
    {
        switch (str)
        {
            case "OPENBIS":
                return ExternalDmsAddressType.OPENBIS;
            case "URL":
                return ExternalDmsAddressType.URL;
            case "FILE_SYSTEM":
                return ExternalDmsAddressType.FILE_SYSTEM;
            default:
                throw new IllegalArgumentException("Unknown address type " + str);
        }
    }
}