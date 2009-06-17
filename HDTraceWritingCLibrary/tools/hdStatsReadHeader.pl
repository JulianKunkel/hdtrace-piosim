#!/usr/bin/env perl
#
# Author: Stephan Krempel <stephan.krempel@gmx.de>
# Version: $Id$

use strict;
use warnings;


print $ARGV[0] . " ";

my $file;
open($file, $ARGV[0]);

my $header_length;
read($file, $header_length, 6);

if ($header_length !~ /^[0-9]{5}\n$/)
{
	die("Failed to read header length")
}

$header_length =~ s/\n//;

my $header;
read($file, $header, $header_length);

$header =~ s/[ \t\n]*</</gs;
$header =~ s/>[ \t\n]*/>/gs;


sub readTags($);

readTags($header);

sub readTags($)
{
	my $str = shift;
	
	my $tag;
	my $attribs;
	my $content;
	our $x;

	while( $str =~ /<([A-Za-z][A-Za-z0-9_\[\]]*)(?{$x=$^N})(.*?)>(.*)<\/(??{$x})>/
			|| $str =~ /<([A-Za-z][A-Za-z0-9_\[\]]*)(.*?)\/>/ )
	{
		$tag = $1;
		$attribs = $2;
		$content = $3;
		$str = $';

#		print "T:" . $tag . "\n\n";
#		print "A:" . $attribs . "\n\n";
#		print "C:" . $content . "\n\n";
#		print "H:" . $str . "\n\n";

		if ($tag =~ /Value/
				&& $attribs =~ / name="(.*)".* type="([A-Z0-9]+)"/)
		{
			print $1 . "=" . $2 . " ";
		}
		
		readTags($content) if defined($content);
	}
}