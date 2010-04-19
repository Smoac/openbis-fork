package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

/**
 * Describes a plate and its context in a hierarchy
 * 
 * @author Tomasz Pylak
 */
public class Plate implements IPlateIdentifier
{
    private String plateCode, experimentCode, projectCode, spaceCodeOrNull;

    public Plate(String plateCode, String experimentCode, String projectCode, String spaceCodeOrNull)
    {
        this.plateCode = plateCode;
        this.experimentCode = experimentCode;
        this.projectCode = projectCode;
        this.spaceCodeOrNull = spaceCodeOrNull;
    }

    /** a code of the plate */
    public String getPlateCode()
    {
        return plateCode;
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

    /** a code of the space to which the plate belongs or null if it is a shared plate */
    public String tryGetSpaceCode()
    {
        return spaceCodeOrNull;
    }

}