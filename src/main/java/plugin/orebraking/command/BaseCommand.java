package plugin.orebraking.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.IOException;

public abstract class BaseCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            try {
                return onExecutePlayerCommand(player,command,label,args);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else {
            return onExecuteNPCCommand(sender,command,label,args);
        }
    }

    /**
     * コマンド実行者がプレイヤーだった場合に実行します。
     * @param player　コマンドを実行したプレイヤー
     * @param command コマンド
     * @param label ラベル
     * @param args コマンド引数
     * @return 処理の実行有無
     */
    public abstract boolean onExecutePlayerCommand(Player player,Command command, String label, String[] args) throws IOException;

    /**
     * コマンド実行者がプレイヤーだった場合に実行します。
     * @param sender　コマンド実行
     * @param command コマンド
     * @param label ラベル
     * @param args コマンド引数
     * @return 処理の実行有無
     */
    public abstract boolean onExecuteNPCCommand(CommandSender sender,Command command, String label, String[] args);
}
