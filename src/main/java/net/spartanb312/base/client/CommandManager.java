package net.spartanb312.base.client;

import me.afterdarkness.moloch.command.commands.Enemy;
import me.afterdarkness.moloch.command.commands.Reset;
import me.afterdarkness.moloch.module.modules.client.GlobalManagers;
import net.spartanb312.base.BaseCenter;
import net.spartanb312.base.command.Command;
import net.spartanb312.base.command.commands.*;
import net.spartanb312.base.core.event.Listener;
import net.spartanb312.base.core.event.Priority;
import net.spartanb312.base.event.events.client.ChatEvent;
import net.spartanb312.base.utils.ChatUtil;

import java.util.*;

import static net.spartanb312.base.core.concurrent.ConcurrentTaskManager.launch;

public class CommandManager {

    public List<Command> commands = new ArrayList<>();
    private final Set<Class<? extends Command>> classes = new HashSet<>();

    public static void init() {
        instance = new CommandManager();
        instance.commands.clear();

        register(Bind.class);
        register(Commands.class);
        register(Config.class);
        register(Friend.class);
        register(Help.class);
        register(Prefix.class);
        register(Send.class);
        register(Toggle.class);
        register(TP.class);

        register(Enemy.class);
        register(Reset.class);

        instance.loadCommands();
        BaseCenter.EVENT_BUS.register(instance);
    }

    private static void register(Class<? extends Command> clazz) {
        instance.classes.add(clazz);
    }

    private void loadCommands() {
        classes.stream().sorted(Comparator.comparing(Class::getSimpleName)).forEach(clazz -> {
            try {
                Command command = clazz.newInstance();
                commands.add(command);
            } catch (Exception e) {
                e.printStackTrace();
                System.err.println("Couldn't initiate Command " + clazz.getSimpleName() + "! Err: " + e.getClass().getSimpleName() + ", message: " + e.getMessage());
            }
        });
    }

    @Listener(priority = Priority.HIGHEST)
    public void onChat(ChatEvent event) {
        if (event.getMessage().startsWith(GlobalManagers.INSTANCE.clientPrefix.getValue())) {
            launch(() -> runCommands(event.getMessage()));
            event.cancel();
        }
    }

    public void runCommands(String s) {
        String readString = s.trim().substring(GlobalManagers.INSTANCE.clientPrefix.getValue().length()).trim();
        boolean commandResolved = false;
        boolean hasArgs = readString.trim().contains(" ");
        String commandName = hasArgs ? readString.split(" ")[0] : readString.trim();
        String[] args = hasArgs ? readString.substring(commandName.length()).trim().split(" ") : new String[0];

        for (Command command : commands) {
            if (command.getCommand().trim().equalsIgnoreCase(commandName.trim().toLowerCase())) {
                command.onCall(readString, args);
                commandResolved = true;
                break;
            }
        }
        if (!commandResolved) {
            ChatUtil.sendNoSpamErrorMessage("Unknown command. try 'help' for a list of commands.");
        }
    }

    private static CommandManager instance;

    public static CommandManager getInstance() {
        if (instance == null) instance = new CommandManager();
        return instance;
    }

}
