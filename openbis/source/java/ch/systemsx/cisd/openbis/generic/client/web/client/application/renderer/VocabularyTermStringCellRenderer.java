package ch.systemsx.cisd.openbis.generic.client.web.client.application.renderer;

import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;

import ch.systemsx.cisd.common.shared.basic.utils.StringUtils;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.BaseEntityModel;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.renderer.VocabularyPropertyColRenderer;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.ISerializableComparable;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.TableModelRowWithObject;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTerm;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.VocabularyTermTableCell;

public class VocabularyTermStringCellRenderer implements GridCellRenderer<BaseEntityModel<?>>
{

    public Object render(BaseEntityModel<?> model, String property, ColumnData config,
            int rowIndex, int colIndex, ListStore<BaseEntityModel<?>> store,
            Grid<BaseEntityModel<?>> grid)
    {
        String originalValue = String.valueOf(model.get(property));
        if (StringUtils.isBlank(originalValue))
        {
            return originalValue;
        } else
        {
            ISerializableComparable cell =
                    ((TableModelRowWithObject<?>) model.getBaseObject()).getValues().get(colIndex);
            VocabularyTermTableCell vocabularyTermTableCell = (VocabularyTermTableCell) cell;
            VocabularyTerm vocabularyTerm = vocabularyTermTableCell.getVocabularyTerm();
            return VocabularyPropertyColRenderer.renderTerm(vocabularyTerm);
        }
    }
}
