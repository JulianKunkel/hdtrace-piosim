all:
	bibtex --min-crossrefs=0 ideas-design.aux || echo -n "NOO"
	pdflatex --interaction nonstopmode --file-line-error-style ideas-design.tex
	rm *log *lot *out *lof 

thumbs:
	pdflatex ideas-design.tex
	thumbpdf ideas-design.pdf
	pdflatex ideas-design.tex

view:
	kpdf ideas-design.pdf

clean:
	rm *.pdf *.bbl *.aux *.blg *log *lot *out *lof *~ *backup *toc

