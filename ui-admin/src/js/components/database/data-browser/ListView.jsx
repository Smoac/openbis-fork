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
  }
})

class ListView extends React.Component {

  constructor(props, context) {
    super(props, context)
    autoBind(this)
  }

  getIcon(file) {
    if (file.folder) {
      return <FolderIcon />
    } else {
      return <FileIcon />
    }
  }

  render() {
    const { classes, files } = this.props
    /* Create strings in messages. */
    return (
      <Container>
        <table className={classes.content}>
          <thead>
            <tr>
              <th className={classes.tableHeader}>Name</th>
              <th className={classes.tableHeader}>Size</th>
              <th className={classes.tableHeader}>Modified</th>
            </tr>
          </thead>
          <tbody>
            {files.map((file, index) =>
              <tr key={index}>
                <td className={classes.nameColumn}>{<>{this.getIcon(file)} {file.name}</>}</td>
                <td className={classes.sizeColumn}>{file.folder ? '-' : file.size}</td>
                <td className={classes.modifiedColumn}>{file.lastModifiedTime.toLocaleString()}</td>
              </tr>
            )}
          </tbody>
        </table>
      </Container>
    )
  }
}

export default withStyles(styles)(ListView)
