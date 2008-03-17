#!/usr/bin/python
import sys
import syslog

import revision
import codereview
import settings

def genNextPrevLinks(change):
  return "<a href=\"change" + str(int(change) -1) +".html\">prev</a>" + \
      "&nbsp;&nbsp;<a href=\"change" + str(int(change) + 1) + ".html\">next</a>"

if __name__ == "__main__":
  syslog.syslog('Starting submission' + str(sys.argv))
  prefs = settings.Settings()
  diff = revision.Revision(sys.argv[1], sys.argv[2], prefs.line_length, prefs.subject_length)
  # Yeah, this syntax freaks me out.
  # TODO(marcin): move to templates. 
  output_html = \
      "<html><head><title> %s </title>" % diff.getDescriptionSnippet()
  output_html += """
<style>
.diff {
  font-size: 70%;
  font-family: sans-serif;
}
.diff_add {
  background: #7CF575;
}
.diff_sub {
  background: #EB6D5B;
}
.diff_chg {
  background: #B2B0E3;
}
</style>
"""
  output_html += "<body>";
  output_html += genNextPrevLinks(sys.argv[2])
  output_html += "<pre>\n" + diff.getDescription() + "</pre>"
  output_html += diff.getDiffsForAllFiles()
  output_html += "</body></html>";

  output_file_name = prefs.save_path + '/change' + sys.argv[2] + '.html'
  output_file_name = output_file_name.replace('//','/')
  output_file = open(output_file_name, 'w')
  output_file.write(output_html)
  syslog.syslog('File saved' + output_file_name)

  notification = codereview.CodeReview(prefs, prefs.get_email_for_author(diff.getAuthor()), prefs.subject_prefix)
  notification.setSubjectInfo(diff.getDescriptionSnippet())
  notification.attachText("New change for review is waiting for you!\n" +
      prefs.cr_url_base + "/change" + sys.argv[2] + ".html  \n\n" + diff.getDescription())
  notification.send(diff.getAuthor())
  syslog.syslog('Email sent. Exiting successfully.')
