#!/usr/bin/env ruby

# List children of the openBIS documentation
pages = `./listPagesInReleaseDocumentation`
doc_url = 'http://svncisd.ethz.ch/doc/openbis/current/'

count = -1
pages.each_line do | page |
  count = count + 1
  # skip the first one 
  next if count < 1

  source = `./getSourceForReleasePage "#{page.chomp}"`
  puts page if source.include?(doc_url)

end
