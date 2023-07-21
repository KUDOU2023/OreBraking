package plugin.orebraking.data;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecutingPlayer {

    private String playerName;
    private int score;
    private int gameTime;


    public ExecutingPlayer(String playerName) {
        this.playerName = playerName;
    }
}
