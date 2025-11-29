// === MelodyGenerator.java ===
import java.util.*;

public class MelodyGenerator {

    private static final Map<String, int[]> SCALE_MAP = new HashMap<>();
    static {
        SCALE_MAP.put("C Major", new int[]{60, 62, 64, 65, 67, 69, 71, 72});
        SCALE_MAP.put("A Minor", new int[]{57, 59, 60, 62, 64, 65, 67, 69});
        SCALE_MAP.put("G Major", new int[]{55, 57, 59, 60, 62, 64, 66, 67});
        SCALE_MAP.put("E Minor", new int[]{52, 54, 55, 57, 59, 60, 62, 64});
    }

    private final Random random = new Random();

    public List<Integer> generateMelody(String key, String mood, int bars) {
        int[] scale = SCALE_MAP.getOrDefault(key, SCALE_MAP.get("C Major"));

        int totalNotes = bars * 4;
        List<Integer> melody = new ArrayList<>();

        List<Integer> motif1 = generateMotif(scale, mood);
        List<Integer> motif2 = generateMotif(scale, mood);

        for (int i = 0; i < totalNotes / 4; i++) {
            List<Integer> motifToUse = (i % 2 == 0) ? motif1 : motif2;
            melody.addAll(applyGroove(applyVariation(motifToUse, scale), i));
        }

        return melody;
    }

    private List<Integer> generateMotif(int[] scale, String mood) {
        List<Integer> motif = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int note = scale[random.nextInt(scale.length)];
            motif.add(note);
        }
        return motif;
    }

    private List<Integer> applyVariation(List<Integer> motif, int[] scale) {
        List<Integer> variation = new ArrayList<>();
        for (int note : motif) {
            int change = random.nextBoolean() ? 0 : (random.nextBoolean() ? 12 : -12);
            variation.add(note + change);
        }
        return variation;
    }

    private List<Integer> applyGroove(List<Integer> motif, int barIndex) {
        List<Integer> grooved = new ArrayList<>();
        for (int i = 0; i < motif.size(); i++) {
            int note = motif.get(i);
            if (i % 4 == 0 || i % 4 == 2) {
                grooved.add(note);
            } else {
                if (random.nextBoolean()) grooved.add(note);
                else grooved.add(-1);
            }
        }
        return grooved;
    }
}
