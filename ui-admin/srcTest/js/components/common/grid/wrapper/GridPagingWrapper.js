import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import SelectField from '@src/js/components/common/form/SelectField.jsx'
import SelectFieldWrapper from '@srcTest/js/components/common/form/wrapper/SelectFieldWrapper.js'
import IconWrapper from '@srcTest/js/components/common/form/wrapper/IconWrapper.js'
import Typography from '@mui/material/Typography'
import IconButton from '@mui/material/IconButton'

export default class GridPagingWrapper extends BaseWrapper {
  getPageSize() {
    return new SelectFieldWrapper(this.findComponent(SelectField))
  }

  getPrevPage() {
    return new IconWrapper(
      this.findComponent(IconButton).filter({ 'data-part': 'prevPage' })
    )
  }

  getNextPage() {
    return new IconWrapper(
      this.findComponent(IconButton).filter({ 'data-part': 'nextPage' })
    )
  }

  getFirstPage() {
    return new IconWrapper(
      this.findComponent(IconButton).filter({ 'data-part': 'firstPage' })
    )
  }

  getLastPage() {
    return new IconWrapper(
      this.findComponent(IconButton).filter({ 'data-part': 'lastPage' })
    )
  }

  getRange() {
    return this.getStringValue(
      this.findComponent(Typography)
        .filter({ 'data-part': 'range' })
        .text()
        .trim()
    )
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        pageSize: this.getPageSize().toJSON(),
        range: this.getRange()
      }
    } else {
      return null
    }
  }
}
