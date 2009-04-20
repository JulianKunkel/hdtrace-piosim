# LaTeX2HTML 2002 (1.62)
# Associate labels original text with physical files.


$key = q/sec:apis/;
$external_labels{$key} = "$URL/" . q|pvfs2-guide.html|; 
$noresave{$key} = "$nosave";

$key = q/sec:basics/;
$external_labels{$key} = "$URL/" . q|pvfs2-guide.html|; 
$noresave{$key} = "$nosave";

1;


# LaTeX2HTML 2002 (1.62)
# labels from external_latex_labels array.


$key = q/sec:apis/;
$external_latex_labels{$key} = q|5|; 
$noresave{$key} = "$nosave";

$key = q/sec:basics/;
$external_latex_labels{$key} = q|2|; 
$noresave{$key} = "$nosave";

1;

