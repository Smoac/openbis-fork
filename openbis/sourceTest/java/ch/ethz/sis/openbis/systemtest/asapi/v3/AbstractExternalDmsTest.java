package ch.ethz.sis.openbis.systemtest.asapi.v3;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDms;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.ExternalDmsAddressType;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.create.ExternalDmsCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.fetchoptions.ExternalDmsFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.ExternalDmsPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.externaldms.id.IExternalDmsId;

public abstract class AbstractExternalDmsTest extends AbstractTest
{

    protected ExternalDmsPermId create(ExternalDmsCreationBuilder creation)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        List<ExternalDmsPermId> ids =
                v3api.createExternalDataManagementSystems(sessionToken, Collections.singletonList(creation.build()));
        return ids.get(0);
    }

    protected ExternalDms get(ExternalDmsPermId id)
    {
        final String sessionToken = v3api.login(TEST_USER, PASSWORD);
        Map<IExternalDmsId, ExternalDms> result =
                v3api.getExternalDataManagementSystems(sessionToken, Collections.singletonList(id), new ExternalDmsFetchOptions());
        return result.get(id);
    }

    protected ExternalDmsCreationBuilder externalDms()
    {
        return new ExternalDmsCreationBuilder();
    }

    protected String uuid()
    {
        return UUID.randomUUID().toString().toUpperCase();
    }

    protected class ExternalDmsCreationBuilder
    {
        private String code = uuid();

        private String address = uuid();

        private String label = uuid();

        private ExternalDmsAddressType type = ExternalDmsAddressType.OPENBIS;

        private boolean addressSet = false;

        public ExternalDmsCreationBuilder withCode(String code)
        {
            this.code = code;
            return this;
        }

        public ExternalDmsCreationBuilder withLabel(String label)
        {
            this.label = label;
            return this;
        }

        public ExternalDmsCreationBuilder withAddress(String address)
        {
            this.address = address;
            this.addressSet = true;
            return this;
        }

        public ExternalDmsCreationBuilder withType(ExternalDmsAddressType type)
        {
            this.type = type;
            if (addressSet == false && ExternalDmsAddressType.FILE_SYSTEM.equals(type))
            {
                this.address = "host:/valid/path/" + uuid();
            }
            return this;
        }

        public ExternalDmsCreation build()
        {
            ExternalDmsCreation creation = new ExternalDmsCreation();
            creation.setCode(code);
            creation.setLabel(label);
            creation.setAddressType(type);
            creation.setAddress(address);
            return creation;
        }
    }

    protected Matcher<ExternalDms> isSimilarTo(final ExternalDms edms)
    {
        return new TypeSafeDiagnosingMatcher<ExternalDms>()
            {

                private String describe(ExternalDms edms)
                {
                    return new ToStringBuilder(edms)
                            .append("address", edms.getAddress())
                            .append("addressType", edms.getAddressType())
                            .append("code", edms.getCode())
                            .append("label", edms.getLabel())
                            .append("urlTemplate", edms.getUrlTemplate())
                            .toString();
                }

                @Override
                public void describeTo(Description desc)
                {
                    desc.appendText(describe(edms));
                }

                @Override
                protected boolean matchesSafely(ExternalDms another, Description desc)
                {
                    desc.appendText(describe(another));
                    return Objects.equals(edms.getAddress(), another.getAddress()) &&
                            Objects.equals(edms.getAddressType(), another.getAddressType()) &&
                            Objects.equals(edms.getCode(), another.getCode()) &&
                            Objects.equals(edms.getLabel(), another.getLabel()) &&
                            Objects.equals(edms.getUrlTemplate(), another.getUrlTemplate());
                }
            };
    }
}
