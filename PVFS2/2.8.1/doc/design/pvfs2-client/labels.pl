# LaTeX2HTML 2002 (1.62)
# Associate labels original text with physical files.


$key = q/figure:generic-sm/;
$external_labels{$key} = "$URL/" . q|pvfs2-client.html|; 
$noresave{$key} = "$nosave";

$key = q/figure:arch/;
$external_labels{$key} = "$URL/" . q|pvfs2-client.html|; 
$noresave{$key} = "$nosave";

1;


# LaTeX2HTML 2002 (1.62)
# labels from external_latex_labels array.


$key = q/figure:generic-sm/;
$external_latex_labels{$key} = q|2|; 
$noresave{$key} = "$nosave";

$key = q/figure:arch/;
$external_latex_labels{$key} = q|1|; 
$noresave{$key} = "$nosave";

1;

