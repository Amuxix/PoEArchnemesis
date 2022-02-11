package poe

import java.awt.Robot

sealed trait Special

enum Reward(val value: Int):
  case Corrupted extends Reward(50)
  case Generic extends Reward(1)
  case Weapon extends Reward(1)
  case Armour extends Reward(1)
  case Uniques extends Reward(1)
  case Gems extends Reward(1)
  case Currency extends Reward(500)
  case Maps extends Reward(400)
  case Trinkets extends Reward(1)
  case Fossils extends Reward(10)
  case Essences extends Reward(200)
  case DivinationCards extends Reward(150)
  case Fragments extends Reward(100)
  case Harbinger extends Reward(10)
  case Blight extends Reward(10)
  case Ritual extends Reward(10)
  case Legion extends Reward(10)
  case Breach extends Reward(10)
  case Labyrinth extends Reward(10)
  case Heist extends Reward(10)
  case Expedition extends Reward(600)
  case Scarabs extends Reward(450)
  case Abyss extends Reward(10)
  case Delirium extends Reward(500)
  case Metamorphosis extends Reward(10)

  case Opulent extends Reward(350) with Special
  case Reroll1 extends Reward(100) with Special
  case Reroll2 extends Reward(150) with Special
  case Reroll3 extends Reward(225) with Special
  case Reroll4 extends Reward(338) with Special
  case Reroll6 extends Reward(759) with Special
  case CorruptAll extends Reward(50) with Special
  case RandomDrop extends Reward(200) with Special
  case AdditionalReward extends Reward(100) with Special
  case DivinationAll extends Reward(-50) with Special
  case UniqueAll extends Reward(-100) with Special
  case CurrencyAll extends Reward(1500) with Special
  case DoubleReward extends Reward(1500) with Special
