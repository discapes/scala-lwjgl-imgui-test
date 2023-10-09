package dev.miikat.scalatest.game

import dev.miikat.scalatest.engine.Camera
import org.lwjgl.glfw.GLFW.*
import scala.util.Using
import org.lwjgl.system.MemoryStack
import org.joml.*

import math.Fractional.Implicits.infixFractionalOps
import math.Integral.Implicits.infixIntegralOps
import math.Numeric.Implicits.infixNumericOps

class CameraControls:
  var prevCursorPos: Vector2f = null

  def processInput(win: Long, camera: Camera, delta: Double): Unit =
    processMouse(win, camera)
    processKeyboard(win, camera, delta)

  def processMouse(win: Long, camera: Camera) =
    val cursorPos = getCursorPos(win)
    if prevCursorPos == null then prevCursorPos = cursorPos
    val move = Vector2f(cursorPos).sub(prevCursorPos)
    prevCursorPos = cursorPos
    
    camera.yaw += move.x
    // glfw screen space has top left as origin, so we subtract y instead of adding
    camera.pitch = Math.clamp(-89.9f, 89.9f, camera.pitch - move.y)
     
  def processKeyboard(win: Long, camera: Camera, delta: Double) =
    val mult = 4f
    val forward = camera.forward
    val right = camera.right
    val forwardFlat = Vector3f(forward.x, 0, forward.z).normalize()
    val rightFlat = Vector3f(right.x, 0, right.z).normalize()
    val moveDir = Vector3f()

    if glfwGetKey(win, GLFW_KEY_SPACE) == GLFW_PRESS then
      moveDir.y += 1
    if glfwGetKey(win, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS then
      moveDir.y -= 1

    if glfwGetKey(win, GLFW_KEY_W) == GLFW_PRESS then
      moveDir.add(forwardFlat)
    if glfwGetKey(win, GLFW_KEY_S) == GLFW_PRESS then
      moveDir.sub(forwardFlat)
      
    if glfwGetKey(win, GLFW_KEY_D) == GLFW_PRESS then
      moveDir.add(rightFlat)
    if glfwGetKey(win, GLFW_KEY_A) == GLFW_PRESS then
      moveDir.sub(rightFlat)

    // if the length is 0, normalize returns NaN
    if (moveDir.length > 0)
      camera.pos.add(moveDir.normalize().mul(mult * delta.toFloat))

  def getCursorPos(win: Long) =
    Using.resource(MemoryStack.stackPush()): stack =>
      val (x, y) = (stack.mallocDouble(1), stack.mallocDouble(1))
      glfwGetCursorPos(win, x, y)
      Vector2f(x.get.toFloat, y.get.toFloat)