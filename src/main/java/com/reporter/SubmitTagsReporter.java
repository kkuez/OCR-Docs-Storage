package com.reporter;

import java.util.Set;

public interface SubmitTagsReporter extends Reporter{
    void submitTags(Set<String> tagSet);
}
