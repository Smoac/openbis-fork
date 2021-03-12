import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import GridHeaderFilter from '@src/js/components/common/grid/GridHeaderFilter.jsx'
import GridHeaderLabel from '@src/js/components/common/grid/GridHeaderLabel.jsx'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  header: {
    '& th': {
      position: 'sticky',
      top: 0,
      zIndex: 10,
      fontWeight: 'bold',
      backgroundColor: theme.palette.background.primary,
      minWidth: '120px'
    }
  },
  cell: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`,
    borderColor: theme.palette.border.secondary
  }
})

class GridHeader extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridHeader.render')

    const { columns, classes } = this.props

    return (
      <TableHead>
        <TableRow>
          {columns.map(column => this.renderFilterCell(column))}
        </TableRow>
        <TableRow classes={{ root: classes.header }}>
          {columns.map(column => this.renderHeaderCell(column))}
        </TableRow>
      </TableHead>
    )
  }

  renderHeaderCell(column) {
    const { sort, sortDirection, onSortChange } = this.props

    return (
      <GridHeaderLabel
        key={column.name}
        column={column}
        sort={sort}
        sortDirection={sortDirection}
        onSortChange={onSortChange}
      />
    )
  }

  renderFilterCell(column) {
    const { filters, onFilterChange } = this.props

    return (
      <GridHeaderFilter
        key={column.name}
        column={column}
        filter={filters[column.name]}
        onFilterChange={onFilterChange}
      />
    )
  }
}

export default withStyles(styles)(GridHeader)
