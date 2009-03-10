#!/usr/bin/python

class Settings:
  """Contains settings of whole application"""
  def __init__(self):
    self.smtp_server = 'localhost'
    self.recipients = [ 
            'brinn':    'bernd.rinn@systemsx.ch',
            'charlesr': 'charles.ramin-wright@systemsx.ch',
            'felmer':   'franz-josef.elmer@systemsx.ch',
            'izabel':   'izabela.adamczyk@systemsx.ch',
            'tpylak':   'tomasz.pylak@systemsx.ch',
            'buczekp':	'piotr.buczek@systemsx.ch',
    ]
    self.cc = []
    self.path = '/data/svn/repositories/productive/hooks/code_review'
    self.save_path = '/data/svn/revs/'	
    self.sendmail = True
    self.cr_url_base = 'https://source.systemsx.ch/revs/'
    self.hostname = 'source.systemsx.ch'
    self.line_length = 120
    self.subject_length = 100
    self.subject_prefix = "CISDCS "
    self.__author2email_map = {
            'brinn':    'bernd.rinn@systemsx.ch',
            'charlesr': 'charles.ramin-wright@systemsx.ch',
            'felmer':   'franz-josef.elmer@systemsx.ch',
            'izabel':   'izabela.adamczyk@systemsx.ch',
            'tpylak':   'tomasz.pylak@systemsx.ch',
            'buczekp':	'piotr.buczek@systemsx.ch',
    }

  def get_email_for_author(self, author):
    if self.__author2email_map.has_key(author):
      return self.__author2email_map[author]
    else:
      return 'svn-noreply@systemsx.ch'
