@echo off

IF EXIST home\%username%\.ssh GOTO START
mkdir home\%username%\.ssh

:START
java -cp lib\datamover.jar;lib\log4j.jar;lib\cisd-base.jar;lib\cisd-args4j.jar;lib\commons-lang.jar;lib\commons-io.jar;lib\activation.jar;lib\mail.jar ch.systemsx.cisd.datamover.Main --rsync-executable=bin\rsync.exe --ssh-executable=bin\ssh.exe --ln-executable=bin\ln.exe %1 %2 %3 %4 %5 %6 %7 %8 %9
