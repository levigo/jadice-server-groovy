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

import com.levigo.jadice.server.shared.types.*;
import com.levigo.jadice.server.util.*;
import com.levigo.jadice.server.script.groovy.StreamScriptSupport;
import com.levigo.jadice.server.nodes.worker.StreamScriptContext;

import com.levigo.jadice.document.Document;
import com.levigo.jadice.document.Page;
import com.levigo.jadice.document.read.Reader;
import com.levigo.jadice.annotation.Annotation;
import com.levigo.jadice.annotation.Annotations;
import com.levigo.jadice.annotation.embedded.EmbeddedAnnotation;
import com.levigo.jadice.annotation.embedded.EmbeddedAnnotations;

import groovy.lang.Closure;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.io.ByteArrayInputStream;

/*
 * Count how many annotations were found in the given documents and store it in the StreamDescriptor
 * of the outgoing stream (a stream with only dummy content!)
 *
 * Works on jadice server 5.1.3.0 and above
 */
class CountAnnotations {
  // Configurable values, can be overridden in ScriptNode configuration
  // --- none ---
  
  // Runtime values
  StreamScriptContext streamContext
  StreamScriptSupport support
  
  def main(foo) {
    support = new StreamScriptSupport(streamContext)    

    def input
    while (input = support.nextInput()) {
        support.info 'Loading document'
        final Reader reader = new Reader()
        reader.read(input.inputStream)
        reader.complete()
        final Document doc = reader.document
        
        final StreamDescriptor targetSD = new StreamDescriptor('only/metadata', input.descriptor)
        
        support.info "document has ${doc.pageCount} pages"
        targetSD.properties.put('page-count', doc.pageCount)
        
        final int[] annoCount = new int[doc.pageCount]
        targetSD.properties.put('annotation-count', annoCount)

        doc.pages.eachWithIndex{ page, idx -> 
            final Collection<Annotation> annotations = Annotations.get(page)
            final Collection<EmbeddedAnnotation> embeddedAnnotations = EmbeddedAnnotations.get(page)

            // XXX: Starting point for further inspections
            
            annoCount[idx] = (annotations ? annotations.size : 0) + (embeddedAnnotations ? embeddedAnnotations.size : 0)
            support.info "document has ${annoCount[idx]} anotation(s) on page $idx"
        }
        support.info 'Finished examination of document'
        support.output(new BundledStream(createDummyStream(), targetSD))
      }
    }
  
    def createDummyStream() {
      return new ByteArrayInputStream("dummy".getBytes())
    }
}