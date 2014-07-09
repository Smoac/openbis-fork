SVN_CMD=svn
SVN_MUCC=svnmucc
REPOSITORY_URL="svn+ssh://svncisd.ethz.ch/repos/cisd"

SPRINT_VERSION_REGEX="^(S([0-9]+)(\.([0-9]+))?)?$"

#
# Extracts the sprint version from specified version/tag. If undefined the sprint version of the specified
# project is read from the repository. This version number increased by one will be returned. 
#
# This function should be used as follows:
# sprint_version=$(get_sprint_version $project $version_tag)
#
get_sprint_version()
{
  project=$1
  version_tag=$2
  if [ "$version_tag" == "" ]; then
    latest=`_list_branches $project sprint|awk -F. '{print substr($1,2)}'|sort -nr|head -1`
    if [ "$latest" == "" ]; then
      echo 1
    else
      echo $(($latest + 1))
    fi
  else
    if [[ $version_tag =~ $SPRINT_VERSION_REGEX ]]; then
      echo "${BASH_REMATCH[2]}"
    else
      echo "Sprint version not of form S<version number>[.<patch number>]: $version_tag"
      echo "Examples of valid sprint versions: S108, S156.8"
      exit 1
    fi
  fi
}

#
# Returns TRUE if the specified sprint branch for the specified project already exists.
#
# This function should be used as follows:
#
# if [ $(sprint_branch_exists $project $sprint_version) == FALSE ]; then echo create branch; fi
#
sprint_branch_exists()
{
  project=$1
  sprint_version=$2
  if [[ `_list_branches $project sprint` =~ S$sprint_version.x ]]; then
    echo TRUE
  else
    echo FALSE
  fi
}

#
# Creates specified sprint branch for specified project if it not already exists. 
# Copies main project and all dependent project (determined by file 'settings.gradle' of main project)
# from trunk into the new branch.  
#
create_sprint_branch_if_necessary()
{
  project=$1
  sprint_version=$2
  dir_name=S$sprint_version.x
  if [[ ! `_list_branches $project sprint` =~ $dir_name ]]; then
    _start_batch
    _batch mkdir "$REPOSITORY_URL/$project/branches/sprint/$dir_name"
    projects="$(_list_projects "$project/trunk") $project gradle build_resources"
    for p in $projects; do
      _batch cp "$REPOSITORY_URL/$p/trunk" "$REPOSITORY_URL/$project/branches/sprint/$dir_name/$p"
    done
    log_message="Create sprint branch '$dir_name' for project $project"
    echo $log_message
    _submit_batch "$log_message"
  fi
}

################################### private functions (only used in this script) ################

_start_batch()
{
  tag_batch_command_count=0
}

_batch()
{
  eval "tag_batch_command$tag_batch_command_count=(\"\$@\")"
  tag_batch_command_count=$(($tag_batch_command_count + 1))
}

_submit_batch()
{
  log_message=$1
  if [ $SVN_MUCC_AVAILABLE == TRUE ]; then
    tag_batch_command_mucc=(--message "$log_message")
    for ((i=0; i < $tag_batch_command_count; i++)); do
      eval "_add_mucc_command_item \"\${tag_batch_command$i[@]}\""
    done
    _echo "$SVN_MUCC" "${tag_batch_command_mucc[@]}"
    "$SVN_MUCC" "${tag_batch_command_mucc[@]}"
  else
    for ((i=0; i < $tag_batch_command_count; i++)); do
      eval "_svn_with_logmessage \"\$log_message\" \"\${tag_batch_command$i[@]}\""
    done
  fi
}

_add_mucc_command_item()
{
  cmd=$1
  shift
  tag_batch_command_mucc[${#tag_batch_command_mucc[*]}]=$cmd
  if [[ $cmd == "cp" ]]; then
    tag_batch_command_mucc[${#tag_batch_command_mucc[*]}]="HEAD"
  fi
  for item in "$@"; do
    tag_batch_command_mucc[${#tag_batch_command_mucc[*]}]="$item"
  done
}

_list_branches() 
{
  project=$1
  branch_type=$2
  "$SVN_CMD" list --non-interactive "$REPOSITORY_URL/$project/branches/$branch_type"
}

_list_projects()
{
  path=$1
  settings_gradle=`"$SVN_CMD" cat --non-interactive "$REPOSITORY_URL/$path/settings.gradle"`
  echo $settings_gradle|awk '/includeFlat/ {line=substr($0,12); while(match(line,",[\t ]*$")) {getline;line=(line $0)}; gsub(",", " ", line); gsub("'\''", "", line); print line}'
}

_svn_with_logmessage()
{
  log_message=$1
  shift
  cmd=$1
  shift
  _svn_with_echo $cmd --message "$log_message" "$@"
}

_svn_with_echo()
{
  _echo "$SVN_CMD" --non-interactive "$@"
  "$SVN_CMD" --non-interactive "$@"
}

_echo()
{
  for item in "$@"; do
    echo -n "\"$item\" "
  done
  echo
}

_assert_repository()
{
  "$SVN_CMD" -q --non-interactive --version > /dev/null
  if [ $? != 0 ]; then
    echo "Subversion client client '$SVN_CMD' not available"
    echo "Please make Subversion command line client available or set variable \$SVN_CMD in script 'utilities.sh' correctly."
    exit 1
  fi
  "$SVN_CMD" info --non-interactive "$REPOSITORY_URL" > /dev/null
  if [ $? != 0 ]; then
    echo "Subversion repository '$REPOSITORY_URL' not available"
    echo "Please check that the variable \$REPOSITORY_URL in script 'utilities.sh' is set correctly."
    exit 1
  fi
  "$SVN_MUCC" --version > /dev/null 2>&1
  if [ $? == 0 ]; then
    SVN_MUCC_AVAILABLE=TRUE
    echo "Command '$SVN_MUCC' available"
  else
    SVN_MUCC_AVAILABLE=FALSE
    echo "Command '$SVN_MUCC' not available"
  fi
}

_assert_repository


