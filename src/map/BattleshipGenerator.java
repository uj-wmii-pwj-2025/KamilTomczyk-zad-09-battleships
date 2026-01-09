package map;

public interface BattleshipGenerator {

    String generateMap();

    static BattleshipGenerator defaultInstance() {
        return BattleshipGeneratorImpl.defaultInstance();
    }
}