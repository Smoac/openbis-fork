package ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("as.dto.externaldms.ExternalDmsType")
public enum ExternalDmsType
{
    OPENBIS, GIT, UNDEFINED;
}
