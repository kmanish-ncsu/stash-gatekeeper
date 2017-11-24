package com.cdk.stash.task;

import com.atlassian.sal.api.component.ComponentLocator;
import com.atlassian.stash.comment.Comment;
import com.atlassian.stash.event.pull.PullRequestEvent;
import com.atlassian.stash.hook.repository.RepositoryHookService;
import com.atlassian.stash.pull.PullRequestService;
import com.atlassian.stash.setting.Settings;
import com.atlassian.stash.task.TaskAnchorType;
import com.atlassian.stash.task.TaskCreateRequest;
import com.atlassian.stash.task.TaskService;
import com.atlassian.stash.user.SecurityService;
import com.atlassian.stash.user.StashUser;
import com.atlassian.stash.user.UserService;
import com.atlassian.stash.util.Operation;

import java.util.Map;

public class TaskManager {

    public Settings getRepoSettings(PullRequestEvent event) {
        RepositoryHookService repository = ComponentLocator.getComponent(RepositoryHookService.class);
        Settings settings = repository.getSettings(event.getPullRequest().getToRef().getRepository(), "com.cdk.stash.hook.gatekeeper-plugin:cdk-pr-hook"); //groupId.artifactId:repositoryHookKey

        return settings;
    }

    public void createTasks(final PullRequestEvent event) {
        final Integer repoId = event.getPullRequest().getToRef().getRepository().getId();

        final TaskService taskService = ComponentLocator.getComponent(TaskService.class);
        final PullRequestService pullRequestService = ComponentLocator.getComponent(PullRequestService.class);
        SecurityService securityService = ComponentLocator.getComponent(SecurityService.class);
        UserService userService = ComponentLocator.getComponent(UserService.class);

        String userName = "admin";
        StashUser stashUser = userService.getUserBySlug(userName);

        try {
            securityService.impersonating(stashUser, "auto adding comment").call(new Operation<Object, Throwable>() {

                public Object perform() throws Throwable {
                    Comment comment = pullRequestService.addComment(repoId, event.getPullRequest().getId(), "Must complete the following tasks:");

                    //get all configured tasks and create
                    Map<String, Object> map = getRepoSettings(event).asMap();
                    for (Map.Entry<String, Object> entry : map.entrySet()) {
                        createTask(comment.getId(), entry.getValue().toString());
                    }

                    return comment;
                }
            });
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    private void createTask(Long commentId, String task) {
        final TaskService taskService = ComponentLocator.getComponent(TaskService.class);

        if (task != null && task.trim() != "") {
            TaskCreateRequest.Builder builder = new TaskCreateRequest.Builder();
            builder.anchorId(commentId);
            builder.anchorType(TaskAnchorType.COMMENT);
            builder.text(task);
            taskService.create(builder.build());
        }
    }
}
