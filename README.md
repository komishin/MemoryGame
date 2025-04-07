# MemoryGme
出現したブロックやエンティティーの中ならペアを見つけて、ペア数もしくは、クリア時間の速さを競うゲームです。



## ゲームの概要
ゲーム開始時には、常に体力を空腹度をMAXにする.

コマンドに「MemoryGame」を入力し、引数に「easy」もしくは「normal」もしくは「hard」を入力して難易度を指定。

ブロックもしくは、エンティティーはペアになっています。

剣で触ってもエンティティーは、ダメージを受けないようにしています。

難易度「hard」の場合は、ZOMBIEが出現するため昼間だと燃えて消えてしまうので、「hard」を選んだ場合は、夜になるように変更し、ゲームが終われば、昼に変更。


|難易度　　　　 |出現物　　　 |備考 　　　　　　　　　　　|
|-----|-----|-----|
| easy | ORK_WOOD |        |
| normal | CHICKEN | 剣でのダメージなし |
| hard | ZOMBIE | 剣でのダメージなし＆夜に変更 |

## 遊び方

１．コマンドに難易度を選択して入力

   難易度 easy 「memorygame easy」

   難易度 normal 「memorygame normal」

   難易度 hard 「memorygame hard」

２. 正方形の形に難易度に応じた対象物が１６個出現します。

３．右クリックもしくは左クリックで出現物を選択して下さい。

４．別の場所の出現物を触れてください。

５．ペアが選択されたら”ペアを見つけました！ 現在のペア数”と表示しペア数が記録されます。

　　ペアでなければ、”最初から選び直してください”と表示されるので、一個目から選び直すようにしてください。

６．制限時間３０秒以内に何ペア見つけられるかの勝負です。

　　もし、時間内にすべて消せた場合は、クリア時間も記録されます。

## 対応バージョン

  ・Minecraft : 1.21.4

  ・Spigot : 1.21.4

