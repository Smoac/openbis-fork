/*
 * Copyright 2012 ETH Zuerich, CISD
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

package ch.systemsx.cisd.openbis.generic.shared.api.v1.dto;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author jakubs
 */
@SuppressWarnings("unused")
public class Material extends MaterialIdentifier
{
    private static final long serialVersionUID = 1L;

    private Map<String, String> properties;

    private Map<String, Material> materialProperties;

    private EntityRegistrationDetails registrationDetails;

    public static final class MaterialInitializer
    {
        private MaterialTypeIdentifier materialTypeIdentifier;

        private String materialCode;

        private Map<String, String> properties;

        private Map<String, Material> materialProperties;

        private EntityRegistrationDetails registrationDetails;

        public MaterialTypeIdentifier getMaterialTypeIdentifier()
        {
            return materialTypeIdentifier;
        }

        public void setMaterialTypeIdentifier(MaterialTypeIdentifier materialTypeIdentifier)
        {
            this.materialTypeIdentifier = materialTypeIdentifier;
        }

        public String getMaterialCode()
        {
            return materialCode;
        }

        public void setMaterialCode(String materialCode)
        {
            this.materialCode = materialCode;
        }

        public Map<String, String> getProperties()
        {
            return properties;
        }

        public void setProperties(Map<String, String> properties)
        {
            this.properties = properties;
        }

        public Map<String, Material> getMaterialProperties()
        {
            return materialProperties;
        }

        public void setMaterialProperties(Map<String, Material> materialProperties)
        {
            this.materialProperties = materialProperties;
        }

        public EntityRegistrationDetails getRegistrationDetails()
        {
            return registrationDetails;
        }

        public void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
        {
            this.registrationDetails = registrationDetails;
        }

    }

    public Material(MaterialInitializer initializer)
    {
        super(initializer.getMaterialTypeIdentifier(), initializer.getMaterialCode());
        this.properties = initializer.getProperties();
        this.materialProperties = initializer.getMaterialProperties();
        this.registrationDetails = initializer.getRegistrationDetails();
    }

    /**
     * @return the material properties
     */
    public Map<String, String> getProperties()
    {
        return Collections.unmodifiableMap(properties);
    }

    public Map<String, Material> getMaterialProperties()
    {
        return Collections.unmodifiableMap(materialProperties);
    }

    public EntityRegistrationDetails getRegistrationDetails()
    {
        return registrationDetails;
    }

    //
    // JSON-RPC
    //

    private Material()
    {
        super(null, null);
    }

    private void setProperties(Map<String, String> properties)
    {
        this.properties = properties;
    }

    private void setMaterialProperties(Map<String, Material> materialProperties)
    {
        this.materialProperties = materialProperties;
    }

    private void setRegistrationDetails(EntityRegistrationDetails registrationDetails)
    {
        this.registrationDetails = registrationDetails;
    }
}
