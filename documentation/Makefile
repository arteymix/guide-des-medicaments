rapport-final-complet.pdf: rapport-final.pdf
	pdfunite $< $(shell find -name 'prototype-*-*.pdf' | sort) $@

rapport-final.pdf: rapport-final.md
	pandoc $< -o $@
