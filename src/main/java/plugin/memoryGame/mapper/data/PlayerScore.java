package plugin.memoryGame.mapper.data;

import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * プレイヤースコアを扱うオブジェクト。
 * DBに存在するテーブルと連動する。
 */
@Getter
@Setter
@NoArgsConstructor
public class PlayerScore {

  private int id;
  private String playerName;
  private int score;
  private String difficulty;
  private int clearTime;
  private LocalDateTime registeredAt;

  public PlayerScore(String playerName, int score, String difficulty, int clearTime){
    this.playerName = playerName;
    this.score = score;
    this.difficulty = difficulty;
    this.clearTime = clearTime;
  }

}
