package org.openhab.binding.rsb.internal.transform;

/*
 * #%L
 * openHAB RSB Binding
 * %%
 * Copyright (C) 2014 - 2018 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.TypeNotSupportedException;
import org.openhab.core.library.types.StopMoveType;
import rst.domotic.binding.openhab.StopMoveHolderType;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class StopMoveTypeTransformer {

    public static rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder transform(StopMoveType stopMoveType) throws CouldNotTransformException {
        switch (stopMoveType) {
            case STOP:
                return rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder.newBuilder().setState(StopMoveHolderType.StopMoveHolder.StopMove.STOP).build();
            case MOVE:
                return rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder.newBuilder().setState(StopMoveHolderType.StopMoveHolder.StopMove.MOVE).build();
            default:
                throw new CouldNotTransformException("Could not transform " + StopMoveType.class.getName() + "! " +StopMoveType.class.getSimpleName() + "[" + stopMoveType.name() + "] is unknown!");
        }
    }

    public static StopMoveType transform(rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder.StopMove stopMove) throws TypeNotSupportedException, CouldNotTransformException {
        switch (stopMove) {
            case STOP:
                return StopMoveType.STOP;
            case MOVE:
                return StopMoveType.MOVE;
            default:
                throw new CouldNotTransformException("Could not transform " + rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder.StopMove.class.getName() + "! " + rst.domotic.binding.openhab.StopMoveHolderType.StopMoveHolder.StopMove.class.getSimpleName() + "[" + stopMove.name() + "] is unknown!");
        }
    }
}
