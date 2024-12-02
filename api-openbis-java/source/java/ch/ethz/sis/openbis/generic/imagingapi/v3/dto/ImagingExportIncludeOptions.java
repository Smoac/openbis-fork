package ch.ethz.sis.openbis.generic.imagingapi.v3.dto;

import ch.systemsx.cisd.base.annotation.JsonObject;

@JsonObject("imaging.dto.ImagingExportIncludeOptions")
public enum ImagingExportIncludeOptions
{
    IMAGE, RAW_DATA;
}

