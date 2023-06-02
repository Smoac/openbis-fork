import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'
import FolderIcon from '@material-ui/icons/FolderOpen'
import FileIcon from '@material-ui/icons/InsertDriveFileOutlined'
import autoBind from 'auto-bind'

const styles = theme => ({
  content: {
    width: '100%',
    borderSpacing: '0',
    fontFamily: theme.typography.fontFamily,
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
    padding: theme.spacing(2)
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
