
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class MusicComposerApp {

    private static List<Integer> lastGeneratedMelody;
    private static List<Integer> recordedNotes = new ArrayList<>();
    private static boolean isRecording = false;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("AstraMelody");
            frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Use a custom particle background panel (animated) instead of the static gradient
            ParticleBackgroundPanel backgroundPanel = new ParticleBackgroundPanel();
            backgroundPanel.setLayout(new BoxLayout(backgroundPanel, BoxLayout.Y_AXIS));
            frame.setContentPane(backgroundPanel);

            // === Heading Label ===
            JLabel headingLabel = new JLabel("AstraMelody", SwingConstants.CENTER);
            headingLabel.setFont(new Font("SansSerif", Font.BOLD, 48));
            headingLabel.setForeground(new Color(255, 215, 0)); // bright gold
            headingLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            headingLabel.setBorder(new EmptyBorder(40, 0, 20, 0));
            backgroundPanel.add(headingLabel);

            // === Glassmorphism Container ===
            JPanel glassPanel = new JPanel() {
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    // Subtle outer shadow
                    g2.setColor(new Color(0, 0, 0, 80));
                    g2.fillRoundRect(5, 5, getWidth() - 10, getHeight() - 10, 55, 55);

                    // Main panel with gradient
                    GradientPaint gradient = new GradientPaint(
                            0, 0, new Color(32, 32, 32),
                            0, getHeight(), new Color(20, 20, 20)
                    );
                    g2.setPaint(gradient);
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.95f));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 50, 50);

                    // Subtle inner highlight
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
                    g2.setColor(Color.WHITE);
                    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 48, 48);

                    // Gold border with glow
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.3f));
                    g2.setColor(new Color(255, 215, 0));
                    g2.setStroke(new BasicStroke(6));
                    g2.drawRoundRect(-1, -1, getWidth() + 2, getHeight() + 2, 52, 52);

                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.8f));
                    g2.setStroke(new BasicStroke(2));
                    g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 50, 50);

                    g2.dispose();
                }
            };
            glassPanel.setLayout(new GridBagLayout());
            glassPanel.setOpaque(false);
            glassPanel.setBorder(new EmptyBorder(50, 60, 50, 60));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(15, 25, 15, 25);
            gbc.gridx = 0;
            gbc.gridy = 0;

            Font font = new Font("SansSerif", Font.BOLD, 28);
            Font labelFont = new Font("SansSerif", Font.PLAIN, 24);
            Color textColor = new Color(255, 215, 0); // gold for labels

            JLabel moodLabel = new JLabel("🎭 Mood:");
            moodLabel.setForeground(textColor);
            moodLabel.setFont(font);
            JLabel keyLabel = new JLabel("🎵 Key:");
            keyLabel.setForeground(textColor);
            keyLabel.setFont(font);
            JLabel instrumentLabel = new JLabel("🎹 Instrument:");
            instrumentLabel.setForeground(textColor);
            instrumentLabel.setFont(font);
            JLabel tempoLabel = new JLabel("🎚 Tempo (BPM):");
            tempoLabel.setForeground(textColor);
            tempoLabel.setFont(font);

            String[] moods = {"Happy", "Sad", "Spooky", "Relaxed"};
            JComboBox<String> moodBox = new JComboBox<>(moods);
            moodBox.setFont(font);
            moodBox.setBackground(new Color(50, 50, 50));
            moodBox.setForeground(Color.WHITE);

            String[] keys = {"C Major", "A Minor", "G Major", "E Minor"};
            JComboBox<String> keyBox = new JComboBox<>(keys);
            keyBox.setFont(font);
            keyBox.setBackground(new Color(50, 50, 50));
            keyBox.setForeground(Color.WHITE);

            String[] instruments = {"Piano", "Synth", "Guitar"};
            JComboBox<String> instrumentBox = new JComboBox<>(instruments);
            instrumentBox.setFont(font);
            instrumentBox.setBackground(new Color(50, 50, 50));
            instrumentBox.setForeground(Color.WHITE);

            JSlider tempoSlider = new JSlider(60, 180, 120);
            tempoSlider.setMajorTickSpacing(30);
            tempoSlider.setMinorTickSpacing(10);
            tempoSlider.setPaintTicks(true);
            tempoSlider.setPaintLabels(true);
            tempoSlider.setFont(new Font("SansSerif", Font.PLAIN, 18));
            tempoSlider.setForeground(new Color(255, 215, 0));
            tempoSlider.setOpaque(false);

            UIManager.put("Slider.labelColor", new Color(255, 215, 0));

            JButton generateButton = new JButton("🎼 Generate");
            JButton playButton = new JButton("▶️ Play");
            JButton saveButton = new JButton("💾 Save MIDI");
            JCheckBox liveModeCheck = new JCheckBox(" Live Play Mode (F1–F12)");
            JCheckBox recordModeCheck = new JCheckBox(" Record Live Play");
            JButton saveRecordingButton = new JButton("💾 Save Recording");

            JButton[] buttons = {generateButton, playButton, saveButton, saveRecordingButton};
            for (JButton btn : buttons) {
                btn.setFont(new Font("SansSerif", Font.BOLD, 26));
                btn.setBackground(new Color(45, 45, 45));
                btn.setForeground(new Color(255, 215, 0));
                btn.setFocusPainted(false);
                btn.setBorderPainted(true);
                btn.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(new Color(255, 215, 0), 2),
                        BorderFactory.createEmptyBorder(12, 24, 12, 24)
                ));
                btn.setCursor(new Cursor(Cursor.HAND_CURSOR));

                // Hover effect
                btn.addMouseListener(new MouseAdapter() {
                    public void mouseEntered(MouseEvent e) {
                        btn.setBackground(new Color(255, 215, 0));
                        btn.setForeground(new Color(20, 20, 20));
                    }

                    public void mouseExited(MouseEvent e) {
                        btn.setBackground(new Color(45, 45, 45));
                        btn.setForeground(new Color(255, 215, 0));
                    }
                });
            }
            liveModeCheck.setFont(font);
            recordModeCheck.setFont(font);
            liveModeCheck.setForeground(textColor);
            recordModeCheck.setForeground(textColor);
            liveModeCheck.setOpaque(false);
            recordModeCheck.setOpaque(false);

            // Add UI Elements
            glassPanel.add(moodLabel, gbc);
            gbc.gridx++;
            glassPanel.add(moodBox, gbc);
            gbc.gridx = 0;
            gbc.gridy++;

            glassPanel.add(keyLabel, gbc);
            gbc.gridx++;
            glassPanel.add(keyBox, gbc);
            gbc.gridx = 0;
            gbc.gridy++;

            glassPanel.add(instrumentLabel, gbc);
            gbc.gridx++;
            glassPanel.add(instrumentBox, gbc);
            gbc.gridx = 0;
            gbc.gridy++;

            glassPanel.add(tempoLabel, gbc);
            gbc.gridx++;
            glassPanel.add(tempoSlider, gbc);
            gbc.gridx = 0;
            gbc.gridy++;

            glassPanel.add(generateButton, gbc);
            gbc.gridx++;
            glassPanel.add(playButton, gbc);
            gbc.gridx = 0;
            gbc.gridy++;

            glassPanel.add(saveButton, gbc);
            gbc.gridx++;
            glassPanel.add(saveRecordingButton, gbc);
            gbc.gridx = 0;
            gbc.gridy++;

            glassPanel.add(liveModeCheck, gbc);
            gbc.gridx++;
            glassPanel.add(recordModeCheck, gbc);

            backgroundPanel.add(glassPanel);
            frame.setVisible(true);

            // === Core Logic ===
            MidiPlayer midiPlayer = new MidiPlayer();
            MelodyGenerator generator = new MelodyGenerator();

            generateButton.addActionListener(e -> {
                String mood = (String) moodBox.getSelectedItem();
                String key = (String) keyBox.getSelectedItem();
                lastGeneratedMelody = generator.generateMelody(key, mood, 8);
                JOptionPane.showMessageDialog(frame, "Melody generated!");
            });

            playButton.addActionListener(e -> {
                if (lastGeneratedMelody == null) {
                    JOptionPane.showMessageDialog(frame, "Please generate a melody first.");
                    return;
                }
                int tempo = tempoSlider.getValue();
                String instrument = (String) instrumentBox.getSelectedItem();
                midiPlayer.playMelody(lastGeneratedMelody, tempo, instrument);
            });

            saveButton.addActionListener(e -> {
                if (lastGeneratedMelody == null) {
                    JOptionPane.showMessageDialog(frame, "Generate a melody first.");
                    return;
                }
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".mid")) {
                        file = new File(file.getAbsolutePath() + ".mid");
                    }
                    midiPlayer.exportMelody(lastGeneratedMelody, tempoSlider.getValue(),
                            (String) instrumentBox.getSelectedItem(), file);
                    JOptionPane.showMessageDialog(frame, "MIDI saved!");
                }
            });

            recordModeCheck.addActionListener(e -> {
                isRecording = recordModeCheck.isSelected();
                recordedNotes.clear();
                JOptionPane.showMessageDialog(frame, isRecording ? "Recording started!" : "Recording stopped!");
            });

            saveRecordingButton.addActionListener(e -> {
                if (recordedNotes.isEmpty()) {
                    JOptionPane.showMessageDialog(frame, "No recorded notes.");
                    return;
                }
                JFileChooser fc = new JFileChooser();
                if (fc.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
                    File file = fc.getSelectedFile();
                    if (!file.getName().toLowerCase().endsWith(".mid")) {
                        file = new File(file.getAbsolutePath() + ".mid");
                    }
                    midiPlayer.exportMelody(recordedNotes, tempoSlider.getValue(),
                            (String) instrumentBox.getSelectedItem(), file);
                    JOptionPane.showMessageDialog(frame, "Recording saved!");
                }
            });

            // === Live Mode Key Bindings ===
            JRootPane rootPane = frame.getRootPane();
            for (int i = 0; i < 12; i++) {
                final int note = 60 + i;
                String keyName = "F" + (i + 1);
                KeyStroke keyStroke = KeyStroke.getKeyStroke(keyName);
                rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
                        .put(keyStroke, "playNote" + i);
                rootPane.getActionMap().put("playNote" + i, new AbstractAction() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if (liveModeCheck.isSelected()) {
                            String instrument = (String) instrumentBox.getSelectedItem();
                            midiPlayer.playSingleNote(note, instrument);
                            if (isRecording) {
                                recordedNotes.add(note);
                            }
                        }
                    }
                });
            }
        });
    }
}
