package ch.systemsx.cisd.openbis.plugin.screening.shared.api.dto;

/**
 * Contains data which uniquely define a plate
 * 
 * @author Tomasz Pylak
 */
public interface IPlateIdentifier
{
    /** a code of the plate */
    String getPlateCode();

    /** a code of the space to which the plate belongs or null if it is a shared plate */
    String tryGetSpaceCode();
}