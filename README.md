# MemoryGme
　出現したブロックやエンティティの中からペアを見つけ、ペア数、または、クリア時間を競うゲームです。

　マインクラフトのゲームの特性を活かして、動きながらペアを見つけていくアクションゲーム方式の神経衰弱ゲームです。

## 苦労した点

　・　出現させるブロック（エンティティ）の数、出現させるブロック（エンティティ）の間隔、ゲームの時間は、変更しやすいよう
　　　に定数で持つようにしました。

　・　コマンドに、プレイヤーの名前、見つけたペア数、難易度、クリアした時間、ゲームをした時間をコマンドに表示しできるようにし、
　　　DBにも登録するようにしました。

　・　ゲーム開始時には、プレイヤーのスコアは積算されず、０から積算されるようにしました。

　・　出現物は、プレイヤーの一定の場所の目の前に出現させるようにしました。

　・　出現物は、正方形の形になるように出現させるようにしました。

　・　コマンド入力後すぐにゲーム開始するのではなく、カウントダウンをするようにしました。

　・　出現させるペアは一定ではなくランダムになるようにしました。

　・　ゲーム時間の前にゲームをクリアした場合の処理を追加しました。

　・　出現させた出現物については、時間が来たら全て消えるようにしました。

　・　ZOMBIEを選んだ場合は、昼は燃えて消えるので選択後夜にして、ゲーム終了時に戻すようにしました。

　・　連続してペアを触れなかったら、再度最初から触り直すようにするために、連続して触ってペアにならなかった記録をリストに入れて

　　　消去するようにしました。

　・　ブロックとエンティティの出現方法は違うので、各々違う処理としました。

　・　CHICKENは剣で切ると一回で倒してしまうこともあるのでエンティティ出現時は、倒せないようにしました。

　・　右クリックでも左クリックでも触ったことを記録できるようにしました。

## プレイ動画（easyバージョン）



## ゲームの概要
　ゲーム開始時には、常に体力を空腹度をMAXにする.

　コマンドに「MemoryGame」を入力し、引数に「easy」もしくは「normal」もしくは「hard」を入力して難易度を指定。

　ブロックもしくは、エンティティはペアになっています。

　剣で触ってもエンティティは、ダメージを受けないようにしています。

　難易度「hard」の場合は、ZOMBIEが出現するため昼間だと燃えて消えてしまうので、「hard」を選んだ場合は、夜になるように変更し、

　ゲームが終われば、昼に変更。


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

## スコア確認動画


## 対応バージョン

  ・Minecraft : 1.21.4

  ・Spigot : 1.21.4

