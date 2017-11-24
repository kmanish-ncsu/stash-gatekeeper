package com.cdk.stash.hook;

import com.atlassian.stash.hook.HookResponse;
import com.atlassian.stash.hook.repository.PreReceiveRepositoryHook;
import com.atlassian.stash.hook.repository.RepositoryHookContext;
import com.atlassian.stash.repository.RefChange;

import javax.inject.Named;
import java.util.Collection;

@Named("cdkPullRequestListener")
public class GatekeeperHook implements PreReceiveRepositoryHook {

    public boolean onReceive(RepositoryHookContext repositoryHookContext, Collection<RefChange> collection, HookResponse hookResponse) {
        System.out.println("--On Receive Hook - Plugin settings : No Of reviewers configured : " + repositoryHookContext.getSettings().getInt("reviewers", 0));

        return true;
    }
}