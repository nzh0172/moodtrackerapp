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
    private JCalendar calendar;
    private JTable table;
    private DefaultTableModel tableModel;

    public CalendarFrame() {
        // Frame settings
        setTitle("Mood Calendar");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Calendar Component
        calendar = new JCalendar();
        calendar.addPropertyChangeListener("calendar", e -> updateMoodEntries());
        add(calendar, BorderLayout.WEST);

        // Table for displaying mood entries
        String[] columnNames = {"Date", "Mood", "Gratitude", "Rating"};
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

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
            int selectedRow = table.getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(CalendarFrame.this, "Please select a row to delete.", "Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Get the ID of the selected mood entry
            int id = (int) tableModel.getValueAt(selectedRow, 0);

            // Confirm deletion
            int confirmation = JOptionPane.showConfirmDialog(CalendarFrame.this, "Are you sure you want to delete this entry?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirmation != JOptionPane.YES_OPTION) {
                return;
            }

            try {
                new MoodRepository().deleteMoodEntry(id);
                tableModel.removeRow(selectedRow);
                JOptionPane.showMessageDialog(CalendarFrame.this, "Mood entry deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(CalendarFrame.this, "Error deleting mood entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
