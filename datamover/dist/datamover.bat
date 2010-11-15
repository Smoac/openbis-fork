@echo off

IF EXIST bin\home\%username%\.ssh GOTO START
mkdir bin\home\%username%\.ssh

:START
java -cp lib\datamover.jar;lib\log4j.jar;lib\cisd-base.jar;lib\cisd-args4j.jar;lib\commons-lang.jar;lib\commons-io.jar;lib\activation.jar;lib\mail.jar ch.systemsx.cisd.datamover.Main --rsync-executable=bin\win\rsync.exe --ssh-executable=bin\win\ssh.exe --ln-executable=bin\win\ln.exe %1 %2 %3 %4 %5 %6 %7 %8 %9
