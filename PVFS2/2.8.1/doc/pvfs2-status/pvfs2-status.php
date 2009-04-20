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
<TITLE>Current PVFS2 status</TITLE>
<META NAME="description" CONTENT="Current PVFS2 status">
<META NAME="keywords" CONTENT="pvfs2-status">
<META NAME="resource-type" CONTENT="document">
<META NAME="distribution" CONTENT="global">

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
<META NAME="Generator" CONTENT="jLaTeX2HTML v2002 JA patch-1.4">
<META HTTP-EQUIV="Content-Style-Type" CONTENT="text/css">

<LINK REL="STYLESHEET" HREF="pvfs2-status.css">

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
<H1 ALIGN="LEFT">Current PVFS2 status</H1>
<P ALIGN="LEFT"><STRONG>PVFS2 Development Team </STRONG></P>
<P ALIGN="LEFT"><STRONG> Last Updated: September 2003 </STRONG></P>

<P>
<BR>

<H2><A NAME="SECTION00010000000000000000">
Contents</A>
</H2>
<!--Table of Contents-->

<UL>
<LI><A NAME="tex2html9"
  HREF="pvfs2-status.php">1 Introduction</A>
<LI><A NAME="tex2html10"
  HREF="pvfs2-status.php#SECTION00030000000000000000">2 Known limitiations and missing features</A>
<LI><A NAME="tex2html11"
  HREF="pvfs2-status.php#SECTION00040000000000000000">3 Experimental features</A>
<LI><A NAME="tex2html12"
  HREF="pvfs2-status.php#SECTION00050000000000000000">4 Placeholder / depricated code</A>
<LI><A NAME="tex2html13"
  HREF="pvfs2-status.php#SECTION00060000000000000000">5 Open issues</A>
<LI><A NAME="tex2html14"
  HREF="pvfs2-status.php#SECTION00070000000000000000">6 Good examples</A>
<LI><A NAME="tex2html15"
  HREF="pvfs2-status.php#SECTION00080000000000000000">About this document ...</A>
</UL>
<!--End of Table of Contents-->
<P>

<P>

<P>

<P>

<H1><A NAME="SECTION00020000000000000000">
1 Introduction</A>
</H1>

<P>
This document describes the current status of PVFS2 development.  This document
only includes issues related to functionality or correctness.  No 
performance optimizations are listed for now.

<P>

<H1><A NAME="SECTION00030000000000000000">
2 Known limitiations and missing features</A>
</H1>

<P>
This section lists file system limitations for which we have a known
solution or plan.

<P>

<UL>
<LI>efficient conversion of MPI datatypes to PVFS2 datatypes in ROMIO
</LI>
<LI>hooks for tuning consistency semantics
</LI>
<LI>hooks for controlling distribution and distribution parameters
</LI>
<LI>standardizing error code format
</LI>
<LI>integration of user level buffer cache
</LI>
<LI>eliminating memory leaks
</LI>
<LI>consistent error handling in client and server state machines
</LI>
<LI>simple failover plan
</LI>
</UL>

<P>

<H1><A NAME="SECTION00040000000000000000">
3 Experimental features</A>
</H1>

<P>
These are features that are implemented but have not been thoroughly tested.

<P>

<UL>
<LI>GM network support
</LI>
<LI>IB network support
</LI>
</UL>

<P>

<H1><A NAME="SECTION00050000000000000000">
4 Placeholder / depricated code</A>
</H1>

<P>
These parts of the code have a working implementation, but we intend to
replace them as time permits.

<P>

<UL>
<LI>``contig'' request encoder implementation
</LI>
<LI>pvfs2-client implementation
</LI>
</UL>

<P>

<H1><A NAME="SECTION00060000000000000000">
5 Open issues</A>
</H1>

<P>
The items on this list are known problems that have not been resolved.

<P>

<UL>
<LI>access control / security
</LI>
<LI>how to manage client side configuration (fstab information)
</LI>
<LI>support for 2.4 series kernels
</LI>
<LI>how to add file systems to an existing system interface run time instance (proper vfs bootstrapping)
</LI>
<LI>managing server configuration files
</LI>
<LI>nonblocking I/O at system interface
</LI>
<LI>how to handle I/O failures, unposting, etc.
</LI>
<LI>TCP module scalability
</LI>
<LI>extended attributes
</LI>
<LI>redundancy
</LI>
</UL>

<P>

<H1><A NAME="SECTION00070000000000000000">
6 Good examples</A>
</H1>

<P>
This section points out specific areas of the code that demonstrate 
best practice for PVFS2 development.

<P>

<UL>
<LI>?
</LI>
</UL>

<P>

<H1><A NAME="SECTION00080000000000000000">
About this document ...</A>
</H1>
 <STRONG>Current PVFS2 status</STRONG><P>
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
 <STRONG>latex2html</STRONG> <TT>-split 0 -show_section_numbers -nonavigation -init_file /tmp/pvfs-2.8.1/doc/latex2html-init pvfs2-status.tex</TT>
<P>
The translation was initiated by Samuel Lang (ANL) on 2009-03-02
<BR><HR>
<ADDRESS>
Samuel Lang (ANL)
2009-03-02
</ADDRESS></table></table></table><?include("../../../../bottom.php"); ?>
</BODY>
</HTML>
