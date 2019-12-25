package com.controller;

import com.objectTemplates.Document;

public abstract class SingleDocumentController extends Controller {
     Document document;

    // GETTER SETTER

    public abstract Document getDocument();

    public abstract void setDocument(Document document);
}
