update images as img set path = (select images_directory
                                 from data_sets as d 
                                   join channel_stacks as cs on cs.ds_id = d.id
                                   join acquired_images as ai on ai.channel_stack_id = cs.id
                                   join images as i on ai.img_id = i.id 
                                 where i.id = img.id)
                                || '/' || path where path not like '%:%';
                                
alter table data_sets drop column images_directory;
