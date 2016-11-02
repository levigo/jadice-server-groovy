groovy scripts for _jadice server_
==================================

This repository contains misc. useful groovy scripts for [levigo](http://www.levigo.com) [jadice server](http://www.levigo.com/document-management/products/jadice-server/).

Feel free to use them, modify them or send us a pull request.

CountAnnotations.groovy
-----------------------

Count how many annotations were found in the given documents and store it in the ```StreamDescriptor``` of the outgoing stream (a stream with only dummy content!)

Works on jadice server 5.1.3.0 and above

Usage:

    final ScriptNode scriptNode = new ScriptNode();
    scriptNode.setScript(new URI("resource:/<PathTo>/CountAnnotations.groovy"));
    // Create a jadice server job and submit a document
    
    // After the job has finished successfully:
    for (Stream s : myStreamOutputNode.getStreamBundle()) {
      final int pageCount = (int) s.getDescriptor().getProperties().get("page-count");
      final int[] annotationCount = (int[]) s.getDescriptor().getProperties().get("annotation-count");
      // ...
    }


PDFPreview.groovy
-----------------

Modifies a given PDF document so that only the first `n` pages remain in the resulting document

Usage:

    final ScriptNode scriptNode = new ScriptNode();
    scriptNode.setScript(new URI("resource:/<PathTo>/PDFPreview.groovy"));

    // Explicit declaration of max. pages:
    scriptNode.getParameters().put("maxPageCount", 42);

PDFReorder.groovy
-----------------

Change the order of pages in a given PDF document.

Usage:

    final ScriptNode scriptNode = new ScriptNode();
    scriptNode.setScript(new URI("resource:/<PathTo>/PDFReorder.groovy"));

    // Declaration of new order:
    final ArrayList<Integer> newOrder = new ArrayList<Integer>();
    // ... add the desired values (zero-based)
    scriptNode.getParameters().put("newOrder", newOrder);
    
SendMail.groovy
---------------

Send documents via mail

Usage:

    final ScriptNode scriptNode = new ScriptNode();
    scriptNode.setScript(new URI("resource:/<PathTo>/SendMail.groovy"));

    // Provide hostname, credentials and the recipient
    scriptNode.getParameters().put("host", SERVER);
    scriptNode.getParameters().put("username", LOGIN);
    scriptNode.getParameters().put("password", PASSWORD);
    scriptNode.getParameters().put("to", "john.doe@example.com");

You can find an additional howto article in our jadice knowledge base at https://levigo.de/info/display/JKB/Senden+einer+E-Mail (german)