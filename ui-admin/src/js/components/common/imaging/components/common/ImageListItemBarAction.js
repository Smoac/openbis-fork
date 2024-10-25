import React from "react";
import {IconButton, ImageListItemBar, Tooltip} from "@mui/material";
import ArrowLeftIcon from "@mui/icons-material/ArrowLeft";
import ArrowRightIcon from "@mui/icons-material/ArrowRight";
import makeStyles from '@mui/styles/makeStyles';

const useStyles = makeStyles(() => ({
    icon: {
        color: 'rgba(0, 0, 0, 0.9)'
    },
    singleActionBar: {
        background:
            'linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.5) 30%, rgba(0,0,0,0.3) 60%, rgba(0,0,0,0) 100%)',
    },
    actionBarL: {
        width: '50%',
        marginRight: '50%',
        background:
            'linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.5) 30%, rgba(0,0,0,0.3) 60%, rgba(0,0,0,0) 100%)',
    },
    actionBarR: {
        width: '50%',
        marginLeft: 'auto',
        background:
            'linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.5) 30%, rgba(0,0,0,0.3) 60%, rgba(0,0,0,0) 100%)',
    }
}));
const ImageListItemBarAction = ({classNames, position, onMove}) => {
    const classes = useStyles();

    return (
        <ImageListItemBar actionPosition={position}
                                 className={classes[classNames]}
                                 actionIcon={
                                     <Tooltip title={"Move " + position}>
                                         <IconButton className={classes.icon} onClick={onMove} size="large">
                                             {position === 'left' ? <ArrowLeftIcon/> : <ArrowRightIcon/>}
                                         </IconButton>
                                     </Tooltip>
                                 }/>
    );
};

export default ImageListItemBarAction;