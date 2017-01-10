package org.openhab.binding.rsb.internal;

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
import java.util.HashMap;
import java.util.Map;
import org.openhab.binding.rsb.RSBBindingProvider;
import org.openhab.core.binding.BindingConfig;
import org.openhab.core.items.Item;
import org.openhab.model.item.binding.AbstractGenericBindingProvider;
import org.openhab.model.item.binding.BindingConfigParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is responsible for parsing the binding configuration.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RSBGenericBindingProvider extends AbstractGenericBindingProvider implements RSBBindingProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(RSBGenericBindingProvider.class);
    private final Map<String, String> itemBindingConfigMap = new HashMap<>();

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBindingType() {
        return "rsb";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void validateItemType(Item item, String bindingConfig) throws BindingConfigParseException {
        //if (!(item instanceof SwitchItem || item instanceof DimmerItem)) {
        //	throw new BindingConfigParseException("item '" + item.getName()
        //			+ "' is of type '" + item.getClass().getSimpleName()
        //			+ "', only Switch- and DimmerItems are allowed - please check your *.items configuration");
        //}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processBindingConfiguration(String context, Item item, String bindingConfig) throws BindingConfigParseException {
        super.processBindingConfiguration(context, item, bindingConfig);
        rsbBindingConfig config = new rsbBindingConfig();

        //parse bindingconfig here ...
        addBindingConfig(item, config);
        
        logger.info("ProcessBindingConfig for item [" + item.getName() + "] with bindingConfig [" + bindingConfig + "] and context [" + context + "]");
        // are empty binding configs automatically skipped?
        itemBindingConfigMap.put(item.getName(), bindingConfig);
    }

    /**
     * listen to all item changes
     */
    @Override
    public boolean providesBindingFor(String itemName) {
//        logger.info("Calling providesBindingFor [" + itemName + "]");
//        return itemBindingConfigMap.containsKey(itemName);
        return true;
    }
    
    class rsbBindingConfig implements BindingConfig {
        // put member fields here which holds the parsed values
    }
    
    @Override
    public Map<String, String> getItemBindingConfigMap() {
        return itemBindingConfigMap;
    }
}
