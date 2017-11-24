/**
 * An alternate to listening events by registering explicitly to EVentPublisher
 */

package com.cdk.stash.event;

import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.stash.event.pull.PullRequestDeclinedEvent;
import com.atlassian.stash.event.pull.PullRequestOpenedEvent;
import com.atlassian.stash.event.pull.PullRequestReopenedEvent;
import com.atlassian.stash.setting.Settings;
import com.cdk.stash.task.TaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;

@Named("customEventListener")
public class CustomEventListener {

    private static final Logger log = LoggerFactory.getLogger(CustomEventListener.class);

    private final EventPublisher eventPublisher;

    //@Autowired
    TaskManager taskManager = new TaskManager(); // TODO this needs to be changed


    public CustomEventListener() {
        System.out.println("-- CustomEventListener constructor");
        this.eventPublisher = ComponentLocator.getComponent(EventPublisher.class);
    }

    @PostConstruct
    public void init() {
        System.out.println("-- Registering CustomEventListener to publisher ");
        // Called after the instance is created (when the plugin is started)
        eventPublisher.register(this);
    }

    @PreDestroy
    public void destroy() {
        System.out.println("-- Un-registering CustomEventListener from publisher");
        // Called just before the instance is destroyed (when the plugin is stopped)
        eventPublisher.unregister(this);
    }


    /**************************************************************************************
    // Add listeners
     /**************************************************************************************/

    @EventListener
    public void onPullRequestOpenedEvent(PullRequestOpenedEvent event) {
        final Integer repoId = event.getPullRequest().getToRef().getRepository().getId();
        System.out.println("--CustomEventListener --- a new PR with id : " + + event.getPullRequest().getId() + "was opened on repo : " + repoId);

        Settings settings = taskManager.getRepoSettings(event);
        System.out.println("-- Plugin settings : no Of reviewers configured : " + settings.getInt("reviewers", 0));

        System.out.println("-- Creating mandatory tasks :");
        taskManager.createTasks(event);
    }

    @EventListener
    public void onPullRequestReopenedEvent(PullRequestReopenedEvent event) {
        final Integer repoId = event.getPullRequest().getToRef().getRepository().getId();
        System.out.println("--CustomEventListener --- a PR was reopened-- " + event.getPullRequest().getId() +  " : " + repoId);
    }

    @EventListener
    public void onPullRequestDeclinedEvent(PullRequestDeclinedEvent event) {
        final Integer repoId = event.getPullRequest().getToRef().getRepository().getId();
        System.out.println("-----Declined PR --- ID : " + event.getPullRequest().getId() +  " : " + repoId);
    }
}

