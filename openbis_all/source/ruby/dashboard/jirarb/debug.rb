
#
# Shows the JSON for an issue -- useful for debugging
#
class DumpIssue < LoggedInCommand
  # Try passing in urls of the form https://jira-bsse.ethz.ch//rest/api/2/issue/BIS-516 as arguments
  def initialize
    super
    @issueUrl = ARGV[1]
  end
  
  def description
    return "dump #{@issueUrl}"
  end
  
  
  def run_logged_in
    if @issueUrl.nil?
      ans =  JiraHelpers.search("project=SP AND fixVersion = S133 ORDER BY \"Global Rank\" ASC")
      @issueUrl = JSON.load(ans)["issues"][0]["self"]
    end

    ans = `curl -s --get --cookie #{$jira_cookie_path} '#{@issueUrl}' --data-urlencode 'expand=transitions'`
    data = JSON.load(ans)
    return JSON.pretty_generate(data)
  end
  
end

#
# Shows the JSON for a search results -- useful for debugging
#
class DumpSearch < LoggedInCommand
  def initialize
    super
    @query = ARGV[1]
    @count = ARGV[2]
  end
  
  def description
    return "search #{@query} #{@count}"
  end
  
  
  def run_logged_in
    @query = "project=SP AND fixVersion = S139 ORDER BY \"Global Rank\" ASC" if @query.nil?
    @count = 1 if @count.nil?

    ans = JiraHelpers.search(@query, @count)
    data = JSON.load(ans)
    return JSON.pretty_generate(data)
  end
  
end