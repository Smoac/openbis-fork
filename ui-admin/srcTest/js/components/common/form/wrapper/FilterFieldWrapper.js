import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import IconWrapper from '@srcTest/js/components/common/form/wrapper/IconWrapper.js'
import TextField from '@mui/material/TextField'
import IconButton from '@mui/material/IconButton'

export default class FilterFieldWrapper extends BaseWrapper {
  getValue() {
    return this.getStringValue(this.findComponent(TextField).prop('value'))
  }

  getClearIcon() {
    return new IconWrapper(this.findComponent(IconButton))
  }

  change(value) {
    this.findComponent(TextField).prop('onChange')({
      target: {
        value
      }
    })
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        value: this.getValue(),
        clearIcon: this.getClearIcon().toJSON()
      }
    } else {
      return null
    }
  }
}
