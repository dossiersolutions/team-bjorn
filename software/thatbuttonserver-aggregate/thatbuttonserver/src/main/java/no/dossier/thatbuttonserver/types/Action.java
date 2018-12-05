package no.dossier.thatbuttonserver.types;

import no.dossier.thatbuttonserver.util.List;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Action {

    public static Action shellAction(String commandTemplate) {
        return new Shell(commandTemplate);
    }

    public static Action forwardMessageAction(String hostName, int port) {
        return new ForwardMessage(hostName, port);
    }

    private static final Action LOG_EVENT = new LogEvent();

    public static Action logEventAction() {
        return LOG_EVENT;
    }

    public static Action triggersAction(List<Trigger> triggers) {
        return new Triggers(triggers);
    }

    private Action() {
    }

    @Override
    public final String toString() {
        return unwrap(
                commandTemplate -> String.format("Shell(%s)", commandTemplate),
                (hostName, port) -> String.format("ForwardMessage(%s, %d)", hostName, port),
                () -> "LogEvent",
                triggers -> String.format("Triggers(%s)", triggers));
    }

    @Override
    public abstract int hashCode();

    @Override
    public abstract boolean equals(Object obj);

    public abstract <A> A unwrap(
            Function<String, A> onShell,
            BiFunction<String, Integer, A> onForwardMessage,
            Supplier<A> onLogEvent,
            Function<List<Trigger>, A> onTriggers);

    public abstract void branch(
            Consumer<String> onShell,
            BiConsumer<String, Integer> onForwardMessage,
            Runnable onLogEvent,
            Consumer<List<Trigger>> onTriggers);


    private static final class Shell extends Action {

        private final String commandTemplate;

        private Shell(String commandTemplate) {
            this.commandTemplate = commandTemplate;
        }

        @Override
        public int hashCode() {
            return commandTemplate.hashCode() + 1795305121;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Shell) {
                Shell that = (Shell) obj;
                result = commandTemplate.equals(that.commandTemplate);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public <A> A unwrap(
                Function<String, A> onShell,
                BiFunction<String, Integer, A> onForwardMessage,
                Supplier<A> onLogEvent,
                Function<List<Trigger>, A> onTriggers) {

            return onShell.apply(commandTemplate);
        }

        @Override
        public void branch(
                Consumer<String> onShell,
                BiConsumer<String, Integer> onForwardMessage,
                Runnable onLogEvent,
                Consumer<List<Trigger>> onTriggers) {

            onShell.accept(commandTemplate);
        }

    }


    private static final class ForwardMessage extends Action {

        private final String hostName;
        private final int port;

        private ForwardMessage(String hostName, int port) {
            this.hostName = hostName;
            this.port = port;
        }

        @Override
        public int hashCode() {
            return (552930233 * hostName.hashCode()) + (1599990983 * port);
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof ForwardMessage) {
                ForwardMessage that = (ForwardMessage) obj;
                result = hostName.equals(that.hostName) &&
                        (port == that.port);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public <A> A unwrap(
                Function<String, A> onShell,
                BiFunction<String, Integer, A> onForwardMessage,
                Supplier<A> onLogEvent,
                Function<List<Trigger>, A> onTriggers) {

            return onForwardMessage.apply(hostName, port);
        }

        @Override
        public void branch(
                Consumer<String> onShell,
                BiConsumer<String, Integer> onForwardMessage,
                Runnable onLogEvent,
                Consumer<List<Trigger>> onTriggers) {

            onForwardMessage.accept(hostName, port);
        }

    }


    private static final class LogEvent extends Action {

        @Override
        public int hashCode() {
            return 952166297;
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof LogEvent;
        }

        @Override
        public <A> A unwrap(
                Function<String, A> onShell,
                BiFunction<String, Integer, A> onForwardMessage,
                Supplier<A> onLogEvent,
                Function<List<Trigger>, A> onTriggers) {

            return onLogEvent.get();
        }

        @Override
        public void branch(
                Consumer<String> onShell,
                BiConsumer<String, Integer> onForwardMessage,
                Runnable onLogEvent,
                Consumer<List<Trigger>> onTriggers) {

            onLogEvent.run();
        }

    }


    private static final class Triggers extends Action {

        private final List<Trigger> triggers;

        private Triggers(List<Trigger> triggers) {
            this.triggers = triggers;
        }

        @Override
        public int hashCode() {
            return triggers.hashCode() + 276508073;
        }

        @Override
        public boolean equals(Object obj) {
            boolean result;
            if (this == obj) {
                result = true;
            } else if (obj instanceof Triggers) {
                Triggers that = (Triggers) obj;
                result = triggers.equals(that.triggers);
            } else {
                result = false;
            }
            return result;
        }

        @Override
        public <A> A unwrap(
                Function<String, A> onShell,
                BiFunction<String, Integer, A> onForwardMessage,
                Supplier<A> onLogEvent,
                Function<List<Trigger>, A> onTriggers) {

            return onTriggers.apply(triggers);
        }

        @Override
        public void branch(
                Consumer<String> onShell,
                BiConsumer<String, Integer> onForwardMessage,
                Runnable onLogEvent,
                Consumer<List<Trigger>> onTriggers) {

            onTriggers.accept(triggers);
        }

    }

}
