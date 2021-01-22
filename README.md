# LR2oraja に以下の変更を加えています

* gdx や ffmpeg, portaudio など各種ライブラリを最新化
* 利用されていない Twitter 連携を削除し jar をスリム化
* OpenJDK 15 でビルド
* 起動時の最新バージョンチェック処理の実行有無の設定
* 皿チョン（beatoraja 正式導入版）
* フルスクリーン時にリフレッシュレートを指定できる機能
* 選曲画面で緑数値が変更できない不具合を修正
* WASAPI 共有/排他モードを追加
* 判定自動調整機能を追加（LR2 の JUDGE AUTO ADJUST のような機能）
* オーディオにクリッピング/ディザリングを ON/OFF する設定を追加

なお、beatoraja を Java 15 以降のバージョンで起動する方法は以下を参照してください
https://codoc.jp/sites/haaEBtTkhA/entries/rDDRUocsng
