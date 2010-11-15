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
 *
 * System program to determine the last changed time of any item below a given directory.
 *
 * Compile with:
 * gcc -O3 -Wall lastchanged.c -o lastchanged
 */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <unistd.h>
#include <dirent.h>
#include <time.h>
#include <dirent.h>
#include <sys/types.h>
#include <sys/stat.h>

#define MAXNAMELEN 256
#define BASE 10


void print_time_and_exit(time_t time)
{
   if (sizeof(time_t) == 4) /* 32 bit */
   {
      printf("%d\n", (int) time);
   } else                   /* 64 bit */
   {
      printf("%lld\n", (long long int) time);
   }
   exit(0);
}

time_t get_time()
{
   time_t now;
   now = time(NULL);
   if (now < 0)
   {
      fprintf(stderr, "Error getting current time\n");
      exit(3);
   }
   return now;
}

void check_young_enough(time_t *youngest, time_t *stop_when_younger, time_t *stop_when_younger_relative)
{
   if (*stop_when_younger > 0 && *youngest > *stop_when_younger)
   {
      if (*stop_when_younger_relative > 0)
      {
         *stop_when_younger = get_time() - *stop_when_younger_relative;
         if (*youngest > *stop_when_younger)
         {
            print_time_and_exit(*youngest);
         }
      } else
      {
         print_time_and_exit(*youngest);
      }
   }
}

time_t last_changed(const char *file, time_t *youngest, time_t *stop_when_younger, time_t *stop_when_younger_relative)
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
   if (st.st_mtime > *youngest)
   {
      *youngest = st.st_mtime;
      check_young_enough(youngest, stop_when_younger, stop_when_younger_relative);
   }
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
         while ((ep = readdir(dp)))
         {
            if (strcmp(ep->d_name, ".") != 0 && strcmp(ep->d_name, "..") != 0)
            {
               fname[dirname_len] = '\0';
               strncat(fname, ep->d_name, MAXNAMELEN);
               *youngest = last_changed(fname, youngest, stop_when_younger, stop_when_younger_relative);
            }
         }
         closedir(dp);
      }
    }
    return *youngest;
}

time_t parse_seconds(char *number_str)
{
   char *endptr;
   long long number = strtoll(number_str, &endptr, BASE);
   if (*endptr != '\0' || number < 0)
   {
      fprintf(stderr, "Illegal argument for time in seconds, expected non-negative integer, found '%s'\n", number_str);
      exit(1);
   }
   time_t time_secs = (time_t) number;
   if (time_secs != number)
   {
      fprintf(stderr, "Integer overflow, %lld out of range.\n", number);
      exit(1);
   }
   return time_secs;
}

int main(int argc, char *argv[])
{
   if (argc != 2 && argc != 3)
   {
       fprintf(stderr, "Syntax: lastchanged <dir> [[r]<stopWhenYoungerSeconds>]\n");
       return 1;
   }
   const char *dir = argv[1];
   time_t stop_when_younger = 0;
   time_t stop_when_younger_relative = 0;
   if (argc == 3)
   {
      if (*argv[2] == 'r')
      {
         stop_when_younger_relative = parse_seconds(argv[2]+1);
         stop_when_younger = get_time() - stop_when_younger_relative;
      } else
      {
         stop_when_younger = parse_seconds(argv[2]);
      }
   }
   time_t youngest = 0;
   print_time_and_exit(last_changed(dir, &youngest, &stop_when_younger, &stop_when_younger_relative));
   
   return 0; /* not reached */
}
