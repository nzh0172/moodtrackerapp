package moodtracker;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MoodRepository {

	public void saveMoodEntry(LocalDate currentDate, String mood, String gratitudeText, int rating) throws SQLException {
	    // Check if there is already an entry for the given date
	    if (isEntryExistsForDate(currentDate)) {
	        throw new SQLException("An entry for today already exists.");
	    }

	    String sql = "INSERT INTO moodentries (mood, rating, gratitude, entryDate) VALUES (?, ?, ?, ?)";
	    try (Connection connection = DatabaseConnection.getConnection();
	         PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setString(1, mood);
	        stmt.setInt(2, rating);
	        stmt.setString(3, gratitudeText);
	        stmt.setDate(4, Date.valueOf(currentDate));
	        stmt.executeUpdate();
	    }
	}

	private boolean isEntryExistsForDate(LocalDate date) throws SQLException {
	    String sql = "SELECT COUNT(*) FROM moodentries WHERE entryDate = ?";
	    try (Connection connection = DatabaseConnection.getConnection();
	         PreparedStatement stmt = connection.prepareStatement(sql)) {
	        stmt.setDate(1, Date.valueOf(date));
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                int count = rs.getInt(1);
	                return count > 0; // If count is greater than 0, an entry exists
	            }
	        }
	    }
	    return false; // No entry exists for the given date
	}


    // Method to get all mood entries
    public List<Mood> getAllEntries() throws SQLException {
        String sql = "SELECT * FROM moodentries";
        List<Mood> moods = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Mood mood = new Mood();
                mood.setId(rs.getInt("id"));
                mood.setMood(rs.getString("mood"));
                mood.setRating(rs.getInt("rating"));
                mood.setGratitude(rs.getString("gratitude"));
                mood.setDate(rs.getDate("entryDate").toLocalDate());
                moods.add(mood);
            }
        }
        return moods;
    }

    // Method to get moods by a specific date
    public List<Mood> getMoodsByDate(LocalDate date) throws SQLException {
        String sql = "SELECT * FROM moodentries WHERE entryDate = ?";
        List<Mood> moods = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setDate(1, Date.valueOf(date));  // Convert LocalDate to SQL Date
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Mood mood = new Mood();
                    mood.setId(rs.getInt("id"));
                    mood.setMood(rs.getString("mood"));
                    mood.setRating(rs.getInt("rating"));
                    mood.setGratitude(rs.getString("gratitude"));
                    mood.setDate(rs.getDate("entryDate").toLocalDate());
                    moods.add(mood);
                }
            }
        }
        return moods;
    }
    
    public void deleteMoodEntry(int id) throws SQLException {
        String sql = "DELETE FROM moodentries WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, id);
            int rowsDeleted = stmt.executeUpdate();
            if (rowsDeleted == 0) {
                throw new SQLException("No entry found with the given ID.");
            }
        }
    }
    
    public List<LocalDate> getDatesWithEntries() throws SQLException {
        String sql = "SELECT DISTINCT entryDate FROM moodentries";
        List<LocalDate> dates = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dates.add(rs.getDate("date").toLocalDate());
            }
        }
        return dates;
    }


}
