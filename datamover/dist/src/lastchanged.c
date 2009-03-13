/*
 * Copyright 2009 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/dir.h>

#define MAXNAMELEN 256
       
time_t last_changed(const char *file, time_t latest)
{
   struct stat st;
   int retval;
   
   retval = lstat(file, &st);
   if (retval < 0)
   {
      fprintf(stderr, "Cannot stat() file %s: ", file);
      perror(NULL);
      exit(2);
   }
   latest = (st.st_mtime > latest) ? st.st_mtime : latest;
   if (S_ISDIR(st.st_mode))
   {
      int dirname_len = strlen(file);
      int fname_len = dirname_len + MAXNAMELEN + 2;
      DIR *dp;
      struct dirent *ep;

      char fname[fname_len];
      strcpy(fname, file);
      if (file[dirname_len - 1] != '/')
      {
         strcat(fname, "/");
         ++dirname_len;
      }

      dp = opendir(file);
      if (dp != NULL)
      {
         while (ep = readdir(dp))
         {
            if (strcmp(ep->d_name, ".") != 0 && strcmp(ep->d_name, "..") != 0)
            {
               fname[dirname_len] = '\0';
               strncat(fname, ep->d_name, MAXNAMELEN);
               latest = last_changed(fname, latest);
            }
         }
         closedir(dp);
      }
    }
    return latest;
}

int main(int argc, char *argv[])
{
   if (argc != 2)
   {
       fprintf(stderr, "Syntax: lastchanged <dir>\n");
       return 1;
   }
   printf("%d\n", last_changed(argv[1], 0));
   
   return 0;
}
