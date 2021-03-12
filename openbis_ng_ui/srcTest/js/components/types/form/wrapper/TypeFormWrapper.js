import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import ConfirmationDialog from '@src/js/components/common/dialog/ConfirmationDialog.jsx'
import ConfirmationDialogWrapper from '@srcTest/js/components/common/dialog/wrapper/ConfirmationDialogWrapper.js'
import TypeFormPreview from '@src/js/components/types/form/TypeFormPreview.jsx'
import TypeFormParameters from '@src/js/components/types/form/TypeFormParameters.jsx'
import TypeFormButtons from '@src/js/components/types/form/TypeFormButtons.jsx'
import TypeFormPreviewWrapper from './TypeFormPreviewWrapper.js'
import TypeFormParametersWrapper from './TypeFormParametersWrapper.js'
import TypeFormButtonsWrapper from './TypeFormButtonsWrapper.js'
import TypeFormDialogRemoveProperty from '@src/js/components/types/form/TypeFormDialogRemoveProperty.jsx'
import TypeFormDialogRemoveSection from '@src/js/components/types/form/TypeFormDialogRemoveSection.jsx'

export default class TypeFormWrapper extends BaseWrapper {
  getPreview() {
    return new TypeFormPreviewWrapper(this.findComponent(TypeFormPreview))
  }

  getParameters() {
    return new TypeFormParametersWrapper(this.findComponent(TypeFormParameters))
  }

  getButtons() {
    return new TypeFormButtonsWrapper(this.findComponent(TypeFormButtons))
  }

  getRemovePropertyDialog() {
    const propertyDialog = this.findComponent(TypeFormDialogRemoveProperty)
    return new ConfirmationDialogWrapper(
      this.findComponent(ConfirmationDialog, propertyDialog)
    )
  }

  getRemoveSectionDialog() {
    const sectionDialog = this.findComponent(TypeFormDialogRemoveSection)
    return new ConfirmationDialogWrapper(
      this.findComponent(ConfirmationDialog, sectionDialog)
    )
  }

  toJSON() {
    return {
      preview: this.getPreview().toJSON(),
      parameters: this.getParameters().toJSON(),
      buttons: this.getButtons().toJSON(),
      removePropertyDialog: this.getRemovePropertyDialog().toJSON(),
      removeSectionDialog: this.getRemoveSectionDialog().toJSON()
    }
  }
}
