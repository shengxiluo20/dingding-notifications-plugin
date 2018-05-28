package com.ztbsuper.dingtalk;

import com.ztbsuper.Messages;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import jenkins.tasks.SimpleBuildStep;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import ren.wizard.dingtalkclient.DingTalkClient;
import ren.wizard.dingtalkclient.message.DingMessage;
import ren.wizard.dingtalkclient.message.LinkMessage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.PrintStream;

/**
 * @author uyangjie
 */
public class DingTalkNotifier extends Notifier implements SimpleBuildStep {

    private String accessToken;
    private String notifyPeople;
    private String message;
    private String imageUrl;
    private String jenkinsUrl;
    private String currentResult;

    @DataBoundConstructor
    public DingTalkNotifier(String accessToken, String notifyPeople, String message, String imageUrl, String jenkinsUrl, String currentResult) {
        this.accessToken = accessToken;
        this.notifyPeople = notifyPeople;
        this.message = message;
        this.imageUrl = imageUrl;
        this.jenkinsUrl = jenkinsUrl;
        this.currentResult = currentResult;
    }

    public String getAccessToken() {
        return accessToken;
    }

    @DataBoundSetter
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getNotifyPeople() {
        return notifyPeople;
    }

    @DataBoundSetter
    public void setNotifyPeople(String notifyPeople) {
        this.notifyPeople = notifyPeople;
    }

    public String getMessage() {
        return message;
    }

    @DataBoundSetter
    public void setMessage(String message) {
        this.message = message;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    @DataBoundSetter
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getJenkinsUrl() {
        return jenkinsUrl;
    }

    @DataBoundSetter
    public void setJenkinsUrl(String jenkinsUrl) {
        this.jenkinsUrl = jenkinsUrl;
    }

    public String getCurrentResult() {
        return currentResult;
    }

    @DataBoundSetter
    public void setCurrentResult(String currentResult) {
        this.currentResult = currentResult;
    }

    @Override
    public void perform(@Nonnull Run<?, ?> run, @Nonnull FilePath filePath, @Nonnull Launcher launcher, @Nonnull TaskListener taskListener) throws InterruptedException, IOException {
        PrintStream log = taskListener.getLogger();
        String buildInfo = run.getFullDisplayName();
        log.println("构建结果:" + this.currentResult);

        if ("SUCCESS".equals(this.currentResult)) {
            log.println("返回结果:构建成功");
            message = message + "[构建成功]";
            imageUrl = "http://icons.iconarchive.com/icons/paomedia/small-n-flat/1024/sign-check-icon.png";
        } else {
            log.println("返回结果:构建失败");
            message = message + "[构建失败]";
            imageUrl = "http://www.iconsdb.com/icons/preview/soylent-red/x-mark-3-xxl.png";
        }

        sendMessage(LinkMessage.builder()
                .title(buildInfo + message)
                .picUrl(imageUrl)
                .text(message)
                .messageUrl((jenkinsUrl.endsWith("/") ? jenkinsUrl : jenkinsUrl + "/") + run.getUrl())
                .build());
    }

    private void sendMessage(DingMessage message) {
        DingTalkClient dingTalkClient = DingTalkClient.getInstance();
        try {
            dingTalkClient.sendMessage(accessToken, message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Symbol("dingTalk")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public FormValidation doCheck(@QueryParameter String accessToken, @QueryParameter String notifyPeople) {
            if (StringUtils.isBlank(accessToken)) {
                return FormValidation.error(Messages.DingTalkNotifier_DescriptorImpl_AccessTokenIsNecessary());
            }
            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Nonnull
        @Override
        public String getDisplayName() {
            return Messages.DingTalkNotifier_DescriptorImpl_DisplayName();
        }
    }
}

