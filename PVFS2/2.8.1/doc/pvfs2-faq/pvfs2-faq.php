<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 3.2 Final//EN">

<!--Converted with jLaTeX2HTML 2002 (1.62) JA patch-1.4
patched version by:  Kenshi Muto, Debian Project.
LaTeX2HTML 2002 (1.62),
original version by:  Nikos Drakos, CBLU, University of Leeds
* revised and updated by:  Marcus Hennecke, Ross Moore, Herb Swan
* with significant contributions from:
  Jens Lippmann, Marek Rouchal, Martin Wilck and others -->
<HTML>
<HEAD>
<TITLE>Frequently Asked Questions about PVFS</TITLE>
<META NAME="description" CONTENT="Frequently Asked Questions about PVFS">
<META NAME="keywords" CONTENT="pvfs2-faq">
<META NAME="resource-type" CONTENT="document">
<META NAME="distribution" CONTENT="global">

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
<META NAME="Generator" CONTENT="jLaTeX2HTML v2002 JA patch-1.4">
<META HTTP-EQUIV="Content-Style-Type" CONTENT="text/css">

<LINK REL="STYLESHEET" HREF="pvfs2-faq.css">

<? include("../../../../header.php"); ?></HEAD>

<?include("../../../..//top.php"); ?> <body id="documentation">



<table width="95%" class="tabletype1" cellpadding="0" cellspacing="0" align="left">
<tr>
<td>
<table width="100%" cellspacing="0" cellpadding="1">
<tr>                                                          
<td class="nav_white" width="70%" valign="top">               
<br>

<table cellspacing="0" cellpadding="1" align="left" width="70%">
<tr>
<td width="80%" valign="top">
<H1 ALIGN="LEFT">Frequently Asked Questions about PVFS</H1>
<P ALIGN="LEFT"><STRONG>PVFS Development Team </STRONG></P>

<P>
<BR>

<H2><A NAME="SECTION00010000000000000000">
Contents</A>
</H2>
<!--Table of Contents-->

<UL>
<LI><A NAME="tex2html104"
  HREF="pvfs2-faq.php">1 Basics</A>
<UL>
<LI><A NAME="tex2html105"
  HREF="pvfs2-faq.php#SECTION00021000000000000000">1.1 What is PVFS?</A>
<LI><A NAME="tex2html106"
  HREF="pvfs2-faq.php#SECTION00022000000000000000">1.2 What does the ``V'' in PVFS stand for?</A>
<LI><A NAME="tex2html107"
  HREF="pvfs2-faq.php#SECTION00023000000000000000">1.3 Is PVFS an attempt to parallelize the *NIX VFS?</A>
<LI><A NAME="tex2html108"
  HREF="pvfs2-faq.php#SECTION00024000000000000000">1.4 What are the components of PVFS that I should know about?</A>
<LI><A NAME="tex2html109"
  HREF="pvfs2-faq.php#SECTION00025000000000000000">1.5 What is the format of the PVFS version string?</A>
</UL>
<BR>
<LI><A NAME="tex2html110"
  HREF="pvfs2-faq.php#SECTION00030000000000000000">2 Supported Architectures and Hardware</A>
<UL>
<LI><A NAME="tex2html111"
  HREF="pvfs2-faq.php#SECTION00031000000000000000">2.1 Does PVFS require any particular hardware?</A>
<LI><A NAME="tex2html112"
  HREF="pvfs2-faq.php#SECTION00032000000000000000">2.2 What architectures does PVFS support?</A>
<LI><A NAME="tex2html113"
  HREF="pvfs2-faq.php#SECTION00033000000000000000">2.3 Does PVFS work across heterogeneous architectures?</A>
<LI><A NAME="tex2html114"
  HREF="pvfs2-faq.php#SECTION00034000000000000000">2.4 Does running PVFS require a particular kernel or kernel version?</A>
<LI><A NAME="tex2html115"
  HREF="pvfs2-faq.php#SECTION00035000000000000000">2.5 What specific hardware architectures are supported by the PVFS kernel module?</A>
<LI><A NAME="tex2html116"
  HREF="pvfs2-faq.php#SECTION00036000000000000000">2.6 Does the PVFS client require a patched Linux kernel?</A>
<LI><A NAME="tex2html117"
  HREF="pvfs2-faq.php#SECTION00037000000000000000">2.7 Can I build the PVFS kernel code directly into the kernel, rather than as a module?</A>
<LI><A NAME="tex2html118"
  HREF="pvfs2-faq.php#SECTION00038000000000000000">2.8 Is there a MacOS X/Cygwin/Windows client for PVFS?</A>
</UL>
<BR>
<LI><A NAME="tex2html119"
  HREF="pvfs2-faq.php#SECTION00040000000000000000">3 Installation</A>
<UL>
<LI><A NAME="tex2html120"
  HREF="pvfs2-faq.php#SECTION00041000000000000000">3.1 How do I install PVFS?</A>
<LI><A NAME="tex2html121"
  HREF="pvfs2-faq.php#SECTION00042000000000000000">3.2 How can I store PVFS data on multiple disks on a single node?</A>
<LI><A NAME="tex2html122"
  HREF="pvfs2-faq.php#SECTION00043000000000000000">3.3 How can I run multiple PVFS servers on the same node?</A>
<LI><A NAME="tex2html123"
  HREF="pvfs2-faq.php#SECTION00044000000000000000">3.4 Can I use multiple metadata servers in PVFS?</A>
<LI><A NAME="tex2html124"
  HREF="pvfs2-faq.php#SECTION00045000000000000000">3.5 Does using multiple metadata servers reduce the chance of file system corruption during hardware failures?</A>
<LI><A NAME="tex2html125"
  HREF="pvfs2-faq.php#SECTION00046000000000000000">3.6 How many servers should I run?</A>
<LI><A NAME="tex2html126"
  HREF="pvfs2-faq.php#SECTION00047000000000000000">3.7 Can PVFS servers listen on two network interfaces simultaneously (i.e. multihome)?</A>
<LI><A NAME="tex2html127"
  HREF="pvfs2-faq.php#SECTION00048000000000000000">3.8 How can I automount PVFS volumes?</A>
<LI><A NAME="tex2html128"
  HREF="pvfs2-faq.php#SECTION00049000000000000000">3.9 Can I mount more than one PVFS file system on the same client?</A>
<LI><A NAME="tex2html129"
  HREF="pvfs2-faq.php#SECTION000410000000000000000">3.10 How can I upgrade from PVFS v1 to PVFS v2?</A>
</UL>
<BR>
<LI><A NAME="tex2html130"
  HREF="pvfs2-faq.php#SECTION00050000000000000000">4 Reporting Problems</A>
<UL>
<LI><A NAME="tex2html131"
  HREF="pvfs2-faq.php#SECTION00051000000000000000">4.1 Where can I find documentation?</A>
<LI><A NAME="tex2html132"
  HREF="pvfs2-faq.php#SECTION00052000000000000000">4.2 What should I do if I have a problem?</A>
<LI><A NAME="tex2html133"
  HREF="pvfs2-faq.php#SECTION00053000000000000000">4.3 How do I report a problem with PVFS?</A>
</UL>
<BR>
<LI><A NAME="tex2html134"
  HREF="pvfs2-faq.php#SECTION00060000000000000000">5 Problems and Solutions</A>
<UL>
<LI><A NAME="tex2html135"
  HREF="pvfs2-faq.php#SECTION00061000000000000000">5.1 When I try to mount, I get 'wrong fs type, bad option, bad
superblock...'</A>
<LI><A NAME="tex2html136"
  HREF="pvfs2-faq.php#SECTION00062000000000000000">5.2 PVFS server consumes 100% of the CPU</A>
<LI><A NAME="tex2html137"
  HREF="pvfs2-faq.php#SECTION00063000000000000000">5.3 PVFS write performance slows down dramatically</A>
<LI><A NAME="tex2html138"
  HREF="pvfs2-faq.php#SECTION00064000000000000000">5.4 I get ``error while loading shared libraries'' when starting PVFS programs</A>
<LI><A NAME="tex2html139"
  HREF="pvfs2-faq.php#SECTION00065000000000000000">5.5 PVFS performance gets really bad once a day, then gets better again</A>
<LI><A NAME="tex2html140"
  HREF="pvfs2-faq.php#SECTION00066000000000000000">5.6 Make kmod24 fails with ``structure has no member...'' errors</A>
<LI><A NAME="tex2html141"
  HREF="pvfs2-faq.php#SECTION00067000000000000000">5.7 When i try to mount a pvfs2 file system, something goes wrong.</A>
<LI><A NAME="tex2html142"
  HREF="pvfs2-faq.php#SECTION00068000000000000000">5.8 I did all three of the above steps and I still can't mount pvfs2</A>
<LI><A NAME="tex2html143"
  HREF="pvfs2-faq.php#SECTION00069000000000000000">5.9 I'm running Redhat and the pvfs2-server can't be killed! What's wrong?</A>
<LI><A NAME="tex2html144"
  HREF="pvfs2-faq.php#SECTION000610000000000000000">5.10 Why do you single out Redhat users? What's so different about Redhat than other distributions?</A>
<LI><A NAME="tex2html145"
  HREF="pvfs2-faq.php#SECTION000611000000000000000">5.11 Where is the kernel source on a Fedora system?</A>
<LI><A NAME="tex2html146"
  HREF="pvfs2-faq.php#SECTION000612000000000000000">5.12 What are extended attributes? How do I use them with PVFS?</A>
<LI><A NAME="tex2html147"
  HREF="pvfs2-faq.php#SECTION000613000000000000000">5.13 What are Access Control Lists? How do I enable Access Control Lists on PVFS?</A>
<LI><A NAME="tex2html148"
  HREF="pvfs2-faq.php#SECTION000614000000000000000">5.14 On SLES 9, 'make kmod' complains about mmgrab and flush_icache_range being undefined</A>
<LI><A NAME="tex2html149"
  HREF="pvfs2-faq.php#SECTION000615000000000000000">5.15 Everything built fine, but when I try to compile programs that use PVFS, I get undefined references</A>
<LI><A NAME="tex2html150"
  HREF="pvfs2-faq.php#SECTION000616000000000000000">5.16 Can we run the Apache webserver to serve files off a PVFS volume?</A>
<LI><A NAME="tex2html151"
  HREF="pvfs2-faq.php#SECTION000617000000000000000">5.17 Trove-dbpf metadata format version mismatch!</A>
<LI><A NAME="tex2html152"
  HREF="pvfs2-faq.php#SECTION000618000000000000000">5.18 Problems with pre-release kernels</A>
</UL>
<BR>
<LI><A NAME="tex2html153"
  HREF="pvfs2-faq.php#SECTION00070000000000000000">6 Performance</A>
<UL>
<LI><A NAME="tex2html154"
  HREF="pvfs2-faq.php#SECTION00071000000000000000">6.1 I configured PVFS with support for multiple intercdonnects (e.g. Infiniband and TCP), but see low performance</A>
<LI><A NAME="tex2html155"
  HREF="pvfs2-faq.php#SECTION00072000000000000000">6.2 I ran Bonnie and/or IOzone and the performance is terrible.
Why? Is there anything I can do?</A>
<LI><A NAME="tex2html156"
  HREF="pvfs2-faq.php#SECTION00073000000000000000">6.3 Why is program XXX so slow?</A>
<LI><A NAME="tex2html157"
  HREF="pvfs2-faq.php#SECTION00074000000000000000">6.4 NFS outperforms PVFS for application XXX. Why?</A>
<LI><A NAME="tex2html158"
  HREF="pvfs2-faq.php#SECTION00075000000000000000">6.5 Can the underlying local file system affect PVFS performance?</A>
<LI><A NAME="tex2html159"
  HREF="pvfs2-faq.php#SECTION00076000000000000000">6.6 Is there any way to tune particular directories for different
workloads?</A>
<LI><A NAME="tex2html160"
  HREF="pvfs2-faq.php#SECTION00077000000000000000">6.7 My app still runs more slowly than I would like. What can I do?</A>
</UL>
<BR>
<LI><A NAME="tex2html161"
  HREF="pvfs2-faq.php#SECTION00080000000000000000">7 Fault Tolerance</A>
<UL>
<LI><A NAME="tex2html162"
  HREF="pvfs2-faq.php#SECTION00081000000000000000">7.1 Does PVFS support some form of fault tolerance?</A>
<LI><A NAME="tex2html163"
  HREF="pvfs2-faq.php#SECTION00082000000000000000">7.2 Can PVFS tolerate client failures?</A>
<LI><A NAME="tex2html164"
  HREF="pvfs2-faq.php#SECTION00083000000000000000">7.3 Can PVFS tolerate disk failures?</A>
<LI><A NAME="tex2html165"
  HREF="pvfs2-faq.php#SECTION00084000000000000000">7.4 Can PVFS tolerate network failures?</A>
<LI><A NAME="tex2html166"
  HREF="pvfs2-faq.php#SECTION00085000000000000000">7.5 Can PVFS tolerate server failures?</A>
</UL>
<BR>
<LI><A NAME="tex2html167"
  HREF="pvfs2-faq.php#SECTION00090000000000000000">8 File System Interfaces</A>
<UL>
<LI><A NAME="tex2html168"
  HREF="pvfs2-faq.php#SECTION00091000000000000000">8.1 How do I get MPI-IO for PVFS?</A>
<LI><A NAME="tex2html169"
  HREF="pvfs2-faq.php#SECTION00092000000000000000">8.2 Can I directly manipulate PVFS files on the PVFS servers without going through some client interface?</A>
</UL>
<BR>
<LI><A NAME="tex2html170"
  HREF="pvfs2-faq.php#SECTION000100000000000000000">9 Management</A>
<UL>
<LI><A NAME="tex2html171"
  HREF="pvfs2-faq.php#SECTION000101000000000000000">9.1 How can I back up my PVFS file system?</A>
<LI><A NAME="tex2html172"
  HREF="pvfs2-faq.php#SECTION000102000000000000000">9.2 Can I add, remove, or change the order of the PVFS servers on an existing PVFS file system?</A>
<LI><A NAME="tex2html173"
  HREF="pvfs2-faq.php#SECTION000103000000000000000">9.3 Are there tools for migrating data between servers?</A>
<LI><A NAME="tex2html174"
  HREF="pvfs2-faq.php#SECTION000104000000000000000">9.4 Why does df show less free space than I think it should? What can I do about that?</A>
<LI><A NAME="tex2html175"
  HREF="pvfs2-faq.php#SECTION000105000000000000000">9.5 Does PVFS have a maximum file system size? If so, what is it?</A>
<LI><A NAME="tex2html176"
  HREF="pvfs2-faq.php#SECTION000106000000000000000">9.6 Mouning PVFS with the interrupt option</A>
</UL>
<BR>
<LI><A NAME="tex2html177"
  HREF="pvfs2-faq.php#SECTION000110000000000000000">10 Missing Features</A>
<UL>
<LI><A NAME="tex2html178"
  HREF="pvfs2-faq.php#SECTION000111000000000000000">10.1 Why don't hardlinks work under PVFS?</A>
<LI><A NAME="tex2html179"
  HREF="pvfs2-faq.php#SECTION000112000000000000000">10.2 Can I mmap a PVFS file?</A>
<LI><A NAME="tex2html180"
  HREF="pvfs2-faq.php#SECTION000113000000000000000">10.3 Will PVFS store new files on servers with more space, allowing files to be stored when one server runs out of space?</A>
<LI><A NAME="tex2html181"
  HREF="pvfs2-faq.php#SECTION000114000000000000000">10.4 Does PVFS have locks?</A>
</UL>
<BR>
<LI><A NAME="tex2html182"
  HREF="pvfs2-faq.php#SECTION000120000000000000000">11 Helping Out</A>
<UL>
<LI><A NAME="tex2html183"
  HREF="pvfs2-faq.php#SECTION000121000000000000000">11.1 How can I contribute to the PVFS project?</A>
</UL>
<BR>
<LI><A NAME="tex2html184"
  HREF="pvfs2-faq.php#SECTION000130000000000000000">12 Implementation Details</A>
<UL>
<LI><A NAME="tex2html185"
  HREF="pvfs2-faq.php#SECTION000131000000000000000">12.1 BMI</A>
</UL>
<BR>
<LI><A NAME="tex2html186"
  HREF="pvfs2-faq.php#SECTION000140000000000000000">About this document ...</A>
</UL>
<!--End of Table of Contents-->
<P>

<P>

<H1><A NAME="SECTION00020000000000000000">
1 Basics</A>
</H1>

<P>
This section covers some basic questions for people who are unfamiliar with PVFS.

<P>

<H2><A NAME="SECTION00021000000000000000">
1.1 What is PVFS?</A>
</H2>

<P>
PVFS is an open-source, scalable parallel file system targeted at production
parallel computation environments.  It is designed specifically to scale to
very large numbers of clients and servers.  The architecture is very modular,
allowing for easy inclusion of new hardware support and new algorithms.  This
makes PVFS a perfect research testbed as well.

<P>

<H2><A NAME="SECTION00022000000000000000">
1.2 What does the ``V'' in PVFS stand for?</A>
</H2>

<P>
The ``V'' in PVFS stands for virtual.  This is a holdover from the original
(PVFS1) project that built a parallel file system on top of local file
systems, which we still do now.  It isn't meant to imply virtualization of
storage, although that is sort of what the file system does.

<P>

<H2><A NAME="SECTION00023000000000000000">
1.3 Is PVFS an attempt to parallelize the *NIX VFS?</A>
</H2>

<P>
No, and we're not even sure what that means!  The design of PVFS does
not depend on the design of the traditional *NIX Virtual Filesystem
Switch (VFS) layer, although we provide a compatibility layer that
allows access to the file system through it.

<P>

<H2><A NAME="SECTION00024000000000000000">
1.4 What are the components of PVFS that I should know about?</A>
</H2>

<P>
The PVFS Guide (<TT><A NAME="tex2html1"
  HREF="http://www.pvfs.org/pvfs2-guide.html">http://www.pvfs.org/pvfs2-guide.html</A></TT>) has more
information on all of these components, plus a discussion of the system as a
whole, the code tree, and more.

<P>

<H2><A NAME="SECTION00025000000000000000"></A>
<A NAME="sec:version-string"></A>
<BR>
1.5 What is the format of the PVFS version string?
</H2>
PVFS uses a three-number version string: X.Y.Z.  The first number (X)
represents the high level design version of PVFS.  The current design
version is 2, and will likely remain there.  The second number (Y) refers
to the major version of the release.  Major versions are incremented with
new features, protocol changes, public API changes, and storage format
changes.  The third number (Z) refers to the minor version of the release,
and is incremented primarily for bug fix releases.

<P>
With our 2.6.0 release,
we changed the release version and name from PVFS2 1.x.x, to PVFS 2.x.x.
Users familiar with 'PVFS2' and had been using PVFS2 1.5.1 
will find the same software in PVFS version 2.6.0 or
later (with updates and new features of course).

<P>
Users of PVFS version 1 can still go to:
<TT><A NAME="tex2html2"
  HREF="http://www.parl.clemson.edu/pvfs">http://www.parl.clemson.edu/pvfs</A></TT>, although we highly
encourage you to upgrade to PVFS version 2, if you are still using
version 1.

<P>

<H1><A NAME="SECTION00030000000000000000">
2 Supported Architectures and Hardware</A>
</H1>

<P>
This section covers questions related to particular system architectures,
operating systems, and other hardware.

<P>

<H2><A NAME="SECTION00031000000000000000">
2.1 Does PVFS require any particular hardware?</A>
</H2>

<P>
Other than hardware supported by the Linux OS, no.  PVFS uses
existing network infrastructure for communication and can currently
operate over TCP, Myrinet, and InfiniBand.  Disk local to servers is
used for PVFS storage, so no storage area network (SAN) is required
either (although it can be helpful when setting up fault tolerant solutions;
see Section&nbsp;<A HREF="pvfs2-faq.php#sec:fault-tolerance">7</A>.

<P>

<H2><A NAME="SECTION00032000000000000000"></A>
<A NAME="sec:supported-architectures"></A>
<BR>
2.2 What architectures does PVFS support?
</H2>

<P>
The majority of PVFS is POSIX-compliant C code that runs in user
space.  As such, much of PVFS can run on most available systems.  See
Question&nbsp;<A HREF="pvfs2-faq.php#sec:supported-hw">2.5</A> for more information on particular
hardware.

<P>
The (optional) part of PVFS that hooks to the operating system on
clients must be written specifically for the particular operating
system.  Question&nbsp;<A HREF="pvfs2-faq.php#sec:kernel-version">2.4</A> covers this issue.

<P>

<H2><A NAME="SECTION00033000000000000000">
2.3 Does PVFS work across heterogeneous architectures?</A>
</H2>

<P>
Yes!  The ``language'' that PVFS uses to talk between clients and
servers is encoded in a architecture-independent format (little-endian
with fixed byte length parameters).  This allows different PVFS
components to interact seamlessly regardless of architecture.

<P>

<H2><A NAME="SECTION00034000000000000000"></A>
<A NAME="sec:kernel-version"></A>
<BR>
2.4 Does running PVFS require a particular kernel or kernel
            version?
</H2>

<P>
You can run the userspace PVFS servers and administration tools on
every major GNU/Linux distribution out of the box, and we intend to
keep it that way.
However, the kernel module that allows client access to the PVFS system
does depend on particular kernel versions because it builds against
the running one (in the same manner as every other Linux module).
The kernel dependent PVFS client support has been written for Linux
kernel versions 2.4.19 (and greater) and 2.6.0 (and greater).  At this
time only Linux clients have this level of support.

<P>

<H2><A NAME="SECTION00035000000000000000"></A>
<A NAME="sec:supported-hw"></A>
<BR>
2.5 What specific hardware architectures are supported by the
            PVFS kernel module?
</H2>

<P>
To our knowledge, PVFS has been verified to be working on x86/IA-32,
IA-64, AMD64, PowerPC (ppc), and Alpha based GNU/Linux distributions.

<P>

<H2><A NAME="SECTION00036000000000000000">
2.6 Does the PVFS client require a patched Linux kernel?</A>
</H2>

<P>
No.  The kernel module source included with PVFS is generally
targeted toward the official ``Linus'' kernels (found at kernel.org).
Patches for the PVFS kernel module code may be provided for major
distributions that have modified their kernel to be incompatible with
the officially released kernels.  The best place to find out more
information about support for a kernel tied to a particular
distribution is on the PVFS2-developers mailing list.

<P>

<H2><A NAME="SECTION00037000000000000000">
2.7 Can I build the PVFS kernel code directly into the kernel,
            rather than as a module?</A>
</H2>

<P>
No, this is currently not supported nor recommended.

<P>

<H2><A NAME="SECTION00038000000000000000">
2.8 Is there a MacOS X/Cygwin/Windows client for PVFS?</A>
</H2>

<P>
At this time we have no plans for porting the code to operating
systems other than Linux.  However, we do encourage porting efforts of
PVFS to other operating systems, and will likely aid in the
development.

<P>

<H1><A NAME="SECTION00040000000000000000">
3 Installation</A>
</H1>

<P>
This section covers issues related to installing and configuring PVFS.

<P>

<H2><A NAME="SECTION00041000000000000000">
3.1 How do I install PVFS?</A>
</H2>

<P>
The PVFS Quick Start Guide
(<TT><A NAME="tex2html3"
  HREF="http://www.pvfs.org/pvfs2/pvfs2-quickstart.html">http://www.pvfs.org/pvfs2/pvfs2-quickstart.html</A></TT>) provides an overview
of both a simple, single-server installation, and a more complicated,
multi-server configuration.

<P>

<H2><A NAME="SECTION00042000000000000000"></A>
<A NAME="sec:multiple-disks"></A>
<BR>
3.2 How can I store PVFS data on multiple disks on a single node?
</H2>

<P>
There are at least two ways to do this.

<P>
In general the best solution to this problem is going to be to get the disks
logically organized into a single unit by some other OS component, then build
a file system on that single logical unit for use by the PVFS server on that
node.

<P>
There are a wide array of hardware RAID controllers that are capable of
performing this task.
The Multiple Devices (MD) driver is a software component of Linux that can be
used to combine multiple disk drives into a single logical unit, complete with
RAID for fault tolerance.
Using the Logical Volume Management (LVM) component of the Linux OS is another
option for this (see the HOWTO at
<TT><A NAME="tex2html4"
  HREF="http://www.tldp.org/HOWTO/LVM-HOWTO.html">http://www.tldp.org/HOWTO/LVM-HOWTO.html</A></TT>).  LVM would also allow you to
add or remove drives at a later time, which can be quite convenient.  You
can of course combine the MD and LVM components in interesting ways as well,
but that's outside the scope of this FAQ.
There's an EVMS program that can be used for managing local storage; this
might be useful for setting up complicated configurations of local storage
prior to starting up PVFS servers.

<P>
A second solution would be to use more than one server on the same node, each
using a different file system to store its data.  This might lead to resource
contention issues, so we suggest trying other options first.

<P>

<H2><A NAME="SECTION00043000000000000000">
3.3 How can I run multiple PVFS servers on the same node?</A>
</H2>

<P>
If you do decide to run more than one PVFS server on the same node,
setting things up is as simple as setting up servers on different
nodes.  Each will need its own entry in the list of Aliases and its
own server-specific configuration file, as described in the Quick Start
(<TT><A NAME="tex2html5"
  HREF="http://www.pvfs.org/pvfs2/pvfs2-quickstart.html">http://www.pvfs.org/pvfs2/pvfs2-quickstart.html</A></TT>).

<P>

<H2><A NAME="SECTION00044000000000000000">
3.4 Can I use multiple metadata servers in PVFS?</A>
</H2>

<P>
Absolutely!  Any PVFS server can store either metadata, data, or both.
Simply allocate unique MetaHandleRanges for each server that you would like to
store metadata; the clients will handle the rest.

<P>

<H2><A NAME="SECTION00045000000000000000">
3.5 Does using multiple metadata servers reduce the chance of
            file system corruption during hardware failures?</A>
</H2>

<P>
Unfortunately, no.  While using multiple metadata servers distributes
metadata, it does not replicate or store redundant information across
these servers.  For information on better handling failures, see
Section&nbsp;<A HREF="pvfs2-faq.php#sec:fault-tolerance">7</A>.

<P>

<H2><A NAME="SECTION00046000000000000000"></A>
<A NAME="sec:howmany-servers"></A>
<BR>
3.6 How many servers should I run?
</H2>

<P>
Really, the answer is ``it depends'', but here are some factors you
should take into account.  

<P>
Running multiple metadata servers might help if you expect to have have
a lot of small files.  The metadata servers are not involved in data
access (file contents) but do have a role in file creation and lookup.
Multiple clients accessing different files will likely access different
metadata servers, so you could see a load balancing effect.

<P>
A good rule of thumb is you should run as many data servers as possible.
One common configuration is to have some nodes with very
high-performance disks acting as servers to the larger cluster.  As you
use more servers in this configuration, the theoretical peak performance
of PVFS increases.  The clients, however, have to make very large
requests in order to stripe the I/O across all the servers.  If your
clients will never write large files, use a smaller number of servers.
If your clients are writing out gigantic checkpoint files or reading in
huge datasets, then use more servers.

<P>
It is entirely possible to run PVFS servers on the same nodes doing
computation. In most cases, however, you will see better performance
if you have some portion of your cluster dedicated to IO and another
portion dedicated to computation.

<P>

<H2><A NAME="SECTION00047000000000000000">
3.7 Can PVFS servers listen on two network interfaces simultaneously (i.e. multihome)?</A>
</H2>

<P>
Yes!  PVFS servers can listen on more than one interface at a time.
Multihome support was added shortly before the PVFS2 1.0 release.

<P>

<H2><A NAME="SECTION00048000000000000000">
3.8 How can I automount PVFS volumes?</A>
</H2>

<P>
The Linux automounter needs some help dealing with PVFS's resource
strings.  A typical mount command (on Linux 2.6) would look like this:

<P>
<PRE>
	mount -t pvfs2 tcp://server0:3334/pvfs2-fs /mnt/pvfs2
</PRE>

<P>
The entry in the automount config file should look like this:

<P>
<PRE>
	pvfs -fstype=pvfs2           tcp://server0\:3334/pvfs2-fs
</PRE>

<P>
Note the backslash-escape of the colon before the port number.  Without that
escape, the automounter will get confused and replace <TT>'tcp://'</TT> with
<TT>'tcp:///'</TT>

<P>

<H2><A NAME="SECTION00049000000000000000"></A>
<A NAME="sec:multiple-mounts"></A>
<BR>
3.9 Can I mount more than one PVFS file system on the same client?
</H2>

<P>
Yes.  However, when setting up the two file systems it is important that both
file systems have unique <TT>Name</TT> and <TT>ID</TT> values (in the
file system configuration file).  This means that you can't simply make a copy
of the <TT>fs.conf</TT> generated by <TT>pvfs2-genconfig</TT>; you will need
to edit the files a bit.  This editing needs to be performed <I>before</I> you
create the storage spaces!

<P>

<H2><A NAME="SECTION000410000000000000000">
3.10 How can I upgrade from PVFS v1 to PVFS v2?</A>
</H2>

<P>
Hans Reiser summarized the upgrade approach from reiserfs V3 to V4 with the following:
<BLOCKQUOTE>
To upgrade from reiserfs V3 to V4, use tar, or sponsor us to write a convertfs.

</BLOCKQUOTE>
Similarly, there are no tools currently provided by the PVFS team to
upgrade from PVFS1 to PVFS2, so tar is your best bet.

<P>

<H1><A NAME="SECTION00050000000000000000">
4 Reporting Problems</A>
</H1>

<P>
This section outlines some steps that will help the developers figure out what
has happened when you have a problem.

<P>

<H2><A NAME="SECTION00051000000000000000">
4.1 Where can I find documentation?</A>
</H2>

<P>
The best place to look for documentation on PVFS is the PVFS web site at
<TT><A NAME="tex2html6"
  HREF="http://www.pvfs.org/">http://www.pvfs.org/</A></TT>.  Documentation (including this FAQ) is also
available in the <TT>doc</TT> subdirectory of the PVFS source distribution.

<P>

<H2><A NAME="SECTION00052000000000000000">
4.2 What should I do if I have a problem?</A>
</H2>

<P>
The first thing to do is to check out the existing documentation and see if it
addresses your problem.  We are constantly updating documentation to clarify
sections that users have found confusing and to add to this document answers
to questions that we have seen.

<P>
The next thing to do is to check out the PVFS mailing list archives at
<TT><A NAME="tex2html7"
  HREF="http://www.pvfs.org/pvfs2/lists.html">http://www.pvfs.org/pvfs2/lists.html</A></TT>.  It is likely that you are not
the first person to see a particular problem, so searching this list will
often result in an immediate answer.

<P>
If you still haven't found an answer, the next thing to do is to mail the
mailing list and report your problem.

<P>
If you enjoy using IRC, you can also join us on irc.freenode.net in
the #pvfs2 channel.

<P>

<H2><A NAME="SECTION00053000000000000000">
4.3 How do I report a problem with PVFS?</A>
</H2>

<P>
First you will need to join the PVFS2 Users Mailing list at
<TT><A NAME="tex2html8"
  HREF="http://www.beowulf-underground.org/mailman/listinfo/pvfs2-users">http://www.beowulf-underground.org/mailman/listinfo/pvfs2-users</A></TT>.  You
must be a user to post to the list; this is necessary to keep down the amount
of spam on the list.

<P>
Next you should gather up some information regarding your system:

<UL>
<LI>Version of PVFS
</LI>
<LI>Version of MPI and MPI-IO (if you're using them)
</LI>
<LI>Version of Linux kernel (if you're using the VFS interface)
</LI>
<LI>Hardware architecture, including CPU, network, storage
</LI>
<LI>Any logs that might be useful to the developers
</LI>
</UL>
Including this information in your first message will help the developers most
quickly help you.  You are almost guaranteed that if you do not include this
information in your first message, you will be asked to provide it in the
first reply, slowing down the process.

<P>
You should be aware that you are also likely to be asked to try the newest
stable version if you are not running that version.  We understand that this
is not always possible, but if it is, please do.

<P>
<I>Note:</I> Please do not send your message to both the PVFS2 Users List and
the PVFS2 Developers List; the lists serve different purposes.  Also, please
do not send your message directly to particular developers.  By keeping
discussion of problems on the mailing lists we ensure that the discussion is
archived and that everyone has a chance to respond.

<P>

<H1><A NAME="SECTION00060000000000000000">
5 Problems and Solutions</A>
</H1>
This section covers error conditions you might encounter, what they might mean,
and how to fix them.

<P>

<H2><A NAME="SECTION00061000000000000000">
5.1 When I try to mount, I get 'wrong fs type, bad option, bad
superblock...'</A>
</H2>

<P>
First, make 100% sure you typed the mount command correctly. As discussed in
the PVFS quickstart, different mount commands are needed for linux-2.4 and
linux-2.6.  A linux-2.6 mount command will look like this: 

<P>
<PRE>
prompt# mount -t pvfs2 tcp://testhost:3334/pvfs2-fs /mnt/pvfs2
</PRE>

<P>
Under linux-2.4, the mount command looks slightly different:
<PRE>
prompt# mount -t pvfs2 pvfs2 /mnt/pvfs2 -o tcp://testhost:3334/pvfs2-fs
</PRE>

<P>
This error could also mean a pvfs2-client process is not running,
either because it was not started before the mount command, or was
terminated at some point. If you can reliably (or even intermittently)
cause the pvfs2-client to exit abnormally, please send a report to the
developers.

<P>
This error can also occur if you attempt to mount a second PVFS file system
on a client, where the new file system has the same name or ID as one
that is already mounted.  If you are trying to mount more than one file system
on the same client and have problems, please see question
<A HREF="pvfs2-faq.php#sec:multiple-mounts">3.9</A>.

<P>
Finally, be sure there are no typos in your command line, as this is
commonly the case!

<P>

<H2><A NAME="SECTION00062000000000000000"></A>
<A NAME="sec:server_100pct_cpu"></A>
<BR>
5.2 PVFS server consumes 100% of the CPU
</H2>

<P>
On some systems, the pvfs2-server will start consuming 100% of the CPU
after you try to read or write a file to PVFS.  gdb indicates that the
server is spending a lot of time in the glibc routine
<TT>'.handle_kernel_aio'</TT>.  Please check to see if your
distribution has an updated glibc package.  RHEL3, for example, will
exhibit this behavior with glibc-2.3.2-95.6, but not with the updated
glibc-2.3.2-95.20 package.  We have also seen this behavior on ppc64
systems running glibc-2.3.3-18.ydl.4 .  If you encounter this problem
and your distribution does not have an updated glibc package, you can
configure pvfs2 with <TT>-disable-aio-threaded-callbacks</TT>, though
this will result in a performance hit.  An alternate workaround is to
set <TT>LD_ASSUME_KERNEL</TT> to 2.4.1 before running pvfs2-server.
pvfs2-server will then use an older (and not as optimized) thread
library that does not have this bug.

<P>
At this time we do not know which of the two suggested workarounds is
better from a performance standpoint.  The <TT>LD_ASSUME_KERNEL</TT>
method might make more sense:  when/if the system's glibc is
upgraded, you will only have to restart pvfs2-server with the
environment variable unset.  You would not have to rebuild pvfs2 to take
advantage of the fix.

<P>

<H2><A NAME="SECTION00063000000000000000"></A>
<A NAME="sec:write_slowdown"></A>
<BR>
5.3 PVFS write performance slows down dramatically
</H2>

<P>
Phil Carns noticed that on some kernels, write-heavy workloads can trigger a
kernel bug.   The symptoms are that the PVFS server will only be able to
deliver a few KB/s, and the CPU utilization will be close to 100%.  The cause
appears to be related to ext3's ``reservation'' code (designed to reduce
fragmentation).  The solution is to either mount the filesystem with the
'noreservation' option, or upgrade your kernel.  

<P>
For more information, including URLs to several other reports of this issue, see Phil's original post:  
<TT><A NAME="tex2html9"
  HREF="http://www.beowulf-underground.org/pipermail/pvfs2-developers/2006-March/001885.html">http://www.beowulf-underground.org/pipermail/pvfs2-developers/2006-March/001885.html</A></TT>
<P>

<H2><A NAME="SECTION00064000000000000000">
5.4 I get ``error while loading shared libraries'' when starting PVFS programs</A>
</H2>

<P>
PVFS needs several libraries.  If those libraries aren't in the default
locations, you might need to add flags when running PVFS's configure script.
At configure time you can, for example, pass <TT>-with-db=/path/to/db
-with-gm=/path/to/gm</TT> to compile with Berkeley DB and Myiricom GM libraries.
The configure options let the compiler know where to find the libraries at
compile time.  

<P>
Those compile-time options, however, aren't enough to find the libraries at
run-time.  There are two ways to teach the system where to find libraries:  

<P>

<UL>
<LI>add /usr/local/BerkeleyDB.4.3/lib to the /etc/ld.so.conf config file
  and re-run 'ldconfig' OR
</LI>
<LI>add /usr/local/BerkeleyDB.4.3/lib to the <TT>LD_LIBRARY_PATH</TT>
    environment variable.
</LI>
</UL>

<P>
I would suggest the ld.so.conf approach, since that will work for all users on
your system.

<P>

<H2><A NAME="SECTION00065000000000000000"></A>
<A NAME="sec:cron-indexing"></A>
<BR>
5.5 PVFS performance gets really bad once a day, then gets
     better again
</H2>

<P>
Several sites have reported poor PVFS performance early in the day that
eventually goes away until the next day, when the cycle begins again.  Daily
cron jobs might be the culprit in these cases.  In particular, most Linux
distributions have a daily cron job (maybe called 'slocate', 'locate' or
'updatedb') that indexes the entire file system.  Networked file systems such
as NFS are often excluded from this indexing.  

<P>
The exact steps to remove PVFS from this indexing process vary among
distributions.  Generally speaking, there should be a cron script in
<TT>/etc/cron.daily</TT> called 'slocate' or 'updatedb'.  That script should
have a list of excluded file systems (like /var/run and /tmp) and flie types (
like 'proc' and 'nfs').  Either add 'pvfs2' to the list of file types or add
the pvfs2 mount point to the list of excluded file systems.   Be sure to do
this on all machines in your cluster.  

<P>

<H2><A NAME="SECTION00066000000000000000">
5.6 Make kmod24 fails with ``structure has no member...'' errors</A>
</H2>

<P>
On some Redhat and Redhat-derived distributions, ``make kmod24'' might
fail with errors like this:

<P>
<PRE>
console]:make kmod24
 CC [M]  /usr/src/pvfs2/src/kernel/linux-2.4/pvfs2-utils.o
pvfs2-utils.c: In function `mask_blocked_signals':
pvfs2-utils.c:1063: structure has no member named `sig'
pvfs2-utils.c:1070: structure has no member named `sigmask_lock'
pvfs2-utils.c:1073: too many arguments to function `recalc_sigpending'
pvfs2-utils.c: In function `unmask_blocked_signals':
pvfs2-utils.c:1082: structure has no member named `sigmask_lock'
pvfs2-utils.c:1084: too many arguments to function `recalc_sigpending'
make[1]: *** [pvfs2-utils.o] Error 1
make: *** [kmod24] Error 2
</PRE>

<P>
Redhat, and derived distributions, have a linux-2.4 based kernel with many
linux-2.6 features backported.  These backported features change the
interface to the kernel fairly significantly.  PVFS versions newer than
1.0.1 have a new configure option <TT>-enable-redhat24</TT>.  With this
option, we will be able to accommodate the backported features (and the
associated interface changes).

<P>

<H2><A NAME="SECTION00067000000000000000">
5.7 When i try to mount a pvfs2 file system, something goes wrong.</A>
</H2>

<P>

<UL>
<LI>First, are all the userspace components running?  If <TT>pvfs2-ping</TT>
doesn't work, the VFS interface won't, either.
</LI>
<LI>Make sure the pvfs2 kernel module is loaded
</LI>
<LI>Make sure pvfs2-client and pvfs2-client core are running
</LI>
<LI>Take a look at dmesg.  <TT>pvfs2_get_sb - wait timed out</TT> could
      indicate a problem with <TT>pvfs2-client-core</TT>.  See the next
      question.
</LI>
</UL>

<P>

<H2><A NAME="SECTION00068000000000000000"></A>
<A NAME="sec:nptl_and_mounting"></A>
<BR>
5.8 I did all three of the above steps and I still can't mount pvfs2
</H2>

<P>
There's one last thing to check.  Are you you are using a Redhat or Fedora
distribution, but running with a stock kernel.org 2.4 kernel?  If so, you need
to set the environment variable <TT>LD_ASSUME_KERNEL</TT> to 2.4.1 or
<TT>pvfs2-client-core</TT> will try to use the NPTL thread library.  NPTL
requires a 2.6 kernel (or a heavily backported 2.4 kernel, which Redhat
provides).  Redhat systems expect to have such a kernel, so running a stock
kernel.org 2.4 kernel can cause issues with any multi-threaded application.  In
this particular case, the <TT>pvfs2-client-core</TT> failure is hidden and can
be tricky to diagnose. 

<P>

<H2><A NAME="SECTION00069000000000000000">
5.9 I'm running Redhat and the pvfs2-server can't be killed! What's wrong?</A>
</H2>

<P>
On some Redhat systems, for compatibility reasons, the pvfs2-server
program is actually a script that wraps the installed pvfs2-server
binary.  We do this ONLY if we detect that PVFS is being installed on
a system with an NPTL implementation that we're incompatible with.
Specifically, the script exports the LD_ASSUME_KERNEL=2.2.5
environment variable and value to avoid using the NPTL at run-time.
The script quite literally exports this variable and then runs the
installed pvfs2-server binary which is named
<TT>pvfs2-server.bin</TT>.  So to properly shutdown or kill the
pvfs2-server application once it's running, you need to issue a
<TT>killall pvfs2-server.bin</TT> command instead of the more common
<TT>killall pvfs2-server</TT> command.

<P>

<H2><A NAME="SECTION000610000000000000000">
5.10 Why do you single out Redhat users?  What's so different
  about Redhat than other distributions?</A>
</H2>

<P>
Some Redhat versions (and probably some other less popular
distributions) use a heavily modified Linux 2.4.x kernel.  Due to the
changes made in the memory manager and signal handling, our default
Linux 2.4.x kernel module will not even compile!  We have
compatibility code that can mend the differences in place, but we have
to be able to detect that you're running such a system.  Our configure
script tries hard to determine which version you're running and
matches it against a known list.  If you suspect you need this fix and
our script does not properly detect it, please send mail to the
mailing list and include the contents of your /etc/redhat-release
file.

<P>
In addition, some Redhat versions ship with an NPTL (threading
library) implementation that PVFS is not compatible with.  We cannot
explain why the errors we're seeing are occurring, as they appear to be
in glibc and the threading library itself.  In short, we disable the
use of the NPTL on these few Redhat systems.  It should be noted that
we are fully compatible with other distributions that ship NPTL
libraries (such as Gentoo and Debian/unstable).

<P>

<H2><A NAME="SECTION000611000000000000000">
5.11 Where is the kernel source on a Fedora system?</A>
</H2>

<P>
Older systems used to split up the kernel into several packages
(<TT>kernel</TT>, <TT>kernel-headers</TT>, <TT>kernel-source</TT>).
Fedora kernels are not split up that way.  Everything you need to build a
kernel module is in /lib/modules/`uname -r`/build.  For example, Fedora
Core 3 ships with linux-2.6.9-1.667.  When configuring PVFS, you would
pass <TT>-with-kernel=/lib/modules/2.6.9-1.667/build</TT> to the
configure script.

<P>
In Fedora Core 4 things changed a little bit.  In order to build the pvfs2
kernel module, make sure you have both a <TT>kernel</TT> and
<TT>kernel-devel</TT> package installed.  If you have an SMP box, then you'll
need to install the -smp versions of both - i.e. <TT>kernel-smp</TT> and
<TT>kernel-smp-devel</TT>.   After both packages are installed,
/lib/modules/`uname -r`/build will once again contain a correctly configured
kernel source tree.

<P>

<H2><A NAME="SECTION000612000000000000000">
5.12 What are extended attributes? How do I use them with PVFS?</A>
</H2>
Extended attributes are name:value pairs associated with objects (files and directories
in the case of PVFS). They are extensions to the
normal attributes which are associated with all objects in the system (i.e. the stat data).
A complete overview of the extended attributes concepts can be found in man pages section 5 for attr.
On supported 2.4 kernels and all 2.6 kernels, PVFS allows users to store extended attributes
on file-system objects through the VFS as well as through the system interface. Example
usage scenarios are shown below,
To set an extended attribute ("key1", "val1") on a PVFS file foo,
<PRE>
prompt# setfattr -n key1 -v val1 /path/to/mounted/pvfs2/foo
</PRE>
To retrieve an extended attribute for a given key ("key1") on a PVFS file foo,
<PRE>
prompt# getfattr -n key1 /path/to/mounted/pvfs2/foo
</PRE>
To retrieve all attributes of a given PVFS file foo,
<PRE>
prompt# getfattr -m "" /path/to/mounted/pvfs2/foo
</PRE>
Note that PVFS uses a few standard names for its internal use that prohibit users
from reusing the same names. A list of such keys are as follows at the time
of writing of this document ("dir_ent", "root_handle",
"datafile_handles", "metafile_dist", "symlink_target"). Further, Linux also uses
a set of reserved keys to hold extended attributes that begin with the prefix "system.",
thus making them unavailable for regular usage.

<P>

<H2><A NAME="SECTION000613000000000000000">
5.13 What are Access Control Lists? How do I enable Access Control Lists on PVFS?</A>
</H2>
Recent versions of PVFS support POSIX Access Control Lists (ACL), which are used to define fine-grained 
discretionary access rights for files and directories. Every object can be thought of as having 
associated with it an ACL that governs the discretionary access to that object; this ACL
is referred to as an access ACL. In addition, a directory may have an associated ACL that 
governs the initial access ACL for objects created within that directory; this ACL 
is referred to as a default ACL. Each ACL consists of a set of ACL entries. An ACL entry 
specifies the access permissions on the associated object for an individual user or a group 
of users as a combination of read, write and search/execute permissions.

<P>
PVFS supports POSIX ACLs by storing them as extended attributes. However, support
for access control based permission checking does not exist on 2.4 Linux kernels and is hence disabled on them.
Most recent version of the Linux 2.6 kernels do allow for such permission checks, and PVFS enables
ACLs on such kernels.
However, in order to use and enforce access control lists on 2.6 kernels, one must mount
the PVFS file system by specifying the "acl" option in the mount command line. For example,
<PRE>
prompt# mount -t pvfs2 tcp://testhost:3334/pvfs2-fs /mnt/pvfs2 -o acl
</PRE>
Please refer to the man pages of "setfacl", "getfacl" or section 5 acl for detailed usage
information.

<P>

<H2><A NAME="SECTION000614000000000000000">
5.14 On SLES 9, 'make kmod' complains about <TT>mmgrab</TT> and
 <TT>flush_icache_range</TT> being undefined</A>
</H2>

<P>
SLES 9 (and possibly other kernels) makes use of internal symobls in some
inlined kernel routines.  PVFS2-1.3.2 or newer has the configure option
<TT>-disable-kernel-aio</TT>.  Passing this option to configure results in a pvfs2
kernel module that uses only exported symbols.  

<P>

<H2><A NAME="SECTION000615000000000000000"></A>
<A NAME="sec:undefined_references"></A>
<BR>
5.15 Everything built fine, but when I try to compile programs that use PVFS, I get undefined references
</H2>

<P>
The <TT>libpvfs2</TT> library requires a few additional libraries.  Usually
"-lpthread -lcrypto -lssl" are required.  Further, Myrinet and Infiniband have
their own libraries.  If you do not link the required libraries, you will
probably get errors such as <TT>undefined reference to `BIO_f_base64'</TT>.

<P>
The easiest and most portable way to ensure that you link in all required
libraries when you link <TT>libpvfs2</TT> is to use the <TT>pvfs2-config</TT>
utility.  <TT>pvfs2-config -libs</TT> will give you the full set of linker
flags needed.  Here's an example of how one might use this tool:
<PRE>
$ gcc -c $(pvfs2-config --cflags) example.c 
$ gcc example.o -o example $(pvfs2-config --libs)
</PRE>

<P>

<H2><A NAME="SECTION000616000000000000000">
5.16 Can we run the Apache webserver to serve files off a PVFS volume?</A>
</H2>

<P>
Sure you can! However, we recommend that you turn off the EnableSendfile option in
httpd.conf before starting the web server. Alternatively, you could configure
PVFS with the option <TT>-enable-kernel-sendfile</TT>. Passing this option
to configure results in a pvfs2 kernel module that supports the sendfile
callback.
But we recommend that unless the files that are being served are large enough
this may not be a good idea in terms of performance. Apache 2.x+ uses the <TT>sendfile</TT>
system call that normally stages the file-data through the page-cache. On recent 2.6 kernels,
this can be averted by providing a <TT>sendfile</TT> callback routine at the file-system.
Consequently, this ensures that we don't end up with stale or inconsistent cached data on such
kernels. However, on older 2.4 kernels the <TT>sendfile</TT> system call streams the data through
the page-cache and thus there is a real possibility of the data being served stale.
Therefore users of the <TT>sendfile</TT> system call are warned to be wary of this detail.

<P>

<H2><A NAME="SECTION000617000000000000000"></A>
<A NAME="sec:trove-migration"></A>
<BR>
5.17 Trove-dbpf metadata format version mismatch!
</H2>

<P>
In PVFS2-1.5.0 or newer the format of the metadata storage has change from
previous versions (1.4.0 or earlier).  This affects users that have created
file systems with the earlier versions of pvfs2, and wish to upgrade to the
most recent version.  We've provided a migration tool that must be run
(a one-time only procedure) to convert the file system from the old format
to the new one.  The migration tool can be used as follows:
<PRE>
$PVFS_INSTALL/bin/pvfs2-migrate-collection --all fs.conf server.conf-&lt;hostname&gt;
</PRE>
This command finds all the pvfs2 storage collections specified in the
configuration files and migrates them to the new format.  Instead of
using <TT>-all</TT>, the option <TT>-fs</TT> can be used to specify the name of
the storage collection that needs to be migrated (usually there's only
one storage collection, with the default name of 'pvfs2-fs').

<P>

<H2><A NAME="SECTION000618000000000000000"></A>
<A NAME="sec:rc-kernels"></A>
<BR>
5.18 Problems with pre-release kernels
</H2>

<P>
For better or worse, the Linux kernel development process for the 2.6 series
does not make much effort to maintain a stable kernel API.  As a result, we
often find we need to make small adjustments to the PVFS kernel module to track
recent kernel additions or changes.  

<P>
If you are using a pre-release kernel (anything with -rc in the name), you
stand a good chance of running into problems.  We are unable to track every
pre-release kernel, but do make an effort to publish necessary patches once a
kernel is officially released. 

<P>

<H1><A NAME="SECTION00070000000000000000">
6 Performance</A>
</H1>

<P>
This section covers issues related to the performance of PVFS.

<P>

<H2><A NAME="SECTION00071000000000000000"></A>
<A NAME="sec:multi-method-badperf"></A>
<BR>
6.1 I configured PVFS with support for multiple intercdonnects (e.g. Infiniband and TCP), but see low performance
</H2>

<P>
When multiple interconnects are enabled, PVFS will poll both interfaces.  This
gives PVFS maximum flexiblity, but does incur a performance penalty when one
interface is not being used.  For highest performance, configure PVFS with only
one fast method.  Consult the <TT>without-bmi-tcp</TT> option or omit the
<TT>with-&lt;METHOD&gt;</TT> option when configuring PVFS.  

<P>
Note that it can sometimes be useful to have multiple interconnects enabled.
The right choice depends a lot on your situation.

<P>

<H2><A NAME="SECTION00072000000000000000"></A>
<A NAME="sec:badperf"></A>
<BR>
6.2 I ran Bonnie and/or IOzone and the performance is terrible.
Why? Is there anything I can do?
</H2>

<P>
We designed PVFS to work well for scientific applications in a cluster
environment.  In such an environment, a file system must either spend
time ensuring all client-side caches are in sync, or not use a cache
at all (which is how PVFS currently operates).  The <TT>bonnie</TT>
and <TT>bonnie++</TT> benchmarks read and write very small blocks -
on the order of 1K.  These many small requests must travel from the
client to the server and back again.  Without client-side caching,
there is no sane way to speed this up.

<P>
To improve benchmark performance, specify a bigger block size.   PVFS
has several more aggressive optimizations that can be turned on, but
those optimizations require that applications accessing PVFS can cope
with out-of-sync caches.  

<P>
In the future, PVFS is looking to provide optional semantics for use
through the VFS that will allow some client-side caching to speed these
kinds of serial benchmarks up.  By offering a way to explicitly sync
data at any given time or by providing 'close-to-open' semantics, these
kinds of caching improvements become an option for some applications.

<P>
Bear in mind that benchmarks such as IOzone and Bonnie were meant to
stress local file systems.   They do not accurately reflect the types of
workloads for which we designed PVFS.  Furthermore, because of their
serial nature, PVFS will be unable to deliver its full performance.
Instead try running a parallel file system benchmark like IOR
(<TT><A NAME="tex2html10"
  HREF="ftp://ftp.llnl.gov/pub/siop/ior/">ftp://ftp.llnl.gov/pub/siop/ior/</A></TT>).

<P>

<H2><A NAME="SECTION00073000000000000000"></A>
<A NAME="sec:why_so_slow"></A>
<BR>
6.3 Why is program XXX so slow?
</H2>
See Question&nbsp;<A HREF="pvfs2-faq.php#sec:badperf">6.2</A>.  If the program uses small block sizes to
access a PVFS file, performance will suffer.  

<P>
Setting both (or either of) the <TT>TroveSyncMeta</TT> and
<TT>TroveSyncData</TT> options to <TT>no</TT> in the config file can
improve performance in some situations.  If you set the
value to no and the server is terminated unexpectedly, you will likely
lose data (or access to it).  Also, PVFS has a transparent server
side attribute cache (enabled by default), which can speed up
applications which read a lot of attributes (<TT>ls</TT>, for
example).  Playing around with the <TT>AttrCache*</TT> config file
settings may yield some performance improvements.  If you're running a
serial application on a single node, you can also use the client side
attribute cache (disabled by default).  This timeout is adjustable as
a command line argument to pvfs2-client.

<P>

<H2><A NAME="SECTION00074000000000000000"></A>
<A NAME="sec:nfs_vs_pvfs2"></A>
<BR>
6.4 NFS outperforms PVFS for application XXX. Why?
</H2>

<P>
In an environment where there is one client accessing a file on one
server, NFS will outperform PVFS in many benchmarks.  NFS has
completely different consistency semantics, which work very well when
just one process accesses a file.  There is some ongoing work that
will optionally offer similar consistency semantics for PVFS, at
which point we will be playing on a level field, so to speak.
However, if you insist on benchmarking PVFS and NFS in a
single-client test, there are some immediate adjustments you can make.

<P>
The easiest way to improve PVFS performance is to increase the block
size of each access.  Large block sizes help most file systems, but
for PVFS they make a much larger difference in performance than they
do for other file systems.

<P>
Also, if the <TT>TroveSyncMeta</TT> and <TT>TroveSyncData</TT> options
are set to <TT>no</TT> in your PVFS configuration file, the server
will sync data to disk only when a flush or close operation is called.
The <TT>TroveSyncMeta</TT> option is set to <TT>yes</TT> by default, 
to limit the amount of
data that could be lost if a server is terminated unexpectedly.  With
this option enabled, it is somewhat analogous to mounting your NFS
volume with the <TT>sync</TT> flag, forcing it to sync data after each
operation.

<P>
As a final note on the issue, if you plan on running application XXX,
or a similar workload, and the NFS consistency semantics are adequate
for what you're doing, then perhaps PVFS is not a wise choice of file
system for you.  PVFS is not designed for serial workloads,
particularly one with small accesses.

<P>

<H2><A NAME="SECTION00075000000000000000"></A>
<A NAME="sec:local_fs"></A>
<BR>
6.5 Can the underlying local file system affect PVFS performance?
</H2>

<P>
Yes!  However, the interaction between the PVFS servers and the local
file system hosting the storage space has not been fully explored.  No
doubt a great deal of time could be spent on different file systems
and their parameters.

<P>
People have looked at sync performance for a variety of file systems.
Some file systems will flush all dirty buffers when <TT>fsync</TT> is
called.  Other file systems will only flush dirty buffers belonging to
the file. See the threads starting at
<TT><A NAME="tex2html11"
  HREF="http://www.parl.clemson.edu/pipermail/pvfs2-developers/2004-July/000740.html">http://www.parl.clemson.edu/pipermail/pvfs2-developers/2004-July/000740.html</A></TT>and at
<TT><A NAME="tex2html12"
  HREF="http://www.parl.clemson.edu/pipermail/pvfs2-developers/2004-July/000741.html">http://www.parl.clemson.edu/pipermail/pvfs2-developers/2004-July/000741.html</A></TT>.

<P>
These tests demonstrate wide variance in file system behavior.
Interested users are encouraged to experiment and discuss their
findings on the PVFS lists.

<P>
If you're looking for a quick suggestion for a local file system type
to use, we suggest ext3 with ``journal data writeback'' option as a
reasonable choice.

<P>

<H2><A NAME="SECTION00076000000000000000"></A>
<A NAME="sec:dir_tuning"></A>
<BR>
6.6 Is there any way to tune particular directories for different
workloads?
</H2>

<P>
Yes.  This can be done by using extended attributes to set directory
hints.  Three hints are currently supported, and they allow you to specify
the distribution, distribution parameters, and number of datafiles to
stripe across.  They will not change the characteristics of existing
files, but they will take effect for any newly created files within the
directory.  These hints will also be inherited by any new
subdirectories.

<P>

<H3><A NAME="SECTION00076100000000000000">
6.6.1 Distribution</A>
</H3>

<P>
The distribution can be set as follows:

<P>
<PRE>
prompt# setfattr -n "user.pvfs2.dist_name" -v "basic_dist" /mnt/pvfs2/directory
</PRE>

<P>
Supported distribution names can be found by looking in the pvfs2-dist-*
header files.

<P>

<H3><A NAME="SECTION00076200000000000000">
6.6.2 Distribution parameters</A>
</H3>

<P>
Some distributions allow you to set parameters that impact how the
distribution behaves.  These parameters can be set as follows:

<P>
<PRE>
prompt# setfattr -n "user.pvfs2.dist_params" -v "strip_size:4096" /mnt/pvfs2/directory
</PRE>

<P>
You can specify more than one "parameter:value" pair by seperating them with
commas.

<P>

<H3><A NAME="SECTION00076300000000000000">
6.6.3 Number of datafiles</A>
</H3>

<P>
You can also specify the number of datafiles to stripe across: 

<P>
<PRE>
prompt# setfattr -n "user.pvfs2.num_dfiles" -v "1" /mnt/pvfs2/directory
</PRE>

<P>
PVFS defaults to striping files across each server in the file system.
However, you may find that for small files it is advantages to limit each
file to only a subset of servers (or even just one).

<P>

<H2><A NAME="SECTION00077000000000000000"></A>
<A NAME="sec:tuning"></A>
<BR>
6.7 My app still runs more slowly than I would like.  What can I do?
</H2>

<P>
If you ask the mailing list for help with performance, someone will probably
ask you one or more of the following questions:

<P>

<UL>
<LI>Are you running servers and clients on the same nodes?  We support this
      configuration - sometimes it is required given space or budget
      constraints.  You will not, however, see the best performance out of this
      configuration.  See Section&nbsp;<A HREF="pvfs2-faq.php#sec:howmany-servers">3.6</A>. 

<P>
</LI>
<LI>Have you benchmarked your network?  A tool like netpipe or ttcp can help
      diagnose point-to-point issues.  PVFS will tax your bisection bandwidth,
      so if ppossible, run simultaneous instances of these network benchmarks
      on multiple machine pairs and see if performance suffers.  One user
      realized the cluster had a hub (not a switch, a hub) connecting all the
      nodes.  Needless to say, performance was pretty bad. 

<P>
</LI>
<LI>Have you examined buffer sizes?  On linux, the settings /proc can make a
      big difference in TCP performance.  Set
      <TT>/proc/sys/net/core/rmem_default</TT> and
      <TT>/proc/sys/net/core/wmem_default</TT> 

<P>
</LI>
</UL>

<P>
Tuning applications can be quite a challenge.  You have disks, networks,
operating systems, PVFS, the application, and sometimes MPI.  We are
working on a document to better guide the tuning of systems for
IO-intensive workloads.

<P>

<H1><A NAME="SECTION00080000000000000000"></A>
<A NAME="sec:fault-tolerance"></A>
<BR>
7 Fault Tolerance
</H1>

<P>
This section covers issues related to fault tolerance in the context of PVFS.

<P>

<H2><A NAME="SECTION00081000000000000000">
7.1 Does PVFS support some form of fault tolerance?</A>
</H2>

<P>
Systems can be set up to handle many types of failures for PVFS.  Given enough
hardware, PVFS can even handle server failure.

<P>

<H2><A NAME="SECTION00082000000000000000">
7.2 Can PVFS tolerate client failures?</A>
</H2>

<P>
Yes.  One of the benefits of the PVFS design is that client failures are not a
significant event in the system.  Because there is no locking system in PVFS,
and no shared state stored on clients in general, a client failure does not
affect either the servers or other clients.

<P>

<H2><A NAME="SECTION00083000000000000000">
7.3 Can PVFS tolerate disk failures?</A>
</H2>

<P>
Yes, if configured to do so.  Multiple disks on each server may be used to
form redundant storage for that server, allowing servers to continue operating
in the event of a disk failure.  See section <A HREF="pvfs2-faq.php#sec:multiple-disks">3.2</A> for more
information on this approach.

<P>

<H2><A NAME="SECTION00084000000000000000">
7.4 Can PVFS tolerate network failures?</A>
</H2>

<P>
Yes, if your network has redundant links.  Because PVFS uses standard
networks, the same approaches for providing multiple network connections to a
server may be used with PVFS.  <I>Need a reference of some sort.</I>

<P>

<H2><A NAME="SECTION00085000000000000000">
7.5 Can PVFS tolerate server failures?</A>
</H2>

<P>
Yes.  We currently have a recipe describing the hardware and software
needed to set up PVFS in a high availability cluster.  Our method is
outlined in the `pvfs2-ha.{ps,pdf}' file in the doc subdirectory of the
PVFS distribution.  This configuration relies on shared storage and
commodity ``heartbeat'' software to provide means for failover.

<P>
Software redundancy offers a less expensive solution to redundancy,
but usually at a non-trivial cost to performance.  We are studying how
to implement software redundancy with lower overhead, but at this time
we provide no software-only server failover solution.

<P>

<H1><A NAME="SECTION00090000000000000000">
8 File System Interfaces</A>
</H1>

<P>
This section covers issues related to accessing PVFS file systems.

<P>

<H2><A NAME="SECTION00091000000000000000">
8.1 How do I get MPI-IO for PVFS?</A>
</H2>

<P>
The ROMIO MPI-IO implementation, as provided with MPICH2 and others, supports
PVFS.  You can find more information in the ROMIO section of the
pvfs2-quickstart: <TT><A NAME="tex2html13"
  HREF="http://www.pvfs.org/pvfs2/pvfs2-quickstart.html#sec:romio">http://www.pvfs.org/pvfs2/pvfs2-quickstart.html#sec:romio</A></TT>
<P>

<H2><A NAME="SECTION00092000000000000000">
8.2 Can I directly manipulate PVFS files on the PVFS servers
            without going through some client interface?</A>
</H2>

<P>
You can, yes, but you probably should not.  The PVFS developers are not
likely to help you out if you do this and something gets messed up...

<P>

<H1><A NAME="SECTION000100000000000000000">
9 Management</A>
</H1>

<P>
This section covers questions about managing PVFS file systems.

<P>

<H2><A NAME="SECTION000101000000000000000">
9.1 How can I back up my PVFS file system?</A>
</H2>

<P>
The default storage implementation for PVFS (called Trove DBPF for ``DB Plus
Files'') stores all file system data held by a single server in a single
subdirectory.  In that subdirectory is a directory tree containing UNIX files
with file data and metadata.
This entire directory tree can be backed up in any manner you like and
restored if problems occur.

<P>
As a side note, this was not possible in PVFS v1, and is one of the many
improvements present in the new system.

<P>

<H2><A NAME="SECTION000102000000000000000">
9.2 Can I add, remove, or change the order of the PVFS servers
            on an existing PVFS file system?</A>
</H2>

<P>
You can add and change the order of PVFS servers for an existing PVFS file
system.  At this time, you must stop all the servers in order to do so.

<P>
To add a new server:

<OL>
<LI>Unmount all clients
</LI>
<LI>Stop all servers
</LI>
<LI>Edit your config file to:
  
<OL>
<LI>Add a new Alias for the new server
</LI>
<LI>Add a new DataHandleRange for the new server (picking a range you
        didn't previously use)
  
</LI>
</OL>
</LI>
<LI>Deploy the new config file to all the servers, including the new one
</LI>
<LI>Create the storage space on the new server
</LI>
<LI>Start all servers
</LI>
<LI>Remount clients
</LI>
</OL>

<P>
To reorder the servers (causing round-robin to occur in a different relative
order):

<OL>
<LI>Unmount all clients
</LI>
<LI>Stop all servers
</LI>
<LI>Edit your config file to reorder the DataHandleRange entries
</LI>
<LI>Deploy the new config file to all the servers
</LI>
<LI>Start all servers
</LI>
<LI>Remount clients
</LI>
</OL>

<P>
Note that adding a new server will <I>not</I> cause existing datafiles to be
placed on the new server, although new ones will be (by default).  Migration
tools are necessary to move existing datafiles (see
Question&nbsp;<A HREF="pvfs2-faq.php#sec:migration">9.3</A>) both in the case of a new server, or if you
wanted to migrate data off a server before removing it.

<P>

<H2><A NAME="SECTION000103000000000000000"></A>
<A NAME="sec:migration"></A>
<BR>
9.3 Are there tools for migrating data between servers?
</H2>

<P>
Not at this time, no.

<P>

<H2><A NAME="SECTION000104000000000000000"></A>
<A NAME="sec:df-free-space"></A>
<BR>
9.4 Why does df show less free space than I think it should? What
            can I do about that?
</H2>

<P>
PVFS uses a particular algorithm for calculating the free space on a file
system that takes the minimum amount of space free on a single server and
multiplies this value by the number of servers storing file data.
This algorithm was chosen because it provides a lower-bound on the amount of
data that could be stored on the system at that point in time.

<P>
If this value seems low, it is likely that one of your servers has less space
than the others (either physical space, or because someone has put some other
data on the same local file system on which PVFS data is stored).  The
<TT>pvfs2-statfs</TT> utility, included with PVFS, can be used to check the
amount of free space on each server, as can the <TT>karma</TT> GUI.

<P>

<H2><A NAME="SECTION000105000000000000000">
9.5 Does PVFS have a maximum file system size? If so, what is it?</A>
</H2>

<P>
PVFS uses a 64-bit value for describing the offsets into files, so
theoretically file sizes are virtually unlimited.  However, in practice other
system constraints place upper bounds on the size of files and file systems.

<P>
To best calculate maximum file and file system sizes, you should determine the
maximum file and file system sizes for the local file system type that you are
using for PVFS server storage and multiply these values by the number of
servers you are using.

<P>

<H2><A NAME="SECTION000106000000000000000"></A>
<A NAME="sec:mountintr"></A>
<BR>
9.6 Mouning PVFS with the interrupt option
</H2>
The PVFS kernel module supports the <TT>intr</TT> option provided by
network file systems.  This allows applications to be sent kill signals
when a filesystem is unresponsive (due to network failures, etc.).  The
option can be specified at mount time:
<PRE>
mount -t pvfs2 -o intr tcp://hosta:3334/pvfs2-fs /pvfs-storage/
</PRE>

<P>

<H1><A NAME="SECTION000110000000000000000">
10 Missing Features</A>
</H1>

<P>
This section discusses features that are not present in PVFS that are present
in some other file systems.

<P>

<H2><A NAME="SECTION000111000000000000000">
10.1 Why don't hardlinks work under PVFS?</A>
</H2>

<P>
We didn't implement hardlinks, and there is no plan to do so.  Symlinks are
implemented.

<P>

<H2><A NAME="SECTION000112000000000000000">
10.2 Can I <TT>mmap</TT> a PVFS file?</A>
</H2>

<P>
Private, read-only mmapping of files is supported.  Shared mmapping of files
is not.  Supporting this would force a great deal of additional infrastructure
into PVFS that would compromise the design goals of simplicity and
robustness.  This ``feature'' was intentionally left out, and it will remain
so.

<P>

<H2><A NAME="SECTION000113000000000000000">
10.3 Will PVFS store new files on servers with more space, allowing
            files to be stored when one server runs out of space?</A>
</H2>

<P>
No.  Currently PVFS does not intelligently place new files based on free
space.  It's a good idea, and possible, but we have not done this yet.  See
Section&nbsp;<A HREF="pvfs2-faq.php#sec:contributing">11.1</A> for notes on how you could help get this
feature in place.

<P>

<H2><A NAME="SECTION000114000000000000000">
10.4 Does PVFS have locks?</A>
</H2>

<P>
No.  Locking subsystems add a great deal of shared state to a parallel file 
system implementation, and one of the primary design goals was to eliminate
shared state in PVFS.  This results in a simpler, more fault tolerant
overall system than would have been possible had we integrated locking into
the file system.

<P>
It's possible that an add-on locking subsystem will be developed at some point;
however, there is no plan to build such a system at this time.

<P>

<H1><A NAME="SECTION000120000000000000000">
11 Helping Out</A>
</H1>

<P>
This section covers ways one could contribute to the PVFS project.

<P>

<H2><A NAME="SECTION000121000000000000000"></A>
<A NAME="sec:contributing"></A>
<BR>
11.1 How can I contribute to the PVFS project?
</H2>

<P>
There are lots of ways to directly or indirectly contribute to the PVFS
project.  Reporting bugs helps us make the system better, and describing your
use of the PVFS system helps us better understand where and how PVFS is
being deployed.

<P>
Even better, patches that fix bugs, add features, or support new hardware are
very welcome!  The PVFS community has historically been a friendly one, and we
encourage users to discuss issues and exchange ideas on the mailing lists.

<P>
If you're interested in this type of exchange, we suggest joining the PVFS2
Developers List, grabbing the newest CVS version of the code, and seeing what
is new in PVFS.  See <TT><A NAME="tex2html14"
  HREF="http://www.pvfs.org/pvfs2/developers.html">http://www.pvfs.org/pvfs2/developers.html</A></TT> for more
details.

<P>

<H1><A NAME="SECTION000130000000000000000">
12 Implementation Details</A>
</H1>

<P>
This section answers questions regarding specific components of the
implementation.  It is most useful for people interested in augmenting or
modifying PVFS.

<P>

<H2><A NAME="SECTION000131000000000000000">
12.1 BMI</A>
</H2>

<P>
This section specifically covers questions about the BMI interface and
implementations.

<P>

<H3><A NAME="SECTION000131100000000000000">
12.1.1 What is the maximum packet size for BMI?</A>
</H3>

<P>
Each BMI module is allowed to define its own maximum message size.  See
<TT>BMI_tcp_get_info</TT>, <TT>BMI_gm_get_info</TT>, and
<TT>BMI_ib_get_info</TT> for examples of the maximum sizes that each of the
existing modules support.  The maximum should be reported when you issue a
<TT>get_info</TT> call with the option set to <TT>BMI_CHECK_MAXSIZE</TT>.
Higher level components of PVFS perform these checks in order to make sure
that they don't choose buffer sizes that are too large for the underlying
network.

<P>

<H3><A NAME="SECTION000131200000000000000">
12.1.2 What happens if I try to match a BMI send with a BMI receive
               that has too small a buffer?</A>
</H3>

<P>
If the receive buffer is too small for the incoming message, then the
communication will fail and an error will be reported if possible.  We
don't support any semantics for receiving partial messages or anything like
that.  Its ok if the receive buffer is too big, though.

<P>

<H1><A NAME="SECTION000140000000000000000">
About this document ...</A>
</H1>
 <STRONG>Frequently Asked Questions about PVFS</STRONG><P>
This document was generated using the
<A HREF="http://www.latex2html.org/"><STRONG>LaTeX</STRONG>2<tt>HTML</tt></A> translator Version 2002 (1.62)
<P>
Copyright &#169; 1993, 1994, 1995, 1996,
<A HREF="http://cbl.leeds.ac.uk/nikos/personal.html">Nikos Drakos</A>, 
Computer Based Learning Unit, University of Leeds.
<BR>
Copyright &#169; 1997, 1998, 1999,
<A HREF="http://www.maths.mq.edu.au/~ross/">Ross Moore</A>, 
Mathematics Department, Macquarie University, Sydney.
<P>
The command line arguments were: <BR>
 <STRONG>latex2html</STRONG> <TT>-split 0 -show_section_numbers -nonavigation -init_file /tmp/pvfs-2.8.1/doc/latex2html-init pvfs2-faq.tex</TT>
<P>
The translation was initiated by Samuel Lang (ANL) on 2009-03-02
<BR><HR>
<ADDRESS>
Samuel Lang (ANL)
2009-03-02
</ADDRESS></table></table></table><?include("../../../../bottom.php"); ?>
</BODY>
</HTML>
