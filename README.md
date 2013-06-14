groovy scripts for _jadice server_
==================================

This repository contains misc. useful groovy scripts for [levigo](http://www.levigo.com) [jadice server](http://www.levigo.com/document-management/products/jadice-server/).

Feel free to use them, modify them or send us a pull request.

PDFPreview.groovy
-----------------

Modifies a given PDF document so that only the first `n` pages remain in the resulting document

Usage:

    final ScriptNode scriptNode = new ScriptNode();
    scriptNode.setScript(new URI("resource:/<PathTo>/PDFPreview.groovy"));

    // Explicit declaration of max. pages:
    scriptNode.getParameters().put("maxPageCount", 42);
	
PDFPreview.groovy
-----------------

Change the order of pages in a given PDF document.

Usage

    final ScriptNode scriptNode = new ScriptNode();
    scriptNode.setScript(new URI("resource:/<PathTo>/PDFReorder.groovy"));

    // Declaration of new order:
	List<Integer> newOrder = new ArrayList<Integer>();
	// ... add the desired values (zero-based)
    scriptNode.getParameters().put("newOrder", newOrder);
	
