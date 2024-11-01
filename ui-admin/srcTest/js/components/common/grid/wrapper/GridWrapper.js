import BaseWrapper from '@srcTest/js/components/common/wrapper/BaseWrapper.js'
import TableSortLabel from '@mui/material/TableSortLabel'
import GridPaging from '@src/js/components/common/grid/GridPaging.jsx'
import GridPagingWrapper from '@srcTest/js/components/common/grid/wrapper/GridPagingWrapper.js'
import GridHeader from '@src/js/components/common/grid/GridHeader.jsx'
import GridFilter from '@src/js/components/common/grid/GridFilter.jsx'
import GridRow from '@src/js/components/common/grid/GridRow.jsx'
import GridRowWrapper from '@srcTest/js/components/common/grid/wrapper/GridRowWrapper.js'
import GridColumnWrapper from '@srcTest/js/components/common/grid/wrapper/GridColumnWrapper.js'

export default class GridWrapper extends BaseWrapper {
  getColumns() {
    const columns = this.wrapper.prop('columns')

    const labels = this.findComponent(GridHeader)
    const filters = this.findComponent(GridFilter)
    const sorts = this.findComponent(TableSortLabel)

    return columns.map((column, index) => {
      const label = labels.at(index)
      const filter = filters.at(index)
      const sort = sorts.at(index)

      return new GridColumnWrapper(column, label, filter, sort)
    })
  }

  getRows() {
    const rows = []
    this.findComponent(GridRow).forEach(rowWrapper => {
      rows.push(new GridRowWrapper(rowWrapper))
    })
    return rows
  }

  getPaging() {
    return new GridPagingWrapper(this.findComponent(GridPaging))
  }

  toJSON() {
    if (this.wrapper.exists()) {
      return {
        columns: this.getColumns().map(row => row.toJSON()),
        rows: this.getRows().map(row => row.toJSON()),
        paging: this.getPaging().toJSON()
      }
    } else {
      return null
    }
  }
}
