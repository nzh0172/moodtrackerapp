package moodtracker;

import com.toedter.calendar.JCalendar;

import javax.swing.*;
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
    private MoodRepository moodRepository;
    private final JTextArea infoTextArea;

    public CalendarFrame() {
        this.moodRepository = new MoodRepository();  // Initialize the repository

        // Frame settings
        setTitle("Mood Calendar");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Calendar Component
        calendar.addPropertyChangeListener("calendar", e -> updateTextArea());
        add(calendar, BorderLayout.WEST);

        // Panel for the JTextArea
        JPanel labelPanel = new JPanel();
        labelPanel.setLayout(new BorderLayout());

        // Info Text Area to display Mood, Gratitude, and Rating
        infoTextArea = new JTextArea();
        infoTextArea.setEditable(false); // Make it non-editable
        infoTextArea.setWrapStyleWord(true); // Word wrap
        infoTextArea.setLineWrap(true); // Enable line wrapping
        infoTextArea.setFont(new Font("Arial", Font.PLAIN, 14)); // Set font size
        infoTextArea.setBackground(getBackground()); // Make background same as panel
        labelPanel.add(new JScrollPane(infoTextArea), BorderLayout.CENTER); // Add to panel

        // Add label panel to frame
        add(labelPanel, BorderLayout.CENTER);

        // Delete Button
        JButton deleteButton = new JButton("Delete Selected Entry");
        deleteButton.addActionListener(new DeleteActionListener());
        add(deleteButton, BorderLayout.SOUTH);

        setVisible(true);
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

    private void updateTextArea() {
        // Get the selected date from the calendar
        Date selectedDate = calendar.getDate();
        LocalDate localDate = selectedDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        try {
            // Fetch moods for the selected date
            List<Mood> moods = moodRepository.getMoodsByDate(localDate);

            if (!moods.isEmpty()) {
                Mood mood = moods.get(0); // Assuming only one entry per day

                // Combine mood, gratitude, and rating into a single text
                StringBuilder text = new StringBuilder();
                text.append("Mood: ").append(mood.getMood()).append("\n");
                text.append("Rating: ").append(getStars(mood.getRating())).append("\n");
                text.append("Gratitude: ").append("\n").append(mood.getGratitude());


                // Update the text area
                infoTextArea.setText(text.toString());
            } else {
                resetTextArea(); // If no mood data for the day
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error retrieving mood entries: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            resetTextArea();
        }
    }

    private void resetTextArea() {
        infoTextArea.setText("No mood entry available for this day.");
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
