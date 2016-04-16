package com.catinthedark.shapeshift.entity

import com.badlogic.gdx.math.Rectangle
import com.catinthedark.shapeshift.Assets.Animations.PlayerAnimationPack
import com.catinthedark.shapeshift.common.Const
import com.catinthedark.shapeshift.view._

case class Enemy(var x: Float, var state: State, var frags: Int, pack: PlayerAnimationPack) {
  var animationCounter = 0f
  
  def texture (delta: Float) = {
    state match {
      case _ =>
        pack.running
    }
//    state match {
//      case UP => pack.up
//      case SHOOTING =>
//        animationCounter += delta
//        pack.shooting.getKeyFrame(animationCounter)
//      case DOWN => pack.up
//      case _ =>
//        println(s"Unknown enemy state $state")
//        pack.up
//    }
  }

  def rect: Rectangle = {
    state match {
      case UP | SHOOTING | RUNNING =>
        new Rectangle(Const.Projection.calcEnemyX(x), Const.UI.enemyY(), Const.UI.enemyUpWH().x, Const.UI.enemyUpWH().y)
      case DOWN | CRAWLING | KILLED =>
        null
    }
  }
  
  def physRect: Rectangle = {
    state match {
      case UP | SHOOTING | RUNNING =>
        new Rectangle(Const.Projection.calcEnemyX(x), Const.UI.enemyY(), Const.UI.enemyUpPhysWH().x, Const.UI.enemyUpPhysWH().y)
      case _ =>
        null
    }
  }
}
case class Player(var x: Float, var state: State, var frags: Int, pack: PlayerAnimationPack, var water: Int = 0) {

  var animationCounter = 0f
  var coolDown = false

  def texture (delta: Float) = {
    state match {
      case _ =>
        animationCounter += delta
        pack.running
    }
//    state match {
//      case UP => pack.up
//      case SHOOTING =>
//        animationCounter += delta
//        pack.shooting.getKeyFrame(animationCounter)
//      case DOWN => pack.down
//      case RUNNING =>
//        animationCounter += delta
//        pack.running.getKeyFrame(animationCounter)
//      case CRAWLING =>
//        animationCounter += delta
//        pack.crawling.getKeyFrame(animationCounter)
//      case KILLED => pack.killed
//    }
  }

  def rect: Rectangle = {
    state match {
      case UP | SHOOTING | RUNNING =>
        new Rectangle(x, Const.UI.playerY(), Const.UI.playerUpWH().x, Const.UI.playerUpWH().y)
      case DOWN | CRAWLING | KILLED =>
        new Rectangle(x, Const.UI.playerY(), Const.UI.playerDownWH().x, Const.UI.playerDownWH().y)
    }
  }
}
