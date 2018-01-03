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
import org.openhab.core.library.types.PercentType;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class PercentTypeTransformer {

    public static rst.domotic.binding.openhab.PercentType.Percent transform(final PercentType percentType) throws CouldNotTransformException {
        return rst.domotic.binding.openhab.PercentType.Percent.newBuilder().setValue((percentType.doubleValue())).build();
    }

    public static PercentType transform(rst.domotic.binding.openhab.PercentType.Percent percentType) throws CouldNotTransformException {
        return new PercentType((int) percentType.getValue());
    }
}
