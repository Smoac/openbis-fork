package ch.systemsx.cisd.openbis.plugin.screening.shared.api.v1.dto;

import java.util.ArrayList;
import java.util.Collection;

import ch.systemsx.cisd.common.collections.Modifiable;

/**
 * This collection was created to help Jackson library embed/detect types of the collection's 
 * items during JSON serialization/deserialization.
 * (see http://wiki.fasterxml.com/JacksonPolymorphicDeserialization#A5._Known_Issues)
 *  
 * @author pkupczyk
 */
public class PlateList extends ArrayList<Plate> implements Modifiable
{
    private static final long serialVersionUID = 1L;

    public PlateList(Collection<? extends Plate> c)
    {
        super(c);
    }
}