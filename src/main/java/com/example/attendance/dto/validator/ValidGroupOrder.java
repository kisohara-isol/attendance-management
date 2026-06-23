package com.example.attendance.dto.validator;

import jakarta.validation.GroupSequence;

/**
 * バリデーショングループを定義するためのインターフェース
 */
//バリデーション用アノテーションに(groups = ○○.class)とグループを指定
//まず一番左のクラスを指定したバリデーションがすべて実行され、
//終了したら1つ右のクラスを指定したバリデーションが実行され…という形。
//バリデーションを行うコントローラには@Validated(ValidGroupOrder.class)をつける
@GroupSequence({ ValidFirst.class, ValidSecond.class ,ValidThird.class})
public interface ValidGroupOrder {

}
