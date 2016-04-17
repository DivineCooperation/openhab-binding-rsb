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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.CouldNotTransformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.extension.protobuf.ClosableDataBuilder;
import org.dc.jul.extension.rsb.com.RSBCommunicationService;
import org.dc.jul.extension.rsb.iface.RSBInformerInterface;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.openhab.binding.rsb.RSBBindingProvider;
import org.openhab.binding.rsb.internal.transform.HSVTypeTransformer;
import org.openhab.binding.rsb.internal.transform.IncreaseDecreaseTypeTransformer;
import org.openhab.binding.rsb.internal.transform.OnOffTypeTransformer;
import org.openhab.binding.rsb.internal.transform.OpenClosedTypeTransformer;
import org.openhab.binding.rsb.internal.transform.PercentTypeTransformer;
import org.openhab.binding.rsb.internal.transform.StopMoveTypeTransformer;
import org.openhab.binding.rsb.internal.transform.UpDownTypeTransformer;
import org.openhab.core.binding.AbstractBinding;
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
import rst.homeautomation.openhab.RSBBindingType;
import rst.homeautomation.state.ActiveDeactiveType;

/**
 *
 * @author Divine Threepwood
 * @since 0.0.1
 */
public class RSBBinding extends AbstractBinding<RSBBindingProvider> implements ManagedService {

//    public static final String RPC_METHODE_INTERNAL_RECEIVE_UPDATE = "internalReceiveUpdate";
//    public static final String RPC_METHODE_INTERNAL_RECEIVE_COMMAND = "internalReceiveCommand";
//    public static final String RPC_METHODE_INTERNAL_RECEIVE_UPDATE = "internalReceiveUpdate";
//    public static final String RPC_METHODE_INTERNAL_RECEIVE_COMMAND = "internalReceiveCommand";
    public static final String RPC_METHODE_SEND_COMMAND = "sendCommand";
    public static final String RPC_METHODE_POST_COMMAND = "postCommand";
    public static final String RPC_METHODE_POST_UPDATE = "postUpdate";

    public static final Scope SCOPE_OPENHAB_IN = new Scope("/openhab/in");
    public static final Scope SCOPE_OPENHAB_OUT_UPDATE = new Scope("/openhab/out/update");
    public static final Scope SCOPE_OPENHAB_OUT_COMMAND = new Scope("/openhab/out/command");

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

//    private final RSBRemoteService<DALBindingType.DALBinding> openhabItemUpdateInformer;
    private RSBInformerInterface<OpenhabCommand> openhabInformer;
    private final RSBCommunicationService<RSBBindingType.RSBBinding, RSBBindingType.RSBBinding.Builder> openhabController;

    /**
     * the refresh interval (optional, defaults to 60000ms)
     */
//    private long refreshInterval = 60000;
    public RSBBinding() throws InstantiationException {
        logger.info("Create " + getClass().getSimpleName() + "...");
        instance = this;

        try {

//            openhabItemUpdateInformer = new RSBRemoteService<DALBindingType.DALBinding>() {
//
//                @Override
//                public void notifyUpdated(DALBindingType.DALBinding data) {
//                    RSBBinding.this.notifyUpdated(data);
//                }
//            };
            openhabController = new RSBCommunicationService<RSBBindingType.RSBBinding, RSBBindingType.RSBBinding.Builder>(RSBBindingType.RSBBinding.newBuilder()) {

                @Override
                public void registerMethods(RSBLocalServerInterface server) throws CouldNotPerformException {
                    server.addMethod(RPC_METHODE_SEND_COMMAND, new sendCommandCallback());
                    server.addMethod(RPC_METHODE_POST_COMMAND, new postCommandCallback());
                    server.addMethod(RPC_METHODE_POST_UPDATE, new postUpdateCallback());
                }
            };

        } catch (CouldNotPerformException ex) {
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    public void init() throws InitializationException, InterruptedException {
        try {
            openhabController.init(SCOPE_OPENHAB_IN);
//            openhabInformer = RSBFactory.getInstance().createSynchronizedInformer(SCOPE_OPENHAB_OUT, OpenhabCommand.class);
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

//    public static RSBBinding getInstance() {
//        if (instance == null) {
//            throw new NullPointerException("RSBBinding not initialized!");
//        }
//        return instance;
//    }
//    public final void notifyUpdated(DALBindingType.DALBinding data) {
//        switch (data.getState().getState()) {
//        case ACTIVE:
//            logger.debug("Received state. RSB is active!");
//            break;
//        case DEACTIVE:
//            logger.debug("Received state. RSB is deactive!");
//            break;
//        case UNKNOWN:
//            logger.debug("Received state. RSB is unkown!");
//            break;
//        }
//    }
    @Override
    public void activate() {
        try {
            init();
            logger.info("Activate " + getClass().getSimpleName() + "...");
            super.activate();
//            setProperlyConfigured(true);

            openhabController.activate();
            openhabInformer.activate();

            try (ClosableDataBuilder<RSBBindingType.RSBBinding.Builder> dataBuilder = openhabController.getDataBuilder(this)) {
                dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.ACTIVE).build());
            } catch (Exception ex) {
                logger.warn("Unable to pusb activation!", ex);
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
            openhabController.deactivate();
        } catch (InterruptedException | CouldNotPerformException ex) {
            logger.warn("Unable to push deactivation!", ex);
        }

        try {
            openhabInformer.deactivate();
        } catch (InterruptedException | CouldNotPerformException ex) {
            logger.warn("Unable to deacticate the communication service in [" + getClass().getSimpleName() + "]", ex);
        }

        try (ClosableDataBuilder<RSBBindingType.RSBBinding.Builder> dataBuilder = openhabController.getDataBuilder(this)) {
            dataBuilder.getInternalBuilder().setState(ActiveDeactiveType.ActiveDeactive.newBuilder().setState(ActiveDeactiveType.ActiveDeactive.ActiveDeactiveState.DEACTIVE).build());
        } catch (Exception ex) {
            logger.warn("Unable to push deactivation!", ex);
        }
    }

    public class sendCommandCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            OpenhabCommand command = (OpenhabCommand) request.getData();
            try {
                logger.info("Send on bus Item[" + command.getItem() + "] Command[" + command.toString() + "].");
                validateEventPublichser();
                eventPublisher.sendCommand(command.getItem(), extractCommand(command));
                return new Event(Void.class);
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not send Command[" + command + "] for Item[" + command.getItem() + "]", ex);
            }
        }
    }

    public class postCommandCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            OpenhabCommand command = (OpenhabCommand) request.getData();
            try {
                logger.info("Post on bux Item[" + command.getItem() + "] Command[" + command.toString() + "].");
                validateEventPublichser();
                eventPublisher.postCommand(command.getItem(), extractCommand(command));
                return new Event(Void.class);
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not post Command[" + command + "] for Item[" + command.getItem() + "]", ex);
            }
        }
    }

    public class postUpdateCallback extends EventCallback {

        @Override
        public Event invoke(final Event request) throws Throwable {
            OpenhabCommand command = (OpenhabCommand) request.getData();
            try {
                logger.info("Post update on bus: Item[" + command.getItem() + "] Update[" + command.toString() + "].");
                validateEventPublichser();
                eventPublisher.postUpdate(command.getItem(), (State) extractCommand(command));
                return new Event(Void.class);
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not post Update[" + command + "] for Item[" + command.getItem() + "]", ex);
            }
        }
    }

    private void validateEventPublichser() throws InvalidStateException {
        if (eventPublisher == null) {
            throw new InvalidStateException("EventPublisher not registed!");
        }
    }

    private Command extractCommand(final OpenhabCommand command) throws CouldNotPerformException {
        try {
            switch (command.getType()) {
            case DECIMAL:
                return new DecimalType(command.getDecimal());
            case HSB:
                return HSVTypeTransformer.transform(command.getHsb());
            case INCREASEDECREASE:
                return IncreaseDecreaseTypeTransformer.transform(command.getIncreaseDecrease().getState());
            case ONOFF:
                return OnOffTypeTransformer.transform(command.getOnOff().getState());
            case OPENCLOSED:
                return OpenClosedTypeTransformer.transform(command.getOpenClosed().getState());
            case PERCENT:
                return new PercentType((int) command.getPercent().getValue());
            case STOPMOVE:
                return StopMoveTypeTransformer.transform(command.getStopMove().getState());
            case STRING:
                return new StringType(command.getText());
            case UPDOWN:
                return UpDownTypeTransformer.transform(command.getUpDown().getState());
            default:
                throw new CouldNotPerformException("Unknown Openhab item [" + command.getItem() + "]");
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not extract command!", ex);
        }
    }

//    /**
//     * @{inheritDoc
//     */
//    @Override
//    protected long getRefreshInterval() {
//        return refreshInterval;
//    }
//
//    /**
//     * @{inheritDoc
//     */
//    @Override
//    protected String getName() {
//        return "rsb binding";
//    }
    /**
     * @{inheritDoc
     */
//    @Override
//    protected void execute() {
//        // TODO mpohling: Implement connection watchdog.
//    }
//
//        notifyUpdate(itemName, (State) command, RPC_METHODE_INTERNAL_RECEIVE_COMMAND);
//    }
    /**
     * @{inheritDoc
     */
    @Override
    public void internalReceiveUpdate(String itemName, State newState) {
        try {
            OpenhabCommand openhabCommand = buildOpenhabCommand(itemName, newState);
            logger.info("Publish Update[" + openhabCommand.getItem() + " = " + newState + "]");
            openhabController.send(new Event(SCOPE_OPENHAB_OUT_UPDATE, OpenhabCommand.class, openhabCommand));
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not notify Update["+itemName+" = "+newState+"]!", ex);
        }
    }

    public String getItemBindingConfig(final String itemName) {
        for (RSBBindingProvider provider : providers) {
            if (provider.getItemBindingConfigMap().containsKey(itemName)) {
                return provider.getItemBindingConfigMap().get(itemName);
            }
        }
        //TODO tamino: Are this binding configs in any case available? Than please throw exception instead returning empty string.
        return "";
    }

    public OpenhabCommand buildOpenhabCommand(final String itemName, final Command command) throws CouldNotPerformException {
        return buildOpenhabCommand(itemName, (State) command);
    }
    
    public OpenhabCommand buildOpenhabCommand(final String itemName, final State state) throws CouldNotPerformException {
        try {
            return getTypeBuilder(state).setItem(itemName).setItemBindingConfig(getItemBindingConfig(itemName)).build();
        } catch (CouldNotTransformException ex) {
            throw new CouldNotPerformException("Unable to build openhab command!", ex);
        }
    }

    /**
     * @{inheritDoc
     */
    @Override
    public void internalReceiveCommand(String itemName, Command command) {
        try {
            OpenhabCommand openhabCommand = buildOpenhabCommand(itemName, command);
            logger.info("Publish Command[" + openhabCommand.getItem() + " = " + command + "]");
            openhabController.send(new Event(SCOPE_OPENHAB_OUT_UPDATE, OpenhabCommand.class, openhabCommand));
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not notify Command["+itemName+" = "+command+"]!", ex);
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
//            String refreshIntervalString = (String) config.get("refresh");
//            if (StringUtils.isNotBlank(refreshIntervalString)) {
//                refreshInterval = Long.parseLong(refreshIntervalString);
//            }
//
//            // read further config parameters here ...
//            setProperlyConfigured(true);
        }
//        logger.info("Directory change!");
    }
}
