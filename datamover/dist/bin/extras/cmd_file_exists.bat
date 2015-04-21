echo off
set MarkerString=.MARKER_is_finished_
set file=%Markerstring%%1
echo %file%

if exist %file% (
    echo markerfile %file% exists
    exit /B 0
) else (
    echo marker file %file% doesn't exist
    exit /B 1
)