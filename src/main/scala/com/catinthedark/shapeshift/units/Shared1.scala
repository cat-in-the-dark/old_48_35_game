package com.catinthedark.shapeshift.units

import com.catinthedark.shapeshift.{Assets, Shared0}
import com.catinthedark.shapeshift.common.Const
import com.catinthedark.shapeshift.entity.{Entity, Tree, Enemy, Player}
import com.catinthedark.shapeshift.view.IDLE

import scala.collection.mutable


class Shared1(val shared0: Shared0,
              var entities: mutable.ListBuffer[Entity]) {
  def reset() = {
    
  }
  
  var (player, enemy) = init()
  
  def init() = {
    if (shared0.networkControl.isServer)
      (Player(Const.Balance.playerStartPos, IDLE, Assets.Animations.HunterAnimationPack, Const.Balance.hunterBalance, 0), Enemy(Const.Balance.playerStartPos, IDLE, Assets.Animations.WolfAnimationPack, 0))
    else
      (Player(Const.Balance.playerStartPos, IDLE, Assets.Animations.WolfAnimationPack, Const.Balance.wolfBalance, 0), Enemy(Const.Balance.playerStartPos, IDLE, Assets.Animations.HunterAnimationPack, 0))
  }
}
