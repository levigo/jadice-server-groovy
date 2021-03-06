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
import com.levigo.jadice.server.nodes.worker.StreamScriptContext;
import com.levigo.jadice.server.script.groovy.StreamScriptSupport;
import groovy.lang.Closure;

import java.util.List;

import com.levigo.jadice.server.nodes.worker.StreamScriptContext;
import com.levigo.jadice.server.script.groovy.StreamScriptSupport;
import com.levigo.jadice.server.shared.types.Stream;
import com.levigo.jadice.server.shared.types.StreamDescriptor;
import com.levigo.jadice.server.script.groovy.Pipeline
import com.levigo.jadice.server.script.groovy.PipelineBuilder
import com.levigo.jadice.server.script.groovy.StreamScriptSupport
import com.levigo.jadice.server.shared.types.StreamDescriptor
import com.levigo.jadice.server.shared.types.*
import com.levigo.jadice.server.util.*

class PDFPreview {
	// Configurable value
	int maxPageCount = 5

	// Runtime values
	StreamScriptContext streamContext
	StreamScriptSupport support
  
	def main(foo) {
		support = new StreamScriptSupport(streamContext)	
		support.info("Start PDF Preview Handling")
	
		List<String> routingRules = [com.levigo.jadice.server.nodes.RouterNode.STREAM_NUMBER_PATTERN + "< " + maxPageCount];
	
		def input
		while (input = support.nextInput()) {
			// Splitting PDF and re-assembling it with the first $maxPageCount pages only
			support.output() {
				com.levigo.jadice.server.pdf.PDFSplitNode()
				com.levigo.jadice.server.nodes.RouterNode(
					routingRules : routingRules
				)
				com.levigo.jadice.server.pdf.PDFMergeNode()
			}
	  
		} // End while (input=support.input())
	}
}