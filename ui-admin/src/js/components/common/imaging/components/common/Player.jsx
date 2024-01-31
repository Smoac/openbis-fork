import * as React from 'react';
import {createTheme, makeStyles, styled, useTheme} from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Slider from '@material-ui/core/Slider';
import IconButton from '@material-ui/core/IconButton';
import PauseRounded from '@material-ui/icons/PauseRounded';
import PlayArrowRounded from '@material-ui/icons/PlayArrowRounded';
import FastForwardRounded from '@material-ui/icons/FastForwardRounded';
import FastRewindRounded from '@material-ui/icons/FastRewindRounded';
import MobileStepper from "@material-ui/core/MobileStepper";
import {ThemeProvider} from "@material-ui/core";

const themeDisabled = createTheme({
    overrides: {
        // Style sheet name ⚛️
        MuiIconButton: {
            // Name of the rule
            root: {
                // Some CSS
                color: 'rgba(0, 0, 0, 1)'
            }
        },
    },
});

const themeSlider = createTheme({
   overrides: {
       MuiSlider: {
           thumb: {
               width: '8px',
               height: '8px',
               marginTop: '-3px',
           },
           markLabel: {
               top: '20px',
               fontSize: '0.6rem',
           },
           marked:{
               marginBottom: "unset"
           }
       }
   }
});

const Widget = styled('div')(() => ({
    padding: 10,
    borderRadius: 16,
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
    },
    root: {
        justifyContent: 'center',
        padding: 'unset',
        background: 'unset'
    }
});
export default function Player({ label= 'DEFAULT', onStep, steps = [], speeds = defaultSpeeds, speedable = false }) {
    const classes = useStyles();
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
            //console.log(`RUN activeStep=${activeStep} value=${steps[activeStep]}`);
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

    const handleNext = () => {
        setActiveStep((prevActiveStep) => prevActiveStep + 1);
    };

    const handleBack = () => {
        setActiveStep((prevActiveStep) => prevActiveStep - 1);
    };

    const handlePlay = () => {
        setPaused(!paused);
        if (paused) {
            resetTimeout();
            timeoutRef.current = setTimeout(
                () =>
                    setActiveStep((prevIndex) =>
                        prevIndex === steps.length - 1 ? 0 : prevIndex + 1
                    ),
                2000
            );
        }
        return () => {
            resetTimeout();
        };
    }

    return (
        <Widget>
            <Box className={classes.rootBox}>
                <ThemeProvider theme={themeDisabled}>
                    <IconButton aria-label="previous"
                                onClick={handleBack}
                                disabled={paused || activeStep <= 0}
                                size="small"
                    >
                        <FastRewindRounded />
                    </IconButton>
                    <IconButton aria-label={paused ? 'play' : 'pause'}
                                onClick={handlePlay}
                                size="small"
                    >
                        {paused ? <PlayArrowRounded /> : <PauseRounded />}
                    </IconButton>
                    <IconButton aria-label="next"
                                onClick={handleNext}
                                disabled={paused || activeStep === steps.length-1}
                                size="small"
                    >
                        <FastForwardRounded />
                    </IconButton>
                </ThemeProvider>
            </Box>
            {!paused && <MobileStepper variant="text"
                                       steps={steps.length}
                                       position="static"
                                       activeStep={activeStep}
                                       className={classes.root}
            />}
            <ThemeProvider theme={themeSlider}>
                {speedable && <Slider
                    className={classes.thumb}
                    value={speed}
                    color="primary"
                    onChange={handleSpeedChange}
                    step={null}
                    min={defaultSpeeds[0].value}
                    max={defaultSpeeds[defaultSpeeds.length - 1].value}
                    marks={defaultSpeeds}
                />}
            </ThemeProvider>
        </Widget>
    );
}