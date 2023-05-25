import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import Container from '@src/js/components/common/form/Container.jsx'

const styles = theme => ({
  containerDefault: {
    padding: `${theme.spacing(1)}px ${theme.spacing(2)}px`
  },
  containerSquare: {
    padding: `${theme.spacing(2)}px ${theme.spacing(2)}px`
  },
  content: {
    width: '100%'
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
                <td className={classes.nameColumn}>{file.name}</td>
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
