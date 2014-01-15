# 
# == Issue Object
#
class Issue
  
  def initialize(data)
    @issue = data
  end
  
  def key
    return @issue["key"]
  end
  
  def fields
    return @issue["fields"]
  end
  
  def implements
    implements = nil
    links = self.fields["issuelinks"]
    unless links.nil?
      links.each { | link | implements = Issue.new(link["outwardIssue"]) if "implements" == link["type"]["outward"] && !link["outwardIssue"].nil? }
    end

    return implements
  end
  
  def implemented_by
    implementedby = []
    links = self.fields["issuelinks"]
    unless links.nil?
      links.each { | link | implementedby << Issue.new(link["inwardIssue"]) if "implements" == link["type"]["outward"] && !link["inwardIssue"].nil?}
    end
    return implementedby
  end
  
  def time
    return 0 if self.fields["timetracking"].nil?
    return self.fields["timetracking"]["remainingEstimateSeconds"] ? self.fields["timetracking"]["remainingEstimateSeconds"] : 0
  end
  
  def time_spent
    # The result is preformatted by JIRA
    return "0h" if self.fields["timetracking"].nil?
    return self.fields["timetracking"]["timeSpent"] ? self.fields["timetracking"]["timeSpent"] : "0h"
  end
  
  def worklogs
    return [] if self.fields["worklog"].nil?
    return self.fields["worklog"]["worklogs"]
  end  
  
  def status
    return self.fields["status"] ? self.fields["status"]["name"] : nil
  end
  
  def tester
    return self.fields["customfield_10250"] ? self.fields["customfield_10250"]["name"] : nil
  end
  
  def summary
    return self.fields["summary"]
  end
  
  def assignee
    return self.fields["assignee"] ? self.fields["assignee"]["name"] : nil
  end
  
  def fix_version
    fix_versions = self.fields["fixVersions"]
    return "Unscheduled" if fix_versions.nil?
    return "" if fix_versions[0].nil?
    return fix_versions[0]["name"]
  end
  
  def fix_versions
    fix_versions = self.fields["fixVersions"]
    return "Unscheduled" if fix_versions.nil?
    return fix_versions.collect { | each | each["name"] }.join(" ")
  end
  
  def transitions
    @issue["transitions"]
  end
  
  def next_sprint
    self.fields["customfield_10550"]
  end
  
  def resolved_or_closed?
    status = self.status
    (status == "Resolved" || status == "Closed")
  end
  
  def beneficiaries
    beneficiaries = self.fields["customfield_10040"]
    if beneficiaries.nil?
      return ["openBIS"] if self.key.match(/^BIS-/)
      return ["BSSE YeastLab"] if self.key.match(/^YSC-/)
      return ["Infrastructure"] if self.key.match(/^SWE-/)
      return ["Unknown"] 
    end
    return beneficiaries.collect { | b | b["value"] }
  end
end


#
# A module that implements some helpful operations
#
module JiraHelpers

  def JiraHelpers.search(query, limit=nil)
    search_cmd = "curl -s --get --cookie #{$jira_cookie_path} '#{$jira_api_url}/search' --data-urlencode 'os_authType=cookie' --data-urlencode 'jql=#{query}' --data-urlencode 'fields=*all,-comment'"
    search_cmd = search_cmd + " --data-urlencode 'maxResults=#{limit}'" unless limit.nil?
    return `#{search_cmd}`
  end
  
  def JiraHelpers.issue(issue_number)
    issue_cmd = "curl -s --get --cookie #{$jira_cookie_path} '#{$jira_api_url}/issue/#{issue_number}' --data-urlencode 'os_authType=cookie' --data-urlencode 'expand=transitions'"
    data = `#{issue_cmd}`
    issue_data = JSON.load(data)
    return Issue.new(issue_data)    
  end  
  
  def JiraHelpers.rank(issue, after, before=nil)
    rank_data = {"issueKeys" => [issue], "customFieldId" => 10050 }
    rank_data["rankAfterKey"] = after
    rank_data["rankBeforeKey"] = before unless before.nil?    
    rank_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' -X PUT '#{$jira_url}/rest/greenhopper/1.0/rank' -d '#{JSON.generate(rank_data)}'"
    return `#{rank_cmd}`
  end

  def JiraHelpers.versions(project)
    versions_cmd = "curl -s --get --cookie #{$jira_cookie_path} '#{$jira_api_url}/project/#{project}/versions' --data-urlencode 'os_authType=cookie' --data-urlencode 'expand'"
    ans = `#{versions_cmd}`
    return JSON.load(ans)
  end 
  
  def JiraHelpers.create_sp_issue(summary, description, fixversion)
    # May need to escape the summary
    issue_data = {
      "fields" => { 
        "project" => {"key" => "SP" } , 
        "summary" => summary, 
        "description" => description, 
        "fixVersions" => [ {"name" => fixversion } ],
        "issuetype" => {"name" => "Task"} 
      } 
    }
    issue_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issue/' -d '#{JSON.generate(issue_data)}'"
    ans = `#{issue_cmd}`
    return JSON.load(ans)
  end
  
  def JiraHelpers.link_sp_to_bis(sp_key, bis_key)
    link_data = {
      "type" => { "name" => "Hierarchy" },
      "inwardIssue" => { "key" => sp_key },
      "outwardIssue" => { "key" => bis_key }
    }
    link_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issueLink' -d '#{JSON.generate(link_data)}'"
    return `#{link_cmd}`
  end
  
  def JiraHelpers.remove_next_sprint(bis_key)
    update_data = {
      "fields" => {
        "customfield_10550" => nil
      }
    }
    update_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issue/#{bis_key}' -X PUT -d '#{JSON.generate(update_data)}'"
    return `#{update_cmd}`
  end
  
  def JiraHelpers.set_next_sprint(bis_key)
    update_data = {
      "fields" => {
        "customfield_10550" => [
            {"value"=>"Yes", "id"=>"10240", "self"=>"https://jira-bsse.ethz.ch/rest/api/2/customFieldOption/10240"}
          ]
      }
    }
    update_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issue/#{bis_key}' -X PUT -d '#{JSON.generate(update_data)}'"
    return `#{update_cmd}`
  end
  
  def JiraHelpers.set_fix_version(bis_key, fixversion)
    update_data = { "fields" => { "fixVersions" => [ {"name" => fixversion } ] } }
    update_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issue/#{bis_key}' -X PUT -d '#{JSON.generate(update_data)}'"
    return `#{update_cmd}`
  end
  
  def JiraHelpers.add_fix_version(issue, fixversion)
    fix_versions = issue.fields["fixVersions"]
    fix_versions = [] unless fix_versions
    fix_versions.push({"name" => fixversion })
    update_data = { "fields" => { "fixVersions" => fix_versions } }
    update_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issue/#{issue.key}' -X PUT -d '#{JSON.generate(update_data)}'"
    return `#{update_cmd}`
  end
  
  def JiraHelpers.transition(bis_key, trans_id)
    transition_data = {
      "transition" => { "id" => "#{trans_id}"}
    }
    transition_cmd = "curl -s --cookie #{$jira_cookie_path} -H 'Content-Type: application/json' '#{$jira_api_url}/issue/#{bis_key}/transitions' -d '#{JSON.generate(transition_data)}'"
    return `#{transition_cmd}`
  end

  # Search and return the full data for the found objects
  def JiraHelpers.search_full(query, silent, limit=nil)
    print "Retrieving issues" unless silent
    ans = JiraHelpers.search(query, limit)
    data = JSON.load(ans)
    
    full_issues_data = data["issues"]
    full_issues = full_issues_data.collect { | issue_data | Issue.new(issue_data) }
    print "\n" unless silent   
    return full_issues
  end
  
  # Get the full data for entries that implement {issues}.
  def JiraHelpers.retrieve_implementors(issues, silent)
    print "Retrieving implementing issues" unless silent
    implementors = []
    issues.each do | issue |
      print "." unless silent
      implementors.concat issue.implemented_by
    end

    print "\n" unless silent   
    return implementors
  end
  
  # Get the full data for entries that implement {issues}.
  def JiraHelpers.retrieve_full_implements(issues, silent, limit=nil)
    issues_with_implementor = issues.reject { | issue | issue.implements == nil }
    implementors = issues_with_implementor.collect { | issue | issue.implements }
    implementor_keys = implementors.flatten.collect { | issue | "\"" + issue.key + "\"" }
    parent_keys = implementor_keys.join(",")
    searchq = "issuekey in (#{parent_keys})"
    return JiraHelpers.search_full(searchq, silent, limit)
  end
  
  def JiraHelpers.session_valid_raw
    # Just get the response code, don't care about the rest
    ans = `curl -s -w %{http_code} -o /dev/null --head --get --cookie #{$jira_cookie_path} --data-urlencode 'os_authType=cookie' '#{$jira_url}/rest/auth/1/session'`
    return ans
  end  
  
  def JiraHelpers.session_valid?
    # Just get the response code, don't care about the rest
    ans = JiraHelpers.session_valid_raw
    return false if $?.to_i != 0
    return ans == "200"
  end
end

#
# A module that simplifies interacting with git
module GitHelpers
  def GitHelpers.list_commits(key)
    return `git cisd log --grep="#{key}" | sed -n '/git-svn-id/p' | sed -E 's/.*trunk@([0-9]+).*/r\\1/'`
  end
end

#
# A module that implements some helpful operations
#
module InputHelpers
  def InputHelpers.args(first_arg)
    cmd = ARGV[first_arg .. -1].inject("") { | all, each | all + " " + each }
    cmd.strip!
    return cmd
  end
  
  def InputHelpers.sprint_name(sprint_name_or_number)
    sprintNumber = "S" if sprint_name_or_number.nil?
    sprintNumber = sprint_name_or_number.match("^[S|s].*") ? sprint_name_or_number : "S" + sprint_name_or_number
    return sprintNumber
  end

  def InputHelpers.sprint_names(sprint_names_or_numbers)
    if sprint_names_or_numbers.include? '..'
      sprints = sprint_names_or_numbers.split('..')
      input = Range.new(sprints[0], sprints[1])
    else
      input = sprint_names_or_numbers.split(',').collect { | each | each.strip() }
    end
    return input.collect { | each | InputHelpers.sprint_name(each) }
  end
    
end
  
#
# == Commands
#

#
# The abstract superclass of commands
# 
class JiraCommand
  
  attr_accessor :silent
  
  def initialize
    @silent = true
  end
  
  # Return a description of the command to run
  def description
    return nil
  end
  
  # Run the command and return the result
  def run
    return nil
  end

  # Return true if the result should be printed. 
  #
  # Default: print if the result is not empty.
  def should_print_result(result)
    return !result.empty? 
  end

  # helper method to print time in a formatted way
  def ptime(time)
    time == 0 ? "" : "%.1fh" % (time / 3600.0)
  end

end

#
# The login command
#
class Login < JiraCommand
 
  def initialize
    super
    print "Enter jira login (e.g. alincoln): "
    @jira_user = $stdin.gets().strip
  end
 
  def description
    return "login"
  end
  
  def run
    # The url portion for logging in
    # may also want to try curl -c cookie_jar -H "Content-Type: application/json" -d '{"username" : "admin", "password" : "admin"}' #{$jira_url}/jira/rest/auth/latest/session
    jira_login = 'secure/Dashboard.jspa?os_authType=basic'
    Dir.mkdir($jira_prefs_path) unless File.exists?($jira_prefs_path)
    return `curl --head -s -u #{@jira_user} --cookie-jar #{$jira_cookie_path} '#{$jira_url}/#{jira_login}'`
  end
end

#
# Check if the session is valid
#
class SessionValid < JiraCommand
 
  def initialize
    super
  end
 
  def description
    return "session"
  end
  
  def run
    return JiraHelpers.session_valid_raw
  end
end

#
# Lists the issues in the sprint
#
class LoggedInCommand < JiraCommand
  def run
    Login.new.run unless File.exists?($jira_cookie_path)
    Login.new.run unless JiraHelpers.session_valid?
    return self.run_logged_in
  end

  # For subclasses to implement
  def run_logged_in
    return ""
  end
end
