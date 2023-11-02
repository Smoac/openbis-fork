import * as React from 'react';
import { styled, useTheme } from '@material-ui/core/styles';
import Box from '@material-ui/core/Box';
import Slider from '@material-ui/core/Slider';
import IconButton from '@material-ui/core/IconButton';
import PauseRounded from '@material-ui/icons/PauseRounded';
import PlayArrowRounded from '@material-ui/icons/PlayArrowRounded';
import FastForwardRounded from '@material-ui/icons/FastForwardRounded';
import FastRewindRounded from '@material-ui/icons/FastRewindRounded';

/* import SkipPreviousIcon from '@material-ui/icons/SkipPrevious';
import PlayArrowIcon from '@material-ui/icons/PlayArrow';
import SkipNextIcon from '@material-ui/icons/SkipNext'; */

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
        label: 'x1',
    },
    {
        value: 2000,
        label: 'x2',
    },
    {
        value: 5000,
        label: 'x5',
    },
    {
        value: 10000,
        label: 'x10',
    },
];

export default function Player({ speeds = defaultSpeeds, speedable = false }) {
    //{playable: playable, speeds:speeds}
    //const playerConfig = React.useContext(PlayerContext);
    //console.log(playerConfig);
    const theme = useTheme();
    const duration = 200; // seconds
    const [position, setPosition] = React.useState(32);
    const [paused, setPaused] = React.useState(true);
    function formatDuration(value) {
        const minute = Math.floor(value / 60);
        const secondLeft = value - minute * 60;
        return `${minute}:${secondLeft < 10 ? `0${secondLeft}` : secondLeft}`;
    }

    const mainIconColor = theme.palette.mode === 'dark' ? '#fff' : '#000';

    return (
        <Widget>
            <Box
                sx={{
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    mt: -1,
                }}
            >
                <IconButton aria-label="previous">
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
                        />
                    ) : (
                        <PauseRounded sx={{ fontSize: '3rem' }} htmlColor={mainIconColor} />
                    )}
                </IconButton>
                <IconButton aria-label="next">
                    <FastForwardRounded fontSize="large" htmlColor={mainIconColor} />
                </IconButton>
            </Box>
            {/* <Box sx={{ display: 'flex', alignItems: 'center', pl: 1, pb: 1 }}>
				<IconButton aria-label="previous">
					{theme.direction === 'rtl' ? <SkipNextIcon /> : <SkipPreviousIcon />}
				</IconButton>
				<IconButton aria-label="play/pause">
					<PlayArrowIcon sx={{ height: 38, width: 38 }} />
				</IconButton>
				<IconButton aria-label="next">
					{theme.direction === 'rtl' ? <SkipPreviousIcon /> : <SkipNextIcon />}
				</IconButton>
			</Box> */}
            {speedable && <Slider
                aria-label="Speed"
                defaultValue={speeds[0].value}
                sx={{
                    color: 'rgba(0,0,0,0.87)',
                    '& .MuiSlider-track': { border: 'none', },
                    '& .MuiSlider-thumb': {
                        width: 24,
                        height: 24,
                        backgroundColor: '#fff',
                        '&:before': { boxShadow: '0 4px 8px rgba(0,0,0,0.4)' },
                        '&:hover, &.Mui-focusVisible, &.Mui-active': { boxShadow: 'none' },
                    }
                }}
                step={null}
                min={speeds[0].value}
                max={speeds[speeds.length - 1].value}
                marks={speeds}
            />}
        </Widget>
    );
}