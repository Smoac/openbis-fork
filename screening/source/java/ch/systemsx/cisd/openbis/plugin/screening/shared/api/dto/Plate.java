package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

import java.io.Serializable;

/**
 * Describes a plate and its context in a hierarchy
 * 
 * @author Tomasz Pylak
 */
public class Plate extends PlateIdentifier implements Serializable
{
    private static final long serialVersionUID = 1L;

    private String experimentCode, projectCode;

    public Plate(String plateCode, String experimentCode, String projectCode, String spaceCodeOrNull)
    {
        super(plateCode, spaceCodeOrNull);
        this.experimentCode = experimentCode;
        this.projectCode = projectCode;
    }

    /** a code of the experiment to which the plate belongs */
    public String getExperimentCode()
    {
        return experimentCode;
    }

    /** a code of the project to which the plate belongs */
    public String getProjectCode()
    {
        return projectCode;
    }
}