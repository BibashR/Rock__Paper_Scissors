import java.util.Random;

public class RandomStrategy implements Strategy {
    private final Random rand = new Random();
    @Override
    public String getMove(String playerMove) {
        int v = rand.nextInt(3);
        switch (v) {
            case 0: return "R";
            case 1: return "P";
            default: return "S";
        }
    }
    @Override
    public String getName() { return "Random"; }
}
