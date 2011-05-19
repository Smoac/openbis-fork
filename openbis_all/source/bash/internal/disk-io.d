#!/usr/sbin/dtrace -s
/*
 * disk-io.d - print out calls to file open/close and read/write operations
 *
 * Usage -- make sure the file is executable. Then run it with the -p flag specifying the process to trace.
 * Example: 
 *   ./disk-ip.d -p {id of process to trace})
 *
 * The pid of java programs can be obtained from jps.
 */ 

syscall::open*:entry
/pid == $target/
{
	self->filename = copyinstr(arg0);
	printf("%s", self->filename);
}

syscall::close*:entry
/pid == $target/
{
	printf("%s",fds[arg0].fi_pathname);
}

syscall::read*:entry
/pid == $target/
{
	printf("%s", fds[arg0].fi_pathname);
}

syscall::write*:entry
/pid == $target/
{
	printf("%s", fds[arg0].fi_pathname);
}