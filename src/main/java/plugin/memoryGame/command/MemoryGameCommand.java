package plugin.memoryGame.command;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import plugin.memoryGame.Main;
import java.util.UUID;
import plugin.memoryGame.PlayerScoreData;
import plugin.memoryGame.data.ExecutingPlayer;
import plugin.memoryGame.mapper.data.PlayerScore;

public class MemoryGameCommand extends BaseCommand implements Listener {

  // 定数の定義
  public static final String EASY = "easy";
  public static final String NORMAL = "normal";
  public static final String HARD = "hard";
  public static final String NONE = "none";
  private static final int SIDE_LENGTH = 4; // ブロックの配置の行と列の数
  private static final int BLOCK_SPACING = 3; // ブロック間のスペース
  private static final int INITIAL_GAME_TIME = 30; // 初期時間
  private static World hardDifficultyWorld;
  // マップとリストの定義
  private static Map<UUID, Integer> entityPairMap = new HashMap<>();
  private Map<UUID, Object> enemyMap = new HashMap<>();// クラスフィールドとして宣言
  private Map<UUID, UUID> firstClickedBlockUUIDs = new HashMap<>();// クラスのフィールドとして追加
  private Map<UUID, Integer> firstClickedBlockPairIds = new HashMap<>();
  private final Map<UUID, Integer> lastClickedPairIds = new HashMap<>();// プレイヤーごとの最後に触れたペアIDを保持するMap
  private final Map<UUID, UUID> lastClickedEntityIds = new HashMap<>();// プレイヤーごとの最後に触れたエンティティUUIDを保持するMap
  private List<ExecutingPlayer> executingPlayerList = new ArrayList<>();
  private Set<UUID> missMatchList = new HashSet<>();// 新しいゲーム開始を待っているプレイヤーのセット
  private final Map<Player, BukkitTask> activeTimers = new HashMap<>();

  private static final String LIST ="list";
  private Main main;
  private PlayerScoreData playerScoreData = new PlayerScoreData();



  public MemoryGameCommand(Main main) {
    this.main = main;
  }

  @Override
  public boolean onExecutePlayerCommand(Player player, Command command, String label,
      String[] args) {
    String difficulty = getDifficulty(args, player);

    if (HARD.equals(difficulty)) {
      hardDifficultyWorld = player.getWorld();
      setNight(player.getWorld());
    }

    if (args.length == 1 && LIST.equals(args[0])){
      sendPlayerScoreList(player);
      return false;
    }

    if (executingPlayerList.isEmpty()) {
      addNewPlayer(player);
    } else {
      for (ExecutingPlayer executingPlayer : executingPlayerList) {
        if (!executingPlayer.getPlayerName().equals(player.getName())) {
          addNewPlayer(player);
        }
      }
    }

    if (difficulty.equals(NONE)) {
      return false;
    }


    setPlayerDifficulty(player, difficulty);

    resetPlayerScore(player);

    healPlayer(player);

    Location finalLocation = getFinalLocation(player);

    startCountdown(player, finalLocation, difficulty);

    return true;
  }

  @Override
  public boolean onExecuteNPCCommand(CommandSender sender, Command command, String label,
      String[] args) {
    return false;
  }

  /**
   * 現在登録されているスコアの一覧をメセージに送る。
   *
   * @param player プレイヤー
   */
  private void sendPlayerScoreList(Player player) {
    List<PlayerScore> playerScoreList = playerScoreData.selectList();
    for (PlayerScore playerScore : playerScoreList){
      player.sendMessage(playerScore.getId() + " | "
          + playerScore.getPlayerName() + " | "
          + playerScore.getScore() + " | "
          + playerScore.getDifficulty() + " | "
          + playerScore.getClearTime() + " | "
          + playerScore.getRegisteredAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    }
  }

  /**
   * 新規のプレイヤー情報をリストに追加。
   *
   * @param player コマンドを実行したプレイヤー
   */
  private void addNewPlayer(Player player) {
    ExecutingPlayer newPlayer = new ExecutingPlayer();
    newPlayer.setPlayerName(player.getName());
    executingPlayerList.add(newPlayer);
  }

  /**
   * プレイヤーのスコアをリセット
   *
   * @param player スコアをリセットするプレイヤー
   */
  private void resetPlayerScore(Player player) {
    for (ExecutingPlayer executingPlayer : executingPlayerList) {
      if (executingPlayer.getPlayerName().equals(player.getName())) {
        executingPlayer.setScore(0);
        break;
      }
    }
  }

  /**
   * コマンド引数から難易度を取得。
   *
   * @param args コマンド引数の配列
   * @param player コマンドを実行したプレイヤー
   * @return 有効な難易度（"easy", "normal", "hard"）が指定された場合はその難易度、
   *         それ以外の場合は "NONE"
   */
  private String getDifficulty(String[] args, Player player) {
    Set<String> difficulties = new HashSet<>();
    difficulties.add("easy");
    difficulties.add("normal");
    difficulties.add("hard");

    if (args.length == 1 && difficulties.contains(args[0])) {
      return args[0];
    }

    // エラーメッセージを送信
    player.sendMessage(ChatColor.RED + "実行できません。コマンド引数の１つ目に難易度設定が必要です。[easy, normal, hard]");
    return "none";
  }


  /**
   * プレイヤーを回復し、最高級の装備を与える。
   *
   * @param player 回復と装備を与えるプレイヤー
   */
  private void healPlayer(Player player) {
    player.setHealth(20);
    player.setFoodLevel(20);

    PlayerInventory inventory = player.getInventory();
    inventory.setHelmet(new ItemStack(Material.NETHERITE_HELMET));
    inventory.setChestplate(new ItemStack(Material.NETHERITE_CHESTPLATE));
    inventory.setLeggings(new ItemStack(Material.NETHERITE_LEGGINGS));
    inventory.setBoots(new ItemStack(Material.NETHERITE_BOOTS));
    inventory.setItemInMainHand(new ItemStack(Material.NETHERITE_SWORD));
  }

  /**
   * プレイヤーの前方10ブロックの位置にある安全な地上の位置を計算。
   *
   * @param player テレポート先を計算するプレイヤー
   * @return プレイヤーの前方6ブロックにある安全な地上の位置
   */
  private Location getFinalLocation(Player player) {
    World world = player.getWorld();
    Location playerLocation = player.getLocation();
    Location targetLocation = playerLocation.clone().add(playerLocation.getDirection().normalize().multiply(10));
    int groundY = world.getHighestBlockYAt(targetLocation);
    Location finalLocation = new Location(world, targetLocation.getX(), groundY + 1, targetLocation.getZ());
    return finalLocation;
  }

  /**
   * ゲーム開始前のカウントダウンを開始し、ゲームを準備するメソッド。
   * カウントダウン後、ブロックを配置し、ゲームタイマーを開始。
   *
   * @param player ゲームを開始するプレイヤー
   * @param finalLocation ゲームの開始位置
   * @param difficulty ゲームの難易度
   */
  private void startCountdown(Player player, Location finalLocation, String difficulty) {
    new BukkitRunnable() {
      int countdownTime = 3;

      @Override
      public void run() {
        if (countdownTime > 0) {
          showCountdownTitle(player, countdownTime);
          countdownTime--;
        } else {
          startGame(player, finalLocation, difficulty);
          cancel();
        }
      }
    }.runTaskTimer(main, 0L, 20L);
  }

  /**
   * ゲーム開始までのカウントダウンをプレイヤーにタイトルとして表示。
   * 指定された秒数をタイトルに含めて表示。
   *
   * @param player       タイトルを表示する対象のプレイヤー
   * @param countdownTime カウントダウンの残り時間（秒）
   */
  private void showCountdownTitle(Player player, int countdownTime) {
    player.sendTitle(ChatColor.YELLOW + "ゲーム開始まで", ChatColor.RED.toString() + countdownTime + "秒", 10, 20, 10);
  }

  /**
   * ゲームを開始し、プレイヤーに開始タイトルを表示し、エンティティを設定し、ゲームタイマーを開始。
   *
   * @param player       ゲームを開始するプレイヤー
   * @param finalLocation ゲームの最終的な位置
   * @param difficulty   ゲームの難易度
   */
  private void startGame(Player player, Location finalLocation, String difficulty) {
    player.sendTitle(ChatColor.GREEN + "ゲームスタート！", "", 10, 20, 10);
    settingEntity(difficulty, finalLocation);
    gameTimer(player);
  }


  /**
   * 指定された難易度に基づいてエンティティまたはブロックを設定。
   * 生成されたエンティティまたはブロックは、enemyMapに保存。
   * 既存のenemyMapのデータはクリア。
   *
   * @param difficulty 難易度を示す文字列。EASY, NORMAL, HARDのいずれか。
   * @param finalLocation エンティティまたはブロックを生成する場所
   */
  private void settingEntity(String difficulty, Location finalLocation) {
    Map<Integer, UUID> idMap = new HashMap<>();
    AtomicInteger idCounter = new AtomicInteger(0);

    enemyMap.clear();

    switch (difficulty) {
      case EASY -> spawnBlocks(finalLocation, Material.OAK_WOOD, idMap, enemyMap, idCounter);
      case NORMAL -> spawnEntities(finalLocation, EntityType.CHICKEN, idMap, enemyMap, idCounter);
      case HARD -> spawnEntities(finalLocation, EntityType.ZOMBIE, idMap, enemyMap, idCounter);
    }
  }

  /**
   * 指定された中心位置を基準に、ブロックのグリッドを生成。
   *
   * @param centerLocation グリッドの中心となる位置
   * @param material 生成するブロックの種類
   * @param idMap ID to UUID のマッピングを格納するマップ
   * @param idCounter ユニークなIDを生成するためのカウンター
   */
  private void spawnBlocks(Location centerLocation, Material material,
      Map<Integer, UUID> idMap,
      Map<UUID, Object> enemyMap,
      AtomicInteger idCounter) {
    Location baseLocation = centerLocation.clone().add(
        -(SIDE_LENGTH - 1) * BLOCK_SPACING / 2.0,
        0,
        -(SIDE_LENGTH - 1) * BLOCK_SPACING / 2.0
    );

    List<Integer> pairIds = generatePairIds(SIDE_LENGTH * SIDE_LENGTH);

    int index = 0;
    for (int x = 0; x < SIDE_LENGTH; x++) {
      for (int z = 0; z < SIDE_LENGTH; z++) {
        Location blockLoc = baseLocation.clone().add(x * BLOCK_SPACING, 0, z * BLOCK_SPACING);
        Block block = blockLoc.getBlock();
        block.setType(material);
        UUID uuid = UUID.randomUUID();
        int id = idCounter.incrementAndGet();
        idMap.put(id, uuid);
        enemyMap.put(uuid, block);
        entityPairMap.put(uuid, pairIds.get(index));
        index++;
      }
    }
  }

  /**
   * 指定された中心位置を基準に、エンティティのグリッドを生成。
   *
   * @param centerLocation グリッドの中心となる位置
   * @param entityType スポーンさせるエンティティの種類
   * @param idMap ID to UUID のマッピングを格納するマップ
   * @param idCounter ユニークなIDを生成するためのカウンター
   */
  private void spawnEntities(Location centerLocation, EntityType entityType,
      Map<Integer, UUID> idMap,
      Map<UUID, Object> enemyMap,
      AtomicInteger idCounter) {
    Location baseLocation = centerLocation.clone().add(
        -(SIDE_LENGTH - 1) * BLOCK_SPACING / 2.0,
        0,
        -(SIDE_LENGTH - 1) * BLOCK_SPACING / 2.0
    );

    List<Integer> pairIds = generatePairIds(SIDE_LENGTH * SIDE_LENGTH);

    int index = 0;
    for (int x = 0; x < SIDE_LENGTH; x++) {
      for (int z = 0; z < SIDE_LENGTH; z++) {
        Location spawnLoc = baseLocation.clone().add(x * BLOCK_SPACING, 0, z * BLOCK_SPACING);
        Entity entity = Objects.requireNonNull(spawnLoc.getWorld()).spawnEntity(spawnLoc, entityType);
        UUID uuid = entity.getUniqueId();
        int id = idCounter.incrementAndGet();
        idMap.put(id, uuid);
        enemyMap.put(uuid, entity);
        entityPairMap.put(uuid, pairIds.get(index));
        index++;
      }
    }
  }

  /**
   * ペアのIDを生成するためのリストを作成。
   *
   * @param count 生成するIDの総数。
   * @return シャッフルされたペアIDのリスト
   * @throws IllegalArgumentException countが奇数の場合
   */
  private List<Integer> generatePairIds(int count) {
    List<Integer> pairIds = new ArrayList<>();
    for (int i = 0; i < count / 2; i++) {
      pairIds.add(i + 1);
      pairIds.add(i + 1);
    }
    Collections.shuffle(pairIds);
    return pairIds;
  }

  /**
   * プレイヤーごとのゲームタイマーを開始。
   * 既存のタイマーがある場合はキャンセルし、新しいタイマーを開始。
   * タイマーが0になると、ゲームを終了し、プレイヤーに通知。
   *
   * @param player ゲームタイマーを開始するプレイヤー
   */
  private void gameTimer(Player player) {
    if (activeTimers.containsKey(player)) {
      activeTimers.get(player).cancel();
    }

    AtomicInteger remainingTime = new AtomicInteger(INITIAL_GAME_TIME);

    BukkitTask task = Bukkit.getScheduler().runTaskTimer(main, () -> {
      if (remainingTime.get() > 0) {
        remainingTime.decrementAndGet();
        if (enemyMap.isEmpty()) {
          int clearTime = INITIAL_GAME_TIME - remainingTime.get();

          for (ExecutingPlayer playerScore : executingPlayerList) {
            if (playerScore.getPlayerName().equals(player.getName())) {
              handleGameClear(player, clearTime, playerScore);
              break;
            }
          }
        }
      } else {
        for (ExecutingPlayer playerScore : executingPlayerList) {
          if (playerScore.getPlayerName().equals(player.getName())) {
            handleTimeUp(player, playerScore);
            break;
          }
        }
      }
    }, 0L, 20L);

    activeTimers.put(player, task);
  }



  /**
   * プレイヤーがゲームをクリアした際の処理を行うメソッド。 タイマーを停止し、クリアメッセージを表示。
   *
   * @param player クリアしたプレイヤー
   */
  private void handleGameClear(Player player, int clearTime, ExecutingPlayer executingPlayer) {
    // タイマーを停止
    if (activeTimers.containsKey(player)) {
      activeTimers.get(player).cancel();
    }

    clearClickData(player);

    addCleaTimeList(player, clearTime);

    playerScoreData.insert(new PlayerScore(executingPlayer.getPlayerName(), executingPlayer.getScore(),
        executingPlayer.getDifficulty(), executingPlayer.getClearTime()));

    // クリアメッセージの表示
    player.sendTitle(
        ChatColor.GOLD + "全てクリアしました！",
        player.getName(),
        0, 70, 20
    );

    if (HARD.equals(executingPlayer.getDifficulty())) {
      restoreWorldTime(player);
    }
  }

  /**
   * プレイヤーの制限時間が切れた際の処理を行うメソッド。 タイマーを停止・削除し、スポーンしたエンティティをクリアし、 時間切れメッセージを表示。
   *
   * @param player 制限時間が切れたプレイヤー
   */
  private void handleTimeUp(Player player, ExecutingPlayer executingPlayer) {
    // タイマー終了処理
    activeTimers.get(player).cancel();
    activeTimers.remove(player);
    clearSpawnedEntities();

    clearClickData(player);

    addCleaTimeList(player, INITIAL_GAME_TIME);

    playerScoreData.insert(new PlayerScore(executingPlayer.getPlayerName(), executingPlayer.getScore(),
        executingPlayer.getDifficulty(), executingPlayer.getClearTime()));

    player.sendTitle(
        ChatColor.RED + "時間切れ",
        ChatColor.YELLOW + "ゲーム終了 " + ChatColor.GOLD + player.getName(),
        0, 70, 20
    );

    if (HARD.equals(executingPlayer.getDifficulty())) {
      restoreWorldTime(player);
    }
  }

  /**
   * ワールドの時間を夜に設定する。
   *
   * @param world 時間を変更するワールド
   */
  private void setNight(World world) {
    world.setTime(18000);
  }

  /**
   * ワールドの時間を元に戻す。
   *
   * @param player 時間を元に戻す対象のプレイヤー
   */
  private void restoreWorldTime(Player player) {
    if (hardDifficultyWorld != null && hardDifficultyWorld.equals(player.getWorld())) {
      hardDifficultyWorld.setTime(0);
      hardDifficultyWorld = null;
    }
  }

  /**
   * プレイヤーのスコアを増加させ、現在のペア数をプレイヤーに通知するメソッド。
   *
   * @param player スコアを増加させるプレイヤー
   */
  private void addScore(Player player) {
    for (ExecutingPlayer executingPlayer : executingPlayerList){
      if (executingPlayer.getPlayerName().equals(player.getName())){
        executingPlayer.setScore(executingPlayer.getScore() + 1);
      }
      player.sendMessage(ChatColor.GOLD + "ペアを見つけました！ 現在のペア数: " + executingPlayer.getScore() + "ペア");
    }
  }

  /**
   * 難易度をplayerScoreListに登録するメソッド。
   *
   * @param player    コマンドを実行したプレイヤー
   * @param difficulty 設定する難易度
   */
  private void setPlayerDifficulty(Player player, String difficulty) {
    for (ExecutingPlayer executingPlayer : executingPlayerList) {
      if (executingPlayer.getPlayerName().equals(player.getName())) {
        executingPlayer.setDifficulty(difficulty);
        break;
      }
    }
  }

  /**
   * PlayerScoreにクリア時間を登録し、プレイヤーにクリア時間を通知。
   *
   * @param player    ゲームをクリアしたプレイヤー
   */
  private void addCleaTimeList(Player player, int clearTime) {
    for (ExecutingPlayer executingPlayer : executingPlayerList) {
      if (executingPlayer.getPlayerName().equals(player.getName())) {
        int finalClearTime = Math.min(clearTime, INITIAL_GAME_TIME);

        executingPlayer.setClearTime(finalClearTime);
        player.sendMessage(ChatColor.GOLD + "タイム: " + finalClearTime + "秒");
        break;
      }
    }
  }

  /**
   * プレイヤーのクリック情報をクリア。
   *
   * @param player クリック情報をクリアするプレイヤー
   */
  private void clearClickData(Player player) {
    UUID playerId = player.getUniqueId();
    firstClickedBlockUUIDs.remove(playerId);
    firstClickedBlockPairIds.remove(playerId);
    lastClickedPairIds.remove(playerId);
    lastClickedEntityIds.remove(playerId);
    missMatchList.remove(playerId);
  }

  /**
   * プレイヤーがブロックをクリックした際のイベントを処理。
   *
   * @param event プレイヤーのクリックイベント。
   */
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event) {
    Player player = event.getPlayer();
    UUID playerId = player.getUniqueId();
    Action action = event.getAction();

    if (action == Action.RIGHT_CLICK_BLOCK || action == Action.LEFT_CLICK_BLOCK) {
      Block clickedBlock = event.getClickedBlock();
      if (clickedBlock == null) {
        return;
      }

      if (missMatchList.contains(playerId)) {
        resetPlayerState(playerId, player);
        return;
      }

      Location blockLocation = clickedBlock.getLocation();
      UUID blockUUID = findBlockUUID(blockLocation);

      if (blockUUID != null && entityPairMap.containsKey(blockUUID)) {
        handleBlockInteraction(player, playerId, blockUUID);
      }
    }
  }

  /**
   * プレイヤーの状態をリセット。
   *
   * @param playerId リセット対象のプレイヤーのUUID
   * @param player リセット対象のプレイヤーオブジェクト
   */
  private void resetPlayerState(UUID playerId, Player player) {
    missMatchList.remove(playerId);
    firstClickedBlockUUIDs.remove(playerId);
    firstClickedBlockPairIds.remove(playerId);
    player.sendMessage("最初から選び直してください。");
  }

  /**
   * プレイヤーがクリックしたブロックの処理。
   *
   * @param player クリックしたプレイヤーオブジェクト
   * @param playerId クリックしたプレイヤーのUUID
   * @param blockUUID クリックされたブロックのUUID
   */
  private void handleBlockInteraction(Player player, UUID playerId, UUID blockUUID) {
    int currentPairId = entityPairMap.get(blockUUID);

    if (firstClickedBlockUUIDs.containsKey(playerId) &&
        firstClickedBlockUUIDs.get(playerId).equals(blockUUID)) {
      return;
    }

    if (firstClickedBlockUUIDs.containsKey(playerId)) {
      UUID lastClickedBlockUUID = firstClickedBlockUUIDs.get(playerId);
      int lastClickedBlockPairId = firstClickedBlockPairIds.get(playerId);

      if (lastClickedBlockPairId == currentPairId && !lastClickedBlockUUID.equals(blockUUID)) {
        handleMatchedPair(player, lastClickedBlockUUID, blockUUID);
      } else {
        handleMismatch(player, playerId, blockUUID);
      }

      firstClickedBlockUUIDs.remove(playerId);
      firstClickedBlockPairIds.remove(playerId);
    } else {
      firstClickedBlockUUIDs.put(playerId, blockUUID);
      firstClickedBlockPairIds.put(playerId, currentPairId);

      int blockId = entityPairMap.get(blockUUID);
      player.sendMessage(ChatColor.GREEN + "ブロックNo:" + blockId);
    }
  }

  /**
   * プレイヤーがクリックしたブロックがペアとして一致しなかった場合の処理。
   *
   * @param player クリックしたプレイヤーオブジェクト
   * @param playerId プレイヤーのUUID
   * @param blockUUID クリックされたブロックのUUID
   */
  private void handleMismatch(Player player, UUID playerId, UUID blockUUID) {
    int blockId = entityPairMap.get(blockUUID);
    player.sendMessage(ChatColor.GREEN + "ブロックのNo:" + blockId);

    // ミスマッチリストに追加して新しい開始を待つ状態にする
    missMatchList.add(playerId);
  }


  /**
   * 指定された位置に対応するブロックのUUIDを検索。
   *
   * @param location 検索対象のブロックの位置
   * @return 対応するブロックのUUID。一致するブロックが見つからない場合は、nullを返す
   */
  private UUID findBlockUUID(Location location) {
    for (Map.Entry<UUID, Object> entry : enemyMap.entrySet()) {
      if (entry.getValue() instanceof Block && ((Block) entry.getValue()).getLocation().equals(location)) {
        return entry.getKey();
      }
    }
    return null;
  }

  /**
   * プレイヤーが一致するペアのブロックを見つけた場合の処理。
   *
   * @param player 一致するペアを見つけたプレイヤーオブジェクト
   * @param lastBlockUUID 最初にクリックされたブロックのUUID
   * @param currentBlockUUID 2回目にクリックされたブロックのUUID
   */
  private void handleMatchedPair(Player player, UUID lastBlockUUID, UUID currentBlockUUID) {
    Block lastBlock = (Block) enemyMap.get(lastBlockUUID);
    if (lastBlock != null) {
      lastBlock.setType(Material.AIR);
    }
    Block currentBlock = (Block) enemyMap.get(currentBlockUUID);
    if (currentBlock != null) {
      currentBlock.setType(Material.AIR);
    }

    enemyMap.remove(lastBlockUUID);
    enemyMap.remove(currentBlockUUID);
    entityPairMap.remove(lastBlockUUID);
    entityPairMap.remove(currentBlockUUID);

    addScore(player);
  }


  /**
   * プレイヤーがエンティティとインタラクションを行った際に処理を行うイベントハンドラ。
   * このメソッドは、プレイヤーがエンティティを右クリックした場合に呼び出される。
   * メインハンドでのインタラクションのみを処理。
   *
   * @param event プレイヤーのエンティティインタラクションイベント
   */
  @EventHandler
  public void onEntityInteract(PlayerInteractEntityEvent event) {
    if (event.getHand() == EquipmentSlot.HAND) {
      handleEntityInteraction(event.getPlayer(), event.getRightClicked(), "右クリック");
    }
  }

  /**
   * エンティティがプレイヤーから攻撃（ダメージ）を受けた際に処理を行うイベントハンドラ。
   * このメソッドは、プレイヤーがエンティティを攻撃（左クリック）した場合に呼び出される。
   *
   * @param event エンティティがダメージを受けたイベント
   */
  @EventHandler
  public void onEntityDamage(EntityDamageByEntityEvent event) {
    if (event.getDamager() instanceof Player) {
      handleEntityInteraction((Player) event.getDamager(), event.getEntity(), "左クリック");
      if (enemyMap.containsKey(event.getEntity().getUniqueId())) {
        event.setCancelled(true);
      }
    }
  }

  /**
   * プレイヤーがエンティティとインタラクションを行った際の処理を行う。
   * このメソッドは、プレイヤーがエンティティとインタラクションを行った際の共通処理を実装する。
   *
   * @param player        インタラクションを行ったプレイヤー
   * @param target        インタラクションの対象となったエンティティ
   * @param interactionType インタラクションの種類（"右クリック" または "左クリック"）
   */
  private void handleEntityInteraction(Player player, Entity target, String interactionType) {
    UUID playerId = player.getUniqueId();
    UUID entityUUID = target.getUniqueId();

    if (missMatchList.contains(playerId)) {
      missMatchList.remove(playerId);
      lastClickedEntityIds.remove(playerId);
      lastClickedPairIds.remove(playerId);
      player.sendMessage("最初から選び直してください。");
      return;
    }

    if (entityPairMap.containsKey(entityUUID)) {
      int currentPairId = entityPairMap.get(entityUUID);

      if (lastClickedEntityIds.containsKey(playerId) &&
          lastClickedEntityIds.get(playerId).equals(entityUUID)) {
        return;
      }

      if (lastClickedEntityIds.containsKey(playerId)) {
        UUID lastClickedEntityUUID = lastClickedEntityIds.get(playerId);
        int lastClickedPairId = lastClickedPairIds.get(playerId);

        if (lastClickedPairId == currentPairId && !lastClickedEntityUUID.equals(entityUUID)) {
          handleMatchedEntityPair(player, lastClickedEntityUUID, entityUUID);
        } else {
          int entityId = entityPairMap.get(entityUUID);
          player.sendMessage(ChatColor.GREEN + "エンティティNo:" + entityId);
          missMatchList.add(playerId);
        }
        lastClickedEntityIds.remove(playerId);
        lastClickedPairIds.remove(playerId);
      } else {
        lastClickedEntityIds.put(playerId, entityUUID);
        lastClickedPairIds.put(playerId, currentPairId);
        int entityId = entityPairMap.get(entityUUID);
        player.sendMessage(ChatColor.GREEN + "エンティティNo:" + entityId);
      }
    }
  }

  /**
   * エンティティのペアが一致した場合の処理。
   *
   * @param player          インタラクションを行ったプレイヤー
   * @param lastEntityUUID  以前にインタラクションを行ったエンティティのUUID
   * @param currentEntityUUID 現在インタラクションを行ったエンティティのUUID
   */
  private void handleMatchedEntityPair(Player player, UUID lastEntityUUID, UUID currentEntityUUID) {
    Entity lastEntity = (Entity) enemyMap.get(lastEntityUUID);
    Entity currentEntity = (Entity) enemyMap.get(currentEntityUUID);

    if (lastEntity != null) {
      lastEntity.remove();
    }
    if (currentEntity != null) {
      currentEntity.remove();
    }

    enemyMap.remove(lastEntityUUID);
    enemyMap.remove(currentEntityUUID);
    entityPairMap.remove(lastEntityUUID);
    entityPairMap.remove(currentEntityUUID);

    addScore(player);
  }

  /**
   * スポーンしたエンティティをクリアする。
   */
  private void clearSpawnedEntities() {
    for (Object obj : enemyMap.values()) {
      if (obj instanceof Entity) {
        Entity entity = (Entity) obj;
        entity.remove();
      } else if (obj instanceof Block) {
        Block block = (Block) obj;
        block.setType(Material.AIR);
      }
    }
    enemyMap.clear();
  }
}
