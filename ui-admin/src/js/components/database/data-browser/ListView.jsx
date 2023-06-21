import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Table from '@material-ui/core/Table'
import TableBody from '@material-ui/core/TableBody'
import TableCell from '@material-ui/core/TableCell'
import TableContainer from '@material-ui/core/TableContainer'
import TableHead from '@material-ui/core/TableHead'
import TableRow from '@material-ui/core/TableRow'
import { ListViewItem } from '@src/js/components/database/data-browser/ListViewItem.jsx'
import logger from '@src/js/common/logger.js'

const styles = (theme) => ({
  content: {
    width: '100%',
    borderSpacing: '0',
    fontFamily: theme.typography.fontFamily,
    '& thead > tr > th': {
      fontWeight: 'bold'
    },
    '& tbody > tr': {
      cursor: 'pointer',
      '&:hover': {
        backgroundColor: '#0000000a'
      }
    },
  },
  tableHeader: {
    textAlign: 'left'
  },
  icon: {
    fontSize: '2.5rem'
  },
  text: {
    fontSize: theme.typography.body2.fontSize,
    lineHeight: theme.typography.body2.fontSize
  },
  listContainer: {
    flex: '1 1 100%'
  },
  modifiedColumn: {
    width: '11rem',
    textAlign: 'right'
  },
  tableRow: {
    fontSize: theme.typography.body1.fontSize,
    height: '2rem'
  },
  tableData: {
    padding: theme.spacing(2),
    borderWidth: '0'
  },
})

class ListView extends React.Component {

  render() {
    logger.log(logger.DEBUG, 'ListView.render')
    const { classes, files, configuration } = this.props

    /* Create strings in messages. */
    return (
      <TableContainer>
        <Table className={classes.content}>
          <TableHead>
            <TableRow className={classes.tableRow}>
              <TableCell className={`${classes.tableData} ${classes.tableHeader}`}>Name</TableCell>
              <TableCell className={`${classes.tableData} ${classes.tableHeader}`}>Size</TableCell>
              <TableCell className={`${classes.tableData} ${classes.modifiedColumn} ${classes.tableHeader}`}>Modified</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {files.map((file, index) =>
              <ListViewItem key={index} {...this.props} file={file} />
            )}
          </TableBody>
        </Table>
      </TableContainer>
    )
  }
}

export default withStyles(styles)(ListView)
