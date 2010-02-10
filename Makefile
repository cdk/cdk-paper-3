all: bmc_article.pdf
	@echo "********* Latex Summary *********"
	@grep -i error bmc_article.log || true
	@grep -i warning bmc_article.log || true

update: bmc_article.pdf

bmc_article.bbl: bmc_article.bib
	pdflatex bmc_article || true
	bibtex bmc_article || true

bmc_article.pdf: bmc_article.tex bmc_article.bbl
	pdflatex bmc_article.tex
	pdflatex bmc_article.tex
	pdflatex bmc_article.tex

distclean: clean

clean:
	rm -f *.bbl *.aux bmc_article.pdf *.blg *.log *.ps *.fff *.lof *.lot *.ttt *.dvi *~ *.Rout
