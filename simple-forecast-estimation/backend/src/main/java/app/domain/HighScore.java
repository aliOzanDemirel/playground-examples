package app.domain;

public class HighScore {

    private long sequence;
    private Double highscore;

    public HighScore(long sequence, Double highscore) {
        this.sequence = sequence;
        this.highscore = highscore;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    public Double getHighscore() {
        return highscore;
    }

    public void setHighscore(Double highscore) {
        this.highscore = highscore;
    }

    @Override
    public String toString() {
        return "HighScore{" +
                "sequence=" + sequence +
                ", highscore=" + highscore +
                '}';
    }
}
