"""Take a directory of JPEG images and register them for all HT_PROBE samples, cycling through the images in the directory."""

from ch.systemsx.cisd.openbis.generic.shared.api.v1.dto import SearchCriteria
import os


def process(tr):
  search_service = tr.getSearchService()
  sc = SearchCriteria()
  sc.addMatchClause(SearchCriteria.MatchClause.createAttributeMatch(SearchCriteria.MatchClauseAttribute.TYPE, '5HT_PROBE'))
  five_ht_samps = search_service.searchForSamples(sc)
  five_ht_exp = get_or_create_experiment(tr)
  data_set = create_image_data_set(tr, five_ht_exp)

  incoming_folder = tr.getIncoming().getPath()
  incoming_filenames = os.listdir(incoming_folder)
  count = len(incoming_filenames)
  i = 0
  # match samples to images, cycling through the images
  for samp in five_ht_samps:
    samp = tr.makeSampleMutable(samp)
    samp.setExperiment(five_ht_exp)
    index = i % count
    i = i + 1
    add_image_to_folder(tr, samp, incoming_folder, incoming_filenames[index])

  tr.moveFile(incoming_folder, data_set, "images/")

def get_or_create_experiment(tr):
  exp = tr.getExperiment("/PROBES/PROBE/5HT-EXP")
  if exp:
    return exp

  proj = tr.createNewProject("/PROBES/PROBE")
  proj.setDescription("Project for speculative 5HT experiments")

  exp = tr.createNewExperiment("/PROBES/PROBE/5HT-EXP", "5HT_EXP")
  return exp

def create_image_data_set(tr, exp):
  ds = tr.createNewDataSet('5HT_IMAGE')
  ds.setExperiment(exp)
  return ds

def add_image_to_folder(tr, samp, folder, filename):
  new_filename = samp.getCode() + ".png"
  linked_path = os.path.join(folder, new_filename)
  os.link(os.path.join(folder, filename), linked_path)
