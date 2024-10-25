import React from 'react'
import withStyles from '@mui/styles/withStyles';
import TableRow from '@mui/material/TableRow'
import TableCell from '@mui/material/TableCell'
import logger from '@src/js/common/logger.js'

const styles = () => ({
  cell: {
    padding: 0
  },
  content: {
    position: 'sticky',
    left: 0,
    whiteSpace: 'nowrap',
    width: '750px'
  }
})

class GridRowFullWidth extends React.PureComponent {
  render() {
    logger.log(logger.DEBUG, 'GridRowFullWidth.render')

    const {
      multiselectable,
      columns,
      selected = false,
      children,
      styles = {},
      classes
    } = this.props

    return (
      <TableRow selected={selected}>
        <TableCell
          colSpan={
            columns.length === 0
              ? 1
              : columns.length + (multiselectable ? 1 : 0)
          }
          classes={{
            root: `${classes.cell} ${styles.cell}`
          }}
        >
          <div className={`${classes.content} ${styles.content}`}>
            {children}
          </div>
        </TableCell>
      </TableRow>
    )
  }
}

export default withStyles(styles)(GridRowFullWidth)
