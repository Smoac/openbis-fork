import React from "react";
import {Box} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles({
    outlinedWrapper: {
        width: '95%',
        margin: '16px',
        position:'relative'},
    outlined: {
        border: '1px solid rgba(0, 0, 0, 0.3)',
        borderRadius: '6px',
        position: 'relative',
        lineHeight: '6ex',
        padding: '1.5rem',
        display: 'flex',
    },
    overlapLabel: {
        position: 'absolute',
        top: '-1ex',
        zIndex: 1,
        left: '2em',
        backgroundColor: 'white',
        padding: '0 5px'
    }
});
const OutlinedBox = ({ label, children, style }) => {
    const classes = useStyles();
    return (
        <Box className={classes.outlinedWrapper} style={style}>
            <Typography gutterBottom align='left' className={classes.overlapLabel}>
                {label}
            </Typography>
            <Box className={classes.outlined}>
                {children}
            </Box>
        </Box>
    );
}

export default OutlinedBox ;