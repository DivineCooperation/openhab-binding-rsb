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
import org.openhab.core.library.types.OnOffType;
import rst.homeautomation.openhab.OnOffHolderType;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class OnOffTypeTransformer {

    public static rst.homeautomation.openhab.OnOffHolderType.OnOffHolder transform(OnOffType onOffType) throws CouldNotTransformException {
        switch (onOffType) {
            case OFF:
                return rst.homeautomation.openhab.OnOffHolderType.OnOffHolder.newBuilder().setState(OnOffHolderType.OnOffHolder.OnOff.OFF).build();
            case ON:
                return rst.homeautomation.openhab.OnOffHolderType.OnOffHolder.newBuilder().setState(OnOffHolderType.OnOffHolder.OnOff.ON).build();
            default:
                throw new CouldNotTransformException("Could not transform " + OnOffType.class.getName() + "! " + OnOffType.class.getSimpleName() + "[" + onOffType.name() + "] is unknown!");
        }
    }

    public static OnOffType transform(rst.homeautomation.openhab.OnOffHolderType.OnOffHolder.OnOff onOff) throws TypeNotSupportedException, CouldNotTransformException {
        switch (onOff) {
            case OFF:
                return OnOffType.OFF;
            case ON:
                return OnOffType.ON;
            default:
                throw new CouldNotTransformException("Could not transform " + rst.homeautomation.openhab.OnOffHolderType.OnOffHolder.OnOff.class.getName() + "! " + rst.homeautomation.openhab.OnOffHolderType.OnOffHolder.OnOff.class.getSimpleName() + "[" + onOff.name() + "] is unknown!");
        }
    }
}
