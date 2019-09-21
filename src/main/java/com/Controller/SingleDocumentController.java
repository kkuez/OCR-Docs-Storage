package com.Controller;

import com.ObjectTemplates.Document;

public abstract class SingleDocumentController extends Controller {
     Document document;

    // GETTER SETTER

    public abstract Document getDocument();

    public abstract void setDocument(Document document);
}
