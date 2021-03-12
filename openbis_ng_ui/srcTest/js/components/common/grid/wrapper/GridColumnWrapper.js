import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import FilterFieldWrapper from '@srcTest/js/components/common/form/wrapper/FilterFieldWrapper.js'
import GridColumnLabelWrapper from '@srcTest/js/components/common/grid/wrapper/GridColumnLabelWrapper.js'

export default class GridColumnWrapper extends BaseWrapper {
  constructor(column, labelWrapper, filterWrapper, sortWrapper) {
    super(null)
    this.column = column
    this.labelWrapper = labelWrapper
    this.filterWrapper = filterWrapper
    this.sortWrapper = sortWrapper
  }

  getName() {
    return this.column.name
  }

  getLabel() {
    return new GridColumnLabelWrapper(this.labelWrapper)
  }

  getFilter() {
    return new FilterFieldWrapper(this.filterWrapper)
  }

  getSort() {
    const active = this.sortWrapper.prop('active')
    const direction = this.sortWrapper.prop('direction')

    if (active) {
      return direction
    } else {
      return null
    }
  }

  toJSON() {
    return {
      name: this.getName(),
      label: this.getLabel().toJSON(),
      filter: this.getFilter().toJSON(),
      sort: this.getSort()
    }
  }
}
