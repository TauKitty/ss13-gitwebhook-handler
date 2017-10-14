package io.github.spair.handlers;

import io.github.spair.entities.Issue;
import io.github.spair.services.IssuesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;

@Component
public class IssuesHandler {

    private final IssuesService issuesService;

    @Autowired
    public IssuesHandler(IssuesService issuesService) {
        this.issuesService = issuesService;
    }

    public void handle(HashMap webhook) {
        Issue issue = issuesService.convertWebhookMap(webhook);

        switch (issue.getType()) {
            case OPENED:
                issuesService.processLabels(issue);
                break;
        }
    }
}