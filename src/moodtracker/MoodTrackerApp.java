package moodtracker;

import javax.swing.*;
import java.util.*;
import java.util.List;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class MoodTrackerApp {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MoodTrackerFrame frame = new MoodTrackerFrame();
            frame.setVisible(true);
        });
    }
}

class MoodTrackerFrame extends JFrame {
    private final JComboBox<IconLabel> moodComboBox;
    private final JTextArea gratitudeTextArea;
    private final JSlider ratingSlider;

    public MoodTrackerFrame() {
        // Frame settings
        setTitle("Mood Tracker");
        setSize(400, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Main content panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Current date
        JLabel dateLabel = new JLabel("Date: " + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Mood dropdown
        JPanel moodPanel = new JPanel();
        moodPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel moodLabel = new JLabel("Mood:");
        moodComboBox = new JComboBox<>(new IconLabel[]{
                new IconLabel("Happy", resizeIcon(new ImageIcon("icons/happy.png"), 32, 32)),
                new IconLabel("Sad", resizeIcon(new ImageIcon("icons/sad.png"), 32, 32)),
                new IconLabel("Anxious", resizeIcon(new ImageIcon("icons/anxious.png"), 35, 35)),
                new IconLabel("Excited", resizeIcon(new ImageIcon("icons/excited.png"), 35, 35)),
                new IconLabel("Calm", resizeIcon(new ImageIcon("icons/calm.png"), 32, 32)),
                new IconLabel("Angry", resizeIcon(new ImageIcon("icons/angry.png"), 32, 32)),
                new IconLabel("Neutral", resizeIcon(new ImageIcon("icons/neutral.png"), 32, 32)),

            });
        moodComboBox.setRenderer(new ComboBoxRenderer());
        moodPanel.add(moodLabel);
        moodPanel.add(moodComboBox);
        


        // Rating slider
        JPanel ratingPanel = new JPanel();
        ratingPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        JLabel ratingLabel = new JLabel("Rate your day:");
        ratingSlider = new JSlider(1, 5, 5);
        ratingSlider.setMajorTickSpacing(1);
        ratingSlider.setPaintTicks(true);
        ratingSlider.setPaintLabels(true);
        ratingSlider.setSnapToTicks(true); 
        ratingPanel.add(ratingLabel);
        ratingPanel.add(ratingSlider);

        // Gratitude text box
        JPanel gratitudePanel = new JPanel(new BorderLayout());
        JLabel gratitudeLabel = new JLabel("I feel/I am grateful of:");
        gratitudeTextArea = new JTextArea(5, 30);
        gratitudeTextArea.setLineWrap(true);
        gratitudeTextArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(gratitudeTextArea);

        gratitudePanel.add(gratitudeLabel, BorderLayout.NORTH);
        gratitudePanel.add(scrollPane, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JButton submitButton = new JButton("Submit");
        JButton viewEntriesButton = new JButton("View Entries");

        submitButton.addActionListener(new SubmitAction());
        viewEntriesButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
			try {
				new ViewEntriesFrame();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}));

        buttonPanel.add(submitButton);
        buttonPanel.add(viewEntriesButton);
        
        // Fetch today's mood entry and pre-fill the fields if exists
        MoodRepository moodRepos = new MoodRepository();
        try {
            Mood todayMood = moodRepos.getMoodEntryForToday();
            if (todayMood != null) {
                // Pre-fill the mood, gratitude, and rating if today's entry exists
                moodComboBox.setSelectedItem(new IconLabel(todayMood.getMood(), null)); 
                gratitudeTextArea.setText(todayMood.getGratitude());
                ratingSlider.setValue(todayMood.getRating());
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        JButton calendarButton = new JButton("View Calendar");
        calendarButton.addActionListener(e -> SwingUtilities.invokeLater(() -> new CalendarFrame()));
        buttonPanel.add(calendarButton);


        // Add components to the main panel
        mainPanel.add(dateLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(moodPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(ratingPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(gratitudePanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JButton deleteButton = new JButton("Delete Today's Entry");
        deleteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        deleteButton.addActionListener(new DeleteAction());  // Add the DeleteAction listener

        mainPanel.add(deleteButton); 
        mainPanel.add(buttonPanel);

        // Add main panel to frame
        add(mainPanel, BorderLayout.CENTER);
    }

    private class SubmitAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            IconLabel selectedMood = (IconLabel) moodComboBox.getSelectedItem();
            String gratitudeText = gratitudeTextArea.getText();
            int rating = ratingSlider.getValue();
            LocalDate currentDate = LocalDate.now();

            MoodRepository moodRepos = new MoodRepository();

            try {
                moodRepos.saveOrUpdateMoodEntry(currentDate, selectedMood.getText(), gratitudeText, rating);
                JOptionPane.showMessageDialog(MoodTrackerFrame.this, "Mood entry saved/updated successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                gratitudeTextArea.setText(""); // Clear the text area
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(MoodTrackerFrame.this, "Error saving mood entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private class DeleteAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            LocalDate currentDate = LocalDate.now();
            MoodRepository moodRepos = new MoodRepository();

            try {
                int confirmation = JOptionPane.showConfirmDialog(MoodTrackerFrame.this,
                        "Are you sure you want to delete today's mood entry?",
                        "Confirm Deletion",
                        JOptionPane.YES_NO_OPTION);
                
                if (confirmation == JOptionPane.YES_OPTION) {
                    moodRepos.deleteMoodEntryByDate(currentDate);
                    JOptionPane.showMessageDialog(MoodTrackerFrame.this, "Mood entry for today has been deleted.", "Success", JOptionPane.INFORMATION_MESSAGE);
                    
                    // Optionally, clear the UI fields after deletion
                    moodComboBox.setSelectedIndex(0);  // Reset the mood combobox to the first item
                    gratitudeTextArea.setText("");      // Clear the gratitude text area
                    ratingSlider.setValue(0);          // Reset the rating slider
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(MoodTrackerFrame.this, "Error deleting mood entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    private ImageIcon resizeIcon(ImageIcon icon, int width, int height) {
        Image img = icon.getImage();
        Image resizedImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(resizedImg);
    }
}

class ViewEntriesFrame extends JFrame {
    private JTable table;
    private DefaultTableModel tableModel;

    public ViewEntriesFrame() throws SQLException {
        // Frame settings
        setTitle("View Mood Entries");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        String[] columnNames = {"Date", "Mood", "Gratitude", "Rating"};
        // Get mood entries from the repository
        List<Mood> moods = new MoodRepository().getAllEntries();

        // Prepare data for the table
        Object[][] data = new Object[moods.size()][4];
        for (int i = 0; i < moods.size(); i++) {
            Mood mood = moods.get(i);
            data[i][0] = mood.getDate();      // Date
            data[i][1] = mood.getMood();      // Mood
            data[i][2] = mood.getGratitude(); // Gratitude
            data[i][3] = mood.getRating();    // Rating
        }

        // Initialize tableModel and table
        tableModel = new DefaultTableModel(data, columnNames);
        table = new JTable(tableModel);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(table);

        // Adjust the column widths
        TableColumnModel columnModel = table.getColumnModel();
        for (int i = 0; i < columnModel.getColumnCount(); i++) {
            if (i != 2) { // Skip the Gratitude column (index 2)
                columnModel.getColumn(i).setPreferredWidth(90); // Adjust width for other columns
            }
        }
        columnModel.getColumn(2).setPreferredWidth(400); // Set a larger width for Gratitude

        // Add Delete Button
        JButton deleteButton = new JButton("Delete Selected Entry");
        deleteButton.addActionListener(e -> deleteSelectedEntry());

        // Layout
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(deleteButton, BorderLayout.SOUTH);

        add(panel);
        setVisible(true);
    }

    private void deleteSelectedEntry() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select an entry to delete.", "No Selection", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Get the date of the selected row
        LocalDate dateToDelete = LocalDate.parse(tableModel.getValueAt(selectedRow, 0).toString());

        // Delete the entry from the database
        try {
            MoodRepository moodRepository = new MoodRepository();
            moodRepository.deleteMoodEntryByDate(dateToDelete);

            // Remove the row from the table model
            tableModel.removeRow(selectedRow);
            JOptionPane.showMessageDialog(this, "Entry deleted successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error deleting entry: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

class IconLabel {
    private final String text;
    private final Icon icon;

    public IconLabel(String text, Icon icon) {
        this.text = text;
        this.icon = icon;
    }

    public String getText() {
        return text;
    }

    public Icon getIcon() {
        return icon;
    }

    @Override
    public String toString() {
        return text;
    }
}

class ComboBoxRenderer extends JLabel implements ListCellRenderer<IconLabel> {
    @Override
    public Component getListCellRendererComponent(JList<? extends IconLabel> list, IconLabel value, int index, boolean isSelected, boolean cellHasFocus) {
        setText(value.getText());
        setIcon(value.getIcon());
        setOpaque(true);
        setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);
        return this;
    }
    

}


