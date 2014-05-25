package hds.db;

import java.io.Serializable;

public class RatingRow implements Serializable {

    public final int place;
    public final String githubId;
    public final long lines;

    public RatingRow(int place, String githubId, long lines) {
        this.place = place;
        this.githubId = githubId;
        this.lines = lines;
    }

    public int getPlace() {
        return place;
    }

    public String getGithubId() {
        return githubId;
    }

    public long getLines() {
        return lines;
    }
}
