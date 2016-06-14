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
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.PercentType;

/**
 *
 * @author thuxohl
 */
public class HSVTypeTransformer {

	public static rst.homeautomation.openhab.HSBType.HSB transform(final HSBType hsbType) throws CouldNotTransformException {
		return rst.homeautomation.openhab.HSBType.HSB.newBuilder().setHue(hsbType.getHue().doubleValue()).setBrightness(hsbType.getBrightness().doubleValue()).setSaturation(hsbType.getSaturation().doubleValue()).build();
	}

	public static HSBType transform(rst.homeautomation.openhab.HSBType.HSB hsb) throws CouldNotTransformException {
		return new HSBType(new DecimalType(hsb.getHue()), new PercentType((int) hsb.getSaturation()), new PercentType((int) hsb.getBrightness()));
	}
}
