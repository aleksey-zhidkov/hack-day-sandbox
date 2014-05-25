package hds;

import hds.analysis.AnalysisCallback;
import hds.analysis.AnalysisService;
import hds.db.DB;
import jet.runtime.typeinfo.JetValueParameter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class HomePage extends WebPage {

    private static final long serialVersionUID = 1L;

    private AnalysisService analysisService = new AnalysisService();

    private final AtomicInteger processedFiles = new AtomicInteger(0);
    private final AtomicInteger processedRepos = new AtomicInteger(0);
    private final AtomicInteger reposCount = new AtomicInteger(0);
    private final TextField<String> githubName;
    private final Label languages;
    private final Label techs;
    private String userId;
    private String currentUserLanguages = "";
    private String currentUserTechs = "";

    /**
     * Constructor that is invoked when page is invoked without a session.
     *
     * @param parameters
     *            Page parameters
     */
    public HomePage(final PageParameters parameters) {



        final AnalysisCallback analysisCallback = new AnalysisCallbackImpl();

        Model<String> reposModel = new Model<String>() {
            public String getObject() {
                return String.format("%d / %d", processedRepos.get(), reposCount.get());
            }
        };

        final Model<Integer> filesModel = new Model<Integer>() {
            public Integer getObject() {
                return processedFiles.get();
            }
        };

        final Label repos = new Label("repos", reposModel);
        final Label files = new Label("files", filesModel);
        languages = new Label("languages", new LoadableDetachableModel<Object>() {
            @Override
            protected Object load() {
                return currentUserLanguages;
            }
        });

        techs = new Label("techs", new LoadableDetachableModel<Object>() {
            @Override
            protected Object load() {
                return currentUserTechs;
            }
        });

        add(languages);
        add(techs);

        githubName = new TextField<>("githubName", Model.<String> of());
        final Component progressBar = new WebComponent("progressBar");
        add(progressBar);
        OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {

            private static final long serialVersionUID =
                    2462233190993745889L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {

                // Maybe you want to update some components here?

                // Access the updated model object:
                Object defaultModelObject = getComponent().getDefaultModelObject();
                final String value = defaultModelObject != null ? defaultModelObject.toString() : "";


                githubName.setModelValue(new String[]{value});
            }
        };

        githubName.add(onChangeAjaxBehavior);

        add(githubName);

        // Add the simplest type of label
        add(new AjaxLink("analyze")
        {
            @Override
            public void onClick(AjaxRequestTarget target) {
                repos.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
                files.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
                progressBar.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND) {
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        int progress = (int) ((double) processedRepos.get()) / reposCount.get() * 100;
                        if (progress > 100)
                            progress = 100;
                        progressBar.add(new AttributeModifier("style", "width: " + progress + "%;"));
                        target.add(progressBar);
                    }
                });
                target.add(repos);
                target.add(files);
                target.add(progressBar);

                languages.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
                techs.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));

                target.add(languages);
                target.add(techs);

                processedRepos.set(0);
                reposCount.set(0);
                processedFiles.set(0);

                userId = githubName.getValue();
                analysisService.analyze(userId, analysisCallback);
            }
        });

        add(repos);
        add(files);


    }

    public class AnalysisCallbackImpl implements AnalysisCallback {

        @NotNull
        @Override
        public void onRepositoryFound() {
            reposCount.incrementAndGet();
        }

        @NotNull
        @Override
        public void onRepositoryProcessed() {
            processedRepos.incrementAndGet();
        }

        @NotNull
        @Override
        public void onFileFound() {

        }

        @NotNull
        @Override
        public void onFileProcessed() {
            processedFiles.incrementAndGet();
        }

        @NotNull
        @Override
        public void onLinesByExtensionChanged(@NotNull @JetValueParameter(name = "linesByExtension") ConcurrentHashMap<String, AtomicInteger> linesByExtension) {

        }

        @NotNull
        @Override
        public void onFinish() {
            getResults();
        }

        @NotNull
        @Override
        public void onError(@NotNull @JetValueParameter(name = "reason") String reason) {

        }
    }

    private void getResults() {
        DB db = new DB();
        StringBuffer buffer = new StringBuffer();
        for (String lng: db.resultsLanguages(userId)) {
            buffer.append(lng).append("\n");
        }
        currentUserLanguages = buffer.toString();

        buffer = new StringBuffer();
        for (String lng: db.resultsTechs(userId)) {
            buffer.append(lng).append("\n");
        }
        currentUserTechs = buffer.toString();
    }
}
