package plugin.orebraking.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import plugin.orebraking.PlayerScoreData;
import plugin.orebraking.OreBraking;
import plugin.orebraking.data.ExecutingPlayer;
import plugin.orebraking.mapper.data.PlayerScore;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;


/**
 * 制限時間内に出現した鉱石を破壊し、スコアを獲得するゲームを起動するコマンドです。
 * スコアは鉱石によって変わり、破壊した鉱石の合計によってスコアが変動します。
 * 結果はプレイヤー名、点数、日時で保存されます。
 */
public class OreBrakingCommand extends BaseCommand implements Listener {


        private final List<Block> floorOreBlockList = new ArrayList<>();

        private final List<ExecutingPlayer> executingPlayerList = new ArrayList<>();

        public final int GAME_TIME = 40;

        private int argsX;
        private int argsZ;
        private int argsY;


        public static String LIST = "list";

        List<Integer> coordinateList = new ArrayList<>();

        private Location returnLocation;

        private PlayerInventory inventory;


        private final OreBraking oreBraking;

        private final PlayerScoreData playerScoreData = new PlayerScoreData();

        public OreBrakingCommand(OreBraking oreBraking) {
                this.oreBraking = oreBraking;
        }

        @Override
        public boolean onExecutePlayerCommand(Player player, Command command, String label, String[] args) {
//                最初の引数が「list」だったらスコアを一覧表示して処理を終了する
                if (args.length == 1 && LIST.equals(args[0])){
                        sendPlayerScoreList(player);
                        return false;
                }

//              1度範囲外を指定すると次に範囲内をしてもエラーが表示されるのでクリア　　
                List<Integer> coordinates = getCoordinates(args, player);
                if (coordinates.get(0) == 0 && coordinates.get(1) == 0 && coordinates.get(2) == 0) {
                        this.coordinateList.clear();
                        return false;
                }

                ExecutingPlayer nowExecutingPlayer = getPlayerScore(player);

                initPlayerStatus(player);

                AppearBlockOnPlayer(player, coordinates.get(0), coordinates.get(1), coordinates.get(2));

                player.sendTitle(ChatColor.GRAY + "鉱山の",ChatColor.RED + "大爆発" + "  " + "ご用心！！！",0,60,0);

                gamePlay(player, nowExecutingPlayer);

                return false;
        }

        @Override
        public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label, String[] args) {
                return false;
        }

        /**
         * 現在登録されているスコアの情報を表示します
         * @param player　コマンド実行者したプレイヤー
         */
        private void sendPlayerScoreList(Player player) {
                List<PlayerScore> playerScoreList = playerScoreData.selectList();
                for (PlayerScore playerScore : playerScoreList) {
                        player.sendMessage(playerScore.getId() + " | "
                                + playerScore.getPlayerName() + " | "
                                + playerScore.getScore() + " | "
                                + playerScore.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                }
        }

        /**
         *　コマンド数が３個かつ範囲内の数であるかチェックする
         * @param args コマンド引数
         * @param player　コマンドを実行したプレイヤー
         * @return 範囲内の数であればその数字のリストを範囲外であればリストを０にして返す
         */
        private List<Integer> getCoordinates(String[] args, Player player) {
                if (args.length == 3) {
                        this.argsX = Integer.parseInt(args[0]);
                        this.argsZ = Integer.parseInt(args[1]);
                        this.argsY = Integer.parseInt(args[2]);

                        if (4 < argsX && argsX < 11 && 4 < argsZ && argsZ < 11 && 0 < argsY && argsY < 21) {
                                coordinateList.addAll(Arrays.asList(argsX, argsZ, argsY));
                        }
                }
                coordinateList.addAll(Arrays.asList(0, 0, 0));
                if (coordinateList.equals(Arrays.asList(0, 0, 0))) {
                        player.sendMessage(ChatColor.DARK_RED + "実行出来ません。コマンド引数の各値を[x]横,[z]縦は５～１０までの値,[y]高さは１～２０までの値を入力してください。");
                }

                return coordinateList;
        }

        /**
         * プレイヤーが死亡した際にドロップを無くす
         * @param e プレイヤー死亡時イベント
         */
        @EventHandler
        public void onPlayerDeathEvent(PlayerDeathEvent e) {
                e.getDrops().clear();
        }

        /**
         *スコアの加点とブロック破壊時にドロップを無くす
         *
         * @param e ブロック破壊時に発生するイベント
         */
        @EventHandler
        public void onOreBreak(BlockBreakEvent e) {
                Block oreblock = e.getBlock();
                Player player = e.getPlayer();

                if(floorOreBlockList.stream()
                        .noneMatch(block -> block.equals(oreblock))){
                        return;
                }

                if (floorOreBlockList.stream()
                        .anyMatch(block -> block.equals(oreblock))) {
                        e.setDropItems(false);
                }

                if (oreblock.getType().equals(Material.REDSTONE_ORE)) {
                        player.getWorld().createExplosion(oreblock.getLocation(), 50.0f);
                        player.damage(20);
                        player.sendMessage(ChatColor.RED + "エクスプローーージョン！");
                }


                executingPlayerList.stream()
                        .filter(p -> p.getPlayerName().equals(player.getName()))
                        .findFirst()
                        .ifPresent(p -> {
                                int point = switch (oreblock.getType()) {
                                        case EMERALD_ORE -> 500;
                                        case DIAMOND_ORE -> 300;
                                        case GOLD_ORE -> 50;
                                        case STONE -> 5;
                                        default -> 0;
                                };

                                p.setScore(p.getScore() + point);
                                player.sendMessage("現在のスコアは " + p.getScore() + "点！");
                        });
        }

        /**
         * 鉱石の出現エリアを取得します。
         * 出現エリアはセンターブロックを中心に
         * オフセットでコマンド引数分のブロックを取得し、鉱石にブロックを変換
         *
         * @param player　コマンドを実行したプレイヤー
         * @param argsX args[0]
         * @param argsZ args[1]
         * @param argsY args[2]
         */
        private void AppearBlockOnPlayer(Player player, int argsX, int argsZ, int argsY) {

                player.setHealth(20);
//                コマンド実行場所への帰還
                this.returnLocation = player.getLocation();

                Location firstLocation = player.getLocation();
                Location centerLocation = firstLocation.add(0, 40, 0);

                Block centerBlock = centerLocation.getBlock();


                for (int x = -argsX; x <= argsX; x++) {
                        for (int y = -argsY; y <= 0; y++) {
                                for (int z = -argsZ; z <= argsZ; z++) {
//                                        指定されたオフセットで空気ブロックを取得
                                        Block block = centerBlock.getRelative(x, y, z);
                                        floorOreBlockList.add(block);
                                }
                        }
                }

                shuffleChangeOreBlocks(floorOreBlockList, Material.STONE, 1.0);
                shuffleChangeOreBlocks(floorOreBlockList, Material.GOLD_ORE, 0.3);
                shuffleChangeOreBlocks(floorOreBlockList, Material.REDSTONE_ORE, 0.1);

                shuffleChangeDiamondAndEmeraldOreBlocks(floorOreBlockList);

                player.teleport(centerLocation.add(0, 1, 0));
                player.sendMessage("Number of blocks created: " + floorOreBlockList.size());
        }

        /**
         * 鉱石ブロックへ割合で変換
         * @param floorOreBlocksList　オフセットで取得したブロックのリスト
         * @param material　変換先の鉱石
         * @param changeRate ブロック変換率
         */
        private void shuffleChangeOreBlocks(List<Block> floorOreBlocksList, Material material, double changeRate) {
                int changeBlockCount = (int) (floorOreBlocksList.size() * changeRate);

                Collections.shuffle(floorOreBlocksList);

                for (int i = 0; i < floorOreBlocksList.size(); i++) {
                        if (i < changeBlockCount) {
                                floorOreBlocksList.get(i).setType(material);
                        }
                }

        }

        private void shuffleChangeDiamondAndEmeraldOreBlocks(List<Block> floorBlocksList) {
                int changeBlockCount1 = (int) (floorBlocksList.size() * 0.02);
                int changeBlockCount2 = (int) (floorBlocksList.size() * 0.01);


                Collections.shuffle(floorBlocksList);

                for (int i = 0; i < changeBlockCount1; i++) {
                        floorBlocksList.get(i).setType(Material.DIAMOND_ORE);
                }

                for (int i = changeBlockCount1; i < changeBlockCount1 + changeBlockCount2; i++) {
                        floorBlocksList.get(i).setType(Material.EMERALD_ORE);
                }
        }

        private ExecutingPlayer getPlayerScore(Player player) {
                ExecutingPlayer executingPlayer = new ExecutingPlayer(player.getName());

                if (executingPlayerList.isEmpty()) {
                        executingPlayer = addNewPlayer(player);
                } else {
                        executingPlayer = executingPlayerList.stream().findFirst().map(ps
                                -> ps.getPlayerName().equals(player.getName())
                                ? ps
                                : addNewPlayer(player)).orElse(executingPlayer);
                }

                executingPlayer.setGameTime(GAME_TIME);
                executingPlayer.setScore(0);
                return executingPlayer;
        }

        private ExecutingPlayer addNewPlayer(Player player) {
                ExecutingPlayer newPlayer = new ExecutingPlayer(player.getName());
                executingPlayerList.add(newPlayer);
                return newPlayer;
        }

        private void initPlayerStatus(Player player) {

                removePointEffect(player);
                player.setHealth(20);

                enchantDigSpeedPickaxe(player);
        }

        private static void removePointEffect(Player player){
                player.getActivePotionEffects().stream()
                        .map(PotionEffect::getType)
                        .forEach(player::removePotionEffect);
        }

        /**
         * メタ（内部）情報効率強化を追加し、採掘速度を上昇
         *
         * @param player 実行したプレイヤー
         */
        public void enchantDigSpeedPickaxe(Player player){
                ItemStack netheritePickaxe = new ItemStack(Material.NETHERITE_PICKAXE);


                ItemMeta meta = netheritePickaxe.getItemMeta();
                Objects.requireNonNull(meta).addEnchant(Enchantment.DIG_SPEED,5,true);
                netheritePickaxe.setItemMeta(meta);

                this.inventory = player.getInventory();
                inventory.setItemInMainHand(netheritePickaxe);
        }

        private void gamePlay(Player player, ExecutingPlayer nowExecutingPlayer) {
                Bukkit.getScheduler().runTaskTimer(oreBraking, Runnable -> {
                        if (nowExecutingPlayer.getGameTime() <= 0 || player.getHealth() <= 0) {
                                Runnable.cancel();

                                player.sendTitle("ゲームが終了しました。",
                                        nowExecutingPlayer.getPlayerName() + " " + "合計" + nowExecutingPlayer.getScore() + "点！",
                                        0, 60, 20);

                                endOreBrakingGame(player,nowExecutingPlayer);
                                return;
                        }

                        nowExecutingPlayer.setGameTime(nowExecutingPlayer.getGameTime() - 5);
                }, 0, 5 * 20);
        }

        /**
         *ゲーム終了後の処理
         * @param player プレイヤー
         * @param nowExecutingPlayer　実行しているプレイヤー
         */
        private void endOreBrakingGame(Player player, ExecutingPlayer nowExecutingPlayer){
                player.sendTitle("ゲームが終了しました。",
                        nowExecutingPlayer.getPlayerName() + " " + "合計" + nowExecutingPlayer.getScore() + "点！",
                        0, 60, 20);

                floorOreBlockList.forEach(block -> block.setType(Material.AIR));
                floorOreBlockList.clear();
                coordinateList.clear();

                this.inventory.setItemInMainHand(new ItemStack(Material.AIR));

                player.teleport(this.returnLocation);

                playerScoreData.insert
                        (new PlayerScore(nowExecutingPlayer.getPlayerName()
                                ,nowExecutingPlayer.getScore()));
        }
}
