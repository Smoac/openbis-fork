#!/bin/bash
# Shows openBIS server usage statistics done by users outside CISD

cat ~openbis/sprint/openBIS-server/jetty/logs/*usage_log.txt* | egrep -v "tpylak|brinn|etlserver|kohleman|izabelaa|buczekp|felmer|hclaus|cramakri|baucha|ryanj" | cut -d" " -f1,8,12 | sort | uniq -c