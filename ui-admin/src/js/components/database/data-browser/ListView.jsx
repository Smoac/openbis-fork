import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import FolderIcon from '@material-ui/icons/FolderOpen'
import FileIcon from '@material-ui/icons/InsertDriveFileOutlined'
import Table from '@material-ui/core/Table';
import TableBody from '@material-ui/core/TableBody';
import TableCell from '@material-ui/core/TableCell';
import TableContainer from '@material-ui/core/TableContainer';
import TableHead from '@material-ui/core/TableHead';
import TableRow from '@material-ui/core/TableRow';
import Paper from '@material-ui/core/Paper';
import autoBind from 'auto-bind'

const styles = theme => ({
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
  nameColumn: {
    textAlign: 'left'
  },
  sizeColumn: {
    width: '11rem',
    textAlign: 'left'
  },
  modifiedColumn: {
    width: '11rem',
    textAlign: 'right'
  },
  icon: {
    verticalAlign: 'middle',
    fontSize: '2.5rem'
  },
  text: {
    fontSize: theme.typography.body2.fontSize,
    lineHeight: theme.typography.body2.fontSize
  },
  listContainer: {
    flex: '1 1 100%'
  },
  tableRow: {
    fontSize: theme.typography.body1.fontSize,
    height: '2rem'
  },
  tableData: {
    padding: theme.spacing(2),
    borderWidth: '0'
  }
})

class ListView extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)

    const { configuration } = this.props

    this.extensionToIconType = new Map(
      configuration.flatMap(
        (configObject) => configObject.extensions.map(extension => [extension, configObject.icon])
      )
    )
  }

  getIcon(file) {
    const { classes } = this.props

    if (file.folder) {
      return <FolderIcon className={classes.icon} />
    } else {
      const iconType = this.extensionToIconType.get(file.name.substring(file.name.lastIndexOf(".") + 1))
      return iconType ? React.createElement(iconType, { className: classes.icon }) : <FileIcon className={classes.icon} />
    }
  }

  render() {
    const { classes, files } = this.props

    /* Create strings in messages. */
    return (
      <TableContainer component={Paper}>
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
              <TableRow key={index} className={classes.tableRow}>
                <TableCell className={`${classes.tableData} ${classes.nameColumn}`}>{<>{this.getIcon(file)} {file.name}</>}</TableCell>
                <TableCell className={`${classes.tableData} ${classes.sizeColumn}`}>{file.folder ? '-' : file.size}</TableCell>
                <TableCell className={`${classes.tableData} ${classes.modifiedColumn}`}>{file.lastModifiedTime.toLocaleString()}</TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>
    )
  }
}

export default withStyles(styles)(ListView)
