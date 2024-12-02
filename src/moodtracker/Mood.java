package moodtracker;

import java.time.LocalDate;

public class Mood {
    private int id;
    private String mood;
    private int rating;
    private String gratitude;
    private LocalDate date;

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getMood() {
        return mood;
    }

    public void setMood(String mood) {
        this.mood = mood;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getGratitude() {
        return gratitude;
    }

    public void setGratitude(String gratitude) {
        this.gratitude = gratitude;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}
