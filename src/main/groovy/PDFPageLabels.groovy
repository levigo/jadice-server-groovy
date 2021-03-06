/*
Copyright (c) 2016, levigo holding gmbh.
All rights reserved.

Redistribution and use in source and binary forms, with or without modification, 
are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, 
   this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, 
   this list of conditions and the following disclaimer in the documentation
   and/or other materials provided with the distribution.
 * Neither the name of levigo holding gmbh nor the names of its contributors
   may be used to endorse or promote products derived from this software 
   without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE 
POSSIBILITY OF SUCH DAMAGE.

*/

import java.util.List

import com.levigo.jadice.server.nodes.worker.StreamScriptContext
import com.levigo.jadice.server.script.groovy.StreamScriptSupport
import com.levigo.jadice.server.shared.types.Stream
import com.levigo.jadice.server.shared.types.StreamDescriptor
import com.levigo.jadice.server.script.groovy.Pipeline
import com.levigo.jadice.server.script.groovy.PipelineBuilder
import com.levigo.jadice.server.script.groovy.StreamScriptSupport
import com.levigo.jadice.server.shared.types.StreamDescriptor
import com.levigo.jadice.server.shared.types.*
import com.levigo.jadice.server.util.*

import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.common.PDPageLabelRange
import org.apache.pdfbox.pdmodel.common.PDPageLabels

class PDFPageLabels {
	// Configurable values
	
	/**
	 * Prefix of the page labels
	 */
	String prefix = null
	
	/**
	 * The Style of the page label.
	 *   Possible values are:
	 *     - null: apply no numbering
	 *     - "DECIMAL" (default) : 1, 2, 3, ...
	 *     - "ROMAN_UPPER": I, II, III, ...
	 *     - "ROMAN_LOWER": i, ii, iii, ...
	 *     - "LETTERS_UPPER": A, B, C, ...
	 *     - "LETTERS_LOWER": a, b, c, ...
	 */
	 String style = "decimal"

	// Runtime values
	StreamScriptContext streamContext
	StreamScriptSupport support
  
	def main(foo) {
		support = new StreamScriptSupport(streamContext)	
		support.info "Start PDF Reordering Handling"
	
		def input
		while (input = support.nextInput()) {
			// Read document with Apache PDF Box and alter the page labels
			PDDocument document = PDDocument.load(input.inputStream)
			PDPageLabels labels = new PDPageLabels(document, document.getDocumentCatalog().getCOSObject())
			PDPageLabelRange range = new PDPageLabelRange(
				prefix: prefix,
				style: selectStyle()
			)
			labels.setLabelItem(0, range)
			document.documentCatalog.pageLabels = labels
			
			// Save the resulting document
			OutputStream os = streamContext.output(input.descriptor)
			document.save(os)
			document.close()
		} // End while (input=support.input())
	}
	
	private String selectStyle() {
		switch (style?.toUpperCase()) {
			case null:
				return null
			case "DECIMAL":
				return PDPageLabelRange.STYLE_DECIMAL
			case "ROMAN_UPPER":
				return PDPageLabelRange.STYLE_ROMAN_UPPER
			case "ROMAN_LOWER":
				return PDPageLabelRange.STYLE_ROMAN_LOWER
			case "LETTERS_UPPER":
				return PDPageLabelRange.STYLE_LETTERS_UPPER
			case "LETTERS_LOWER":
				return PDPageLabelRange.STYLE_LETTERS_LOWER
			default:
				support.fail("Unknown style '${style}'")
				return null
		}
	}
}