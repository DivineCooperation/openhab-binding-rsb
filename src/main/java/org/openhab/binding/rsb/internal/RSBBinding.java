package org.openhab.binding.rsb.internal;

/*
 * #%L
 * openHAB RSB Binding
 * %%
 * Copyright (C) 2014 - 2016 DivineCooperation
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
import java.util.Dictionary;
import java.util.concurrent.Future;
import org.apache.commons.lang.StringUtils;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.CouldNotTransformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.extension.protobuf.ClosableDataBuilder;
import org.dc.jul.extension.rsb.com.RSBCommunicationService;
import org.dc.jul.extension.rsb.com.RSBRemoteService;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.openhab.binding.rsb.RSBBindingProvider;
import org.openhab.binding.rsb.internal.transform.HSVTypeTransformer;
import org.openhab.binding.rsb.internal.transform.IncreaseDecreaseTypeTransformer;
import org.openhab.binding.rsb.internal.transform.OnOffTypeTransformer;
import org.openhab.binding.rsb.internal.transform.OpenClosedTypeTransformer;
import org.openhab.binding.rsb.internal.transform.PercentTypeTransformer;
import org.openhab.binding.rsb.internal.transform.StopMoveTypeTransformer;
import org.openhab.binding.rsb.internal.transform.UpDownTypeTransformer;
import org.openhab.core.binding.AbstractActiveBinding;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.HSBType;
import org.openhab.core.library.types.IncreaseDecreaseType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.OpenClosedType;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.Type;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.converter.DefaultConverterRepository;
import rsb.converter.ProtocolBufferConverter;
import rsb.patterns.EventCallback;
import rst.homeautomation.openhab.DALBindingType;
import rst.homeautomation.openhab.OpenhabCommandType;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.DECIMAL;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.HSB;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.INCREASEDECREASE;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.ONOFF;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.OPENCLOSED;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.PERCENT;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.STOPMOVE;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.STRING;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.CommandType.UPDOWN;
import rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.ExecutionType;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.ExecutionType.ASYNCHRONOUS;
import static rst.homeautomation.openhab.OpenhabCommandType.OpenhabCommand.ExecutionType.SYNCHRONOUS;
import rst.homeautomation.openhab.RSBBindingType;
import rst.homeautomation.state.ActiveDeactiveType;

/**
 *
 * @author Divine Threepwood
 * @since 0.0.1
 */
public class RSBBinding extends AbstractActiveBinding<RSBBindingProvider> implements ManagedService {

    public static final String RPC_METHODE_INTERNAL_RECEIVE_UPDATE = "internalReceiveUpdate";
    public static final String RPC_METHODE_INTERNAL_RECEIVE_COMMAND = "internalReceiveCommand";
    public static final String RPC_METHODE_EXECUTE_COMMAND = "executeCommand";

    public static final Scope SCOPE_OPENHAB_IN = new Scope("/openhab/in");
    public static final Scope SCOPE_OPENHAB_OUT = new Scope("/openhab/out");

    static {
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(
                new ProtocolBufferConverter<>(OpenhabCommandType.OpenhabCommand.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(
                new ProtocolBufferConverter<>(RSBBindingType.RSBBinding.getDefaultInstance()));
        DefaultConverterRepository.getDefaultConverterRepository().addConverter(
                new ProtocolBufferConverter<>(DALBindingType.DALBinding.getDefaultInstance()));
    }

    private static final Logger logger = LoggerFactory.getLogger(RSBBinding.class);

    private static RSBBinding instance;

    private final RSBRemoteService<DALBindingType.DALBinding> openhabItemUpdateInformer;
    private final RSBCommunicationService<RSBBindingType.RSBBinding, RSBBindingType.RSBBinding.Builder> openhabCommandExecutionController;

    /**
     * the refresh interval (optional, defaults to 60000ms)
     */
    private long refreshInterval = 60000;

    public RSBBinding() throws InstantiationException {
        logger.info("Create " + getClass().getSimpleName() + "...");
        instance = this;

        try {
            openhabItemUpdateInformer = new RSBRemoteService<DALBindingType.DALBinding>() {

                @Override
                public void notifyUpdated(DALBindingType.DALBinding data) {
                    RSBBinding.this.notifyUpdated(data);
                }
            };

            openhabCommandExecutionController = new RSBCommunicationService<RSBBindingType.RSBBinding, RSBBindingType.RSBBinding.Builder>(RSBBindingType.RSBBinding.newBuilder()) {

                @Override
                public void registerMethods(RSBLocalServerInterface server) throws CouldNotPerformException {
                    server.addMethod(RPC_METHODE_EXECUTE_COMMAND, new ExecuteCommandCallback());
                }
            };

        } catch (CouldNotPerformException ex) {
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    public void init() throws InitializationException, InterruptedException {
        openhabCommandExecutionController.init(SCOPE_OPENHAB_IN);
        openhabItemUpdateInformer.init(SCOPE_OPENHAB_OUT);
    }

    public static RSBBinding getInstance() {
        if (instance == null) {
            throw new NullPointerException("RSBBinding not initialized!");
        }
        return instance;
    }

    public final void notifyUpdated(DALBindingType.DALBinding data) {
        switch (data.getState().getState()) {
            case ACTIVE:
                logger.debug("Received state. Dal is active!");
                break;
            case DEACTIVE:
                logger.debug("Received state. Dal is deactive!");
                break;
            case UNKNOWN:
                logger.debug("Received state. Dal is unkown!");
                break;
        }
    }

    @Override
    public void activate() {
        try {
            init();
            logger.info("Activate " + getClass().getSimpleName() + "...");
            super.activate();
            setProperlyConfigured(true);

            openhabCommandExecutionController.activate();
            openhabItemUpdateInformer.activate();

            try (ClosableDataBuilder<RSBBindingType.RSBBinding.Builder> dataBuilder = openhabCommandExecutionController.getDataBuilder(this)) {
                dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.ACTIVE).build());
            } catch (Exception ex) {
                logger.warn("Unable to setup openhab service as deactive in [" + getClass().getSimpleName() + "]", ex);
            }
        } catch (Exception ex) {
            logger.error("Could not activate " + getClass().getSimpleName() + "!", ex);
        }
    }

    @Override
    public void deactivate() {
        logger.info("Deactivate " + getClass().getSimpleName() + "...");
        super.deactivate();

        try {
            openhabCommandExecutionController.deactivate();
        } catch (InterruptedException | CouldNotPerformException ex) {
            logger.warn("Unable to deacticate the communication service in [" + getClass().getSimpleName() + "]", ex);
        }

        try {
            openhabItemUpdateInformer.deactivate();
        } catch (InterruptedException | CouldNotPerformException ex) {
            logger.warn("Unable to deacticate the communication service in [" + getClass().getSimpleName() + "]", ex);
        }

        try (ClosableDataBuilder<RSBBindingType.RSBBinding.Builder> dataBuilder = openhabCommandExecutionController.getDataBuilder(this)) {
            dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.DEACTIVE).build());
        } catch (Exception ex) {
            logger.warn("Unable to setup openhab service as deactive in [" + getClass().getSimpleName() + "]", ex);
        }
    }

    public synchronized Future executeCommand(String itemName, Command command, ExecutionType type) throws CouldNotPerformException, InterruptedException {
        logger.info("Send Item[" + itemName + "] Command[" + command.toString() + "].");

        if (eventPublisher == null) {
            throw new CouldNotPerformException("Could not send Command[" + command + "] for Item[" + itemName + "]. EventPublisher not registed!");
        }

        switch (type) {
            case SYNCHRONOUS:
                eventPublisher.sendCommand(itemName, command);
                break;
            case ASYNCHRONOUS:
                eventPublisher.postCommand(itemName, command);
                break;
            default:
                throw new AssertionError("Could not handle unknown ExecutionType[" + type + "]!");
        }

        Thread.sleep(150); // hue binding delay hack

        return null; // TODO mpohling: implement Future handling.
    }

    public class ExecuteCommandCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            OpenhabCommand command = (OpenhabCommand) request.getData();

            switch (command.getType()) {
                case DECIMAL:
                    executeCommand(command.getItem(), new DecimalType(command.getDecimal()), command.getExecutionType());
                    break;
                case HSB:
                    executeCommand(command.getItem(), HSVTypeTransformer.transform(command.getHsb()), command.getExecutionType());
                    break;
                case INCREASEDECREASE:
                    executeCommand(command.getItem(), IncreaseDecreaseTypeTransformer.transform(command.getIncreaseDecrease().getState()), command.getExecutionType());
                    break;
                case ONOFF:
                    executeCommand(command.getItem(), OnOffTypeTransformer.transform(command.getOnOff().getState()), command.getExecutionType());
                    break;
                case OPENCLOSED:
                    executeCommand(command.getItem(), OpenClosedTypeTransformer.transform(command.getOpenClosed().getState()), command.getExecutionType());
                    break;
                case PERCENT:
                    executeCommand(command.getItem(), new PercentType((int) command.getPercent().getValue()), command.getExecutionType());
                    break;
                case STOPMOVE:
                    executeCommand(command.getItem(), StopMoveTypeTransformer.transform(command.getStopMove().getState()), command.getExecutionType());
                    break;
                case STRING:
                    executeCommand(command.getItem(), new StringType(command.getText()), command.getExecutionType());
                    break;
                case UPDOWN:
                    executeCommand(command.getItem(), UpDownTypeTransformer.transform(command.getUpDown().getState()), command.getExecutionType());
                    break;
                default:
                    throw new CouldNotPerformException("Unknown Openhab command. Could not execute command for item [" + command.getItem() + "]");
            }
            return new Event(Void.class);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected long getRefreshInterval() {
        return refreshInterval;
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected String getName() {
        return "rsb binding";
    }

    /**
     * @{inheritDoc
     */
    @Override
    protected void execute() {
        // TODO mpohling: Implement connection watchdog.
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void internalReceiveCommand(String itemName, Command command) {
        logger.info("Incomming Item[" + itemName + "] Command[" + command.toString() + "].");
        notifyUpdate(itemName, (State) command, RPC_METHODE_INTERNAL_RECEIVE_COMMAND);
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void internalReceiveUpdate(String itemName, State newState) {
        logger.info("Incomming Item[" + itemName + "] State[" + newState.toString() + "].");
        notifyUpdate(itemName, newState, RPC_METHODE_INTERNAL_RECEIVE_UPDATE);
    }

    private void notifyUpdate(String itemName, State newState, String methodName) {
        OpenhabCommand openhabCommand;
        String itemBindingConfig = "";
        for (RSBBindingProvider provider : providers) {
            if (provider.getItemBindingConfigMap().containsKey(itemName)) {
                logger.info("Found provider with item name");
                itemBindingConfig = provider.getItemBindingConfigMap().get(itemName);
                logger.info("Got binding config [" + itemBindingConfig + "] for item [" + itemName + "]");
            }
        }
        try {
            try {
                openhabCommand = getTypeBuilder(newState).setItem(itemName).setExecutionType(SYNCHRONOUS).setItemBindingConfig(itemBindingConfig).build();
            } catch (CouldNotTransformException ex) {
                throw new CouldNotPerformException("Unable to build openhab command!", ex);
            }
            //TODO mpohling: implement thread pool to publish bus events.
            logger.info("Calling remote method [" + methodName + "]");
            openhabItemUpdateInformer.callMethodAsync(methodName, openhabCommand);
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not notify data update!", ex);
        }
    }

    private OpenhabCommand.Builder getTypeBuilder(Type type) throws CouldNotTransformException {
        OpenhabCommand.Builder newBuilder = OpenhabCommand.newBuilder();
        if (type instanceof HSBType) {
            return newBuilder.setHsb(HSVTypeTransformer.transform((HSBType) type)).setType(HSB);
        } else if (type instanceof PercentType) {
            return newBuilder.setPercent(PercentTypeTransformer.transform((PercentType) type)).setType(PERCENT);
        } else if (type instanceof DecimalType) {
            return newBuilder.setDecimal(((DecimalType) type).doubleValue()).setType(DECIMAL);
        } else if (type instanceof StringType) {
            return newBuilder.setText((((StringType) type).toString())).setType(STRING);
        } else if (type instanceof IncreaseDecreaseType) {
            return newBuilder.setIncreaseDecrease(IncreaseDecreaseTypeTransformer.transform((IncreaseDecreaseType) (type))).setType(INCREASEDECREASE);
        } else if (type instanceof OnOffType) {
            return newBuilder.setOnOff(OnOffTypeTransformer.transform((OnOffType) type)).setType(ONOFF);
        } else if (type instanceof OpenClosedType) {
            return newBuilder.setOpenClosed(OpenClosedTypeTransformer.transform((OpenClosedType) type)).setType(OPENCLOSED);
        } else if (type instanceof StopMoveType) {
            return newBuilder.setStopMove(StopMoveTypeTransformer.transform((StopMoveType) type)).setType(STOPMOVE);
        } else if (type instanceof UpDownType) {
            return newBuilder.setUpDown(UpDownTypeTransformer.transform((UpDownType) type)).setType(UPDOWN);
        } else {
            throw new CouldNotTransformException("Unsupported openhab type [" + type.toString() + "]. Could not convert to OpenhabCommand!");
        }
    }

    @Override
    public void updated(Dictionary config) throws ConfigurationException {
        if (config != null) {

            // to override the default refresh interval one has to add a
            // parameter to openhab.cfg like
            // <bindingName>:refresh=<intervalInMs>
            String refreshIntervalString = (String) config.get("refresh");
            if (StringUtils.isNotBlank(refreshIntervalString)) {
                refreshInterval = Long.parseLong(refreshIntervalString);
            }

            // read further config parameters here ...
            setProperlyConfigured(true);
        }
//        logger.info("Directory change!");
    }
}
