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
import org.openhab.core.library.types.UpDownType;
import rst.homeautomation.openhab.UpDownHolderType;

/**
 *
 * @author thuxohl
 */
public class UpDownTypeTransformer {

    public static rst.homeautomation.openhab.UpDownHolderType.UpDownHolder transform(UpDownType upDownType) throws CouldNotTransformException {
        switch (upDownType) {
            case DOWN:
                return rst.homeautomation.openhab.UpDownHolderType.UpDownHolder.newBuilder().setState(UpDownHolderType.UpDownHolder.UpDown.DOWN).build();
            case UP:
                return rst.homeautomation.openhab.UpDownHolderType.UpDownHolder.newBuilder().setState(UpDownHolderType.UpDownHolder.UpDown.UP).build();
            default:
                throw new CouldNotTransformException("Could not transform " + UpDownType.class.getName() + "! " + UpDownType.class.getSimpleName() + "[" + upDownType.name() + "] is unknown!");
        }
    }

    public static UpDownType transform(rst.homeautomation.openhab.UpDownHolderType.UpDownHolder.UpDown upDown) throws TypeNotSupportedException, CouldNotTransformException {
        switch (upDown) {
            case DOWN:
                return UpDownType.DOWN;
            case UP:
                return UpDownType.UP;
            default:
                throw new CouldNotTransformException("Could not transform " + rst.homeautomation.openhab.UpDownHolderType.UpDownHolder.UpDown.class.getName() + "! " + rst.homeautomation.openhab.UpDownHolderType.UpDownHolder.UpDown.class.getSimpleName() + "[" + upDown.name() + "] is unknown!");
        }
    }
}
