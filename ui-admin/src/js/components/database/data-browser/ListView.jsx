import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import FolderIcon from '@material-ui/icons/FolderOpen'
import FileIcon from '@material-ui/icons/DescriptionOutlined'
import autoBind from 'auto-bind'

const styles = theme => ({
  content: {
    width: '100%',
    fontFamily: theme.typography.fontFamily,
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
    verticalAlign: 'middle'
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
    height: '2em'
  },
  tableData: {
    padding: theme.spacing(2),
  }
})

class ListView extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)
  }

  getIcon(file) {
    const { classes } = this.props

    if (file.folder) {
      return <FolderIcon className={classes.icon} />
    } else {
      return <FileIcon className={classes.icon} />
    }
  }

  render() {
    const { classes, files } = this.props
    /* Create strings in messages. */
    return (
      <Container>
        <table className={classes.content}>
          <thead>
            <tr className={classes.tableRow}>
              <th className={`${classes.tableData} ${classes.tableHeader}`}>Name</th>
              <th className={`${classes.tableData} ${classes.tableHeader}`}>Size</th>
              <th className={`${classes.tableData} ${classes.modifiedColumn} ${classes.tableHeader}`}>Modified</th>
            </tr>
          </thead>
          <tbody>
            {files.map((file, index) =>
              <tr key={index} className={classes.tableRow}>
                <td className={`${classes.tableData} ${classes.nameColumn}`}>{<>{this.getIcon(file)} {file.name}</>}</td>
                <td className={`${classes.tableData} ${classes.sizeColumn}`}>{file.folder ? '-' : file.size}</td>
                <td className={`${classes.tableData} ${classes.modifiedColumn}`}>{file.lastModifiedTime.toLocaleString()}</td>
              </tr>
            )}
          </tbody>
        </table>
      </Container>
    )
  }
}

export default withStyles(styles)(ListView)
