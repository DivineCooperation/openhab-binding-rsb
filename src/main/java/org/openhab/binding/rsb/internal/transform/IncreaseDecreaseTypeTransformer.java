package org.openhab.binding.rsb.internal.transform;

/*
 * #%L
 * openHAB RSB Binding
 * %%
 * Copyright (C) 2014 - 2016 openbase.org
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
import org.openhab.core.library.types.IncreaseDecreaseType;
import rst.homeautomation.openhab.IncreaseDecreaseHolderType;

/**
 *
 * @author thuxohl
 */
public class IncreaseDecreaseTypeTransformer {

    public static rst.homeautomation.openhab.IncreaseDecreaseHolderType.IncreaseDecreaseHolder transform(IncreaseDecreaseType increaseDecreaseType) throws CouldNotTransformException {
        switch (increaseDecreaseType) {
            case INCREASE:
                return rst.homeautomation.openhab.IncreaseDecreaseHolderType.IncreaseDecreaseHolder.newBuilder().setState(IncreaseDecreaseHolderType.IncreaseDecreaseHolder.IncreaseDecrease.INCREASE).build();
            case DECREASE:
                return rst.homeautomation.openhab.IncreaseDecreaseHolderType.IncreaseDecreaseHolder.newBuilder().setState(IncreaseDecreaseHolderType.IncreaseDecreaseHolder.IncreaseDecrease.DECREASE).build();
            default:
                throw new CouldNotTransformException("Could not transform " + IncreaseDecreaseType.class.getName() + "! " + IncreaseDecreaseType.class.getSimpleName() + "[" + increaseDecreaseType.name() + "] is unknown!");
        }
    }

    public static IncreaseDecreaseType transform(rst.homeautomation.openhab.IncreaseDecreaseHolderType.IncreaseDecreaseHolder.IncreaseDecrease increaseDecrease) throws TypeNotSupportedException, CouldNotTransformException {
        switch (increaseDecrease) {
            case INCREASE:
                return IncreaseDecreaseType.INCREASE;
            case DECREASE:
                return IncreaseDecreaseType.DECREASE;
            default:
                throw new CouldNotTransformException("Could not transform " + rst.homeautomation.openhab.IncreaseDecreaseHolderType.IncreaseDecreaseHolder.IncreaseDecrease.class.getName() + "! " + rst.homeautomation.openhab.IncreaseDecreaseHolderType.IncreaseDecreaseHolder.IncreaseDecrease.class.getSimpleName() + "[" + increaseDecrease.name() + "] is unknown!");
        }
    }
}
