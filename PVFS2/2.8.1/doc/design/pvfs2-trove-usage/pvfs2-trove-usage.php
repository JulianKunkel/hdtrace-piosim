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
<TITLE>Trove and PVFS2</TITLE>
<META NAME="description" CONTENT="Trove and PVFS2">
<META NAME="keywords" CONTENT="pvfs2-trove-usage">
<META NAME="resource-type" CONTENT="document">
<META NAME="distribution" CONTENT="global">

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
<META NAME="Generator" CONTENT="jLaTeX2HTML v2002 JA patch-1.4">
<META HTTP-EQUIV="Content-Style-Type" CONTENT="text/css">

<LINK REL="STYLESHEET" HREF="pvfs2-trove-usage.css">

<? include("../../../../header.php"); ?></HEAD>

<?include("../../../..//top.php"); ?> <body id="documentation">

<P>
<H1 ALIGN="LEFT">Trove and PVFS2</H1>
<P ALIGN="LEFT"><STRONG>PVFS Development Team</STRONG></P>

<P>

<H1><A NAME="SECTION00010000000000000000">
1 Motivation and Goals</A>
</H1>

<P>
The purpose of this document is to describe the use of Trove in PVFS2.

<P>
PVFS2 deals with four basic types of objects:

<UL>
<LI>directories
</LI>
<LI>metafiles
</LI>
<LI>datafiles
</LI>
<LI>symlinks
</LI>
</UL>

<P>
We will discuss how Trove is used to store these objects in the upcoming
sections.  Additionally we will cover how PVFS2 bootstraps from the Trove
perspective; that is, how it obtains a ``root handle'' and so on.

<P>

<H1><A NAME="SECTION00020000000000000000">
2 Current Implementation (03/21/2003)</A>
</H1>

<P>
This section describes the implementation as of the date above.  Eventually,
when the implementation catches up with the long term plan, this entire
section will probably disappear.

<P>

<H2><A NAME="SECTION00021000000000000000">
2.1 PVFS2 Objects</A>
</H2>

<P>
At this time the type of an object is stored in at least one place, the
dataspace attributes.  These can be retrieved using
<TT>job_trove_dspace_getattr</TT>.  Additionally, as we will see,
directories and metafiles store metadata as keyvals.

<P>
Directories are of type <TT>PVFS_TYPE_DIRECTORY</TT>.  They are actually
stored as two dataspaces in the current implementation.
The first dataspace is used solely to store the attributes of the directory
(under the key <TT>metadata</TT>, as a <TT>PVFS_object_attr</TT> structure)
and, if entries have been created, the handle of a second dataspace where the
directory entries are stored (under the key <TT>dir_ent</TT>, as a
<TT>PVFS_handle</TT> type).
The second dataspace is marked as type <TT>PVFS_TYPE_DIRDATA</TT> to
differentiate it.  This one holds the directory entries, with keys being
short names of files in the directory and values being the corresponding handle
(stored as a <TT>PVFS_handle</TT> type).  This dataspace is created lazily
when the first entry is created (in the <TT>crdirent</TT> state machine).

<P>
Metafiles are made up of a single dataspace and are of type
<TT>PVFS_TYPE_METAFILE</TT>.  Basic attributes
are stored in a keyval under the key <TT>metadata</TT>, as a
<TT>PVFS_object_attr</TT> structure, just as in the directory case.
An additional keyval (<TT>datafile_handles</TT>) stores the array of datafile
handles as <TT>PVFS_handle</TT> types.  A final keyval
(<TT>metafile_dist</TT>) stores the distribution information (in some
arbitrary format at this time).

<P>
Datafiles are made up of a single Trove dataspace of type
<TT>PVFS_TYPE_DATAFILE</TT>.  Currently there are no attributes stored for
datafiles, and all data is stored in the bstream.

<P>
Symlinks are not currently implemented, but the intention is to use a keyval
to hold the target of the link.

<P>

<H2><A NAME="SECTION00022000000000000000">
2.2 Bootstrapping</A>
</H2>

<P>
WHERE DO WE GET THE ROOT HANDLE?

<P>
WHAT ELSE?

<P>

<H1><A NAME="SECTION00030000000000000000">
3 Long Term Plan</A>
</H1>

<P>
This section describes the (probably moving) target for how we will use Trove
to store PVFS2 objects.  Eventually, as we progress, this will start to
describe interesting things such as storing small files in the metafile...

<P>

<H2><A NAME="SECTION00031000000000000000">
3.1 PVFS2 Objects</A>
</H2>

<P>
The biggest overall change is the move to using dataspace attributes for
storing basic metadata for PVFS2 objects.

<P>
Directories are made up of a single dataspace.  Dataspace attributes are used
to store basic metadata.  Keyvals in the dataspace are used to store the
directory entries in (short name, handle) format as before.

<P>
Metafiles are made up of a single dataspace.  Basic metadata is stored in the
dataspace attributes as with directories.  The keyval space is used to store
additional attributes, including the list of datafile handles (TODO: WHAT
ELSE?).

<P>
Datafiles are made up of a single Trove dataspace, with basic metadata stored
in the dataspace attributes and data stored in the bstream (TODO: WHAT KEY?).

<P>
Symlinks are made up of a single Trove dataspace with basic metadata (if any?)
stored in the dataspace attributes and target stored as a keyval (TODO: WHAT KEY?).

<P>

<H2><A NAME="SECTION00032000000000000000">
3.2 Bootstrapping</A>
</H2>

<P>
WHERE DO WE GET THE ROOT HANDLE?

<P>
WHAT ELSE?

<P>

<H1><A NAME="SECTION00040000000000000000">
About this document ...</A>
</H1>
 <STRONG>Trove and PVFS2</STRONG><P>
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
 <STRONG>latex2html</STRONG> <TT>-split 0 -show_section_numbers -nonavigation pvfs2-trove-usage.tex</TT>
<P>
The translation was initiated by Samuel Lang (ANL) on 2009-03-02
<BR><HR>
<ADDRESS>
Samuel Lang (ANL)
2009-03-02
</ADDRESS></table></table></table><?include("../../../../bottom.php"); ?>
</BODY>
</HTML>
