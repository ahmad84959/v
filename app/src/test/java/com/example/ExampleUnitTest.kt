package com.example

import com.example.data.LevelRepository
import com.example.model.BirdType
import com.example.model.Vector2D
import com.example.physics.PhysicsEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testLevelRepository_has10DistinctLevels() {
    assertEquals(10, LevelRepository.TOTAL_LEVELS)
    for (i in 1..10) {
      val level = LevelRepository.getLevel(i)
      assertEquals(i, level.levelNumber)
      assertTrue(level.birds.isNotEmpty())
      assertTrue(level.blocks.isNotEmpty())
      assertTrue(level.bears.isNotEmpty())
      assertNotNull(level.titleAr)
      assertNotNull(level.titleEn)
    }
  }

  @Test
  fun testLevel10_isFinalBossLevel() {
    val bossLevel = LevelRepository.getLevel(10)
    val bossBear = bossLevel.bears.find { it.isBoss }
    assertNotNull(bossBear)
    assertTrue(bossBear!!.health >= 200f)
    assertTrue(bossLevel.birds.size >= 4)
  }

  @Test
  fun testPhysicsTrajectoryCalculation() {
    val engine = PhysicsEngine()
    val dragOffset = Vector2D(50f, 30f)
    val trajectory = engine.calculateTrajectory(dragOffset, count = 10)
    assertTrue(trajectory.isNotEmpty())
    assertEquals(engine.slingshotOrigin.x, trajectory[0].x, 0.01f)
    assertEquals(engine.slingshotOrigin.y, trajectory[0].y, 0.01f)
  }

  @Test
  fun testBirdTypes_haveSpecialAbilities() {
    for (type in BirdType.values()) {
      assertTrue(type.displayNameAr.isNotEmpty())
      assertTrue(type.abilityDescAr.isNotEmpty())
      assertTrue(type.baseRadius > 10f)
      assertTrue(type.mass > 0f)
    }
  }
}

