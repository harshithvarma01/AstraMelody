// MidiPlayer.java (Layered Playback Engine)
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.sound.midi.*;

public class MidiPlayer {
    private static final Map<String, Integer> INSTRUMENT_MAP = new HashMap<>();
    static {
        INSTRUMENT_MAP.put("Piano", 0);
        INSTRUMENT_MAP.put("Guitar", 24);
        INSTRUMENT_MAP.put("Synth", 81);
    }

    private Sequencer sequencer;

    public MidiPlayer() {
        try {
            sequencer = MidiSystem.getSequencer();
            sequencer.open();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playMelody(List<Integer> melody, int tempoBPM, String instrumentName) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, 4);
            Track melodyTrack = sequence.createTrack();
            Track chordTrack = sequence.createTrack();
            Track arpTrack = sequence.createTrack();
            Track padTrack = sequence.createTrack();

            int instrument = INSTRUMENT_MAP.getOrDefault(instrumentName, 0);

            melodyTrack.add(programChange(0, instrument));
            chordTrack.add(programChange(1, instrument));
            arpTrack.add(programChange(2, instrument));
            padTrack.add(programChange(3, instrument));

            int tick = 0;

            for (int note : melody) {
                if (note != -1) {
                    melodyTrack.add(noteOn(0, note, 100, tick));
                    melodyTrack.add(noteOff(0, note, 100, tick + 2));

                    // Chords
                    chordTrack.add(noteOn(1, note, 60, tick));
                    chordTrack.add(noteOn(1, note + 4, 60, tick));
                    chordTrack.add(noteOn(1, note + 7, 60, tick));
                    chordTrack.add(noteOff(1, note, 60, tick + 4));
                    chordTrack.add(noteOff(1, note + 4, 60, tick + 4));
                    chordTrack.add(noteOff(1, note + 7, 60, tick + 4));

                    // Arpeggios (spread by 1 tick)
                    arpTrack.add(noteOn(2, note, 50, tick));
                    arpTrack.add(noteOff(2, note, 50, tick + 1));
                    arpTrack.add(noteOn(2, note + 4, 50, tick + 1));
                    arpTrack.add(noteOff(2, note + 4, 50, tick + 2));
                    arpTrack.add(noteOn(2, note + 7, 50, tick + 2));
                    arpTrack.add(noteOff(2, note + 7, 50, tick + 3));

                    // Pad
                    padTrack.add(noteOn(3, note, 30, tick));
                    padTrack.add(noteOff(3, note, 30, tick + 8));
                }

                tick += 4;
            }

            sequencer.setSequence(sequence);
            sequencer.setTempoInBPM(tempoBPM);
            sequencer.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void exportMelody(List<Integer> melody, int tempoBPM, String instrumentName, File file) {
        try {
            Sequence sequence = new Sequence(Sequence.PPQ, 4);
            Track melodyTrack = sequence.createTrack();
            Track chordTrack = sequence.createTrack();
            Track arpTrack = sequence.createTrack();
            Track padTrack = sequence.createTrack();

            int instrument = INSTRUMENT_MAP.getOrDefault(instrumentName, 0);

            melodyTrack.add(programChange(0, instrument));
            chordTrack.add(programChange(1, instrument));
            arpTrack.add(programChange(2, instrument));
            padTrack.add(programChange(3, instrument));

            int tick = 0;

            for (int note : melody) {
                if (note != -1) {
                    melodyTrack.add(noteOn(0, note, 100, tick));
                    melodyTrack.add(noteOff(0, note, 100, tick + 2));

                    chordTrack.add(noteOn(1, note, 60, tick));
                    chordTrack.add(noteOn(1, note + 4, 60, tick));
                    chordTrack.add(noteOn(1, note + 7, 60, tick));
                    chordTrack.add(noteOff(1, note, 60, tick + 4));
                    chordTrack.add(noteOff(1, note + 4, 60, tick + 4));
                    chordTrack.add(noteOff(1, note + 7, 60, tick + 4));

                    arpTrack.add(noteOn(2, note, 50, tick));
                    arpTrack.add(noteOff(2, note, 50, tick + 1));
                    arpTrack.add(noteOn(2, note + 4, 50, tick + 1));
                    arpTrack.add(noteOff(2, note + 4, 50, tick + 2));
                    arpTrack.add(noteOn(2, note + 7, 50, tick + 2));
                    arpTrack.add(noteOff(2, note + 7, 50, tick + 3));

                    padTrack.add(noteOn(3, note, 30, tick));
                    padTrack.add(noteOff(3, note, 30, tick + 8));
                }
                tick += 4;
            }

            MidiSystem.write(sequence, 1, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private MidiEvent programChange(int channel, int instrument) throws Exception {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.PROGRAM_CHANGE, channel, instrument, 0);
        return new MidiEvent(msg, 0);
    }

    private MidiEvent noteOn(int channel, int note, int velocity, int tick) throws Exception {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_ON, channel, note, velocity);
        return new MidiEvent(msg, tick);
    }

    private MidiEvent noteOff(int channel, int note, int velocity, int tick) throws Exception {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_OFF, channel, note, velocity);
        return new MidiEvent(msg, tick);
    }
    public void playSingleNote(int note, String instrumentName) {
    try {
        Synthesizer synth = MidiSystem.getSynthesizer();
        synth.open();
        MidiChannel[] channels = synth.getChannels();

        int instrument;
        switch (instrumentName) {
            case "Piano":
                instrument = 0;
                break;
            case "Guitar":
                instrument = 24;
                break;
            case "Synth":
                instrument = 80;
                break;
            default:
                instrument = 0;
                break;
        }

        channels[0].programChange(instrument);
        channels[0].noteOn(note, 100);

        Thread.sleep(400); // short note duration
        channels[0].noteOff(note);

        synth.close();
    } catch (Exception e) {
        e.printStackTrace();
    }
}

}
