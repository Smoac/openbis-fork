package ch.ethz.sis.openbis.generic.server.xls.importxls.helper;

import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.create.VocabularyTermCreation;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.fetchoptions.VocabularyTermFetchOptions;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.id.VocabularyTermPermId;
import ch.ethz.sis.openbis.generic.asapi.v3.dto.vocabulary.update.VocabularyTermUpdate;
import ch.ethz.sis.openbis.generic.server.xls.importxls.delay.DelayedExecutionDecorator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportModes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.enums.ImportTypes;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.AttributeValidator;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.IAttribute;
import ch.ethz.sis.openbis.generic.server.xls.importxls.utils.VersionUtils;

import java.util.List;
import java.util.Map;

public class VocabularyTermImportHelper extends BasicImportHelper
{
    private static final String VOCABULARY_CODE_FIELD = "Code";

    private enum Attribute implements IAttribute {
        Version("Version", true),
        Code("Code", true),
        Label("Label", true),
        Description("Description", true);

        private final String headerName;

        private final boolean mandatory;

        Attribute(String headerName, boolean mandatory) {
            this.headerName = headerName;
            this.mandatory = mandatory;
        }

        public String getHeaderName() {
            return headerName;
        }
        public boolean isMandatory() {
            return mandatory;
        }
    }

    private String vocabularyCode;

    private final Map<String, Integer> versions;

    private final DelayedExecutionDecorator delayedExecutor;

    private final AttributeValidator<Attribute> attributeValidator;

    public VocabularyTermImportHelper(DelayedExecutionDecorator delayedExecutor, ImportModes mode, Map<String, Integer> versions)
    {
        super(mode);
        this.versions = versions;
        this.delayedExecutor = delayedExecutor;
        this.attributeValidator = new AttributeValidator<>(Attribute.class);
    }

    @Override public void importBlock(List<List<String>> page, int pageIndex, int start, int end)
    {
        Map<String, Integer> header = parseHeader(page.get(start), false);
        vocabularyCode = getValueByColumnName(header, page.get(start + 1), VOCABULARY_CODE_FIELD);
        super.importBlock(page, pageIndex, start + 2, end);
    }

    @Override protected ImportTypes getTypeName()
    {
        return ImportTypes.VOCABULARY_TERM;
    }

    @Override protected boolean isNewVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        return VersionUtils.isNewVersion(version,
                VersionUtils.getStoredVersion(versions, ImportTypes.VOCABULARY_TERM.getType() + "-" + vocabularyCode, code));
    }

    @Override protected void updateVersion(Map<String, Integer> header, List<String> values)
    {
        String version = getValueByColumnName(header, values, Attribute.Version);
        String code = getValueByColumnName(header, values, Attribute.Code);

        VersionUtils.updateVersion(version, versions, ImportTypes.VOCABULARY_TERM.getType() + "-" + vocabularyCode, code);
    }

    @Override protected boolean isObjectExist(Map<String, Integer> header, List<String> values)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        VocabularyTermPermId termId = new VocabularyTermPermId(code, vocabularyCode);

        VocabularyTermFetchOptions fetchOptions = new VocabularyTermFetchOptions();
        fetchOptions.withVocabulary();

        return this.delayedExecutor.getVocabularyTerm(termId, fetchOptions) != null;
    }

    @Override protected void createObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String label = getValueByColumnName(header, values, Attribute.Label);
        String description = getValueByColumnName(header, values, Attribute.Description);

        VocabularyPermId vocabularyPermId = new VocabularyPermId(vocabularyCode);

        VocabularyTermCreation creation = new VocabularyTermCreation();
        creation.setVocabularyId(vocabularyPermId);
        creation.setCode(code);
        creation.setLabel(label);
        creation.setDescription(description);

        this.delayedExecutor.createVocabularyTerm(creation);
    }

    @Override protected void updateObject(Map<String, Integer> header, List<String> values, int page, int line)
    {
        String code = getValueByColumnName(header, values, Attribute.Code);
        String label = getValueByColumnName(header, values, Attribute.Label);
        String description = getValueByColumnName(header, values, Attribute.Description);

        VocabularyTermPermId termId = new VocabularyTermPermId(code, vocabularyCode);

        VocabularyTermUpdate update = new VocabularyTermUpdate();
        update.setVocabularyTermId(termId);
        update.setLabel(label);
        update.setDescription(description);

        this.delayedExecutor.updateVocabularyTerm(update);
    }

    @Override protected void validateHeader(Map<String, Integer> headers)
    {
        attributeValidator.validateHeaders(Attribute.values(), headers);
    }
}
