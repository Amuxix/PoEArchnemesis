package poe.nemesis

import io.circe.{Codec, Encoder}
import poe.Calculate.*
import poe.nemesis.Reward.*
import poe.nemesis.Archnemesis.*
import poe.nemesis.{Reward, Special}

enum Archnemesis(val rewards: List[Reward], val ingredients: List[Archnemesis], val regex: String):
  case Toxic extends Archnemesis(List(Generic, Gems), List.empty, "to")
  case Chaosweaver extends Archnemesis(List(Gems), List.empty, "ch")
  case Frostweaver extends Archnemesis(List(Armour), List.empty, "fr.+w")
  case Permafrost extends Archnemesis(List(Generic, Armour), List.empty, "pe")
  case Hasted extends Archnemesis(List(Generic), List.empty, "has")
  case Deadeye extends Archnemesis(List(Armour, Trinkets), List.empty, "dea")
  case Bombardier extends Archnemesis(List(Weapon, Armour), List.empty, "bom")
  case Flameweaver extends Archnemesis(List(Weapon), List.empty, "fl.+w")
  case Incendiary extends Archnemesis(List(Generic, Weapon), List.empty, "inc")
  case ArcaneBuffer extends Archnemesis(List(Essences), List.empty, "arc")
  case Echoist extends Archnemesis(List(Generic, Currency), List.empty, "ec")
  case Stormweaver extends Archnemesis(List(Trinkets), List.empty, "st.+w")
  case Dynamo extends Archnemesis(List(Generic, Trinkets), List.empty, "dy")
  case Bonebreaker extends Archnemesis(List(Generic, Weapon), List.empty, "bon")
  case Bloodletter extends Archnemesis(List(Weapon, Trinkets, Corrupted), List.empty, "blo")
  case SteelInfused extends Archnemesis(List(Weapon), List.empty, "ste")
  case Gargantuan extends Archnemesis(List(Currency), List.empty, "ga")
  case Berserker extends Archnemesis(List(Uniques), List.empty, "be")
  case Sentinel extends Archnemesis(List(Armour, Armour), List.empty, "se")
  case Juggernaut extends Archnemesis(List(Harbinger), List.empty, "ju")
  case Vampiric extends Archnemesis(List(Fossils), List.empty, "va")
  case Overcharged extends Archnemesis(List(Trinkets, Trinkets), List.empty, "ov")
  case SoulConduit extends Archnemesis(List(Maps), List.empty, "so.+co")
  case Opulent extends Archnemesis(List(Reward.Opulent), List.empty, "op")
  case Malediction extends Archnemesis(List(DivinationCards), List.empty, "mal")
  case Consecrator extends Archnemesis(List(Fragments), List.empty, "con")
  case Frenzied extends Archnemesis(List(Generic, Uniques), List.empty, "fre")
  case HeraldingMinions extends Archnemesis(List(Fragments, Fragments), List(Dynamo, ArcaneBuffer), "her")
  case Assassin extends Archnemesis(List(Currency, Currency), List(Deadeye, Vampiric), "as")
  case Necromancer extends Archnemesis(List(Generic, Reroll2), List(Bombardier, Overcharged), "ne")
  case Rejuvenating extends Archnemesis(List(Currency, Reroll1), List(Gargantuan, Vampiric), "rej")
  case Executioner extends Archnemesis(List(Legion, Breach), List(Frenzied, Berserker), "exe")
  case Hexer extends Archnemesis(List(Essences, Essences), List(Chaosweaver, Echoist), "hex")
  case DroughtBringer extends Archnemesis(List(Labyrinth, Labyrinth), List(Malediction, Deadeye), "dr")
  case Entangler extends Archnemesis(List(Fossils, Fossils), List(Toxic, Bloodletter), "en")
  case FrostStrider extends Archnemesis(List(Armour, Armour, Armour), List(Frostweaver, Hasted), "fr.+d")
  case IcePrison extends Archnemesis(List(Armour, Armour, Reroll1), List(Permafrost, Sentinel), "ic")
  case FlameStrider extends Archnemesis(List(Weapon, Weapon, Weapon), List(Flameweaver, Hasted), "fl.+d")
  case MagmaBarrier extends Archnemesis(List(Weapon, Weapon, Reroll2), List(Incendiary, Bonebreaker), "mag")
  case MirrorImage extends Archnemesis(List(Scarabs, Reroll2), List(Echoist, SoulConduit), "mi")
  case StormStrider extends Archnemesis(List(Trinkets, Trinkets, Trinkets), List(Stormweaver, Hasted), "sto.+d")
  case ManaSiphoner extends Archnemesis(List(Trinkets, Trinkets, Reroll1), List(Consecrator, Dynamo), "man")
  case Corrupter extends Archnemesis(List(Abyss, Abyss, CorruptAll), List(Bloodletter, Chaosweaver), "corr")
  case TreantHorde extends Archnemesis(List(Generic, RandomDrop), List(Toxic, Sentinel, SteelInfused), "tre")
  case Evocationist extends Archnemesis(List(Generic, Weapon, Armour, Trinkets), List(Flameweaver, Frostweaver, Incendiary), "ev")
  case Invulnerable extends Archnemesis(List(Delirium, Metamorphosis), List(Sentinel, Juggernaut, Consecrator), "inv")
  case CorpseDetonator extends Archnemesis(List(DivinationCards, DivinationCards), List(Necromancer, Incendiary), "corp")
  case Trickster extends Archnemesis(List(Currency, Uniques, DivinationCards), List(Overcharged, Assassin, Echoist), "tric")
  case TemporalBubble extends Archnemesis(List(Heist, Expedition), List(Juggernaut, Hexer, ArcaneBuffer), "te")
  case SoulEater extends Archnemesis(List(Maps, Maps), List(SoulConduit, Necromancer, Gargantuan), "so.+ea")
  case CrystalSkinned extends Archnemesis(List(Harbinger, Harbinger), List(Permafrost, Rejuvenating, Berserker), "cr")
  case EmpoweredElements extends Archnemesis(List(Uniques, Uniques, Reroll1), List(Evocationist, SteelInfused, Chaosweaver), "e.+el")
  case EmpoweringMinions extends Archnemesis(List(Blight, Ritual), List(Necromancer, Executioner, Gargantuan), "e.+mi")
  case Effigy extends Archnemesis(List(DivinationCards, DivinationCards, Reroll1), List(Hexer, Malediction, Corrupter), "ef")
  case TukohamaTouched extends Archnemesis(List(Weapon, Weapon, Fragments, Reroll4), List(Bonebreaker, Executioner, MagmaBarrier), "tu")
  case AbberathTouched extends Archnemesis(List(Trinkets, Trinkets, Maps, Reroll4), List(FlameStrider, Frenzied, Rejuvenating), "abb")
  case BrineKingTouched extends Archnemesis(List(Armour, Armour, Armour, Reroll6), List(IcePrison, StormStrider, HeraldingMinions), "bri")
  case ArakaaliTouched extends Archnemesis(List(DivinationCards, DivinationAll), List(CorpseDetonator, Entangler, Assassin), "ara")
  case ShakariTouched extends Archnemesis(List(Uniques, UniqueAll), List(Entangler, SoulEater, DroughtBringer), "sh")
  case LunarisTouched extends Archnemesis(List(Uniques, AdditionalReward), List(Invulnerable, FrostStrider, EmpoweringMinions), "lu")
  case SolarisTouched extends Archnemesis(List(Scarabs, AdditionalReward), List(Invulnerable, MagmaBarrier, EmpoweringMinions), "sol")
  case KitavaTouched extends Archnemesis(List(Generic, DoubleReward), List(TukohamaTouched, AbberathTouched, Corrupter, CorpseDetonator), "ki")
  case InnocenceTouched extends Archnemesis(List(Currency, Currency, Currency, CurrencyAll), List(LunarisTouched, SolarisTouched, MirrorImage, ManaSiphoner), "inn")

  lazy val name: String = toString.replaceAll("([a-z])([A-Z])", "$1 $2")
  lazy val ingredientFor: List[Archnemesis] = Archnemesis.values.filter(_.ingredients.contains(this)).toList
  lazy val ingredientForBest: Set[Archnemesis] =
    def inner(archnemesis: Archnemesis): Set[Archnemesis] =
      archnemesis.ingredientFor match {
        case List() => Set(archnemesis)
        case ingredientFor => ingredientFor.flatMap(inner).toSet
      }
    inner(this) - this
  val baseIngredients: List[Archnemesis] = ingredients match
    case List() => List(this)
    case ingredients => ingredients.flatMap(_.allIngredients)
  val allIngredients: List[Archnemesis] = ingredients match
    case List() => List(this)
    case ingredients => this +: ingredients.flatMap(_.allIngredients)
  val allIngredientAmounts: Map[Archnemesis, Int] = allIngredients.groupBy(identity).view.mapValues(_.size).toMap
  val allIngredientAmountsByTier: Map[Int, Int] = allIngredients.groupBy(_.tier).view.mapValues(_.size).toMap
  val simpleReward: Double = rewards.sumBy(_.value).toDouble * (1 + (rewards.count(!_.isInstanceOf[Special]) - 1) * .05)
  /*lazy val reward: Double = (simpleReward +: ingredientFor.map { creates =>
    creates.reward / (creates.ingredients.size * (1 + creates.ingredients.size * .05))
  }).max*/
  lazy val reward: Double =
    ((ingredientForBest - this).map { best =>
      (best.reward * best.allIngredientAmounts(this) * baseIngredients.size) / best.baseIngredients.size
    } + simpleReward).max
  lazy val roundedReward: Double = (reward / 100).round(2)
  lazy val rewardReason: String = if simpleReward == reward then "Self" else "Craft"
  val tier: Int = ingredients match {
    case List() => 0
    case ingredients => ingredients.maxBy(_.tier).tier + 1
  }
  val toCraft: Int = ingredients match {
    case List() => 1
    case ingredients => ingredients.sumBy(_.toCraft)
  }

object Archnemesis:
  given Codec[Archnemesis] = Codec.from(_.as[String].map(Archnemesis.valueOf), Encoder[String].contramap[Archnemesis](_.toString))
