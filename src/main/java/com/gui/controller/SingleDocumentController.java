package com.gui.controller;

import com.backend.BackendFacade;
import com.objectTemplates.Document;

public abstract class SingleDocumentController extends Controller {
     Document document;

    // GETTER SETTER

    public abstract Document getDocument();

    public abstract void setDocument(Document document);
}
