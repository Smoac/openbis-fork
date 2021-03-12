import TextField from '@src/js/components/common/form/TextField.jsx'
import TextFieldWrapper from '@srcTest/js/components/common/form/wrapper/TextFieldWrapper.js'
import PageParametersPanelWrapper from '@srcTest/js/components/common/page/wrapper/PageParametersPanelWrapper'

export default class TypeFormParametersSectionWrapper extends PageParametersPanelWrapper {
  getName() {
    return new TextFieldWrapper(
      this.findComponent(TextField).filter({ name: 'name' })
    )
  }

  toJSON() {
    return {
      ...super.toJSON(),
      name: this.getName().toJSON()
    }
  }
}
