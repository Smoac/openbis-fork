import React from 'react'
import { withStyles } from '@material-ui/core/styles'
import GridViewItem from '@src/js/components/database/data-browser/GridViewItem.jsx'
import Grid from '@material-ui/core/Grid'
import autoBind from 'auto-bind'
import logger from '@src/js/common/logger.js'

const styles = theme => ({
  container: {
    fontFamily: theme.typography.fontFamily,
    display: "grid",
    gridTemplateColumns: "repeat(auto-fill, minmax(8rem, 1fr))",
    gridGap: "0.5rem"
  }
});

class GridView extends React.Component {
  constructor(props, context) {
    super(props, context);
    autoBind(this);
  }

  handleClick(event, file) {
    const { clickable, onClick } = this.props;

    if (clickable && onClick) {
      onClick(file);
    }
  }

  handleSelect(event, file) {
    const { selectable, onSelect } = this.props;

    if (selectable && onSelect) {
      onSelect(file);
    }
  }

  handleMultiselect(event) {
    event.preventDefault();
    event.stopPropagation();

    const { multiselectable, onMultiselect, file } = this.props;

    if (multiselectable && onMultiselect) {
      onMultiselect(file);
    }
  }

  render() {
    logger.log(logger.DEBUG, 'GridView.render')
    const { clickable, selectable, multiselectable, classes, configuration, files, selectedFile, multiselectedFiles } = this.props;

    return (
      <Grid container className={classes.container}>
        {files.map((file, index) => (
          <GridViewItem
            key={index}
            {...this.props}
            selected={selectedFile === file}
            multiselected={multiselectedFiles.has(file)}
            file={file}
            onClick={this.handleClick}
            onSelect={this.handleSelect}
            onMultiselect={this.handleMultiselect}
          />
        ))}
      </Grid>
    );
  }
}

export default withStyles(styles)(GridView);
