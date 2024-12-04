package moodtracker;


import com.toedter.calendar.JCalendar;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class CalendarFrame extends JFrame {
    private final JCalendar calendar = new JCalendar();
    private JTable table;
    private DefaultTableModel tableModel;
	private MoodRepository moodRepository;
    private final JLabel moodLabel;
    private final JLabel gratitudeLabel;
    private final JLabel ratingLabel;

    public CalendarFrame() {
        this.moodRepository = new MoodRepository();  // Initialize the repository

    	
        // Frame settings
        setTitle("Mood Calendar");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Calendar Component

        calendar.addPropertyChangeListener("calendar", e -> updateLabels());
        add(calendar, BorderLayout.WEST);

        // Panel for labels
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BoxLayout(labelPanel, BoxLayout.Y_AXIS));

        // Mood Label
        moodLabel = new JLabel("Mood: N/A");
        moodLabel.setIcon(null);
        moodLabel.setFont(new Font("Arial", Font.BOLD, 16));
        labelPanel.add(moodLabel);

        // Gratitude Label
        gratitudeLabel = new JLabel("Gratitude: N/A");
        gratitudeLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        labelPanel.add(gratitudeLabel);

        // Rating Label
        ratingLabel = new JLabel("Rating: N/A");
        ratingLabel.setFont(new Font("Arial", Font.BOLD, 16));
        labelPanel.add(ratingLabel);

        // Add label panel to frame
        add(labelPanel, BorderLayout.CENTER);

        // Delete Button
        JButton deleteButton = new JButton("Delete Selected Entry");
        deleteButton.addActionListener(new DeleteActionListener());
        add(deleteButton, BorderLayout.SOUTH);

        setVisible(true);
    }

    private void updateMoodEntries() {
        Date selectedDate = calendar.getDate();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            // Clear the table
            tableModel.setRowCount(0);

            // Fetch moods for the selected date
            List<Mood> moods = new MoodRepository().getMoodsByDate(localDate);
            for (Mood mood : moods) {
                tableModel.addRow(new Object[]{
                        mood.getDate(),
                        mood.getMood(),
                        mood.getGratitude(),
                        mood.getRating()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving mood entries: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private class DeleteActionListener implements ActionListener {

    	
        @Override
        public void actionPerformed(ActionEvent e) {
            // Get the selected date from the JCalendar
            java.util.Date selectedDate = calendar.getDate();

            // Convert it to LocalDate
            LocalDate localDate = new java.sql.Date(selectedDate.getTime()).toLocalDate();

            // Show confirmation dialog
            int confirmation = JOptionPane.showConfirmDialog(CalendarFrame.this,
                    "Are you sure you want to delete the mood entry for " + localDate + "?",
                    "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION);

            if (confirmation == JOptionPane.YES_OPTION) {
                try {
                    // Delete mood entry by the selected date
                    moodRepository.deleteMoodEntryByDate(localDate);
                    JOptionPane.showMessageDialog(CalendarFrame.this, "Mood entry for " + localDate + " has been deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(CalendarFrame.this, "Error deleting mood entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }
    
    private void updateLabels() {
        // Get the selected date from the calendar
        Date selectedDate = calendar.getDate();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            // Fetch moods for the selected date
            List<Mood> moods = moodRepository.getMoodsByDate(localDate);

            if (!moods.isEmpty()) {
                Mood mood = moods.get(0); // Assuming only one entry per day

                // Update mood label with icon
                moodLabel.setText("Mood: " + mood.getMood());
                moodLabel.setIcon(getMoodIcon(mood.getMood()));

                // Update gratitude label
                gratitudeLabel.setText("Gratitude: " + mood.getGratitude());

                // Update rating label with stars
                ratingLabel.setText("Rating: " + getStars(mood.getRating()));
            } else {
                resetLabels(); // If no mood data for the day
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving mood entries: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            resetLabels();
        }
    }

    private void resetLabels() {
        moodLabel.setText("Mood: N/A");
        moodLabel.setIcon(null);
        gratitudeLabel.setText("Gratitude: N/A");
        ratingLabel.setText("Rating: N/A");
    }

    private Icon getMoodIcon(String mood) {
        String iconPath;
        switch (mood.toLowerCase()) {
            case "happy":
                iconPath = "/icons/happy.png";
                break;
            case "sad":
                iconPath = "/icons/sad.png";
                break;
            case "angry":
                iconPath = "/icons/angry.png";
                break;
            case "excited":
                iconPath = "/icons/excited.png";
                break;
            default:
                iconPath = "/icons/neutral.png"; // Default icon
        }

        // Load the icon
        try {
            return new ImageIcon(getClass().getResource(iconPath));
        } catch (Exception e) {
            return null; // Return no icon if not found
        }
    }

    private String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }
}


