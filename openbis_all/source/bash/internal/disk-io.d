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

syscall::open*:return
/pid == $target/
{
	self->handles[arg1] = self->filename;
}

syscall::close*:entry
/pid == $target/
{
	printf("%s",self->handles[arg0]);
	self->handles[arg0] = 0;
}

syscall::read*:entry
/pid == $target/
{
	printf("%s", self->handles[arg0]);
}

syscall::write*:entry
/pid == $target/
{
	printf("%s", self->handles[arg0]);
}