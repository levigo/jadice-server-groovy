/*
Copyright (c) 2015, levigo holding gmbh.
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

import com.levigo.jadice.server.nodes.worker.StreamScriptContext
import com.levigo.jadice.server.nodes.worker.StreamScriptContext
import com.levigo.jadice.server.script.groovy.StreamScriptSupport
import groovy.lang.Closure

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

class PDFReorder {
	// Configurable value
	List<Integer> newOrder = [0]

	// Runtime values
	StreamScriptContext streamContext
	StreamScriptSupport support
  
	def main(foo) {
		support = new StreamScriptSupport(streamContext)	
		support.info "Start PDF Reordering Handling"
	
		def input
		while (input = support.nextInput()) {
			List<byte[]> pages = []
			List<StreamDescriptor> descriptors = []
		
			support.info "Phase 1: Splitting PDF document into single pages"
			StreamBundle splittedPDF = support.process() {
				com.levigo.jadice.server.pdf.PDFSplitNode()
			}
			
			splittedPDF.eachWithIndex() { stream, idx ->
				// Performance enhancement: Consider only these pages given in the new order
				if (!newOrder.contains(idx)) {
					support.debug "Dropping page $idx"
					return
				}
				pages[idx] = toByteArray(stream.inputStream)
				descriptors[idx] = stream.descriptor
			}
			
			support.info "Phase 2: Reordering PDF pages"
			def streams = []
			newOrder.each() { it ->
				if (!pages[it]) {
					support.error "Given input PDF has no page $it"
					return
				}
				streams << new BundledStream(new ByteArrayInputStream(pages[it]), descriptors[it])
			}
			
			support.info "Phase 3: Merging reordered PDF pages"
			support.output(streams) {
				com.levigo.jadice.server.pdf.PDFMergeNode()
			}
	  
		} // End while (input=support.input())
	}
	
	def byte[] toByteArray(InputStream is) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream()
		Util.copyAndClose(is, baos)
		return baos.toByteArray()
	}
}