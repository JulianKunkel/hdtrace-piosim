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
<TITLE>Flow Design Document</TITLE>
<META NAME="description" CONTENT="Flow Design Document">
<META NAME="keywords" CONTENT="flow-design">
<META NAME="resource-type" CONTENT="document">
<META NAME="distribution" CONTENT="global">

<META HTTP-EQUIV="Content-Type" CONTENT="text/html; charset=iso-8859-1">
<META NAME="Generator" CONTENT="jLaTeX2HTML v2002 JA patch-1.4">
<META HTTP-EQUIV="Content-Style-Type" CONTENT="text/css">

<LINK REL="STYLESHEET" HREF="flow-design.css">

<? include("../../../../header.php"); ?></HEAD>

<?include("../../../..//top.php"); ?> <body id="documentation">

<P>
<H1 ALIGN="LEFT">Flow Design Document</H1>
<P ALIGN="LEFT"><STRONG>PVFS Development Team</STRONG></P>
<P ALIGN="LEFT"><STRONG>July 2002</STRONG></P>

<P>

<H1><A NAME="SECTION00010000000000000000">
1 TODO</A>
</H1>

<UL>
<LI>point to some other document for explanation of concepts common to all
pvfs2 I/O interfaces (contexts, max idle time, test semantics, etc.)
</LI>
</UL>

<P>

<H1><A NAME="SECTION00020000000000000000">
2 Concepts and Motivation</A>
</H1>

<P>
Flows are a high level model for how PVFS2 system components
will perform I/O.  It is designed to abstractly but efficiently move
data from source to destination, where source and destination may be
defined as storage devices, network devices, or memory regions.

<P>
Features include:

<P>

<UL>
<LI><I>Combining I/O mechanisms</I>  The flow interface combines
network I/O and disk I/O into a single framework with a scheduler that takes
both into account.

<P>
</LI>
<LI><I>Multiple protocols</I>
Actual I/O is carried out underneath the flow interface by
<I>flow protocols</I>.   We may implement several different
protocols (using different I/O or buffering techniques, for example)
which can be switched at runtime.

<P>
</LI>
<LI><I>Simple interface</I>  The application interface to
this system will be as high level and simple as possible.  Device
selection, scheduling, buffer management, and request pattern
processing will be transparent to the flow user.  

<P>
</LI>
<LI><I>Datatypes</I>  Flows allow the user to specify both memory
and file datatypes (similar to those used in MPI) , and will handle
breaking down these datatypes into a format that can be used by lower
level I/O interfaces.

<P>
</LI>
</UL>

<P>

<H1><A NAME="SECTION00030000000000000000">
3 Flows</A>
</H1>

<P>

<H2><A NAME="SECTION00031000000000000000">
3.1 Overview</A>
</H2>

<P>
A flow describes a movement of data.  The data always moves from a single
source to a single destination.  There may be (and almost always will be)
multiple flows in progress at the same time for different locations-
in particular for clients that are talking simultaneously to several
servers, or servers that are handling simultaneous I/O requests.

<P>
At the highest level abstraction, it is important that a flow describes
a movement of data in terms of ``what to do'' rather than ``how to do
it''.  For example, when a user sets up a flow, it may indicate that the
first 100 bytes of a file on a local disk should be sent to
a particular host on the network.  It will not specify what protocols to
use, how to buffer the data, or how to schedule the I/O.  All of this will be
handled underneath the flow interface.  The user just requests that a
high level I/O task be performed and then checks for completion until it
is done.

<P>
Note that the ``user'' in the above example is most likely a
system interface or server implementer in pvfs2.  End users will
be unaware of this API.

<P>
A single flow created on a server will match exactly one flow on a
client.  For example, if a single client performs a PVFS2 read, the
server will create a storage to network flow, and the client will
create a network to memory flow.  If a client communicates with N servers 
to complete an I/O operation, then it will issue N flows simultaneously.

<P>
Flows will not be used for exchanging request protocol messages
between the client and server (requests or acknowledgements).
They will only be used for data transfer.  It is assumed that request
messages will be used for handshaking before or after the flow as 
needed.

<P>

<H2><A NAME="SECTION00032000000000000000"></A>
<A NAME="sec:arch"></A>
<BR>
3.2 Architecture
</H2>

<P>
There are two major parts of the flow architecture, as seen in figure
<A HREF="flow-design.php#fig:flow-arch">1</A>.  The first is the
<I>flow interface</I>.  Applications (ie PVFS components) interact with
this interface.  It provides a consistent API regardless of what
protocols are in use, what scheduling is being performed, etc.  

<P>
The second major component of the architecture is the <I>flow
protocol</I>.
There may be many flow protocols active within one flow interface.  Each
flow protocol implements communication between a different pair of data
endpoint types.  For example, one flow protocol may link TCP/IP to
asynchronous unix I/O, while another may link VIA to memory regions.  For
two seperate hosts to communicate, they must share compatible flow
protocols (as indicated by the dotted line at the bottom of figure
<A HREF="flow-design.php#fig:flow-arch">1</A>).

<P>
Flow protocols all adhere to a strict interface 
and must provide the same expected functionality (which will be
described later).  Flow protocols take care of details such as buffering
and flow control if necessary.

<P>

<DIV ALIGN="LEFT"><A NAME="fig:flow-arch"></A><A NAME="105"></A>
<TABLE>
<CAPTION ALIGN="BOTTOM"><STRONG>Figure 1:</STRONG>
Basic flow architecture</CAPTION>
<TR><TD>
<DIV ALIGN="LEFT">
<IMG
 WIDTH="707" HEIGHT="422" ALIGN="BOTTOM" BORDER="0"
 SRC="img1.png"
 ALT="\includegraphics[scale=0.6]{flow-arch.eps}">

</DIV></TD></TR>
</TABLE>
</DIV>

<P>

<H2><A NAME="SECTION00033000000000000000">
3.3 Describing flows</A>
</H2>

<P>
Individual flows are represented using structures called <I>flow
descriptors</I>.  The source and destination of a given flow are represented
by structures called <I>endpoints</I>.  A flow descriptor may serve
many roles.  First of all, when created by a flow interface user, it
describes an I/O task that needs to be performed.  Once it is submitted
to the flow interface, it may keep up with state or progress information.
When the descriptor is finally completed and returned to the user, it will
indicate the status of the completed flow, whether successful or in error.

<P>
Flow endpoints describe the memory, storage, or network locations for
the movement of data.   All flow descriptors must have both a 
source and a destination endpoint.

<P>

<H2><A NAME="SECTION00034000000000000000">
3.4 Usage assumptions</A>
</H2>

<P>
It is assumed that all flows in PVFS2 will be <I>preceded</I> by a
PVFS2 request protocol exchange between the client and server.  In
a file system read case, the client will send a read request, and
the server will send an acknowledgement (which among other things
indicates how much data is available to be read).  In a file
system write case, the client will send a write request, and the
server will send an acknowledgement that indicates when it is safe
to begin the flow to send data to the server.  Once the flow is completed,
a trailing acknowledgment alerts the client that the server has 
completed the write operation.

<P>
The request protocol will transmit information such as file size and 
distribution parameters that may be needed to coordinate remote flows.

<P>

<H1><A NAME="SECTION00040000000000000000">
4 Data structures</A>
</H1>

<P>

<H2><A NAME="SECTION00041000000000000000"></A>
<A NAME="sec:flow-desc"></A>
<BR>
4.1 Flow descriptor
</H2>

<P>
Flow descriptors are created by the flow interface user.  At this time,
the caller may edit these fields directly.  Once the flow has been posted
for service, however, the caller may only interact with the descriptor
through functions defined in the flow interface.  It is not safe to
directly edit a flow descriptor while it is in progress.

<P>
Once a flow is complete, it is again safe to examine fields within
the descriptor (for example, to determine the status of the
completed flow).

<P>
Note that there is an endpoint specific to each type supported by
the flow interface (currently memory, BMI (network), and Trove
(storage)).

<P>
The following fields may be set by the caller prior to posting:

<UL>
<LI>src: source endpoint (BMI, memory, or Trove addressing information)
</LI>
<LI>dest: destination endpoint (BMI, memory, or Trove addressing information)
</LI>
<LI>tag: tag used to match up flows with particular operation sequences
</LI>
<LI>user_ptr: void* pointer reserved for use by the caller (may associate a 
flow with some higher level state structure, for example)
</LI>
<LI>type: specifies what kind of flow protocol to use for this flow
</LI>
<LI>file_req: file datatype (similar to MPI datatype)
</LI>
<LI>file_req_offset: offset into the file datatype
</LI>
<LI>mem_req: memory datatype (similar to MPI datatype) (optional)
</LI>
<LI>aggregate_size: total amount of data the flow should transfer (optional)
</LI>
<LI>file_data: struct containing state information about the file to access, used by the distribution subsystem
</LI>
</UL>

<P>
Special notes:  Both the mem_req and the aggregate_size fields are optional.
However, at least one of them <I>must</I> be set.  Otherwise the flow has 
no way to calculate how much data must be transferred.

<P>
The following fields may be read by the caller after completion of a flow:

<UL>
<LI>state: final state of flow (see enumerated values in flow.h)
</LI>
<LI>error_code: error code (nonzero if state indicates an error)
</LI>
<LI>total_transfered: amount of data moved by the flow
</LI>
</UL>

<P>
The following fields are reserved for use within the flow code:

<UL>
<LI>context_id: specifies which flow level context the descriptor belongs to
</LI>
<LI>flowproto_id: internal identifier for the flowprotocol used
</LI>
<LI>priority: priority level of flow (unused as of yet)
</LI>
<LI>sched_queue_link: for use by internal scheduler
</LI>
<LI>flow_protocol_data: void* reserved for flow protocol use
</LI>
<LI>file_req_state: current state of file datatype processing
</LI>
<LI>mem_req_state: current state of memory datatype processing
</LI>
<LI>result: result of each datatype processing iteration
</LI>
</UL>

<P>

<H1><A NAME="SECTION00050000000000000000">
5 Flow interface</A>
</H1>

<P>
The flow interface is the set of functions that the flow user is allowed
to interact with.  These functions allow you to do such things as create
flows, post them for service, and check for completion.

<P>

<UL>
<LI><I>PINT_flow_initialize()</I>: performs initial setup of flow
	interface - must be called once before any other flow interface functions
</LI>
<LI><I>PINT_flow_finalize()</I>: shuts down the flow interface
</LI>
<LI><I>PINT_flow_alloc()</I>: creates a new flow descriptor
</LI>
<LI><I>PINT_flow_free()</I>: frees up a flow descriptor that is
	no longer needed
</LI>
<LI><I>PINT_flow_reset()</I>: resets a previously used flow
	descriptor to its initial state and values.
</LI>
<LI><I>PINT_flow_set_priority()</I>: sets the priority of a
	particular flow descriptor.  May be called even when a flow is in
	service.
</LI>
<LI><I>PINT_flow_get_priority()</I>: reads the priority of a
	particular flow descriptor.  May be called even when a flow is in
	service.
</LI>
<LI><I>PINT_flow_post()</I>: submits a flow descriptor for
	service
</LI>
<LI><I>PINT_flow_setinfo()</I>: used to set optional
	interface parameters.
</LI>
<LI><I>PINT_flow_getinfo()</I>: used to read optional
	interface parameters.
</LI>
</UL>

<P>
Three functions are provided to test for completion of posted flows:

<P>

<UL>
<LI><I>PINT_flow_test()</I>: tests for completion of a single
	flow
</LI>
<LI><I>PINT_flow_testsome()</I>: tests for completion of any
   flows from a specified set of flows
</LI>
<LI><I>PINT_flow_testcontext()</I>: tests for completion of any
	flows that are in service in the interface 
</LI>
</UL>

<P>

<H1><A NAME="SECTION00060000000000000000">
6 Flow protocol interface</A>
</H1>

<P>
The flow protocols are modular components capable of moving data between
particular types of endpoints.  (See section <A HREF="flow-design.php#sec:arch">3.2</A> for an
overview).  Any flow protocol implementation must conform to a
predefined flow protocol interface in order to interoperate with the
flow system.  

<P>

<UL>
<LI><I>flowproto_initialize()</I>: Initializes the flow
	protocol (called exactly once before posting any flows)
</LI>
<LI><I>flowproto_finalize()</I>: shuts down the flow
	protocol (forceful terminating any pending flows)
</LI>
<LI><I>flowproto_post()</I>: posts a flow descriptor
</LI>
<LI><I>flowproto_find_serviceable()</I>: returns an array of active
	flows from the flow protocol that are either completed or in need of
	service
</LI>
<LI><I>flowproto_service()</I>: performs work on a single
	flow descriptor that is ready for service (as indicated by a
	flowproto_find_serviceable() function)
</LI>
<LI><I>flowproto_getinfo()</I>: reads optional parameters
	from the protocol
</LI>
<LI><I>flowproto_setinfo()</I>: sets optional protocol
	parameters
</LI>
</UL>

<P>
The following section describing the interaction between the flow component
and the flow protocols may be helpful in clarifying how the above functions
will be used.

<P>

<H1><A NAME="SECTION00070000000000000000">
7 Interaction between flow component and flow protocols</A>
</H1>

<P>
The flow code that resides above the flow protocols serves two primary
functions: multiplexing between the various flow protocols, and scheduling
work.

<P>
The multiplexing is handled by simply tracking all active flow protocols and
directing flow descriptors to the appropriate one. 

<P>
The scheduling functionality is the more complicated of the two
responsibilities of the flow code.  This responsibility leads to the 
design of the flow protocol interface and the states of the flow descriptors.
In order to understand these states, it is important to understand that 
flow protocols typically operate with a certain granularity that is 
defined by the flow protocol implementation.  For example, a flow protocol 
may transfer data 128 KB of data at a time.  A simple implementation of a memory to 
network flow may post a network send of 128 KB, wait for it to complete, then
post the next send of 128 KB, and so on.  Each of these iterations is driven by
the top level flow component.  In other words, the flow protocol is not 
autonomous.  Rather than work continuously once it receives a flow descriptor, 
it only performs one iteration of work at a time, and then waits for the 
flow component to tell it to continue.  This provides the flow interface
with an opportunity to schedule flows and choose which ones to service at 
each iteration.

<P>
When a flow descriptor is waiting for the flow component to allow it to
continue, then it is ``ready for service''.  The flow component may
then call the flowproto_service() function to allow it to continue.  In 
the above example, this would cause the flow protocol to post another network
send.  

<P>
In order to discover which flow descriptors are ``ready for service'' (and 
therefore must be scheduled), it calls flowproto_find_serviceable() for each
active flow protocol.  Thus, the service loop of the flow component looks something like this:

<P>

<OL>
<LI>call flowproto_find_serviceable() for each active flow protocol to
generate a list of flows to service
</LI>
<LI>run scheduling algorithm to build list of scheduled flows
</LI>
<LI>call flowproto_service() for each scheduled flow (in order)
</LI>
<LI>if a flow descriptor reaches the completed or error state (at any time),
then move it to a list of completed flow descriptors to be returned to the 
caller
</LI>
</OL>

<P>
The scheduling filter (at the time of this writing) does nothing but service
all flows in order.  More advanced schedulers will be added later.

<P>

<H2><A NAME="SECTION00071000000000000000">
7.1 Example flow protocol (implementation)</A>
</H2>

<P>
The default flow protocol is called "flowproto_bmi_trove'' and is capable of
handling the following endpoint combinations:

<P>

<UL>
<LI>BMI to memory 
</LI>
<LI>memory to BMI
</LI>
<LI>BMI to Trove
</LI>
<LI>Trove to BMI
</LI>
</UL>

<P>
The following summarizes what the principle flow protocol interface functions
do in this protocol:

<P>

<UL>
<LI>flowproto_post(): allocates any intermediate buffers that may be needed,
begins datatype processing
</LI>
<LI>flowproto_service(): posts the next necessary BMI and Trove operations
</LI>
<LI>flowproto_find_serviceable(): tests for completion of pending BMI and 
trove operations, drives the state of any flow descriptors affected by completion, and returns flow descriptors ready for service to caller
</LI>
</UL>

<P>
The flow protocol performs double buffering to keep both the Trove and BMI interfaces as busy as possible when transferring between the two.

<P>
The flow protocol does not have an internal thread.  However, if it detects that the job interface is using threads (through the __PVFS2_JOB_THREADED__ define), then it will use the job interface's thread manager to push on BMI and 
Trove operations.  The find_serviceable() function then just checks for
completion notifications from the thread callback functions, rather than
testing the BMI or Trove interfaces directly.

<P>
Trove support is compiled out if the __PVFS2_TROVE_SUPPORT__ define is
not detected.  This is mainly done in client libraries which do not 
need to use Trove in order to reduce library dependencies.

<P>

<H1><A NAME="SECTION00080000000000000000">
8 Implementation note: avoiding flows</A>
</H1>

<P>
The flow interface will introduce overhead for small operations
that would not otherwise be present.  It may therefore be helpful
to eventually introduce an optimization to avoid the use of flows
for small read or write operations.

<P>
<PRE>
text of an email discussion on this topic (&gt; part by Phil, non &gt;
part by Rob):

&gt; Yeah, we need to get these ideas documented somewhere.  There may actually
&gt; be a couple of eager modes.  By default, BMI only allows unexpected
&gt; messages &lt; 16K or so.  That places a cap on the eager write size,
&gt; unless we had a second eager mode that consists of a) send write request
&gt; b) send write data c) receive ack...

Yes.  These two modes are usually differentiated by the terms "short" and
"eager", where the "short" one puts the data actually into the same
packet/message (depending on the network layer at which we are working).

&gt; Of course all of this would need to be tunable so that we can see what
&gt; works well.  Maybe rules like:
&gt; 
&gt; contig writes &lt; 15K : simple eager write
&gt; 15K &lt; contig writes &lt; 64K : two part eager write
&gt; writes &gt; 64K &amp;&amp; noncontig writes : flow
&gt; 
&gt; contig reads &lt; 64K : eager read
&gt; contig reads &gt; 64K &amp;&amp; noncontig reads : flow

Yeah, something like that.
</PRE>

<P>

<H1><A NAME="SECTION00090000000000000000">
About this document ...</A>
</H1>
 <STRONG>Flow Design Document</STRONG><P>
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
 <STRONG>latex2html</STRONG> <TT>-split 0 -show_section_numbers -nonavigation flow-design.tex</TT>
<P>
The translation was initiated by Samuel Lang (ANL) on 2009-03-02
<BR><HR>
<ADDRESS>
Samuel Lang (ANL)
2009-03-02
</ADDRESS></table></table></table><?include("../../../../bottom.php"); ?>
</BODY>
</HTML>
