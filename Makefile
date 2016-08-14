all: article.pdf
	@echo "********* Latex Summary *********"
	@grep -i error article.log | grep -v infwarerr || true
	@grep -i warning article.log | grep -v infwarerr || true

update: article.pdf

article.bbl: article.bib
	pdflatex article || true
	bibtex article || true

article.pdf: article.tex article.bbl
	pdflatex article.tex
	pdflatex article.tex
	pdflatex article.tex

distclean: clean

clean:
	rm -f *.bbl *.aux bmc_article.pdf *.blg *.log *.ps *.fff *.lof *.lot *.ttt *.dvi *~ *.Rout
