public class Cheat implements Strategy {
    @Override
    public String getMove(String playerMove) {
        switch (playerMove) {
            case "R": return "P";
            case "P": return "S";
            case "S": return "R";
            default:  return "R";
        }
    }
    @Override
    public String getName() { return "Cheat"; }
}
