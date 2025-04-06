package plugin.memoryGame.data;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

/**
 * MemoryGameのゲームを実行する際のプレイヤー情報を扱うオブジェクト。
 * プレイヤー名とペア数、難易度、日時などの情報を持つ。
 */
@Getter
@Setter

public class ExecutingPlayer {
  private int id;
  private String playerName;
  private int score;
  private String difficulty;
  private int clearTime;
  private LocalDate registeredAt;
}
