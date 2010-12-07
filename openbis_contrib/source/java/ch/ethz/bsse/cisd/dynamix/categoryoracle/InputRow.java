/*
 * Copyright 2010 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ch.ethz.bsse.cisd.dynamix.categoryoracle;

import ch.systemsx.cisd.common.annotation.BeanProperty;

/**
 * @author Izabela Adamczyk
 */
public final class InputRow extends Features
{
    private static final String IDENTIFIER = "identifier";

    private static final String CONTAINER = "container";

    private static final String YORF = "yorf";

    private String yorf;

    private String container;

    private String identifier;

    public InputRow()
    {
    }

    public String getContainer()
    {
        return container;
    }

    @BeanProperty(label = CONTAINER)
    public void setContainer(String container)
    {
        this.container = container;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    @BeanProperty(label = IDENTIFIER)
    public void setIdentifier(String identifier)
    {
        this.identifier = identifier;
    }

    public String getYorf()
    {
        return yorf;
    }

    @BeanProperty(label = YORF)
    public void setYorf(String yorf)
    {
        this.yorf = yorf;
    }

}