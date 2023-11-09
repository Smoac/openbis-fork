import * as React from 'react';
import {makeStyles, styled, useTheme} from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Slider from '@material-ui/core/Slider';
import IconButton from '@material-ui/core/IconButton';
import PauseRounded from '@material-ui/icons/PauseRounded';
import PlayArrowRounded from '@material-ui/icons/PlayArrowRounded';
import FastForwardRounded from '@material-ui/icons/FastForwardRounded';
import FastRewindRounded from '@material-ui/icons/FastRewindRounded';
import MobileStepper from "@material-ui/core/MobileStepper";


const Widget = styled('div')(({ theme }) => ({
    padding: 16,
    borderRadius: 16,
    width: 143,
    maxWidth: '100%',
    height: 'fit-content',
    position: 'relative',
    zIndex: 1,
    backgroundColor: 'rgba(111,111,111,0.2)',
    backdropFilter: 'blur(40px)',
}));

const defaultSpeeds = [
    {
        value: 1000,
        label: '1s',
    },
    {
        value: 2000,
        label: '2s',
    },
    {
        value: 5000,
        label: '5s',
    },
    {
        value: 10000,
        label: '10s',
    },
];

const useStyles = makeStyles({
    rootBox: {
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        mt: -1,
    },
    root: {
        display: 'contents'
    }
});
export default function Player({ label= 'DEFAULT', onStep, steps = [], speeds = defaultSpeeds, speedable = false }) {
    const classes = useStyles();
    const theme = useTheme();
    const [activeStep, setActiveStep] = React.useState(-1);
    const [paused, setPaused] = React.useState(true);
    const [speed, setSpeed] = React.useState(2000);
    const timeoutRef = React.useRef(null);

    const handleSpeedChange = (event, newValue) => {
        setSpeed(newValue);
    };

    function resetTimeout() {
        if (timeoutRef.current) {
            clearTimeout(timeoutRef.current);
        }
    }

    React.useEffect(() => {
        if (!paused){
            console.log(`RUN activeStep=${activeStep} value=${steps[activeStep]}`);
            resetTimeout();
            onStep(steps[activeStep], label, true);
            timeoutRef.current = setTimeout(
                () =>
                    setActiveStep((prevIndex) =>
                        prevIndex === steps.length - 1 ? 0 : prevIndex + 1
                    ),
                speed
            );

            return () => {
                resetTimeout();
            };
        }
    }, [activeStep]);

    const duration = 2000;
    const handleNext = () => {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
    };

    const handlePlay = () => {
        resetTimeout();
        timeoutRef.current = setTimeout(
            () =>
                setActiveStep((prevIndex) =>
                    prevIndex === steps.length - 1 ? 0 : prevIndex + 1
                ),
            2000
        );

        return () => {
            resetTimeout();
        };
    }

    const mainIconColor = theme.palette.mode === 'dark' ? '#fff' : '#000';
    //console.log(steps);
    return (
        <Widget>
            <Box className={classes.rootBox}>
                <IconButton aria-label="previous"
                            onClick={handleBack}
                            disabled={paused || activeStep === 0}>
                    <FastRewindRounded fontSize="large" htmlColor={mainIconColor} />
                </IconButton>
                <IconButton
                    aria-label={paused ? 'play' : 'pause'}
                    onClick={() => setPaused(!paused)}
                >
                    {paused ? (
                        <PlayArrowRounded
                            sx={{ fontSize: '3rem' }}
                            htmlColor={mainIconColor}
                            onClick={handlePlay}
                        />
                    ) : (
                        <PauseRounded sx={{ fontSize: '3rem' }} htmlColor={mainIconColor} />
                    )}
                </IconButton>
                <IconButton aria-label="next"
                            onClick={handleNext}
                            disabled={paused || activeStep === steps.length-1}>
                    <FastForwardRounded fontSize="large" htmlColor={mainIconColor} />
                </IconButton>
            </Box>
            {!paused && <MobileStepper variant="dots"
                            steps={steps.length}
                            position="static"
                            activeStep={activeStep}
                            className={classes.root}
                           nextButton={''}
                           backButton={''}
            />}
            {speedable && <Slider
                value={speed}
                onChange={handleSpeedChange}
                step={null}
                min={defaultSpeeds[0].value}
                max={defaultSpeeds[defaultSpeeds.length - 1].value}
                marks={defaultSpeeds}
            />}
        </Widget>
    );
}