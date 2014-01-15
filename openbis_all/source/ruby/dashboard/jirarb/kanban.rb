
#
# Lists the issues in the kanban cycle by combining information from the BIS project and SP project
#
class ListKanban1304 < LoggedInCommand
  def initialize
    super
    @sprintNumber = InputHelpers.sprint_name(ARGV[1])
    @restrict_to_release = true
  end
  
  def description
    return "kanban1304 #{@sprintNumber}"
  end
  
  
  def run_logged_in
    sp_query = "project=SP AND fixVersion = #{@sprintNumber} ORDER BY \"Global Rank\" ASC" 
    sp_issues = JiraHelpers.search_full(sp_query, @silent)
    init_sp_issue_dict(sp_issues)
    
    bis_query= "project=BIS AND status not in (Resolved, Closed) OR (status in (Resolved, Closed) AND fixVersion = #{@sprintNumber}) ORDER BY \"Global Rank\" ASC"
    bis_query= "project=BIS AND labels in (\"12.xx_REL\", \"13.04.X\") AND status not in (Resolved, Closed) OR (status in (Resolved, Closed) AND fixVersion = #{@sprintNumber}) ORDER BY \"Global Rank\" ASC" if @restrict_to_release
    bis_issues = JiraHelpers.search_full(bis_query, @silent)
    enrich_sp_issue_dict(JiraHelpers.retrieve_implementors(bis_issues, @silent))
    
    print_issues_table("BIS", bis_issues)
    print_unseen_sp_issues_table(sp_issues)
    
    puts ("=" * 12)

    # Nothing to show
    issue_count = bis_issues.length
    return "#{issue_count} issues"
  end
  
  def init_sp_issue_dict(sp_issues)
    @sp_issue_dict = {}
    sp_issues.each do | issue |
      key = issue.key
      @sp_issue_dict[key] = issue
    end
    @seen_sp_issues = [].to_set
  end
  
  #
  # Take those issues that are not yet resolved / closed and add them to the sp issues dict
  #
  def enrich_sp_issue_dict(implementors)
    implementors.each do | issue |
      sp = issue.key
      next if @sp_issue_dict[sp]
      
      status = issue.status
      @sp_issue_dict[sp] = issue unless issue.resolved_or_closed?
    end 
  end
  
  def print_unseen_sp_issues_table(full_issues)
    unseen_issues = full_issues.select do | issue |
      sp = issue.key
      !@seen_sp_issues.include?(sp)
    end
    
    return if unseen_issues.length < 1
    
    puts ("=" * 12)
    puts "SP Missed"
    puts ("-" * 12)
    header = "%12s\t%12s\t%6s\t%8s\t%8s\t%8s\t%s" % ["Key", "SP", "Time", "Devel", "Tester", "Status", "Summary"]
    puts header
    subtotal = 0.0
    unseen_issues.each do | issue |
      sp = issue.key
      
      key = "----"
      key = issue.implements.key unless issue.implements.nil?
      time_spent = issue.time_spent
      assignee = issue.assignee
      tester = issue.tester
      summary = issue.summary
      status = issue.status
      # Tasks that are resolved can be considered to have 0 time remaining
      status = issue.status
      row = "%12s\t%12s\t%6s\t%8s\t%8s\t%8s\t%s" % [key, sp, time_spent, assignee, tester, status, summary]
      puts row
    end
  end
  
  def print_issues_table(title, full_issues)
    return if full_issues.length < 1
    
    puts ("=" * 12)
    puts title
    puts ("-" * 12)
    header = "%12s\t%12s\t%6s\t%8s\t%8s\t%8s\t%s" % ["Key", "SP", "Time", "Devel", "Tester", "Status", "Summary"]
    puts header
    subtotal = 0.0
    full_issues.each do | issue |
      key = issue.key
      summary = issue.summary
      parent = issue.fields["parent"]
      parent = parent["key"] unless parent.nil?
      summary = "#{parent} / #{summary}" unless parent.nil?
            
      implementedby = []
      issue.implemented_by.each do | sp_issue |
        sp = sp_issue.key
        # We are only interested in links to issues in the specified sprint
        implementedby << sp if @sp_issue_dict[sp]
      end
      
      if implementedby.length < 1
        row = "%12s\t%12s\t%6s\t%8s\t%8s\t%8s\t%s" % [key, "", "", "", "", "", "[Unscheduled] #{summary}"]
        puts row
        next
      end
      
      implementedby.each_with_index do | sp, index |
        # print one row for each implemented by
        spissue = @sp_issue_dict[sp]
        next if spissue.nil?
        next if @seen_sp_issues.include?(sp)
        
        spfields = spissue.fields
        time_spent = spissue.time_spent
        assignee = spissue.assignee
        tester = spissue.tester

        # Tasks that are resolved can be considered to have 0 time remaining
        fix_version = spissue.fix_version
        issue_in_different_sprint = fix_version != @sprintNumber
        status = spissue.status
        if index < 1 
          issue_summary = summary
          issue_summary =  "[#{fix_version}] #{issue_summary}" if issue_in_different_sprint
          row = "%12s\t%12s\t%6s\t%8s\t%8s\t%8s\t%s" % [key, sp, time_spent, assignee, tester, status, issue_summary]
        else
          issue_summary = "\""
          issue_summary =  "[#{fix_version}] #{issue_summary}" if issue_in_different_sprint
          row = "%12s\t%12s\t%6s\t%8s\t%8s\t%8s\t%s" % ["\"", sp, time_spent, assignee, tester, status, issue_summary]
        end
        puts row
        @seen_sp_issues.add(sp)
      end
    end
  end
end

#
# Lists the issues in the kanban cycle by referring only to the BIS project
#
class ListKanbanOld < LoggedInCommand
  def initialize
    super
    @sprintNumber = InputHelpers.sprint_name(ARGV[1])
  end
  
  def description
    return "kanban #{@sprintNumber}"
  end
  
  
  def run_logged_in
    query = "project=BIS AND status not in (Resolved, Closed) OR (status = Resolved AND fixVersion = #{@sprintNumber}) ORDER BY \"Global Rank\" ASC" 
    full_issues = JiraHelpers.search_full(query, @silent)
    self.print_issues_table(full_issues)
    # Nothing to show
    return "#{full_issues.length} issues"
  end
  
  def print_issues_table(full_issues)
    header = "%8s  %12s\t%12s\t%8s\t%s" % ["Key", "Implements", "Status", "Tester", "Summary"]
    puts header
    full_issues.each do | issue |
      key = issue.key
      implements_key = issue.implements.key unless issue.implements.nil?
      status = issue.status
      tester = issue.tester
      summary = issue.summary
      row = "%8s  %12s\t%12s\t%8s\t%s" % [key, implements_key, status, tester, summary]
      puts row
    end
    print " ", ("-" * 27), "\n"
  end
end