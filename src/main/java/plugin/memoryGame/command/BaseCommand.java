package plugin.memoryGame.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * コマンドを実行して動かすプラグイン処理の基底クラス。
 */
public abstract class BaseCommand implements CommandExecutor {

  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
    if(sender instanceof Player player){
      return onExecutePlayerCommand(player, command, label, args);
    }else {
      return onExecuteNPCCommand(sender, command, label, args);
    }
  }

  /**
   * コマンド実行者がプレイヤーだった場合に実行。
   * @param player　コマンドを実行したプレイヤー
   * @param command コマンド
   * @param  label ラベル
   * @param  args コマンド引数
   * @return 処理の実行有無
   */
  public abstract boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args);

  /**
   * コマンドの実行者がプレイヤー以外だった場合に実行。
   * @param sender　コマンドの実行者
   * @param command コマンド
   * @param  label ラベル
   * @param  args コマンド引数
   * @return 処理の実行有無
   */
  public abstract boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args);

}
