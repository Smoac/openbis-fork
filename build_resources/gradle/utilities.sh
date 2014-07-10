SVN_CMD=svn
SVN_MUCC=svnmucc
REPOSITORY_URL="svn+ssh://svncisd.ethz.ch/repos/cisd"

SPRINT_VERSION_REGEX="^(S([0-9]+)(\.([0-9]+))?)?$"

#
# Asserts specified project exists in the repository. Otherwise the script is terminated.
#
assert_valid_project()
{
  local project=$1
  
  if [  $(_path_exists $project) == FALSE ]; then
    _error "Unknown project: $project"
    exit 1
  fi
}

#
# Asserts specified sprint version/tag is valid. Otherwise the script is terminated.
#
# As a side effect BASH_REMATCH[2] and BASH_REMATCH[4] are set with version and patch number, respectively.
#
assert_valid_sprint_version_tag()
{
  local version_tag=$1
  
  if [[ ! $version_tag =~ $SPRINT_VERSION_REGEX ]]; then
    _error "Sprint version/tag is not of form S<version number>[.<patch number>]: $version_tag"
    _error "Valid examples: S23.12, S108, S156.8"
    exit 1
  fi
}

#
# Returns specified version number if it is not an empty string.
# If undefined the latest sprint version of the specified project is read from the repository.
# This version number increased by one will be returned.
#
# This function should be used as follows:
# sprint_version=$(get_sprint_version $project $version_number)
#
# Note: assert_valid_sprint_version_tag has to be invoked before invoking this function.
#
get_sprint_version()
{
  local project=$1
  local version_number=$2

  if [ "$version_number" == "" ]; then
    latest=`_list_branches $project sprint|awk -F. '{print substr($1,2)}'|sort -nr|head -1`
    if [ "$latest" == "" ]; then
      echo 1
    else
      echo $(($latest + 1))
    fi
  else
    echo "$version_number"
  fi
}

#
# Returns specified patch number if it is not an empty string. 
# If undefined the latest tagged sprint version of the specified project is read from the repository.
# This patch number increased by one will be returned. 
#
# This function should be used as follows:
# sprint_version=$(get_sprint_version $project $version_number $patch_number)
#
# Note: assert_valid_sprint_version_tag has to be invoked before invoking this function.
#
get_sprint_patch()
{
  local project=$1
  local version_number=$2
  local patch_number=$3

  if [ "$patch_number" == "" ]; then
    path=$project/tags/sprint/S$version_number.x
    if [ $(_path_exists $path) == TRUE ]; then
      latest=`_list_folder $path|cut -d/ -f1|cut -d. -f2|sort -nr|head -1`
      if [ "$latest" == "" ]; then
        echo 0
      else
        echo $(($latest + 1))
      fi
    else
      echo 0
    fi
  else
    echo $patch_number
  fi
}

#
# Creates specified sprint branch for specified project if it not already exists. 
# Copies main project and all dependent project (determined by file 'settings.gradle' of main project)
# from trunk into the new branch.  
#
create_sprint_branch_if_necessary()
{
  local project=$1
  local sprint_version=$2
  
  local dir_name=S$sprint_version.x
  local path="$project/branches/sprint/$dir_name"
  if [ $(_path_exists $path) == FALSE ]; then
    _start_batch
    _mkdir "$path"
    local projects="$(_list_projects "$project/trunk") $project gradle build_resources"
    for p in $projects; do
      _batch cp "$REPOSITORY_URL/$p/trunk" "$REPOSITORY_URL/$path/$p"
    done
    local log_message="Create sprint branch '$dir_name' for project $project"
    echo $log_message
    _submit_batch "$log_message"
  fi
}

#
# Tags the specified sprint branch of specified project. 
# Creates <project>/tags/sprint/S<sprint_version>.x and copies to this folder
# the branch <project>/branch/sprint/S<sprint_version>.x as tag <sprint_version>.<patch number>
#
copy_sprint_branch_to_tag()
{
  local project=$1
  local sprint_version=$2
  local patch_number=$3
  
  local dir_name=S$sprint_version.x
  local tag_name=S$sprint_version.$patch_number
  local path="$project/tags/sprint/$dir_name"
  if [ $(_path_exists "$path/$tag_name") == TRUE ]; then
    _error "There exists already a sprint tag '$tag_name' for project $project."
    _error "You can use the sprint version 'S$sprint_version' without specifying the patch number."
    _error "The tagging script will find out the next available patch number."
    exit 1
  fi
  _start_batch
  _mkdir "$path"
  _batch cp "$REPOSITORY_URL/$project/branches/sprint/$dir_name" "$REPOSITORY_URL/$path/$tag_name"
  local log_message="Create sprint tag '$tag_name' for project $project"
  echo $log_message
  _submit_batch "$log_message"
}


################################### private functions (only used in this script) ################

_start_batch()
{
  tag_batch_command_count=0
}

_mkdir()
{
  local path=$1
  
  local paths=()
  while [ $(_path_exists $path) == FALSE ]; do
    paths[${#paths[*]}]=$path
    local path=${path%/*}
  done
  for ((i=$((${#paths[*]} - 1)); i >= 0; i--)); do
    _batch mkdir "$REPOSITORY_URL/${paths[$i]}"
  done
}

_batch()
{
  eval "tag_batch_command$tag_batch_command_count=(\"\$@\")"
  tag_batch_command_count=$(($tag_batch_command_count + 1))
}

_submit_batch()
{
  local log_message=$1
  
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
  local cmd=$1
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
  local project=$1
  local branch_type=$2
  
  local path="$project/branches/$branch_type"
  if [ $(_path_exists $path) == TRUE ]; then
    _list_folder "$path"
  fi
}

_path_exists()
{
  local path=$1
  
  _list_folder $path > /dev/null 2>&1
  if [ $? == 0 ]; then
    echo TRUE
  else
    echo FALSE
  fi
}

_list_folder()
{
  local path=$1
  
  "$SVN_CMD" list --non-interactive "$REPOSITORY_URL/$path"
}

_list_projects()
{
  local path=$1
  
  settings_gradle=`"$SVN_CMD" cat --non-interactive "$REPOSITORY_URL/$path/settings.gradle"`
  echo $settings_gradle|awk '/includeFlat/ {line=substr($0,12); while(match(line,",[\t ]*$")) {getline;line=(line $0)}; gsub(",", " ", line); gsub("'\''", "", line); print line}'
}

_svn_with_logmessage()
{
  local log_message=$1
  local cmd=$2
  shift 2
  
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
    _error "Subversion client client '$SVN_CMD' not available"
    _error "Please make Subversion command line client available or set variable SVN_CMD in script '$BASE/utilities.sh' correctly."
    exit 1
  fi
  "$SVN_CMD" info --non-interactive "$REPOSITORY_URL" > /dev/null
  if [ $? != 0 ]; then
    _error "Subversion repository '$REPOSITORY_URL' not available"
    _error "Please check that the variable REPOSITORY_URL in script '$BASE/utilities.sh' is set correctly."
    exit 1
  fi
  "$SVN_MUCC" --help > /dev/null 2>&1
  if [ $? == 0 ]; then
    SVN_MUCC_AVAILABLE=TRUE
    echo "Command '$SVN_MUCC' available."
  else
    SVN_MUCC_AVAILABLE=FALSE
    echo "Command '$SVN_MUCC' not available."
    echo -n "The command 'svnmucc' allows to create and copy several folders in the repository "
    echo "in one transaction leading to only one new revision."
  fi
}

_error()
{
  echo "ERROR> $@" >&2
}

_assert_repository


