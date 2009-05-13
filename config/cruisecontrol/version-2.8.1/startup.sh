/usr/bin/Xvfb -screen 0 800x600x8 2>&1 >/dev/null &
export PATH=~/bin:$PATH
sh cruisecontrol.sh -webport 8090 -jmxport 8091 2>&1 >/dev/null
