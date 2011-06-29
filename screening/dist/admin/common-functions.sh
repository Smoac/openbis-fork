#
# Default openBIS operations like creating a backup or upgrading to a newer version often
# need to be customized by a specific project (e.g. screening). This function provides 
# infrastructure for such functionality. 
#
# The original script (e.g. upgrade.sh) includes a call specifying a filemask e.g.
#
# executeScriptHooks "Executing upgrade post-hook " "$ROOT/servers/*/bin/post-install-*.sh"
#
# Parameters
# $1 - an appropriate log message to identify the nature of the scripts being executed
# $2 - a file mask specifying the scripts to be executed (see the example above)
executeScriptHooks()
{
  if [ "$1" == "" ]; then 
    echo "ERROR: You must specify a log message when calling 'executeScriptHooks'"
    exit 1
  fi
  if [ "$2" == "" ]; then 
    echo "ERROR: You must specify a file mask for scripts to be executed"
    exit 1
  fi
  
  logmessage=$1
  filemask=$2
  
  for hook in $filemask; do
      echo "$logmessage $hook"
      bash $hook
  done
}

#
# Copies a file (first parameter) to a destination (second parameter). 
# Does nothing if file does not exist.
#
copyIfExists() 
{
  if [ -f "$1" ]; then
      cp -R "$1" "$2"
  fi
}