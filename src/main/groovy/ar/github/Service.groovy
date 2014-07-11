package ar.github

import groovyx.net.http.RESTClient

class Service {

    private String githubApiUrl = 'https://api.github.com/'
    private final RESTClient github = new RESTClient(githubApiUrl)

    Service() {
        github.setHeaders([
                "Authorization": "Basic ${"aleksey-zhidkov:jdev0502".bytes.encodeBase64().toString()}"
        ])
    }

    def List<String> getUserRepos(String githubId) {
        def resp = github.get(path: "users/$githubId/repos")
        printf "resp"
    }

    public static void main(String[] args) {
        new Service().getUserRepos('aleksey-zhidkov')
    }

}
