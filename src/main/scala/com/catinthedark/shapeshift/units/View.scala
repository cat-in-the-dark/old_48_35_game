package com.catinthedark.shapeshift.units

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.{Texture, OrthographicCamera, Color, GL20}
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.maps.MapLayer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType
import com.badlogic.gdx.math.Vector2
import com.catinthedark.shapeshift.Assets
import com.catinthedark.shapeshift.common.Const
import com.catinthedark.shapeshift.common.Const.Balance
import com.catinthedark.shapeshift.entity.Tree
import com.catinthedark.shapeshift.view._
import com.catinthedark.lib._
import com.catinthedark.shapeshift.common.Const
import Magic.richifySpriteBatch
import org.lwjgl.util.Point
import scala.collection.mutable

/**
  * Created by over on 02.01.15.
  */
abstract class View(val shared: Shared1) extends SimpleUnit with Deferred {
  val batch = new SpriteBatch()
  val magicBatch = new MagicSpriteBatch(Const.debugEnabled())
  val shapeRenderer = new ShapeRenderer()
  var trees = mutable.ListBuffer[Tree]()

  val enemyView = new EnemyView(shared) with LocalDeferred
  val camera = new OrthographicCamera(Const.Projection.width, Const.Projection.height)
  camera.position.x = Const.Projection.width / 2
  camera.position.y = Const.Projection.height / 2
  camera.update()

  shared.shared0.networkControl.onMovePipe.ports += enemyView.onMove
  //  shared.shared0.networkControl.onShootPipe.ports += enemyView.onShoot
  //  shared.shared0.networkControl.onAlivePipe.ports += enemyView.onAlive

  override def onActivate() = {
    val layers = Assets.Maps.map1.getLayers
    plantTrees(layers.get("tree1"))
    plantTrees(layers.get("tree2"))
    plantTrees(layers.get("tree3"))
  }

  def plantTrees(layer: MapLayer) = {
    val mapTrees = layer.getObjects.iterator()
    while (mapTrees.hasNext) {
      val tree = mapTrees.next()
      val x = tree.getProperties.get("x", classOf[Float])
      val y = tree.getProperties.get("y", classOf[Float])
      trees += new Tree(x, y, Const.Balance.treeRadius, layerToTexture(layer.getName))
    }
  }

  def onMoveLeft(u: Unit): Unit = {
    val speed = Const.gamerSpeed()
    shared.player.pos.x -= speed
    shared.shared0.networkControl.move(shared.player.pos, shared.player.angle, idle = false)
  }

  def onMoveRight(u: Unit): Unit = {
    val speed = Const.gamerSpeed()
    shared.player.pos.x += speed
    shared.shared0.networkControl.move(shared.player.pos, shared.player.angle, idle = false)
  }

  def onMoveForward(u: Unit): Unit = {
    val speed = Const.gamerSpeed()
    shared.player.pos.y += speed
    shared.shared0.networkControl.move(shared.player.pos, shared.player.angle, idle = false)
  }

  def onMoveBackward(u: Unit): Unit = {
    val speed = Const.gamerSpeed()
    shared.player.pos.y -= speed
    shared.shared0.networkControl.move(shared.player.pos, shared.player.angle, idle = false)
  }
  
  def onIdle(u: Unit): Unit = {
    shared.shared0.networkControl.move(shared.player.pos, shared.player.angle, idle = true)
  }

  def layerToTexture(layerName: String) = layerName match {
    case "tree1" => Assets.Textures.tree1
    case "tree2" => Assets.Textures.tree2
    case "tree3" => Assets.Textures.tree3
    case _ => throw new RuntimeException(s"ooops. texture for layer $layerName not found")
  }

  def drawFloor() =
    batch.managed { self =>
      self.draw(Assets.Textures.floor, 0,0,0,0, 3200, 3200)
    }

//  def drawTreeLayer(layer: MapLayer) = {
//    val trees = layer.getObjects.iterator()
//    val texture = layerToTexture(layer.getName)
//    batch.managed { self =>
//      while (trees.hasNext) {
//        val tree = trees.next()
//        val x = tree.getProperties.get("x", classOf[Float])
//        val y = tree.getProperties.get("y", classOf[Float])
//
//        self.drawCentered(texture, x, y)
//      }
//    }
//  }

  override def run(delta: Float) = {
    Gdx.gl.glClearColor(0, 0, 0, 0)
    Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

    println(s"ppos: x=${shared.player.pos.x}, y=${shared.player.pos.y}")
    println(s"epos: x=${shared.enemy.pos.x}, y=${shared.enemy.pos.y}")


    if (shared.player.pos.x > Const.Projection.width / 2
      && shared.player.pos.x < Const.Projection.mapWidth - Const.Projection.width / 2)
      camera.position.x = shared.player.pos.x
    if (shared.player.pos.y > Const.Projection.height / 2 &&
      shared.player.pos.y < Const.Projection.mapHeight - Const.Projection.height / 2)
      camera.position.y = shared.player.pos.y

    println(s"cpos: x=${camera.position.x}, y=${camera.position.y}")

    camera.update()
    batch.setProjectionMatrix(camera.combined)
    magicBatch.setProjectionMatrix(camera.combined)
    shapeRenderer.setProjectionMatrix(camera.combined)

    drawFloor()
    enemyView.run(delta)

//    val layers = Assets.Maps.map1.getLayers
//    drawTreeLayer(layers.get("tree1"))
//    drawTreeLayer(layers.get("tree2"))
//    drawTreeLayer(layers.get("tree3"))

    trees = trees.sortWith((a, b) => {
      shared.player.pos.dst(a.x, a.y) < shared.player.pos.dst(b.x, b.y)
    })

    shapeRenderer.begin(ShapeType.Filled)
    shapeRenderer.setColor(Color.RED)
    trees.foreach(tree => {
      val pos = shared.player.pos
      val maxRadius = shared.player.balance.maxRadius
      val distance = pos.dst(tree.x, tree.y)
      if (distance < maxRadius) {
        val alpha = Math.atan2(tree.y - pos.y, tree.x - pos.x)
        val beta = Math.asin(Balance.treeRadius / distance)
        val x1 = (pos.x + distance * Math.cos(alpha - beta)).toFloat
        val y1 = (pos.y + distance * Math.sin(alpha - beta)).toFloat
        val x2 = (pos.x + distance * Math.cos(alpha + beta)).toFloat
        val y2 = (pos.y + distance * Math.sin(alpha + beta)).toFloat
        val x3 = (x2 + (maxRadius - distance) * Math.cos(alpha + beta)).toFloat
        val y3 = (y2 + (maxRadius - distance) * Math.sin(alpha + beta)).toFloat
        val x4 = (x1 + (maxRadius - distance) * Math.cos(alpha - beta)).toFloat
        val y4 = (y1 + (maxRadius - distance) * Math.sin(alpha - beta)).toFloat
        shapeRenderer.triangle(x1, y1, x2, y2, x3, y3)
        shapeRenderer.triangle(x1, y1, x3, y3, x4, y4)
      }
    })

    var x = 500
    var y = 300
    var x1 = Gdx.input.getX()
    var y1 = Const.Projection.height.toInt - Gdx.input.getY()
    var phi = 60f
    var dx = x1 - x
    var dy = y1 - y
    var alpha = Math.atan2(dy, dx) * 180 / Math.PI
    var start = alpha + phi / 2
    var degrees = 360 - phi
    shapeRenderer.arc(500, 300, 150, start.toFloat, degrees)
    shapeRenderer.line(x, y, x1, y1)
    shapeRenderer.end()

    magicBatch managed { batch =>
      trees.foreach(tree => {
        magicBatch.drawCircleCentered(tree.texture, tree.x, tree.y, Const.UI.treePhysRadius)
      })

      magicBatch.drawWithDebug(shared.player.texture(delta), shared.player.rect, shared.player.physRect)
      enemyView.render(delta, batch)
    }
  }

  override def onExit() = {
  }
}
