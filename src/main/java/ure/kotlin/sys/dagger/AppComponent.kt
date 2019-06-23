package ure.kotlin.sys.dagger

import dagger.Component
import ure.actors.SpawnActor
import ure.actors.UActorCzar
import ure.actors.actions.UAction
import ure.actors.behaviors.UBehavior
import ure.areas.UArea
import ure.areas.UCartographer
import ure.areas.UCell
import ure.areas.URegion
import ure.areas.gen.Shape
import ure.areas.gen.ULandscaper
import ure.areas.gen.shapers.Shaper
import ure.commands.UCommand
import ure.kotlin.example.ExampleGame
import ure.math.Dimap
import ure.mygame.MyGame
import ure.render.URendererOGL
import ure.sys.ResourceManager
import ure.sys.UCommander
import ure.sys.UREWindow
import ure.sys.dagger.AppModule
import ure.terrain.Stairs
import ure.terrain.UTerrain
import ure.terrain.UTerrainCzar
import ure.things.SpawnItem
import ure.things.UThing
import ure.things.UThingCzar
import ure.ui.Icons.Icon
import ure.ui.Icons.UIconCzar
import ure.ui.RexFile
import ure.ui.UCamera
import ure.ui.ULight
import ure.ui.modals.UModal
import ure.ui.panels.LensPanel
import ure.ui.panels.StatusPanel
import ure.ui.panels.UPanel
import ure.ui.particles.UParticle
import ure.ui.sounds.Sound
import ure.ui.sounds.USpeaker
import javax.inject.Singleton

/**
 * The register for classes that need dependency injection.
 * If you add a class to this register, you'll also need to include
 * Injector.getAppComponent().inject(this);
 * in your constructors to receive the dependencies.  You can then add
 *
 * @Inject
 * UCommander commander
 *
 * (or other injected singletons) to your class and receive the global
 * instance.
 */
@Singleton
@Component(modules = [AppModule::class])
interface AppComponent {
    fun inject(czar: UTerrainCzar)
    fun inject(czar: UThingCzar)
    fun inject(czar: UActorCzar)
    fun inject(czar: UIconCzar)
    fun inject(cartographer: UCartographer)
    fun inject(cmdr: UCommander)
    fun inject(game: ExampleGame)
    fun inject(mygame: MyGame)
    fun inject(act: UAction)
    fun inject(thingi: UThing)
    fun inject(uarea: UArea)
    fun inject(cam: UCamera)
    fun inject(cel: UCell)
    fun inject(terr: UTerrain)
    fun inject(behav: UBehavior)
    fun inject(mod: UModal)
    fun inject(comm: UCommand)
    fun inject(rend: URendererOGL)
    fun inject(statpan: StatusPanel)
    fun inject(lenspan: LensPanel)
    fun inject(landscape: ULandscaper)
    fun inject(reg: URegion)
    fun inject(pan: UPanel)
    fun inject(speak: USpeaker)
    fun inject(stairs: Stairs)
    fun inject(parti: UParticle)
    fun inject(icon: Icon)
    fun inject(rexfile: RexFile)
    fun inject(shape: Shape)
    fun inject(item: SpawnItem)
    fun inject(actor: SpawnActor)
    fun inject(lit: ULight)
    fun inject(snd: Sound)
    fun inject(win: UREWindow)
    fun inject(sha: Shaper)
    fun inject(manager: ResourceManager)
    fun inject(map: Dimap)
}
