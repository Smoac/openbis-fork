package ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.property_type;

import java.util.List;

import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;

import ch.systemsx.cisd.openbis.generic.client.web.client.ICommonClientServiceAsync;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.IViewContext;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.model.ModelDataPropertyNames;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularyRegistrationFieldSet;
import ch.systemsx.cisd.openbis.generic.client.web.client.application.ui.vocabulary.VocabularySelectionWidget;
import ch.systemsx.cisd.openbis.generic.shared.basic.dto.Vocabulary;

/**
 * A small {@link VocabularySelectionWidget} extension suitable for <i>Property Type</i>
 * registration.
 * 
 * @author Christian Ribeaud
 */
final class VocabularySelectionWidgetForPropertyTypeRegistration extends VocabularySelectionWidget
{
    private final VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet;

    VocabularySelectionWidgetForPropertyTypeRegistration(
            final IViewContext<ICommonClientServiceAsync> viewContext,
            final VocabularyRegistrationFieldSet vocabularyRegistrationFieldSet)
    {
        super(viewContext);
        assert vocabularyRegistrationFieldSet != null : "Unspecified VocabularyRegistrationFieldSet";
        this.vocabularyRegistrationFieldSet = vocabularyRegistrationFieldSet;
        addSelectionChangedListener(new SelectionChangedListener<BaseModelData>()
            {

                //
                // SelectionChangedListener
                //

                @Override
                public final void selectionChanged(final SelectionChangedEvent<BaseModelData> se)
                {
                    final BaseModelData selectedItem = se.getSelectedItem();
                    final boolean visible;
                    if (selectedItem != null)
                    {
                        visible =
                                selectedItem.get(ModelDataPropertyNames.CODE).equals(
                                        NEW_VOCABULARY_CODE);
                    } else
                    {
                        visible = false;
                    }
                    vocabularyRegistrationFieldSet.setVisible(visible);
                }
            });

    }

    //
    // VocabularySelectionWidget
    //

    @Override
    public final void setVisible(final boolean visible)
    {
        super.setVisible(visible);
        if (visible == false && isRendered())
        {
            vocabularyRegistrationFieldSet.setVisible(visible);
        }
    }

    @Override
    protected final void refreshStore(final List<Vocabulary> result)
    {
        super.refreshStore(result);
        getStore().insert(createNewVocabularyVocabularyModel(), 0);
    }
}