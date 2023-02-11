package view

import tools.aqua.bgw.components.ComponentView
import tools.aqua.bgw.util.Coordinate
import kotlin.math.cos
import kotlin.math.sin

/**
 * Utility class with static methods. Mainly to help move around [ComponentView]s.
 */
class Utility {
    companion object{
        /**
         * @param comp The comp you want the center
         * @return Local center of the [ComponentView] as [Coordinate]
         */
        fun getCenter(comp : ComponentView) : Coordinate{
            return Coordinate((comp.width / 2), (comp.height / 2))
        }

        /**
         * Moves the given [ComponentView] by some other origin to the specified position
         * @param comp The [ComponentView] to move
         * @param origin The anchor point for the translation
         * @param pos The position where the component is moved to
         */
        private fun moveByOtherOrigin(comp : ComponentView, origin : Coordinate, pos : Coordinate){
            val translationVec = pos - origin
            comp.reposition(comp.posX + translationVec.xCoord, comp.posY + translationVec.yCoord)
        }

        /**
         * Moves the given [ComponentView] by its center to the specified position
         * @param comp The [ComponentView] to move
         * @param pos The position where the component is moved to
         */
        fun moveByCenter(comp : ComponentView, pos : Coordinate){
            moveByOtherOrigin(comp, getCenter(comp), pos)
        }

        /**
         * Position the given [ComponentView]s in a circle. The circle is created clockwise (the first component is
         * at 12 o'clock) and components are placed symmetrical in this circle (consecutive components have always
         * the same angle between them).
         * @param center The center of the circle
         * @param radius Radius of the circle
         * @param moveByCenter Should the components be moved by their center
         * @param comps The components to make the layout
         * @throws IllegalArgumentException When no components are given
         */
        fun circleLayout(center : Coordinate,
                         radius : Double,
                         moveByCenter : Boolean = false,
                         vararg comps: ComponentView){

            require(comps.isNotEmpty())
            val applyRot : Double = (360.0 / comps.size)
            var rot = -90.0
            for(component in comps){
                val pos = Coordinate(
                    center.xCoord + (radius * cos(Math.toRadians(rot))),
                    center.yCoord + (radius * sin(Math.toRadians(rot)))
                )
                if(moveByCenter){
                    moveByCenter(component, pos)
                }
                else{
                    component.reposition(pos.xCoord, pos.yCoord)
                }
                component.rotate(rot + 90)
                rot += applyRot
            }
        }

        /**
         * Get the translation vector between a component and a destination point on a rotated coordinate system.
         * @param comp The component that should be moved
         * @param dest The destination point (as [Coordinate]) where the component should be moved
         * @param axisRot The rotation of the coordinate system in degrees
         * @return The translation vector as [Coordinate]
         */
        fun getTransformedTranslationVector(comp : ComponentView, dest : Coordinate, axisRot : Double) : Coordinate{
            val radRot = Math.toRadians(axisRot)
            val transl = dest - Coordinate(comp.posX, comp.posY)

            val transfPosX = transl.xCoord * cos(radRot) + transl.yCoord * sin(radRot)
            val transfPosY = (-1.0 * transl.xCoord * sin(radRot)) + transl.yCoord * cos(radRot)

            return Coordinate(transfPosX, transfPosY)
        }
    }
}