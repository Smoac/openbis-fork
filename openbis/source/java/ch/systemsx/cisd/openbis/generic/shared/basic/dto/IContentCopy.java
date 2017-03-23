package ch.systemsx.cisd.openbis.generic.shared.basic.dto;

import java.io.Serializable;

public interface IContentCopy extends Serializable
{
    boolean isHyperLinkable();

    String getLabel();

    String getLocation();
}
