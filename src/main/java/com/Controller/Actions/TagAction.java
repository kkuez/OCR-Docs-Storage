package com.Controller.Actions;

import com.ObjectTemplates.Document;
import com.Utils.DBUtil;

import java.util.HashSet;
import java.util.Set;

public class TagAction extends Action {

    Set<String> tags;

    Set<Document> documents;

    public TagAction(Set<String> tags, Set<Document> documents){
        this.tags = tags;

        this.documents = documents;
    }

    @Override
    public void execute() {


    }

    //GETTER SETTER

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }


}
