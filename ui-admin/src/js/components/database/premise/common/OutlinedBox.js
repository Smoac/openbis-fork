import React from "react";
import {Box} from "@material-ui/core";
import Typography from "@material-ui/core/Typography";
import {makeStyles} from "@material-ui/core/styles";

const useStyles = makeStyles({
    outlinedWrapper: {
        width: '100%',
        margin: '12px 0 0 0',
        position:'relative'},
    outlined: {
        border: '1px solid rgba(0, 0, 0, 0.3)',
        borderRadius: '6px',
        position: 'relative',
        padding: '0.8rem',
        display: 'flex',
        '&:hover': {
            borderColor: 'rgba(0, 0, 0, 0.5)'
        }
    },
    overlapLabel: {
        position: 'absolute',
        top: '-1.3ex',
        zIndex: 1,
        left: '1em',
        backgroundColor: 'white',
        padding: '0 5px'
    }
});
const OutlinedBox = ({ label, children, style }) => {
    const classes = useStyles();
    return (
        <Box className={classes.outlinedWrapper} style={style}>
            <Typography variant='body2' gutterBottom align='left' className={classes.overlapLabel}>
                {label}
            </Typography>
            <Box className={classes.outlined}>
                {children}
            </Box>
        </Box>
    );
}

export default OutlinedBox ;