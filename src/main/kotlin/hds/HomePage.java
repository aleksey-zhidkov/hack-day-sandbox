package hds;

import hds.analysis.AnalysisCallback;
import hds.analysis.AnalysisService;
import hds.db.DB;
import jet.runtime.typeinfo.JetValueParameter;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.IAjaxIndicatorAware;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.extensions.ajax.markup.html.AjaxIndicatorAppender;
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

    private AtomicInteger progress = new AtomicInteger();

    private String userId;
    private String currentUserLanguages = "";
    private String currentUserTechs = "";
    private final Component progressBar;

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
        languages.setEscapeModelStrings(false);

        techs = new Label("techs", new LoadableDetachableModel<Object>() {
            @Override
            protected Object load() {
                return currentUserTechs;
            }
        });
        techs.setEscapeModelStrings(false);

        add(languages);
        add(techs);

        githubName = new TextField<>("githubName", Model.<String> of());

        progressBar = new WebComponent("progressBar");
        progressBar.add(new AttributeModifier("style", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return "width:" + progress.get() + "%";
            }
        }));

        OnChangeAjaxBehavior onChangeAjaxBehavior = new OnChangeAjaxBehavior() {

            private static final long serialVersionUID =
                    2462233190993745889L;

            @Override
            protected void onUpdate(final AjaxRequestTarget target) {
                Object defaultModelObject = getComponent().getDefaultModelObject();
                final String value = defaultModelObject != null ? defaultModelObject.toString() : "";


                githubName.setModelValue(new String[]{value});
                target.add(progressBar);
            }
        };

        progressBar.setOutputMarkupId(true);

        add(progressBar);


        githubName.add(onChangeAjaxBehavior);

        add(githubName);

        // Add the simplest type of label
        add(new AjaxLink("analyze")
        {
            @Override
            public void onClick(AjaxRequestTarget target) {
                    repos.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
                    files.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));

                target.add(repos);
                target.add(files);
                target.add(progressBar);
                currentUserLanguages = "";
                currentUserTechs = "";

                languages.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
                techs.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND));
//                progressBar.add(new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND) {
//
//                });

                target.add(languages);
                target.add(techs);
                target.add(progressBar);


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

    private Integer getProgress() {
        if (reposCount.get() == 0 || processedRepos.get() == 0)
            return 0;

        return (int) ((double) processedRepos.get()) / reposCount.get() * 100;
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
            progress.set(getProgress());
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
            progress.set(0);
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
            buffer.append("<a href=\"ratings?type=lng&lng=" + lng.substring(0, lng.indexOf(':')) + "\">").append(lng).append("</a>").append("<br/><br/>");
        }
        currentUserLanguages = buffer.toString();

        buffer = new StringBuffer();
        for (String lng: db.resultsTechs(userId)) {
            buffer.append("<a href=\"ratings?type=tech&lng=" + lng.substring(0, lng.indexOf(':')) + "\">").append(lng).append("</a>").append("<br/><br/>");
        }
        currentUserTechs = buffer.toString();
    }
}
