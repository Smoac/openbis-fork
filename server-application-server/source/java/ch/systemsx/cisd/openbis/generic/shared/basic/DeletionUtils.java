/*
 * Copyright ETH 2008 - 2023 Zürich, Scientific IT Services
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
package ch.systemsx.cisd.openbis.generic.shared.basic;

import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Deletion;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.EntityKind;

/**
 * @author Piotr Buczek
 */
public class DeletionUtils
{
    private static final int MAX_NUMBER = 5;

    public static String createDescriptionOfDeletedEntities(Deletion deletion)
    {
        StringBuilder builder = new StringBuilder();
        String experiments = createList(deletion, EntityKind.EXPERIMENT, "Experiment");
        if (experiments.length() > 0)
        {
            builder.append(experiments);
        }
        String samples = createList(deletion, EntityKind.SAMPLE, "Sample");
        if (samples.length() > 0)
        {
            builder.append(samples);
        }
        String dataSets = createList(deletion, EntityKind.DATA_SET, "Data Set");
        if (dataSets.length() > 0)
        {
            builder.append(dataSets);
        }
        return builder.toString();
    }

    private static String createList(Deletion deletion, EntityKind entityKind, String name)
    {
        StringBuilder builder = new StringBuilder();
        int count = 0;
        for (IEntityInformationHolderWithIdentifier entity : deletion.getDeletedEntities())
        {
            if (entity.getEntityKind() == entityKind)
            {
                if (count < MAX_NUMBER)
                {
                    builder.append("  ").append(entity.getIdentifier()).append(" (");
                    builder.append(entity.getEntityType().getCode()).append(")\n");
                    count++;
                }
            }
        }

        int numberOfAdditionalEntities = 0;

        switch (entityKind)
        {
            case DATA_SET:
                numberOfAdditionalEntities = deletion.getTotalDatasetsCount();
                break;
            case SAMPLE:
                numberOfAdditionalEntities = deletion.getTotalSamplesCount();
                break;
            case EXPERIMENT:
                numberOfAdditionalEntities = deletion.getTotalExperimentsCount();
                break;
            default:
                // nothing
                break;
        }

        numberOfAdditionalEntities -= count;

        if (count == 0)
        {
            if (numberOfAdditionalEntities == 0)
            {
                return "";
            } else if (numberOfAdditionalEntities == 1)
            {
                return "1 " + name + "\n";
            } else if (numberOfAdditionalEntities > 1)
            {
                return numberOfAdditionalEntities + " " + name + "s\n";
            }
        }

        if (numberOfAdditionalEntities > 0)
        {
            builder.append("  and ").append(numberOfAdditionalEntities).append(" more\n");
        }

        if (count == 1)
        {
            return name + " " + builder.toString();
        }

        return name + "s:\n" + builder.toString();
    }

    public static boolean isDeleted(IDeletionProvider deletableOrNull)
    {
        return deletableOrNull != null && deletableOrNull.getDeletion() != null;
    }

    public static boolean isDeleted(Object objectOrNull)
    {
        if (objectOrNull != null && objectOrNull instanceof IDeletionProvider)
        {
            return isDeleted((IDeletionProvider) objectOrNull);
        } else
        {
            return false;
        }
    }
}
